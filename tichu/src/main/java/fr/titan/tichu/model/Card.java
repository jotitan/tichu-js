package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.CardWS;

import java.io.Serializable;

/**
 * User: Titan Date: 29/03/14 Time: 16:42
 */
public abstract class Card implements Serializable{

    private CardType cardType;
    private Player owner;

    public Card(CardType cardType) {
        this.cardType = cardType;
    }

    public abstract int getScore();

    public CardType getType() {
        return this.cardType;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public abstract CardWS toCardWS();

    public abstract int getValue();

    public boolean isPhoenix() {
        return cardType.equals(CardType.PHOENIX);
    }

    public boolean isDog() {
        return cardType.equals(CardType.DOGS);
    }

}
