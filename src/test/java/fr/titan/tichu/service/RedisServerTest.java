package fr.titan.tichu.service;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.embedded.RedisServer;

import java.io.IOException;

/**
 * 
 */
public class RedisServerTest {
    private RedisServer redisServer;

    @Before
    public void startRedisServer() throws IOException {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @Test
    public void testConnection() {
        Jedis jedis = new Jedis("localhost", 6379);
        jedis.connect();
        Assert.assertTrue(jedis.isConnected());
        jedis.set("test", "value");

        Assert.assertEquals("value", jedis.get("test"));
        jedis.close();
    }

    @Test
    public void testPoolConnection() {
        JedisPool jedisPool = new JedisPool("localhost", 6379);
        Jedis jedis = jedisPool.getResource();
        jedis.connect();
        Assert.assertTrue(jedis.isConnected());
        jedis.set("test", "value");

        Assert.assertEquals("value", jedis.get("test"));
        jedis.close();
    }

    @Test
    public void testPubSub() {
        final JedisPubSub pubSub = new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                System.out.println("Message get " + message);
            }

            @Override
            public void onPMessage(String pattern, String channel, String message) {
            }

            @Override
            public void onSubscribe(String channel, int subscribedChannels) {
                System.out.println("Subscribe " + channel);
            }

            @Override
            public void onUnsubscribe(String channel, int subscribedChannels) {
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
            }

            @Override
            public void onPSubscribe(String pattern, int subscribedChannels) {
            }
        };

        final JedisPool pool = new JedisPool("localhost", 6379);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Suscribe");
                pool.getResource().subscribe(pubSub, "channel_test");
            }
        });

        thread.start();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        System.out.println("Publish");
        pool.getResource().publish("channel_test", "Test message");

    }

    @After
    public void stopRedisServer() throws Exception {
        redisServer.stop();
    }
}
