package fr.titan.tichu.service;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import redis.clients.jedis.Jedis;

/**
 * Manage Redis cache to save game information
 */
public class CacheService {
    private Jedis jedis;

    public CacheService(String host,int port){
       jedis = new Jedis(host,port);
       jedis.connect();
    }

    public void close(){
        jedis.close();
    }

    public Game getGame(String name){
        String key = "game:" + name;
        byte[] game = jedis.get(key.getBytes());
        if(game == null){
            return null;
        }
        return ObjectHelper.deserialize(Game.class,game);
    }

    public void removeGame(String name){
        String key = "game:" + name;
        jedis.del(key);
    }

    public boolean saveGame(Game game){
        String key = "game:" + game.getGame();
        // If first insert, save token user
        return "OK".equals(jedis.set(key.getBytes(), ObjectHelper.serialize(game)));
    }

    public void linkToken(Player player){
        String key = "player:token:" + player.getToken();
        jedis.set(key,player.getGame().getGame());
    }

    public Game getGameByToken(String token){
        String game = jedis.get("player:token:" + token);
        if(game == null){
            return null;
        }
        return getGame(game);
    }

    public boolean isConnected(){
        return jedis.isConnected();
    }

}
