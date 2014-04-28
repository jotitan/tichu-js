package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;

/**
 * Manage Redis cache to save game information
 */
public class CacheFactory {
    private static GameCache gameCache;

    private static MessageCache messageCache;

    /**
     * Return the correct cache manager.
     * 
     * @param host
     * @param port
     * @return If redis up, return redis implementation, otherwise return memory implementation
     */
    public static GameCache getCache(String host, int port) {
        if (gameCache != null) {
            return gameCache;
        }
        try {
            gameCache = new RedisGameCache(host, port);
        } catch (Exception e) {
            gameCache = new MemoryGameCache();
        }
        return gameCache;
    }

    public static MessageCache getMessageCache(String host, int port) {
        if (messageCache != null) {
            return messageCache;
        }
        try {
            messageCache = new RedisMessageCache(host, port);
        } catch (Exception e) {
            messageCache = new MemoryMessageCache();
        }
        return messageCache;
    }
}
