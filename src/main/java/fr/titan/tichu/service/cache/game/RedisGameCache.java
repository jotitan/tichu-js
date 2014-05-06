package fr.titan.tichu.service.cache.game;

import fr.titan.tichu.service.cache.RedisConfiguration;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.service.ObjectHelper;
import redis.clients.jedis.JedisPool;

/**
 *
 */
public class RedisGameCache implements GameCache {

    private JedisPool jedisPool;

    public RedisGameCache(String host, int port) throws Exception {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(RedisConfiguration.NUMBER_CONNECTION);
        jedisPool = new JedisPool(config, host, port);

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
    public boolean saveGame(Game game) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "game:" + game.getGame();
            // If first insert, save name in global liste
            if (!jedis.exists(key)) {
                jedis.sadd("games", game.getGame());
            }
            return "OK".equals(jedis.set(key.getBytes(), ObjectHelper.serialize(game)));
        } finally {
            jedisPool.returnResource(jedis);
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
    public void removeGame(String name) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String key = "game:" + name;
            jedis.del(key);
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
}
