package fr.titan.tichu.model.ws;

import java.io.Serializable;

/**
*
 */
public class CardWS implements Serializable{
    private int value;
    private String color;

    public CardWS(){

    }

    public CardWS(int value, String color) {
        this.value = value;
        this.color = color;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
