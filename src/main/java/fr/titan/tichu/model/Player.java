package fr.titan.tichu.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.PlayerWS;
import fr.titan.tichu.ws.ChatWebSocket;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;

/**
 * User: Titan Date: 29/03/14 Time: 11:47
 */
public class Player {
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

    private TichuClientCommunication client;
    private ChatWebSocket chatClient;

    private boolean reconnect = false;

    public enum Orientation {
        O(0), N(1), E(2), S(3);

        private Orientation(int pos) {
            this.pos = pos;
        }

        private Orientation right;
        private Orientation left;
        private Orientation face;
        private int pos;

        public Orientation getNext() {
            return getLeft();
        }

        public Orientation getLeft() {
            if (left == null) {
                left = getByIndex((this.getPos() + 1) % 4);
            }
            return this.left;
        }

        public Orientation getRight() {
            if (right == null) {
                right = getByIndex((this.getPos() + 3) % 4);
            }
            return this.right;
        }

        public Orientation getFace() {
            if (face == null) {
                face = getByIndex((this.getPos() + 2) % 4);
            }
            return this.face;
        }

        private Orientation getByIndex(int index) {
            for (Orientation or : values()) {
                if (or.getPos() == index) {
                    return or;
                }
            }
            return null;
        }

        public int getPos() {
            return pos;
        }
    }

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
        return this.client != null;
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

    /* Find if a combinaison with a specific value exist (case mahjong) */
    public boolean findCombinaisonWithValue(int value, FoldType type, Integer high, Integer length) {
        if (!hasCard(value)) {
            return false;
        }
        /* Can play what he wants */
        if (type == null) {
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
            int maxOther = getMaxCards(value,0);
            switch(nb){
                case 3 :
                    if(value <= high){
                        // VAlue is the Pair, have to find brelan
                        maxOther = getMaxCards(value,high);
                        return  maxOther>=3 || (maxOther == 2 && phoenix);
                    }
                    // Find PAIR
                    return  maxOther>=2 || (maxOther == 1 && phoenix);
                case 2 :
                    // Find BRELAN or PAIR with phoenix
                    return  maxOther>=3 || (maxOther == 2 && phoenix);
                case 1 :
                    // Find BRELAN
                    if(!phoenix){
                        return false;
                    }
                    return maxOther>=3;
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
                        nbPair = 0;
                        previousValue = 0;
                        jokerNotUsed = phoenix;
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

    private int countCards(int value) {
        int nb = 0;
        for (Card card : cards) {
            nb += (card.getValue() == value) ? 1 : 0;
        }
        return nb;
    }

    private int getMaxCards(int exceptValue,int min){
        int previous = 0;
        int nb = 0;
        int max = 0;
        for(Card card : cards){
            if(card.getValue()!=exceptValue & card.getValue() >=min){
                if(previous == 0 || card.getValue() == previous){
                    previous = card.getValue();
                    nb++;
                }else{
                    max = Math.max(nb,max);
                    previous = card.getValue();
                    nb = 1;
                }
            }
        }
        return Math.max(nb,max);
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

    public TichuClientCommunication getClient() {
        return client;
    }

    public void setClientCommunication(TichuClientCommunication client) {
        this.client = client;
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

    public boolean isDistributeAllCards() {
        return distributeAllCards;
    }

    public ChatWebSocket getChatClient() {
        return chatClient;
    }

    public void setChatClient(ChatWebSocket chatClient) {
        this.chatClient = chatClient;
    }

    public boolean isReconnect() {
        return reconnect;
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }
}
