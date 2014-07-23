package fr.tichu.model.ws;

import fr.titan.tichu.Orientation;
import fr.titan.tichu.model.FoldType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represent a fold of a player
 */
public class Fold implements Serializable{
    private List<CardWS> cards = new ArrayList<CardWS>();
    private FoldType type;
    private Float high;
    private int nb;
    // Value user want after mahjong
    private Integer mahjongValue;
    private Float jokerValue;
    private Orientation player;

    public Fold() {
    }

    public Fold(FoldType type, Float high, CardWS... cards) {
        this.cards = Arrays.asList(cards);
        this.type = type;
        this.high = high;
    }

    public List<CardWS> getCards() {
        return cards;
    }

    public void addCard(CardWS card) {
        this.cards.add(card);
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

    public boolean isBomb() {
        return type.equals(FoldType.SQUAREBOMB) || type.equals(FoldType.STRAIGHTBOMB);
    }

    public Float getHigh() {
        return high;
    }

    public void setHigh(Float high) {
        this.high = high;
    }

    public Orientation getPlayer() {
        return player;
    }

    public void setPlayer(Orientation player) {
        this.player = player;
    }

    public Integer getMahjongValue() {
        return mahjongValue;
    }

    public void setMahjongValue(Integer mahjongValue) {
        this.mahjongValue = mahjongValue;
    }

    public int getNb() {
        return nb;
    }

    public void setNb(int nb) {
        this.nb = nb;
    }

    public Float getJokerValue() {
        return jokerValue;
    }

    public void setJokerValue(Float jokerValue) {
        this.jokerValue = jokerValue;
    }

    @Override
    public String toString() {
        return "Fold : " + "type=" + type + ", high=" + high + ", player=" + player;
    }
}
