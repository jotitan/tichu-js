package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.CardWS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represent un paquet de carte
 */
public class CardPackage {
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

    public Card getMahjongCard() {
        return mahjongCard;
    }
}
