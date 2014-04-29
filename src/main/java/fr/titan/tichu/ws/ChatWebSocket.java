package fr.titan.tichu.ws;

import java.io.IOException;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.GameService;
import fr.titan.tichu.service.cache.CacheFactory;
import fr.titan.tichu.service.cache.MessageCache;

/**
 * Chat in websocket to communicate
 */
@ServerEndpoint("/chat")
public class ChatWebSocket implements TichuClientCommunication {

    private GameService gameService = new GameService();
    private Player player;
    private RemoteEndpoint.Basic basic;
    private MessageCache messageCache = CacheFactory.getMessageCache();

    @OnOpen
    public void open(Session session) {
        String token = session.getRequestParameterMap().get("token").get(0);
        this.player = gameService.getPlayerByToken(token);
        messageCache.registerChat(this.player, this);
        this.basic = session.getBasicRemote();
    }

    @Override
    public void send(ResponseType type, Object object) {

    }

    @Override
    public void send(String message) {
        try {
            this.basic.sendText(message);
        } catch (IOException ioex) {
        }
    }

    @OnMessage
    public void receiveMessage(String message, Session session) {
        String data = "{\"player\":\"" + player.getOrientation().toString() + "\",\"message\":\"" + message + "\"}";
        messageCache.sendMessageToAll(player.getGame(), ResponseType.CHAT, data);
    }

    @OnClose
    public void close(Session session) {
    }
}