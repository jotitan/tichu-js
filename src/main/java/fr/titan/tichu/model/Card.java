package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.CardWS;

/**
 * User: Titan Date: 29/03/14 Time: 16:42
 */
public abstract class Card {

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
}