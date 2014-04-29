package fr.titan.tichu.service.cache;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;
import fr.titan.tichu.ws.TichuWebSocket;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA. User: 960963 Date: 28/04/14 Time: 16:21 To change this template use File | Settings | File Templates.
 */
public class RedisMessageCache implements MessageCache {
    final private Logger logger = LoggerFactory.getLogger(RedisMessageCache.class);

    final private JedisPool jedisPool;

    public RedisMessageCache(String host, int port) throws Exception {
        jedisPool = new JedisPool(host, port);
        try {
            jedisPool.getResource().isConnected();
        } catch (Exception e) {
            throw new Exception("Cannot connect to redis server");
        }
    }

    @Override
    public void register(final Player player, final TichuClientCommunication clientCommunication) {
        final JedisPubSub jedisPubSub = new JedisPubSub() {
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
        new Thread(new Runnable() {
            @Override
            public void run() {
                String key = "message:player:" + player.getToken();
                String keyGame = "message:game:" + player.getGame().getGame();
                try{
                    jedisPool.getResource().subscribe(jedisPubSub, key, keyGame);
                }catch(JedisConnectionException jex){
                    // Connexion reset
                    logger.info("Connection end " + player.getName());
                }
            }
        }).start();
    }

    @Override
    public void unregister(Player player) {
        // jedis


    }

    @Override
    public void sendMessage(Game game, Player player, ResponseType type, Object o) {
        jedisPool.getResource().publish("message:player:" + player.getToken(), createMessage(type, o));
    }

    @Override
    public void sendMessageToAll(Game game, ResponseType type, Object o) {
        jedisPool.getResource().publish("message:game:" + game.getGame(), createMessage(type, o));
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
