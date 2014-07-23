package fr.titan.tichu.service.mock;

import java.util.List;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.message.MessagePublishThread;

/**
*
 */
public class TichuWebSocketMock implements TichuClientCommunication {
    private String player;

    private List<ResponseType> responses;

    public TichuWebSocketMock(String player, List<ResponseType> responses) {
        this.player = player;
        this.responses = responses;
    }

    @Override
    public void send(ResponseType type, Object object) {
        System.out.println("MOCK " + this.player + " " + type);
        responses.add(type);
    }

    public void send(String message) {
        responses.add(ResponseType.EMPTY);
        System.out.println("MOCK " + this.player + " " + message);
    }

    @Override
    public void setPublishThread(MessagePublishThread thread) {

    }
}
