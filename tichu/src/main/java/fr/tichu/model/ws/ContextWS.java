package tichu.model.ws;

import com.google.common.collect.Lists;
import fr.titan.tichu.model.Card;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.Score;

import java.util.List;

/**
 * Represent game context when user connect
 */
public class ContextWS {
    /* List of player on table */
    private List<PlayerWS> players = Lists.newArrayList();
    private PlayerWS playerUser;
    private PlayerWS currentPlayer;
    private boolean gameBegin;
    /* Cards in hands players */
    private List<CardWS> cards = Lists.newArrayList();

    /* Fold on table */
    private List<Fold> folds = Lists.newArrayList();

    private List<Score> scoreTeam1;
    private List<Score> scoreTeam2;

    /* Last event */
    private ResponseType type;

    public ContextWS() {
    }

    public ContextWS(Game game, Player player) {
        setFolds(game.getFolds());
        /* Compact players */
        for (Player p : game.getPlayers()) {
            PlayerWS playerWS = p.getPlayerWS();
            playerWS.setLastFoldIsCall(p.isLastFoldIsCall());
            playerWS.setNbCard(p.getNbcard() > 0 ? p.getDistributeAllCards() ? p.getNbcard() : 9 : 0);
            playerWS.setConnected(p.isConnected());
            addPlayer(playerWS);

            if (p.equals(game.getCurrentPlayer())) {
                setType(ResponseType.NEXT_PLAYER);
                setCurrentPlayer(playerWS);
            }
            /* Context for user */
            if (p.equals(player)) {
                setPlayerUser(playerWS);
                if (player.getCards() != null && player.getCards().size() > 0) {
                    List<Card> cards = player.getCards().size() != 14 || player.getDistributeAllCards() ? player.getCards() : player.getCards().subList(
                            0, 9);
                    for (Card card : cards) {
                        addCard(card.toCardWS());
                    }
                    if (!player.getDistributeAllCards()) {
                        setType(ResponseType.DISTRIBUTION_PART1);
                    } else {
                        if (getCurrentPlayer() == null && !player.getChangeCards().isComplete()) {
                            setType(ResponseType.CHANGE_CARD_MODE);
                        }
                    }
                }
            }
        }
        /* Send score */
        setScoreTeam1(game.getTeam1().getScores());
        setScoreTeam2(game.getTeam2().getScores());
    }

    public List<PlayerWS> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerWS> players) {
        this.players = players;
    }

    public boolean isGameBegin() {
        return gameBegin;
    }

    public void setGameBegin(boolean gameBegin) {
        this.gameBegin = gameBegin;
    }

    public List<CardWS> getCards() {
        return cards;
    }

    public void setCards(List<CardWS> cards) {
        this.cards = cards;
    }

    public void addPlayer(PlayerWS player) {
        this.players.add(player);
    }

    public void addCard(CardWS card) {
        this.cards.add(card);
    }

    public PlayerWS getPlayerUser() {
        return playerUser;
    }

    public void setPlayerUser(PlayerWS playerUser) {
        this.playerUser = playerUser;
    }

    public PlayerWS getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(PlayerWS currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public ResponseType getType() {
        return type;
    }

    public void setType(ResponseType type) {
        this.type = type;
    }

    public List<Fold> getFolds() {
        return folds;
    }

    public void setFolds(List<Fold> folds) {
        this.folds = folds;
    }

    public List<Score> getScoreTeam1() {
        return scoreTeam1;
    }

    public void setScoreTeam1(List<Score> scoreTeam1) {
        this.scoreTeam1 = scoreTeam1;
    }

    public List<Score> getScoreTeam2() {
        return scoreTeam2;
    }

    public void setScoreTeam2(List<Score> scoreTeam2) {
        this.scoreTeam2 = scoreTeam2;
    }
}
