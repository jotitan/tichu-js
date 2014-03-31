package fr.titan.tichu.ws;

import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.ResponseRest;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;
import fr.titan.tichu.service.GameService;
import fr.titan.tichu.service.MessageService;
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
public class TichuWebSocket {
    private Logger logger = LoggerFactory.getLogger(TichuWebSocket.class);

    private MessageService messageService;

    private GameService gameService;

    private Player player;

    private RemoteEndpoint.Basic basic;

    public TichuWebSocket() {
        logger.info("INIT WEB");
        messageService = new MessageService();
        gameService = new GameService();
    }

    @OnOpen
    public void open(Session session) {
        logger.info("OPEN");
        String token = session.getRequestParameterMap().get("token").get(0);
        this.player = gameService.connectGame(token);
        if (this.player != null) {
            this.player.setWebSocket(this);
            this.basic = session.getBasicRemote();
            sendMessage(ResponseType.CONNECTION_OK, new ResponseRest(1, this.player));
        } else {
            sendMessage(ResponseType.CONNECTION_KO, new ResponseRest(0, "erreur"));
        }
    }

    /**
     * 
     * @param type
     *            Type of response (new player...)
     * @param object
     *            Object with data
     */
    public void sendMessage(ResponseType type, Object object) {
        ObjectMapper om = new ObjectMapper();
        ByteArrayOutputStream tab = new ByteArrayOutputStream();
        try {
            om.writer().writeValue(tab, new ResponseWS(type, object));
            this.basic.sendText(new String(tab.toByteArray()));
        } catch (IOException ioex) {
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
        System.out.println("CLOSE");
        if (this.player != null) {
            this.player.setPlayerStatus(PlayerStatus.DISCONNECTED);
        }

    }

}
