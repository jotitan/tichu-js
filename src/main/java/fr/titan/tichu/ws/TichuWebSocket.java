package fr.titan.tichu.ws;


import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.ResponseRest;
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
 * User: Titan
 * Date: 26/03/14
 * Time: 20:36
 */
@ServerEndpoint(value="/chat4")
public class TichuWebSocket {
    private Logger logger = LoggerFactory.getLogger(TichuWebSocket.class);

    private MessageService messageService;

    private GameService gameService;

    private Player player;

    public TichuWebSocket(){
        logger.info("INIT WEB");
        messageService = new MessageService();
        gameService = new GameService();
    }

    @OnOpen
    public void open(Session session){
        logger.info("OPEN");
        String token = session.getRequestParameterMap().get("token").get(0);
        this.player = gameService.connectGame(token);
        if(this.player!=null){
            this.player.setWebSocket(this);
            sendMessage(new ResponseRest(1, this.player), session);
        }else{
            sendMessage(new ResponseRest(0,"erreur"),session);
        }
    }

    private void sendMessage(Object object,Session session){
        ObjectMapper om = new ObjectMapper();
        ByteArrayOutputStream tab = new ByteArrayOutputStream();
        try{
            om.writer().writeValue(tab,object);
            session.getBasicRemote().sendText(new String(tab.toByteArray()));
        }   catch(IOException ioex){}
    }

    /**
     * Can receive annonce (tichu, grand tichu), fold
     * @param message
     * @param session
     */
    @OnMessage
    public void message(String message,Session session){
        logger.info("MESSAGE : " + message);
        messageService.treatMessage(message);
        try{
            session.getBasicRemote().sendText("Salut mon pote " + message);
        }   catch(IOException ioex){}
    }

    @OnClose
    public void close(Session session,CloseReason closeReason){
        System.out.println("CLOSE");
        if(this.player!=null){
            this.player.setPlayerStatus(PlayerStatus.DISCONNECTED);
        }

    }

}
