package fr.titan.tichu.service.cache.message;

import java.util.HashMap;

import com.google.common.collect.Maps;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.ws.ResponseType;

/**
 *
 */
public class MemoryMessageCache implements MessageCache {
    private HashMap<String, TichuClientCommunication> webSocketByToken = Maps.newHashMap();
    private HashMap<String, TichuClientCommunication> chatByToken = Maps.newHashMap();

    @Override
    public void sendMessage(Game game, Player player, ResponseType type, Object o) {
        TichuClientCommunication ws = webSocketByToken.get(player.getToken());
        ws.send(type, o);
    }

    @Override
    public void sendMessageToAll(Game game, ResponseType type, Object o) {
        for (Player player : game.getPlayers()) {
            if (type.equals(ResponseType.CHAT)) {
                // If chat communication, use the chat websocket
                TichuClientCommunication chat = chatByToken.get(player.getToken());
                if (chat != null) {
                    chat.send((String) o);
                }
            } else {
                TichuClientCommunication ws = webSocketByToken.get(player.getToken());
                if (ws != null && player.getPlayerStatus().equals(PlayerStatus.CONNECTED)) {
                    ws.send(type, o);
                }
            }
        }
    }

    @Override
    public void register(String game, String token, TichuClientCommunication clientCommunication) {
        webSocketByToken.put(token, clientCommunication);
    }

    @Override
    public void registerChat(Player player, TichuClientCommunication clientCommunication) {
        chatByToken.put(player.getToken(), clientCommunication);
    }

    @Override
    public void unregister(String token) {
        webSocketByToken.remove(token);
    }
}
