package fr.titan.tichu.service.cache.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;

/**
 * Created with IntelliJ IDEA. User: 960963 Date: 28/04/14 Time: 16:21 To change this template use File | Settings | File Templates.
 */
public class RedisMessageCache implements MessageCache {
    final private Logger logger = LoggerFactory.getLogger(RedisMessageCache.class);

    final private JedisPool jedisPool;

    public RedisMessageCache(String host, int port) throws Exception {
        jedisPool = new JedisPool(host, port);
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.isConnected();
        } catch (Exception e) {
            throw new Exception("Cannot connect to redis server");
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void register(final String game, final String token, final TichuClientCommunication clientCommunication) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = "message:player:" + token;
                String keyGame = "message:game:" + game;
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.subscribe(createJedisSubscribe(clientCommunication), key, keyGame);
                } catch (JedisConnectionException jex) {
                    // Connexion reset
                    logger.info("Connection end " + token);
                } finally {
                    jedisPool.returnResource(jedis);
                }
            }
        }).start();
    }

    @Override
    public void registerChat(final Player player, final TichuClientCommunication clientCommunication) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String keyChat = "chat:game:" + player.getGame().getGame();
                Jedis jedis = jedisPool.getResource();
                try {
                    jedis.subscribe(createJedisSubscribe(clientCommunication), keyChat);
                } catch (JedisConnectionException jex) {
                    // Connexion reset
                    logger.info("Connection end " + player.getName());
                } finally {
                    jedisPool.returnResource(jedis);
                }
            }
        }).start();
    }

    private JedisPubSub createJedisSubscribe(final TichuClientCommunication clientCommunication) {
        return new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {
                logger.info("Receive on " + channel + " : " + message);
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
            }

            @Override
            public void onPUnsubscribe(String pattern, int subscribedChannels) {
            }

            @Override
            public void onPSubscribe(String pattern, int subscribedChannels) {
            }
        };
    }

    @Override
    public void unregister(String token) {
        // jedis

    }

    @Override
    public void sendMessage(Game game, Player player, ResponseType type, Object o) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.publish("message:player:" + player.getToken(), createMessage(type, o));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void sendMessageToAll(Game game, ResponseType type, Object o) {
        Jedis jedis = jedisPool.getResource();
        try {
            if (type.equals(ResponseType.CHAT)) {
                jedis.publish("chat:game:" + game.getGame(), (String) o);
            } else {
                jedis.publish("message:game:" + game.getGame(), createMessage(type, o));
            }
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    private String createMessage(ResponseType type, Object o) {
        ObjectMapper om = new ObjectMapper();
        ByteArrayOutputStream tab = new ByteArrayOutputStream();
        try {
            om.writer().writeValue(tab, new ResponseWS(type, o));
            return new String(tab.toByteArray());
        } catch (IOException ioex) {
            System.out.println(ioex);
        }
        return null;
    }
}
