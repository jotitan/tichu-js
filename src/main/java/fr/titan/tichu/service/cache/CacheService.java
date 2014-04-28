package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;

/**
 * Manage Redis cache to save game information
 */
public class CacheService {

    /**
     * Return the correct cache manager.
     * @param host
     * @param port
     * @return If redis up, return redis implementation, otherwise return memory implementation
     */
    public static GameCache getCache(String host, int port){
        GameCache gameCache = null;
        try{
            gameCache = new RedisGameCache(host,port);
        }catch(Exception e){
            gameCache = new MemoryGameCache();
        }
        return gameCache;
    }
}
