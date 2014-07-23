package fr.tichu.service.cache;

import fr.titan.tichu.service.cache.game.GameCache;
import fr.titan.tichu.service.cache.message.MemoryMessageCache;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.RedisMessageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Manage Redis cache to save game information
 */
public class CacheFactory {
    private static GameCache gameCache;

    private static MessageCache messageCache;

    final static private Logger logger = LoggerFactory.getLogger(CacheFactory.class);

    private static String host;
    private static int port;
    private static String pass;

    static {
        Properties p = new Properties();
        try {
            p.load(CacheFactory.class.getResourceAsStream("/tichu.properties"));
            host = p.getProperty("redis.host");
            port = Integer.valueOf(p.getProperty("redis.port"));
            pass = p.getProperty("redis.pass");
        } catch (IOException ioex) {
            logger.error("Error when loading redis properties, default configuration");
        } catch (Exception e) {
        }
    }

    public static MessageCache getMessageCache(String host, Integer port) {
        if (messageCache != null) {
            return messageCache;
        }
        try {
            messageCache = new RedisMessageCache(host, port, pass);
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
