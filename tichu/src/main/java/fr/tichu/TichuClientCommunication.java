package tichu;

import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.message.MessagePublishThread;

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

    /**
     * Set the publish thread used to communicate with redis. Usefull to close thread when connection fail
     * 
     * @param thread
     */
    void setPublishThread(MessagePublishThread thread);
}
