package fr.titan.tichu;

import fr.titan.tichu.model.ws.ResponseType;

/**
 *
 */
public interface TichuClientCommunication {
    void send(ResponseType type, Object object);
}
