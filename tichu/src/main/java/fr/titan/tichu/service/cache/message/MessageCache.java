package fr.titan.tichu.service.cache.message;

import com.google.inject.ImplementedBy;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;

/**
 * To put message into temp cache
 */
@ImplementedBy(MessageCacheImpl.class)
public interface MessageCache {

    /**
     * Send message to specific player
     * 
     * @param game
     * @param player
     * @param type
     * @param o
     */
    void sendMessage(Game game, Player player, ResponseType type, Object o);

    /**
     * Broadcast message to all player
     * 
     * @param game
     * @param type
     * @param o
     */
    void sendMessageToAll(Game game, ResponseType type, Object o);

    /**
     * Register the websocket with the player
     * 
     * @param game
     * @param token
     * @param clientCommunication
     */
    void register(String game, String token, TichuClientCommunication clientCommunication);

    void registerChat(Player player, TichuClientCommunication clientCommunication);

}
