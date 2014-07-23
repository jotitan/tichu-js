package fr.tichu.service.cache.message;

import fr.titan.tichu.TichuClientCommunication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
*
 */
public class MessagePublishThread extends Thread {
    private Logger logger = LoggerFactory.getLogger(MessagePublishThread.class);
    private String[] keys;
    private TichuClientCommunication clientCommunication;
    private JedisPool pool;
    private Jedis jedis;

    public MessagePublishThread(String[] keys, TichuClientCommunication clientCommunication, JedisPool pool) {
        this.keys = keys;
        this.clientCommunication = clientCommunication;
        this.pool = pool;
    }

    @Override
    public void run() {
        try {
            /* This jedis resource is never back to pool (always open) */
            jedis = pool.getResource();
            jedis.subscribe(createJedisSubscribe(clientCommunication), keys);

        } catch (JedisConnectionException jex) {
            // Connexion reset
            logger.info("Publish Jedis Connection End");
        } finally {
            logger.info("Free jedis resource");
            if (jedis.isConnected()) {
                pool.returnResource(jedis);
            }
        }
    }

    /**
     * Close the connection to redis
     */
    public void close() {
        if (this.jedis.isConnected()) {
            this.jedis.disconnect();
        }
        // this.jedis.quit();
    }

    private JedisPubSub createJedisSubscribe(final TichuClientCommunication clientCommunication) {
        return new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                logger.debug("Receive on " + channel + " : " + message);
                clientCommunication.send(message);
            }

            @Override
            public void onPMessage(String pattern, String channel, String message) {
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                logger.info("Suscribe to " + channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
                logger.info("Unsubscribe to " + channel);
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
            }

            @Override
            public void onPSubscribe(String pattern, int subscribedChannels) {
            }
        };
    }
}
