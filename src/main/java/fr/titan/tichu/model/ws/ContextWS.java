package fr.titan.tichu.model.ws;

import java.util.List;

import com.google.common.collect.Lists;
import fr.titan.tichu.model.Score;

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
