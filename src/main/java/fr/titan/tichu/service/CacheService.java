package fr.titan.tichu.service;

import fr.titan.tichu.model.Game;
import redis.clients.jedis.Jedis;

/**
 * Manage Redis cache to save game information
 */
public class CacheService {
    private Jedis jedis;
    final private String host = "";
    final private Integer port = 0;

    public CacheService(){
           jedis = new Jedis(host,port);
    }

    public Game getGame(int id){
        String key = "game:" + id;

        return null;
    }
}
