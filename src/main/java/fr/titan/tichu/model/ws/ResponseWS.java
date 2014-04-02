package fr.titan.tichu.model.ws;

/**
 * Object to communicate with client with websocket
 */
public class ResponseWS {
    private ResponseType type;
    private Object object;

    public ResponseWS(ResponseType responseType, Object object) {
        this.type = responseType;
        this.object = object;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
