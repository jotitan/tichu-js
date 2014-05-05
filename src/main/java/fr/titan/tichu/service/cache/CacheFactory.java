package fr.titan.tichu.service.cache;

import java.io.IOException;
import java.util.Properties;

import fr.titan.tichu.service.cache.game.GameCache;
import fr.titan.tichu.service.cache.game.MemoryGameCache;
import fr.titan.tichu.service.cache.message.MemoryMessageCache;
import fr.titan.tichu.service.cache.game.RedisGameCache;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.RedisMessageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage Redis cache to save game information
 */
public class CacheFactory {
    private static GameCache gameCache;

    private static MessageCache messageCache;

    final static private Logger logger = LoggerFactory.getLogger(CacheFactory.class);

    private static String host;
    private static int port;

    static {
        Properties p = new Properties();
        try {
            p.load(CacheFactory.class.getResourceAsStream("/tichu.properties"));
            host = p.getProperty("redis.host");
            port = Integer.valueOf(p.getProperty("redis.port"));
        } catch (IOException ioex) {
            logger.error("Error when loading redis properties, default configuration");
        } catch (Exception e) {
        }
    }

    /**
     * Return the correct cache manager.
     * 
     * @return If redis up, return redis implementation, otherwise return memory implementation
     */
    public static GameCache getCache(String host, Integer port) {
        if (gameCache != null) {
            return gameCache;
        }
        try {
            gameCache = new RedisGameCache(host, port);
        } catch (Exception e) {
            logger.info("No Redis found, use memory cache instead (" + host + ":" + port + ")");
            gameCache = new MemoryGameCache();
        }
        return gameCache;
    }

    public static MessageCache getMessageCache(String host, Integer port) {
        if (messageCache != null) {
            return messageCache;
        }
        try {
            messageCache = new RedisMessageCache(host, port);
        } catch (Exception e) {
            logger.info("No Redis found, use memory cache instead (" + host + ":" + port + ")");
            messageCache = new MemoryMessageCache();
        }
        return messageCache;
    }

    protected static void resetCaches() {
        messageCache = null;
        gameCache = null;
    }
}
