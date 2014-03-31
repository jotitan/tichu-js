package fr.titan.tichu.model;

/**
 * User: Titan
 * Date: 29/03/14
 * Time: 18:17
 */
public class ResponseRest {
    private String message;
    private int status;
    private Object object;

    public ResponseRest(int status){
        this.status = status;
    }

    public ResponseRest(int status, String message){
        this(status);
        this.message = message;
    }

    public ResponseRest(int status,Object object){
        this.status = status;
        this.object = object;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
