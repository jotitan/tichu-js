package fr.titan.tichu.service.cache;

import java.io.IOException;
import java.util.Properties;

import com.google.inject.Singleton;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage Redis cache to save game information
 */
@Singleton
public class MessageCacheImpl implements MessageCache {
    private MessageCache messageCache;

    final private Logger logger = LoggerFactory.getLogger(MessageCacheImpl.class);

    public MessageCacheImpl() {
        Properties p = new Properties();
        try {
            p.load(MessageCacheImpl.class.getResourceAsStream("/tichu.properties"));

        } catch (IOException ioex) {
            logger.error("Error when loading redis properties, default configuration");
        } catch (Exception e) {
        }
        String host = p.getProperty("redis.host");
        int port = Integer.valueOf(p.getProperty("redis.port"));
        try {
            messageCache = new RedisMessageCache(host, port);
        } catch (Exception e) {
            logger.info("No Redis found, use memory message cache instead");
            messageCache = new MemoryMessageCache();
        }
    }

    @Override
    public void sendMessage(Game game, Player player, ResponseType type, Object o) {
        messageCache.sendMessage(game, player, type, o);
    }

    @Override
    public void sendMessageToAll(Game game, ResponseType type, Object o) {
        messageCache.sendMessageToAll(game, type, o);
    }

    @Override
    public void register(Player player, TichuClientCommunication clientCommunication) {
        messageCache.register(player, clientCommunication);
    }

    @Override
    public void registerChat(Player player, TichuClientCommunication clientCommunication) {
        messageCache.registerChat(player, clientCommunication);
    }

    @Override
    public void unregister(Player player) {
        messageCache.unregister(player);
    }
}
