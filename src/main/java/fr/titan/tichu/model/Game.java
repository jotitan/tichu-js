package fr.titan.tichu.model;

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

    /** When game is visible by anyone */
    public boolean publicGame = true;

    /* Last player who make a fold */
    private Player lastPlayer = null;

    public Game() {
        for (Orientation or : Orientation.values()) {
            this.players.add(or.getPos(), new Player(this, or));
        }
        team1 = new Team(this.players.get(0), this.players.get(2), 0);
        team2 = new Team(this.players.get(1), this.players.get(3), 1);
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
            if (this.lastPlayer.ended() || isDogPresent(this.lastFold)) {
                try {
                    this.nextPlayer();
                    this.lastPlayer = this.currentPlayer;
                } catch (Exception e) {
                    // Impossible cause end game test before
                }
            }
        }
        this.currentPlayer = this.lastPlayer;
        this.lastPlayer = null;
        this.cardOfFolds = Lists.newArrayList();
        this.lastFold = Lists.newArrayList();
        this.folds = Lists.newLinkedList();
        for (Player player : players) {
            player.newTurn();
        }
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

    public Player getPlayerByToken(String token) {
        for (Player player : players) {
            if (player.getToken() != null && player.getToken().equals(token)) {
                return player;
            }
        }
        return null;
    }

    public Team isCapot() {
        return orderEndRound == 2 ? team1.hasFinished() ? team1 : team2.hasFinished() ? team2 : null : null;
    }

    public boolean isRoundEnded() {
        return orderEndRound >= 3 || isCapot() != null;
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
            if (!player.getPlayerStatus().equals(PlayerStatus.CONNECTED) || player.getNbcard() > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean isLastIsDog() {
        return isDogPresent(this.lastFold);
    }

    public boolean isLastIsDragon() {
        return this.lastFold != null && this.lastFold.size() == 1 && this.lastFold.contains(cardPackage.getDragonCard());
    }

    private boolean isDogPresent(List<Card> cards) {
        return cards.contains(cardPackage.getDogsCard());
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

    /**
     * Verify if fold can be play
     * 
     * @param fold
     * @param player
     * @return type of error : * No dog in a fold with many cards * have to play mahjong * dog play in turn * higher fold
     * @throws CheatException
     */
    public AnalyseFoldType verifyFold(Fold fold, Player player) throws CheatException {
        // Verify if the mahjongValue contract is respected
        List<Card> cards = cardPackage.getCards(fold.getCards());
        if (this.folds.isEmpty()) {
            if (isDogPresent(cards) && (cards.size() != 1)) {
                return AnalyseFoldType.DOG_IMPOSSIBLE;
            }
            if (this.mahjongValue == null) {
                return AnalyseFoldType.OK;
            }
            if (!player.hasCard((float) this.mahjongValue)) {
                return AnalyseFoldType.OK;
            } else {
                return AnalyseFoldType.MUST_PLAY_MAHJONG_VALUE; // can play mahjong card
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
                return AnalyseFoldType.NO_DOG_IN_TURN;
            }
            Fold last = this.folds.getLast();
            if (this.mahjongValue != null
                    && player.canPlayMahjongValue((float) this.mahjongValue, last.getType(), last.getHigh(), last.getCards().size())
                    && !isMajhongPresent(cards)) {
                return AnalyseFoldType.MUST_PLAY_MAHJONG_VALUE;
            }

            return last.getType().equals(fold.getType()) && last.getHigh() < fold.getHigh() && last.getNb() == fold.getNb() ? AnalyseFoldType.OK
                    : AnalyseFoldType.FOLD_TOO_LOW;
        }
    }

    public AnalyseFoldType verifyCall(Player player) {
        if (this.mahjongValue == null) {
            return AnalyseFoldType.OK;
        }

        if (this.folds.isEmpty()) {
            // Impossible to call when first
            return AnalyseFoldType.NO_CALL_WHEN_FIRST;
        } else {
            Fold last = this.folds.getLast();
            return player.canPlayMahjongValue((float) this.mahjongValue, last.getType(), last.getHigh(), last.getCards().size()) ? AnalyseFoldType.MUST_PLAY_MAHJONG_VALUE
                    : AnalyseFoldType.OK;
        }
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
            // Shift the player to the next to simulate dogs behaviour
            this.currentPlayer = this.players.get(this.currentPlayer.getOrientation().getLeft().getPos());
        }
        searchNextPlayer();
    }

    /* Define the next player */
    private void searchNextPlayer() throws Exception {
        if (isRoundEnded()) {
            throw new Exception("End of game");
        }
        /* First round */
        if (this.currentPlayer == null) {
            this.currentPlayer = this.getCardPackage().getMahjongCard().getOwner();
        } else {
            Orientation or = this.currentPlayer.getOrientation().getNext();
            this.currentPlayer = this.players.get(or.getPos());
            /* If last player has just ended, game stop on him */
            if (this.currentPlayer.equals(this.lastPlayer) && this.currentPlayer.ended()) {
                return;
            }
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
        if (password != null && !"".equals(password.trim())) {
            game.setPassword(true);
        }
        return game;
    }

    public int getConnectedPlayers() {
        int nb = 0;
        for (Player player : players) {
            nb += player.isConnected() ? 1 : 0;
        }
        return nb;
    }

    public List<Fold> getFolds() {
        return folds;
    }

    public Team getTeam1() {
        return team1;
    }

    public Team getTeam2() {
        return team2;
    }

    public boolean isPublicGame() {
        return publicGame;
    }

    public void setPublicGame(boolean publicGame) {
        this.publicGame = publicGame;
    }

}
