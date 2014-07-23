package fr.tichu.service.cache;

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

    private String host;
    private Integer port;
    private String password;

    public RedisConfiguration() {
        Properties p = new Properties();
        try {
            p.load(RedisConfiguration.class.getResourceAsStream("/tichu.properties"));
        } catch (IOException ioex) {
            logger.error("Error when loading redis properties, default configuration");
        } catch (Exception e) {

        }
        host = p.getProperty("redis.host");
        port = Integer.valueOf(p.getProperty("redis.port"));
        password = p.getProperty("redis.pass");
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }
}
