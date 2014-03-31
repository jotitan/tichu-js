package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.CardWS;

import java.util.ArrayList;
import java.util.List;

/**
 * Represent a fold of a player
 */
public class Fold {
    private List<CardWS> cards = new ArrayList<CardWS>();
    private FoldType type;
    private int high;
    private Player.Orientation player;

    public List<CardWS> getCards() {
        return cards;
    }

    public void setCards(List<CardWS> cards) {
        this.cards = cards;
    }

    public FoldType getType() {
        return type;
    }

    public void setType(FoldType type) {
        this.type = type;
    }

    public int getHigh() {
        return high;
    }

    public void setHigh(int high) {
        this.high = high;
    }

    public Player.Orientation getPlayer() {
        return player;
    }

    public void setPlayer(Player.Orientation player) {
        this.player = player;
    }
}
