package fr.titan.tichu;

import fr.titan.tichu.model.ws.ResponseType;

/**
 *
 */
public interface TichuClientCommunication {
    /**
     * Send a message composed with type and object (will be converted in string)
     * 
     * @param type
     * @param object
     */
    void send(ResponseType type, Object object);

    /**
     * Send a string message
     * 
     * @param message
     */
    void send(String message);
}
