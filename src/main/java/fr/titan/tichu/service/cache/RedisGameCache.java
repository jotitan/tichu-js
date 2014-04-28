package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;
import fr.titan.tichu.service.ObjectHelper;
import org.codehaus.jackson.map.ObjectMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class RedisGameCache implements GameCache {

    private Jedis jedis;

    public RedisGameCache(String host, int port) throws Exception {
        jedis = new Jedis(host, port);
        try {
            jedis.connect();
        } catch (Exception e) {
            throw new Exception("Cannot connect to redis server");
        }
    }

    @Override
    public boolean addGame(Game game) {
        String key = "game:" + game.getGame();
        // If first insert, save token user
        return "OK".equals(jedis.set(key.getBytes(), ObjectHelper.serialize(game)));
    }

    @Override
    public Game getGame(String name) {
        if (name == null) {
            return null;
        }
        String key = "game:" + name;
        byte[] game = jedis.get(key.getBytes());
        if (game == null) {
            return null;
        }
        return ObjectHelper.deserialize(Game.class, game);
    }

    @Override
    public void removeGame(String name) {
        String key = "game:" + name;
        jedis.del(key);
    }

    public void close() {
        jedis.close();
    }

    @Override
    public void addPlayer(Player player, Game game) {
        String key = "player:" + player.getToken();
        jedis.set(key.getBytes(), ObjectHelper.serialize(player));

        String keyGame = "game:player:" + player.getToken();
        jedis.set(keyGame, game.getGame());
    }

    @Override
    public Player getPlayer(String token) {
        String key = "player:" + token;
        byte[] player = jedis.get(key.getBytes());
        if (player == null) {
            return null;
        }
        return ObjectHelper.deserialize(Player.class, player);
    }

    @Override
    public Game getGameByTokenPlayer(String token) {
        String keyGame = "game:player:" + token;
        String game = jedis.get(keyGame);
        return getGame(game);
    }


}
