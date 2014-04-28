package fr.titan.tichu.service.cache;

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

    @Override
    public void sendMessage(Game game, Player player, ResponseType type, Object o) {
        TichuClientCommunication ws = webSocketByToken.get(player.getToken());
        ws.send(type, o);
    }

    @Override
    public void sendMessageToAll(Game game, ResponseType type, Object o) {
        for (Player player : game.getPlayers()) {
            TichuClientCommunication ws = webSocketByToken.get(player.getToken());
            if (ws != null && player.getPlayerStatus().equals(PlayerStatus.CONNECTED)) {
                ws.send(type, o);
            }
        }
    }

    @Override
    public void register(Player player, TichuClientCommunication clientCommunication) {
        webSocketByToken.put(player.getToken(), clientCommunication);
    }

    @Override
    public void unregister(Player player) {
        webSocketByToken.remove(player.getToken());
    }
}
