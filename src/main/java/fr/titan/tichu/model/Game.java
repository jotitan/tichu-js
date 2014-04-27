package fr.titan.tichu.model;

import com.google.common.collect.Lists;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.GameWS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;

import fr.titan.tichu.Orientation;
import fr.titan.tichu.exception.CheatException;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.GameWS;

/**
 * Respresent a game
 */

public class Game implements Serializable {
    /* Name of the game */
    private String game;
    /* Password to access the game */
    private String password;
    /* 4 players of the game */
    private List<Player> players = new ArrayList<Player>(4);

    private Team team1;

    private Team team2;

    private int orderEndRound = 0;

    private CardPackage cardPackage = new CardPackage();

    /* Card requested after mahjong card */
    private Integer mahjongValue = null;

    /* List of fold on the table (composing a round) */
    private LinkedList<Fold> folds = Lists.newLinkedList();
    /* Cards on table */
    private List<Card> cardOfFolds = Lists.newArrayList();
    /* Previous played fold on table */
    private List<Card> lastFold = Lists.newArrayList();
    /* Wait this player to play */
    private Player currentPlayer = null;

    /* Last player who make a fold */
    private Player lastPlayer = null;

    public Game() {
        for (Orientation or : Orientation.values()) {
            this.players.add(or.getPos(), new Player(this, or));
        }
        team1 = new Team(this.players.get(0), this.players.get(2));
        team2 = new Team(this.players.get(1), this.players.get(3));
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
        cardPackage.createCards();
    }

    public void newTurn() {
        if (this.lastPlayer != null) {
            this.lastPlayer.addCardsOfFold(this.cardOfFolds);
        }
        this.currentPlayer = this.lastPlayer;
        this.lastPlayer = null;
        this.cardOfFolds = Lists.newArrayList();
        this.lastFold = Lists.newArrayList();
        this.folds = Lists.newLinkedList();
    }

    /* Init the game for the round */
    public void newRound() {
        saveScore();
        newTurn();
        orderEndRound = 0;
        this.lastPlayer = null;
        this.currentPlayer = null;
        distribute();
    }

    public void newGame() {
        this.team1.resetScore();
        this.team2.resetScore();
        newRound();
    }

    public Team getWinner() {
        if (team1.hasWon() && team2.hasWon()) {
            return team1.getScore() > team2.getScore() ? team1 : team2;
        }
        return team1.hasWon() ? team1 : team2.hasWon() ? team2 : null;
    }

    public GameWS saveScore() {
        if (players == null || players.size() != 4) {
            return null;
        }
        List<Player> orderPlayers = getPlayersByOrder();
        if (orderPlayers.size() == 0) {
            return null;
        }
        /* First take folds of last */
        orderPlayers.get(0).addCardsOfFold(orderPlayers.get(3).getCardOfFolds());
        /* The before last take card in hand of the last */
        orderPlayers.get(2).addCardsOfFold(orderPlayers.get(3).getCards());

        GameWS game = new GameWS();

        game.setScore1(team1.buildScore(team2.isCapot()));
        game.setScore2(team2.buildScore(team1.isCapot()));
        return game;
    }

    public boolean isRoundEnded() {
        int nbEnded = 0;
        for (Player player : players) {
            nbEnded += player.ended() ? 1 : 0;
        }
        return nbEnded >= 3;
    }

    public List<Player> getPlayersByOrder() {
        List<Player> players = new ArrayList<Player>(4);
        for (Player player : players) {
            players.add(player.getEndPosition(), player);
        }
        return players;
    }

    public Player getLosePlayer() {
        for (Player player : players) {
            if (player.getNbcard() != 0 || player.getEndPosition() == 3) {
                return player;
            }
        }
        return null; // impossible:
    }

    private void resetCards() {
        for (Player player : this.players) {
            player.resetCards();
        }
        cardPackage.resetCards();
    }

    /**
     * Distribute cards to 4 people
     * 
     * @return
     */
    protected void distribute() {
        // Reset cards of player
        resetCards();
        List<Card> copyCards = cardPackage.getCopy();

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

    public boolean isLastIsDog() {
        return isDogPresent(this.lastFold);
    }

    private boolean isDogPresent(List<Card> cards) {
        for (Card card : cards) {
            if (card.isDog()) {
                return true;
            }
        }
        return false;
    }

    private boolean isMajhongPresent(List<Card> cards) {
        if (this.mahjongValue == null) {
            return false;
        }
        for (Card card : cards) {
            if (card.getValue() == this.mahjongValue) {
                return true;
            }
        }
        return false;
    }

    /* Verify if fold can be play */
    /* Change the return to explain problem */
    public boolean verifyFold(Fold fold, Player player) throws CheatException {
        // Verify if the mahjongValue contract is respected
        List<Card> cards = cardPackage.getCards(fold.getCards());
        if (this.folds.isEmpty()) {
            if (isDogPresent(cards) && (cards.size() != 1)) {
                return false;
            }
            if (this.mahjongValue == null) {
                return true;
            }
            if (!player.hasCard(this.mahjongValue)) {
                return true;
            } else {
                return false; // can play mahjong card
            }
        } else {
            // Verify that card belongs to player
            for (Card card : cards) {
                if (card.getOwner() == null || !card.getOwner().equals(player)) {
                    throw new CheatException("Card " + card.getValue() + " not belong to user");
                }
            }
            // Can't play dogs when fold are on table
            if (isDogPresent(cards)) {
                return false;
            }
            Fold last = this.folds.getLast();
            if (this.mahjongValue != null && player.canPlayMahjongValue(this.mahjongValue, last.getType(), last.getCards().size(), last.getHigh())
                    && !isMajhongPresent(cards)) {
                return false;
            }

            return last.getType().equals(fold.getType()) && last.getHigh() < fold.getHigh() && last.getNb() == fold.getNb();
        }
    }

    public boolean verifyCall(Player player) {
        if (this.mahjongValue == null) {
            return true;
        }

        if (this.folds.isEmpty()) {
            // Impossible to call when first
        } else {
            Fold last = this.folds.getLast();
            return !player.canPlayMahjongValue(this.mahjongValue, last.getType(), last.getCards().size(), last.getHigh());
        }
        return true;
    }

    public boolean verifyBomb(Fold bomb) {
        if (this.folds.isEmpty() || !this.folds.getLast().isBomb()) {
            return true;
        }
        /* Straight bomb higher than square bomb */
        if (bomb.getType().equals(FoldType.STRAIGHTBOMB) && this.folds.getLast().getType().equals(FoldType.SQUAREBOMB)) {
            return true;
        }
        /* Higher if bomb is same type and higher combinaison. Otherwise, if bomb is square and the last straight, not good */
        return bomb.getType().equals(this.folds.getLast().getType()) && bomb.getHigh() > this.folds.getLast().getHigh();
    }

    /* Play the fold */
    public void playFold(Player player, Fold fold) {
        this.lastPlayer = player;
        this.lastFold = cardPackage.getCards(fold.getCards());
        this.cardOfFolds.addAll(lastFold);

        this.folds.addLast(fold);

        // Check if mahjong contract is ok
        if (this.mahjongValue != null) {
            if (isMajhongPresent(this.lastFold)) {
                this.mahjongValue = null;
            }
        }
        if (fold.getMahjongValue() != null) {
            this.mahjongValue = fold.getMahjongValue();
        }
    }

    public boolean isTurnWin() {
        return this.lastPlayer != null && this.lastPlayer.equals(this.currentPlayer);
    }

    public void nextPlayer() throws Exception {
        if (this.currentPlayer != null && isDogPresent(this.lastFold)) {
            this.currentPlayer = this.players.get(this.currentPlayer.getOrientation().getLeft().getPos());
        }
        searchNextPlayer();
    }

    /* Define the next player */
    private void searchNextPlayer() throws Exception {
        if (this.orderEndRound >= 2) {
            throw new Exception("End of game");
        }
        if (this.currentPlayer == null) {
            /* First round */
            this.currentPlayer = this.getCardPackage().getMahjongCard().getOwner();
        } else {
            Orientation or = this.currentPlayer.getOrientation().getNext();
            this.currentPlayer = this.players.get(or.getPos());
            if (this.currentPlayer.ended()) {
                searchNextPlayer();
            }
        }
    }

    public Player getPartner(Player player) {
        return this.players.get(player.getOrientation().getFace().getPos());
    }

    public Player getPlayer(Orientation orientation) {
        return this.players.get(orientation.getPos());
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

    public CardPackage getCardPackage() {
        return cardPackage;
    }

    public Fold getLastFold() {
        return folds.isEmpty() ? null : folds.getLast();
    }

    public List<Card> getCardOfFolds() {
        return cardOfFolds;
    }

    public void setCardOfFolds(List<Card> cardOfFolds) {
        this.cardOfFolds = cardOfFolds;
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

    public int getAndIncreaseEndPosition() {
        return this.orderEndRound++;
    }

    public GameWS toGameWS() {
        GameWS game = new GameWS();
        game.setGame(this.game);
        for (Player player : this.players) {
            game.addPlayer(player.getPlayerWS());
        }
        return game;
    }

    public List<Fold> getFolds() {
        return folds;
    }
}
