package fr.titan.tichu.model;


import fr.titan.tichu.model.ws.CardWS;

import java.io.Serializable;
import java.util.*;

/**
 * Represent un paquet de carte
 */
public class CardPackage implements Serializable {
    private List<Card> cards;
    private Map<String, Card> indexCards = new HashMap<String, Card>();
    private Card mahjongCard;
    private Card dogsCard;
    private Card phoenixCard;
    private Card dragonCard;

    public void createCards() {
        if (cards == null) {
            cards = new ArrayList<Card>();
            for (String color : new String[] { "green", "blue", "red", "black" }) {
                for (int i = 2; i < 15; i++) {
                    Card card = new ValueCard(i, color);
                    cards.add(card);
                    indexCards.put(i + color, card);
                }
            }
            this.mahjongCard = SpecialCard.SpecialCardFactory.Mahjong.get();
            this.dogsCard = SpecialCard.SpecialCardFactory.Dogs.get();
            this.phoenixCard = SpecialCard.SpecialCardFactory.Phoenix.get();
            this.dragonCard = SpecialCard.SpecialCardFactory.Dragon.get();
            cards.add(this.mahjongCard);
            cards.add(this.dogsCard);
            cards.add(this.phoenixCard);
            cards.add(this.dragonCard);
        }
    }

    public List<Card> getCards() {
        return cards;
    }

    public void resetCards() {
        for (Card card : cards) {
            card.setOwner(null);
        }
    }

    public List<Card> getCopy() {
        return new ArrayList<Card>(this.cards);
    }

    public Card getCard(CardWS cardWS) {
        switch (cardWS.getValue()) {
        case 1:
            return mahjongCard;
        case 15:
            return dogsCard;
        case 16:
            return phoenixCard;
        case 17:
            return dragonCard;
        default:
            return indexCards.get(cardWS.getValue() + cardWS.getColor());
        }
    }

    public List<Card> getCards(List<CardWS> cardsWS) {
        List<Card> cards = new ArrayList<Card>();
        if (cardsWS == null) {
            return cards;
        }
        for (CardWS card : cardsWS) {
            cards.add(getCard(card));
        }
        return cards;
    }

    public List<Card> getCards(CardWS... cardsWS) {
        return getCards(Arrays.asList(cardsWS));
    }

    public Card getMahjongCard() {
        return mahjongCard;
    }

    public Card getDragonCard() {
        return dragonCard;
    }

    public Card getDogsCard() {
        return dogsCard;
    }
}
