package tichu.service.cache.message;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.RedisConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage Redis cache to save game information
 */
@Singleton
public class MessageCacheImpl implements MessageCache {
    private MessageCache messageCache;

    final private Logger logger = LoggerFactory.getLogger(MessageCacheImpl.class);

    @Inject
    public MessageCacheImpl(RedisConfiguration redisConfiguration) {
        if (redisConfiguration.getHost() != null) {
            try {
                messageCache = new RedisMessageCache(redisConfiguration.getHost(), redisConfiguration.getPort(), redisConfiguration.getPassword());
            } catch (Exception e) {
                setDefault();
            }
        } else {
            setDefault();
        }
    }

    private void setDefault() {
        logger.info("No Redis found, use memory message cache instead");
        messageCache = new MemoryMessageCache();
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
    public void register(String game, String token, TichuClientCommunication clientCommunication) {
        messageCache.register(game, token, clientCommunication);
    }

    @Override
    public void registerChat(Player player, TichuClientCommunication clientCommunication) {
        messageCache.registerChat(player, clientCommunication);
    }
}
