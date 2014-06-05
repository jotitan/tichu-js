package fr.titan.tichu.service;

import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.embedded.RedisServer;

import java.io.IOException;
import java.util.List;

/**
 * 
 */
public class RedisServerTest {
    private RedisServer redisServer;

    @Before
    public void startRedisServer() throws IOException {
        //redisServer = new RedisServer(6379);
        //redisServer.start();
    }

    @Test
    public void testConnect(){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(12);
          JedisPool pool = new JedisPool(config,"pub-redis-13488.eu-west-1-1.1.ec2.garantiadata.com",13488,1000,"tichu-titan",0);
          Jedis jedis = pool.getResource();
        System.out.println(jedis.exists("toto"));

        jedis.set("toto","bieng");
        System.out.println(jedis.get("toto"));
        pool.returnResource(jedis);
    }

    @Test
    public void testPool() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(12);
        JedisPool pool = new JedisPool(config, "localhost", 6379);
        List<Jedis> jedisList = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            Jedis jedis = pool.getResource();
            jedisList.add(jedis);
            System.out.println(i + " " + jedis.keys("*"));
        }

        for (Jedis jedis : jedisList) {
            pool.returnResource(jedis);
        }
    }

    @Test
    public void testPublish() {
        JedisPool pool = new JedisPool("localhost", 6379);

        Jedis jedis = createThread(pool);

        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }

        pool.getResource().publish("channel-test", "Channel test");

        // jedis.disconnect();
        jedis.quit();
        try {
            Thread.sleep(200);
        } catch (Exception e) {
        }

    }

    private Jedis createThread(final JedisPool pool) {
        final Jedis jedis = pool.getResource();
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    jedis.subscribe(new JedisPubSub() {
                        @Override
                        public void onMessage(String channel, String message) {
                            System.out.println("MEssage : " + channel + " : " + message);
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
                            System.out.println("UNsubscribe");
                        }

                        @Override
                        public void onPUnsubscribe(String pattern, int subscribedChannels) {
                        }

                        @Override
                        public void onPSubscribe(String pattern, int subscribedChannels) {
                        }
                    }, "channel-test");
                } catch (Exception e) {
                    System.out.println("End connection");
                }
                System.out.println("End subscribe");
            }
        }).start();
        return jedis;
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
   //     redisServer.stop();
    }
}
