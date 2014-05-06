package fr.titan.tichu.service.cache;

import com.google.inject.ImplementedBy;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * Find the redis configuration. First in environnement, second in tichu.properties
 */
@Singleton
public class RedisConfiguration {

    final private Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    final public static Integer NUMBER_CONNECTION = 64;

    public Object[] getConfiguration() {
        Properties p = new Properties();
        try {
            p.load(RedisConfiguration.class.getResourceAsStream("/tichu.properties"));

        } catch (IOException ioex) {
            logger.error("Error when loading redis properties, default configuration");
        } catch (Exception e) {
            return null;
        }
        String host = p.getProperty("redis.host");
        int port = Integer.valueOf(p.getProperty("redis.port"));
        return new Object[] { host, port };
    }

}
