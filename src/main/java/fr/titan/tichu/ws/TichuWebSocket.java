package fr.titan.tichu.ws;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.ResponseRest;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;
import fr.titan.tichu.service.GameService;
import fr.titan.tichu.service.MessageService;
import fr.titan.tichu.service.cache.CacheFactory;
import fr.titan.tichu.service.cache.MessageCache;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: Titan Date: 26/03/14 Time: 20:36
 */
@ServerEndpoint(value = "/chat4")
public class TichuWebSocket implements TichuClientCommunication {
    private Logger logger = LoggerFactory.getLogger(TichuWebSocket.class);

    private MessageService messageService;

    private GameService gameService;

    private MessageCache messageCache;

    private Player player;

    private RemoteEndpoint.Basic basic;

    public TichuWebSocket() {
        logger.info("INIT WEB");
        messageService = new MessageService();
        gameService = new GameService();
        messageCache = CacheFactory.getMessageCache("localhost", 6379);
    }

    @OnOpen
    public void open(Session session) {
        logger.info("OPEN");
        String token = session.getRequestParameterMap().get("token").get(0);
        this.player = gameService.connectGame(token);
        if (this.player != null) {
            messageCache.register(this.player, this);

            this.basic = session.getBasicRemote();
            send(ResponseType.CONNECTION_OK, gameService.getContextGame(this.player));
            // If reconnection, no checktabke
            if (!player.isReconnect()) {
                gameService.checkTableComplete(player);
            }
        } else {
            this.basic = session.getBasicRemote();
            send(ResponseType.CONNECTION_KO, new ResponseRest(0, "erreur"));
        }
    }

    /**
     * 
     * @param type
     *            Type of response (new player...)
     * @param object
     *            Object with data
     */
    public void send(ResponseType type, Object object) {
        synchronized (this) {
            logger.info("Response " + type + "(" + this.player.getName() + ")");
            if (this.basic != null && player != null && player.getPlayerStatus().equals(PlayerStatus.DISCONNECTED)) {
                return;
            }

            ObjectMapper om = new ObjectMapper();
            ByteArrayOutputStream tab = new ByteArrayOutputStream();
            try {
                om.writer().writeValue(tab, new ResponseWS(type, object));
                this.basic.sendText(new String(tab.toByteArray()));
            } catch (IOException ioex) {
                System.out.println(ioex);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    @Override
    public void send(String message) {
        synchronized (this) {
            logger.info("Response Auto " + "(" + this.player.getName() + ")");
            if (this.basic != null && player != null && player.getPlayerStatus().equals(PlayerStatus.DISCONNECTED)) {
                return;
            }
            try {
                this.basic.sendText(message);
            } catch (IOException ioex) {
                System.out.println(ioex);
            }
        }
    }

    /**
     * Can receive annonce (tichu, grand tichu), fold
     * 
     * @param message
     * @param session
     */
    @OnMessage
    public void message(String message, Session session) {
        logger.info("MESSAGE : " + message);
        messageService.treatMessage(this.player, message);
    }

    @OnClose
    public void close(Session session, CloseReason closeReason) {
        // Have to pause the game
        logger.info("CLOSE " + this.player.getName());
        synchronized (this) {
            this.basic = null;
            if (this.player != null) {
                this.player.setPlayerStatus(PlayerStatus.DISCONNECTED);
                messageCache.unregister(this.player);
                messageService.playerDisconnect(this.player);
            }
        }

    }

}
