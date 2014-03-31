package fr.titan.tichu.model;

/**
 * User: Titan
 * Date: 29/03/14
 * Time: 16:42
 */
public abstract class Card {

    private CardType cardType;
    private Player owner;

    public Card(CardType cardType){
        this.cardType = cardType;
    }

    public CardType getType(){
        return this.cardType;
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }
}
