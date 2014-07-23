package fr.tichu.model.ws;

import com.google.common.collect.Lists;
import fr.titan.tichu.model.Score;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GameWS {
    private String game;
    private List<PlayerWS> players = Lists.newArrayList();
    private Score score1;
    private Score score2;

    private boolean password = false;

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

    public void addPlayer(PlayerWS player) {
        this.players.add(player);
    }

    public Score getScore1() {
        return score1;
    }

    public void setScore1(Score score1) {
        this.score1 = score1;
    }

    public Score getScore2() {
        return score2;
    }

    public void setScore2(Score score2) {
        this.score2 = score2;
    }

    public boolean isPassword() {
        return password;
    }

    public void setPassword(boolean password) {
        this.password = password;
    }
}
