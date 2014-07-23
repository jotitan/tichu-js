package fr.tichu.model.ws;

import org.codehaus.jackson.annotate.JsonIgnore;

import java.io.Serializable;

/**
*
 */
public class ChangeCards implements Serializable{
    private CardWS toLeft;
    private CardWS toRight;
    private CardWS toPartner;

    public CardWS getToLeft() {
        return toLeft;
    }

    public void setToLeft(CardWS toLeft) {
        this.toLeft = toLeft;
    }

    public CardWS getToRight() {
        return toRight;
    }

    public void setToRight(CardWS toRight) {
        this.toRight = toRight;
    }

    public CardWS getToPartner() {
        return toPartner;
    }

    public void setToPartner(CardWS toPartner) {
        this.toPartner = toPartner;
    }

    @JsonIgnore
    public boolean isComplete(){
        return toLeft!=null && toRight!=null && toPartner!=null;
    }
}
