package fr.titan.tichu.ws;

import com.google.inject.Inject;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.GameService;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.MessagePublishThread;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * Chat in websocket to communicate
 */
@ServerEndpoint(value = "/chat", configurator = WebSocketConfigurator.class)
public class ChatWebSocket implements TichuClientCommunication {

    @Inject
    private GameService gameService;
    @Inject
    private MessageCache messageCache;
    private Player player;
    private RemoteEndpoint.Basic basic;

    private MessagePublishThread chatThread;

    @OnOpen
    public void open(Session session) {
        String token = session.getRequestParameterMap().get("token").get(0);
        this.player = gameService.getPlayerByToken(token);
        if (this.player != null) {
            messageCache.registerChat(this.player, this);
        }
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
        if (chatThread != null && chatThread.isAlive()) {
            this.chatThread.close();
        }
    }

    @Override
    public void setPublishThread(MessagePublishThread thread) {
        this.chatThread = thread;
    }
}
