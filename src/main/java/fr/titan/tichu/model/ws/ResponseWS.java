package fr.titan.tichu.model.ws;

/**
 * Object to communicate with client with websocket
 */
public class ResponseWS {
    private ResponseType responseType;
    private Object object;

    public ResponseWS(ResponseType responseType, Object object) {
        this.responseType = responseType;
        this.object = object;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
