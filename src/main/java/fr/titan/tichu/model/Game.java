package fr.titan.tichu.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Respresent a game
 */

public class Game {
    /* Name of the game */
    private String game;
    /* Password to access the game */
    private String password;
    /* 4 players of the game */
    private List<Player> players = new ArrayList<Player>(4);
    /* Scores */
    private int score1 = 0;
    private int score2 = 0;

    /* Cards of game */
    private List<Card> cards;

    private Fold lastFold;
    private List<Fold> folds = new ArrayList<Fold>();
    /* Wait this player to play */
    private Player currentPlayer = null;

    /* Last player who make a fold */
    private Player lastPlayer = null;
    private Card mahjongCard;

    public Game() {
        for (Player.Orientation or : Player.Orientation.values()) {
            this.players.add(or.getPos(), new Player(this, or));
        }
        createCards();
    }

    public Game(String game) {
        this();
        this.game = game;
    }

    public Game(String game, String password) {
        this(game);
        this.password = password;
    }

    protected void createCards() {
        if (cards == null) {
            cards = new ArrayList<Card>();
            for (String color : new String[] { "green", "blue", "red", "black" }) {
                for (int i = 2; i < 15; i++) {
                    cards.add(new ValueCard(i, color));
                }
            }
            this.mahjongCard = SpecialCard.SpecialCardFactory.Mahjong.get();
            cards.add(this.mahjongCard);
            cards.add(SpecialCard.SpecialCardFactory.Dogs.get());
            cards.add(SpecialCard.SpecialCardFactory.Phoenix.get());
            cards.add(SpecialCard.SpecialCardFactory.Dragon.get());

        }
    }

    public void newTurn() {
        this.currentPlayer = null;
        this.lastFold = null;
    }

    /* Init the game for the round */
    public void newRound() {
        newTurn();
        this.folds = new ArrayList<Fold>();
        this.lastPlayer = null;
        distribute();
    }

    public void newGame() {
        this.score1 = 0;
        this.score2 = 0;
        newRound();
    }

    private void resetCards() {
        for (Player player : this.players) {
            player.resetCards();
        }
        for (Card card : this.cards) {
            card.setOwner(null);
        }
    }

    /**
     * Distribute cards to 4 people
     * 
     * @return
     */
    protected void distribute() {
        // Reset cards of player
        resetCards();
        List<Card> copyCards = new ArrayList<Card>(this.cards);

        for (Player player : players) {
            for (int i = 0; i < 14; i++) {
                int posCard = (int) Math.round(Math.random() * 1000) % copyCards.size();
                player.addCard(copyCards.get(posCard));
                copyCards.remove(posCard);
            }
        }
    }

    /**
     * When all player are connected
     * 
     * @return
     */
    public boolean canPlay() {
        for (Player player : this.players) {
            if (!player.getPlayerStatus().equals(PlayerStatus.CONNECTED)) {
                return false;
            }
        }
        return true;
    }

    /* Verify if fold can be play */
    public boolean verifyFold(Fold fold) {
        if (this.lastFold == null) {
            return true;
        }
        return this.lastFold.getType().equals(fold.getType()) && this.lastFold.getHigh() < fold.getHigh();
    }

    /* Play the fold */
    public void playFold(Fold fold) {
        this.folds.add(fold);
        this.lastFold = fold;
    }

    public boolean isTurnWin() {
        return this.lastPlayer != null && this.lastPlayer.equals(this.currentPlayer);
    }

    /* Define the next player */
    public void nextPlayer() {
        if (this.currentPlayer == null) {
            this.currentPlayer = this.mahjongCard.getOwner();
        } else {
            Player.Orientation or = this.currentPlayer.getOrientation().getNext();
            this.currentPlayer = this.players.get(or.getPos());
        }
    }

    public void setGame(String game) {
        this.game = game;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public List<Card> getCards() {
        return cards;
    }

    public Fold getLastFold() {
        return lastFold;
    }

    public void setLastFold(Fold lastFold) {
        this.lastFold = lastFold;
    }

    public List<Fold> getFolds() {
        return folds;
    }

    public void setFolds(List<Fold> folds) {
        this.folds = folds;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(Player currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getGame() {
        return game;
    }

    public Player getLastPlayer() {
        return lastPlayer;
    }
}
