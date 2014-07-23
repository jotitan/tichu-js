package tichu.model.ws;

/**
 *
 */
public class RequestWS {
    private RequestType type;

    private String value;

    public RequestType getType() {
        return type;
    }

    public void setType(RequestType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
