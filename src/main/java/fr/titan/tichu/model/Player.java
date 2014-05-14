package fr.titan.tichu.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import fr.titan.tichu.Orientation;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.PlayerWS;

/**
 *
 */
public class Player implements Serializable {
    private String name;
    private String token; // To verify identity
    private Orientation orientation;
    private List<Card> cards = new ArrayList<Card>();
    /* Stock cards swap between players */
    private ChangeCards changeCards = new ChangeCards();
    private PlayerStatus playerStatus = PlayerStatus.FREE_CHAIR;
    private Player partner;

    private Game game;
    private AnnonceType annonce = null;

    /* Order of position when end the round */
    private int endPosition = -1;
    private boolean distributeAllCards = false;
    /* List of win folds */
    private List<Card> cardOfFolds = new ArrayList<Card>();

    private void sortCards() {
        Collections.sort(this.cards, new Comparator<Card>() {
            @Override
            public int compare(Card o1, Card o2) {
                return o1.getValue() - o2.getValue();
            }
        });
    }

    public Player(Game game, Orientation orientation) {
        this.game = game;
        this.orientation = orientation;
    }

    public boolean isConnected() {
        return playerStatus.equals(PlayerStatus.CONNECTED);
    }

    public int getNbcard() {
        return cards.size();
    }

    public boolean ended() {
        return cards.size() == 0;
    }

    public boolean win() {
        return cards.size() == 0 && endPosition == 0;
    }

    /* Add a card to the hand */
    public void addCard(Card card) {
        this.cards.add(card);
        card.setOwner(this);
        sortCards();
    }

    public boolean hasCard(int value) {
        for (Card card : cards) {
            if (card.getValue() == value) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPhoenix() {
        for (Card card : cards) {
            if (card.isPhoenix()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find if a combinaison with a specific value exist (case mahjong)
     * 
     * @param value
     *            Value required by the mahjong
     * @param type
     * @param high
     * @param length
     * @return
     */
    public boolean canPlayMahjongValue(int value, FoldType type, Integer high, Integer length) {
        if (!hasCard(value)) {
            return false;
        }
        /* Can play what he wants */
        if (type == null) {
            return true;
        }
        // Bomb case
        if (countCards(value) == 4 || isStraightBomb(value)) {
            return true;
        }
        /* When previous card is higher */
        if (high != null && value <= high && (type.equals(FoldType.SINGLE) || type.equals(FoldType.PAIR) || type.equals(FoldType.BRELAN))) {
            return false;
        }
        boolean phoenix = hasPhoenix();
        switch (type) {
        case SINGLE:
            if (high != null && value <= high) {
                return false;
            }
            return true;
        case PAIR:
            if (phoenix) {
                return true;
            }
            int nb = countCards(value);
            return nb >= 2;

        case BRELAN:
            nb = countCards(value);
            return nb >= 3 || (nb == 2 && phoenix);
        case FULLHOUSE:
            nb = Math.min(3, countCards(value));
            // Pas obligatoire d etre au dessus
            int maxOther = getMaxCards(value, 0);
            switch (nb) {
            case 3:
                if (value <= high) {
                    // Value is the Pair, have to find brelan
                    maxOther = getMaxCards(value, high);
                    return maxOther >= 3 || (maxOther == 2 && phoenix);
                }
                // Find PAIR
                return maxOther >= 2 || (maxOther == 1 && phoenix);
            case 2:
                if (value > high && phoenix) {
                    // Find PAIR
                    return maxOther >= 2;
                }
                maxOther = getMaxCards(value, high);
                // Find BRELAN or PAIR with phoenix
                return maxOther >= 3 || (maxOther == 2 && phoenix);
            case 1:
                // Find BRELAN
                if (!phoenix) {
                    return false;
                }
                maxOther = getMaxCards(value, high);
                return maxOther >= 3;
            }
            return false;
        case STRAIGHTPAIR:
            int previous = 0;
            Map<Integer, Integer> values = Maps.newHashMap();
            for (Card card : cards) {
                if (!values.containsKey(card.getValue())) {
                    values.put(card.getValue(), 1);
                } else {
                    values.put(card.getValue(), values.get(card.getValue()) + 1);
                }
            }
            List<Integer> nbPairs = Lists.newArrayList();
            int nbPair = 0;
            boolean jokerNotUsed = phoenix;
            int previousValue = 0;
            for (Integer valueCard : values.keySet()) {
                if (valueCard > high) {
                    if (previousValue == 0 || previousValue == valueCard - 1) {
                        if (values.get(valueCard) >= 2) {
                            nbPair++;
                            previousValue = valueCard;
                        } else {
                            if (jokerNotUsed) {
                                jokerNotUsed = false;
                                nbPair++;
                                previousValue = valueCard;
                            }
                        }
                    } else {
                        // Save and rebegin
                        if (value <= previousValue && value >= previousValue - nbPair) {
                            nbPairs.add(nbPair);
                        }
                        if (values.get(valueCard) >= 2) {
                            nbPair = 1;
                            previousValue = valueCard;
                            jokerNotUsed = phoenix;
                        } else {
                            if (phoenix) {
                                nbPair = 1;
                                previousValue = valueCard;
                                jokerNotUsed = true;
                            } else {
                                nbPair = 0;
                                previousValue = 0;
                                jokerNotUsed = phoenix;
                            }
                        }
                    }
                }
            }
            // Last element
            if (value <= previousValue && value >= previousValue - nbPair) {
                nbPairs.add(nbPair);
            }
            for (Integer i : nbPairs) {
                if (i >= length) {
                    return true;
                }
            }
            return false;
        case SQUAREBOMB:
            return countCards(value) == 4;
        case STRAIGHTBOMB:
            Map<String, List<Card>> colorStraight = Maps.newHashMap();
            for (Card card : cards) {
                if (card.getValue() > high && card instanceof ValueCard) {
                    String color = ((ValueCard) card).getColor();
                    if (!colorStraight.containsKey(color)) {
                        colorStraight.put(color, new ArrayList<Card>());
                    }
                    colorStraight.get(color).add(card);
                }
            }

            for (String color : colorStraight.keySet()) {
                previous = 0;
                nb = 0;
                int first = colorStraight.get(color).get(0).getValue();
                for (Card card : colorStraight.get(color)) {
                    if (previous == 0 || previous == card.getValue() - 1) {
                        nb++;
                    } else {
                        if (nb >= length && value >= first && value <= first + length) {
                            return true;
                        }
                        nb = 0;
                        first = card.getValue();
                    }
                    previous = card.getValue();
                }
                if (nb >= length && value >= first && value <= first + length) {
                    return true;
                }
            }
            return false;
        case STRAIGHT:
            if (value <= high || value > (high + length)) {
                return false;
            }
            List<LinkedList<Card>> parts = Lists.newArrayList();
            LinkedList<Card> currentPart = Lists.newLinkedList();
            previous = 0;
            for (Card card : cards) {
                if (card.getValue() != previous) {
                    if (previous == 0 || !(previous + 1 == card.getValue())) {
                        currentPart = Lists.newLinkedList();
                        parts.add(currentPart);
                    }
                    currentPart.add(card);
                    previous = card.getValue();
                }
            }
            // Compare length
            for (int i = 0; i < parts.size(); i++) {
                LinkedList<Card> list = parts.get(i);
                if ((list.size() >= length || (phoenix && list.size() >= length - 1)) && list.getFirst().getValue() > high
                        && value >= list.getFirst().getValue() && value <= list.getLast().getValue()) {
                    return true;
                }
                // Try to link this list with next if phoenix exist
                if (phoenix && i < parts.size() - 1) {
                    if (list.getLast().getValue() == parts.get(i + 1).getFirst().getValue() - 2 && list.size() + parts.get(i + 1).size() >= length - 1) {
                        return true;
                    }
                }
            }

        default:
            return false;
        }
    }

    private boolean isStraightBomb(int valueMahjong) {
        Map<String, List<Card>> colorStraight = Maps.newHashMap();
        for (Card card : cards) {
            if (card instanceof ValueCard) {
                String color = ((ValueCard) card).getColor();
                if (!colorStraight.containsKey(color)) {
                    colorStraight.put(color, new ArrayList<Card>());
                }
                colorStraight.get(color).add(card);
            }
        }

        for (String color : colorStraight.keySet()) {
            int previous = 0;
            int nb = 0;
            int first = colorStraight.get(color).get(0).getValue();
            for (Card card : colorStraight.get(color)) {
                if (previous == 0 || previous == card.getValue() - 1) {
                    nb++;
                } else {
                    if (nb >= 5 && valueMahjong >= first && valueMahjong <= first + nb) {
                        return true;
                    }
                    nb = 0;
                    first = card.getValue();
                }
                previous = card.getValue();
            }
            if (nb >= 5 && valueMahjong >= first && valueMahjong <= first + nb) {
                return true;
            }
        }
        return false;
    }

    /* Count card with this value */
    private int countCards(int value) {
        int nb = 0;
        for (Card card : cards) {
            nb += (card.getValue() == value) ? 1 : 0;
        }
        return nb;
    }

    /* Count the biggest number of card */
    /**
     * @param exceptValue
     *            : not use like possible value
     */
    /**
     * @param min
     *            : value are greater than min
     */
    private int getMaxCards(int exceptValue, int min) {
        int previous = 0;
        int nb = 0;
        int max = 0;
        for (Card card : cards) {
            if (card.getValue() != exceptValue & card.getValue() > min) {
                if (previous == 0 || card.getValue() == previous) {
                    previous = card.getValue();
                    nb++;
                } else {
                    max = Math.max(nb, max);
                    previous = card.getValue();
                    nb = 1;
                }
            }
        }
        return Math.max(nb, max);
    }

    public int getPointCards() {
        int score = 0;
        for (Card card : this.cardOfFolds) {
            score += card.getScore();
        }
        return score;
    }

    /**
     * 
     * @param card
     *            : player to give the card
     */
    public void giveCard(Card card) {
        card.setOwner(null);
        cards.remove(card);
    }

    public void playFold(List<Card> fold) {
        for (Card card : fold) {
            card.setOwner(null);
        }
        this.cards.removeAll(fold);
    }

    public void resetCards() {
        cards = new ArrayList<Card>();
        changeCards = new ChangeCards();
        cardOfFolds = new ArrayList<Card>();
        annonce = null;
        endPosition = -1;
        distributeAllCards = false;
    }

    public int getSucceedAnnonce() {
        if (annonce == null) {
            return 0;
        }
        int score = annonce.equals(AnnonceType.GRAND_TICHU) ? 200 : 100;
        return endPosition == 0 ? score : -score;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public void createToken(String game) {
        try {
            String base = game + orientation + System.currentTimeMillis();
            MessageDigest md5 = MessageDigest.getInstance("md5");
            byte[] bytes = md5.digest(base.getBytes());
            BigInteger bi = new BigInteger(1, bytes);
            this.token = String.format("%0" + (bytes.length << 1) + "X", bi);
        } catch (Exception e) {

        }
    }

    public boolean canTichu() {
        return this.cards.size() == 14 && this.partner.annonce == null;
    }

    public String getToken() {
        return token;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public PlayerWS getPlayerWS() {
        PlayerWS player = new PlayerWS(this.name, this.orientation, this.endPosition);
        player.setAnnonce(this.annonce);
        player.setConnected(playerStatus.equals(PlayerStatus.CONNECTED));
        return player;
    }

    public void addCardsOfFold(List<Card> cards) {
        this.cardOfFolds.addAll(cards);
    }

    public List<Card> getCardOfFolds() {
        return cardOfFolds;
    }

    public AnnonceType getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceType annonce) {
        this.annonce = annonce;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public ChangeCards getChangeCards() {
        return changeCards;
    }

    public void setPartner(Player partner) {
        this.partner = partner;
    }

    public void setDistributeAllCards(boolean distributeAllCards) {
        this.distributeAllCards = distributeAllCards;
    }

    public boolean getDistributeAllCards() {
        return distributeAllCards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Player player = (Player) o;

        if (name != null ? !name.equals(player.name) : player.name != null)
            return false;
        if (orientation != player.orientation)
            return false;
        if (token != null ? !token.equals(player.token) : player.token != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        result = 31 * result + (orientation != null ? orientation.hashCode() : 0);
        return result;
    }
}
