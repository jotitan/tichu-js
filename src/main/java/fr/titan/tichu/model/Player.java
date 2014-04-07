package fr.titan.tichu.model;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.PlayerWS;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Player(Game game, Orientation orientation) {
        this.game = game;
        this.orientation = orientation;
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
        return new PlayerWS(this.name, this.orientation, this.endPosition);
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
}
