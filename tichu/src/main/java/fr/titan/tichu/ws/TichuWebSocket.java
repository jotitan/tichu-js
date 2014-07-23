package fr.tichu.ws;

import com.google.inject.Inject;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.rest.ResponseRest;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;
import fr.titan.tichu.service.GameService;
import fr.titan.tichu.service.MessageService;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.MessagePublishThread;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.server.ServerEndpoint;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * User: Titan Date: 26/03/14 Time: 20:36
 */
@ServerEndpoint(value = "/chat4", configurator = WebSocketConfigurator.class)
public class TichuWebSocket implements TichuClientCommunication {
    private Logger logger = LoggerFactory.getLogger(TichuWebSocket.class);

    @Inject
    private MessageService messageService;

    @Inject
    private GameService gameService;

    @Inject

    private MessageCache messageCache;

    private String playerName;

    private String token;

    private String gameName;

    private RemoteEndpoint.Basic basic;

    private MessagePublishThread publishThread;

    public TichuWebSocket() {
        logger.info("INIT WEB");
    }

    @OnOpen
    public void open(Session session) {
        logger.info("OPEN");
        token = session.getRequestParameterMap().get("token").get(0);
        Player player = gameService.connectGame(token);
        if (player != null) {
            playerName = player.getName();
            gameName = player.getGame().getGame();
            messageCache.register(gameName, token, this);
            this.basic = session.getBasicRemote();
            send(ResponseType.CONNECTION_OK, gameService.getContextGame(token));
            // If reconnection, no checktable
            gameService.checkTableComplete(token);
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
            logger.info("Response " + type + "(" + gameName + ")");
            if (this.basic == null || token == null) {
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
            logger.info("Response Auto " + "(" + playerName + ")");
            if (this.basic == null || token == null) {
                return;
            }
            try {
                this.basic.sendText(message);
            } catch (NullPointerException nex) {
                System.out.println(nex);
            } catch (IOException ioex) {
            }
        }
    }

    /**
     * Can receive annonce (tichu, grand tichu), fold
     * 
     * @param message
     *            : Message received
     * @param session
     *            : Session of websocket
     */
    @OnMessage
    public void message(String message, Session session) {
        if (!message.contains("HEARTBEAT")) {
            logger.info("MESSAGE : " + message);
        }
        messageService.treatMessage(token, message);
    }

    @OnClose
    public void close(Session session, CloseReason closeReason) {
        // Have to pause the game

        logger.info("CLOSE " + playerName);
        synchronized (this) {
            this.basic = null;
            if (token != null) {
                if (this.publishThread != null) {
                    publishThread.close();
                }
                gameService.playerDisconnect(token);

            }
        }

    }

    @Override
    public void setPublishThread(MessagePublishThread thread) {
        this.publishThread = thread;
    }
}
