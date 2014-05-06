package fr.titan.tichu.service.cache.message;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import fr.titan.tichu.service.cache.RedisConfiguration;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
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
        /* In this case, 2 connections per user, so 2 block resource */
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(RedisConfiguration.NUMBER_CONNECTION);
        jedisPool = new JedisPool(config, host, port);
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.isConnected();
        } catch (Exception e) {
            throw new Exception("Cannot connect to redis server");
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void register(final String game, final String token, final TichuClientCommunication clientCommunication) {
        String key = "message:player:" + token;
        String keyGame = "message:game:" + game;
        MessagePublishThread thread = new MessagePublishThread(new String[] { key, keyGame }, clientCommunication, jedisPool);
        clientCommunication.setPublishThread(thread);
        thread.start();
    }

    @Override
    public void registerChat(final Player player, final TichuClientCommunication clientCommunication) {
        String keyChat = "chat:game:" + player.getGame().getGame();
        MessagePublishThread thread = new MessagePublishThread(new String[] { keyChat }, clientCommunication, jedisPool);
        clientCommunication.setPublishThread(thread);
        thread.start();
    }

    @Override
    public void sendMessage(Game game, Player player, ResponseType type, Object o) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            jedis.publish("message:player:" + player.getToken(), createMessage(type, o));
        } finally {
            jedisPool.returnResource(jedis);
        }
    }

    @Override
    public void sendMessageToAll(Game game, ResponseType type, Object o) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
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
