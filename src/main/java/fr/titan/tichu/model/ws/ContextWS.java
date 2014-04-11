package fr.titan.tichu.model.ws;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Represent game context when user connect
 */
public class ContextWS {
    private List<PlayerWS> players = Lists.newArrayList();
    private PlayerWS playerUser;
    private PlayerWS currentPlayer;
    private boolean gameBegin;
    private List<CardWS> cards = Lists.newArrayList();

    private ResponseType type; // Dernier evenement ?

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
}
