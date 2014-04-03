package fr.titan.tichu.model.ws;

import com.google.common.collect.Lists;

import java.util.List;

/**
 *
 */
public class GameWS {
    private String game;
    private List<PlayerWS> players = Lists.newArrayList();

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public List<PlayerWS> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerWS> players) {
        this.players = players;
    }

    public void addPlayer(PlayerWS player){
        this.players.add(player);
    }
}
