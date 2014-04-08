package fr.titan.tichu.ws;

import fr.titan.tichu.model.Player;
import fr.titan.tichu.service.GameService;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;

/**
 * Chat in websocket to communicate
 */
@ServerEndpoint("/chat")
public class ChatWebSocket {

    private GameService gameService = new GameService();
    private Player player;
    private RemoteEndpoint.Basic basic;

    @OnOpen
    public void open(Session session) {
        String token = session.getRequestParameterMap().get("token").get(0);
        this.player = gameService.getPlayerByToken(token);
        this.player.setChatClient(this);
        this.basic = session.getBasicRemote();
    }

    private void broadcast(Player player, String message) {
        for (Player p : player.getGame().getPlayers()) {
            if (p.getChatClient() != null && !p.equals(player)) {
                try {
                    String data = "{\"player\":\"" + player.getOrientation().toString() + "\",\"message\":\"" + message + "\"}";
                    p.getChatClient().getBasic().sendText(data);
                } catch (IOException ioex) {
                }
            }
        }
    }

    @OnMessage
    public void receiveMessage(String message, Session session) {
        broadcast(this.player, message);
    }

    @OnClose
    public void close(Session session) {
    }

    public RemoteEndpoint.Basic getBasic() {
        return basic;
    }
}
