package tichu.service.cache.game;

import com.google.common.collect.Maps;
import fr.titan.tichu.service.cache.RedisConfiguration;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.service.ObjectHelper;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public class RedisGameCache implements GameCache {

    private JedisPool jedisPool;

    public RedisGameCache(String host, int port, String password) throws Exception {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(RedisConfiguration.NUMBER_CONNECTION);
        if(password!=null && !"".equals(password.trim())){
            jedisPool = new JedisPool(config, host, port,1000,password);
        }else{
            jedisPool = new JedisPool(config, host, port);
        }

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.connect();
        } catch (Exception e) {
            throw new Exception("Cannot connect to redis server");
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public boolean createGame(Game game) throws Exception {
        return saveGame(game, true);
    }

    private boolean saveGame(Game game, boolean failIfExist) throws Exception {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "game:" + game.getGame();
            // If first insert, save name in global liste
            if (!jedis.exists(key)) {
                jedis.sadd("games", game.getGame());
                if (game.isPublicGame()) {
                    jedis.sadd("games:bynb:0", game.getGame());
                }
            } else {
                if (failIfExist) {
                    throw new Exception("Game with name " + game.getGame() + " already exist");
                }
            }
            return "OK".equals(jedis.set(key.getBytes(), ObjectHelper.serialize(game)));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public boolean saveGame(Game game) {
        try {
            return saveGame(game, false);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Game getGame(String name) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            if (name == null) {
                return null;
            }
            String key = "game:" + name;
            byte[] game = jedis.get(key.getBytes());
            if (game == null) {
                return null;
            }
            return ObjectHelper.deserialize(Game.class, game);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public boolean removeGame(String name) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "game:" + name;
            long del = jedis.del(key);
            jedis.srem("games", name);
            jedis.srem("games:bynb:0", name);
            jedis.srem("games:bynb:1", name);
            jedis.srem("games:bynb:2", name);
            jedis.srem("games:bynb:3", name);
            return del > 0;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    public void close() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.close();
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void addPlayer(Player player, Game game) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String keyGame = "game:player:" + player.getToken();
            jedis.set(keyGame, game.getGame());

            if (game.isPublicGame()) {
                int nbConnected = game.getConnectedPlayers();
                switch (nbConnected) {
                case 3:
                    // Remove key
                    jedis.srem("games:bynb:3", game.getGame());
                    break;
                default:
                    long ret = jedis.smove("games:bynb:" + (nbConnected), "games:bynb:" + (nbConnected + 1), game.getGame());
                    if (ret == 0) {
                        // Error, no element move
                    }
                }
            }
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Map<Integer, Set<String>> getFreeChairGames() {
        Jedis jedis = null;
        try {
            Map<Integer, Set<String>> games = Maps.newHashMap();
            jedis = jedisPool.getResource();
            games.put(0, jedis.smembers("games:bynb:0"));
            games.put(1, jedis.smembers("games:bynb:1"));
            games.put(2, jedis.smembers("games:bynb:2"));
            games.put(3, jedis.smembers("games:bynb:3"));
            return games;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Player getPlayer(String token) {
        Game game = getGameByTokenPlayer(token);
        if (game != null) {
            return game.getPlayerByToken(token);
        }
        return null;
    }

    @Override
    public Game getGameByTokenPlayer(String token) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String keyGame = "game:player:" + token;
            String game = jedis.get(keyGame);
            return getGame(game);
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    /* Save the time of last HB */
    @Override
    public void heartbeat(String token) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "player:heartbeat:" + token;
            jedis.set(key, String.valueOf(System.currentTimeMillis()));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Long lastHeartbeat(Player player) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "player:heartbeat:" + player.getToken();
            String heartbeat = jedis.get(key);
            return heartbeat != null ? Long.valueOf(heartbeat) : null;
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public Set<String> getGames() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.smembers("games");
        } finally {
            jedisPool.returnResource(jedis);
        }
    }
}
