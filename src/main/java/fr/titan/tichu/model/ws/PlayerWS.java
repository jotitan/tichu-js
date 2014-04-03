package fr.titan.tichu.model.ws;

import fr.titan.tichu.model.Player;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Simple info about player
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlayerWS {
    private String name;
    private Player.Orientation orientation;
    private String token;
    private int orderEnd = -1;

    public PlayerWS() {
    }

    public PlayerWS(String name, Player.Orientation orientation) {
        this.name = name;
        this.orientation = orientation;
    }

    public PlayerWS(String name, Player.Orientation orientation, int orderEnd) {
        this(name, orientation);
        this.orderEnd = orderEnd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Player.Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Player.Orientation orientation) {
        this.orientation = orientation;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "Player :" + "orientation=" + orientation + ", name=" + name;
    }
}