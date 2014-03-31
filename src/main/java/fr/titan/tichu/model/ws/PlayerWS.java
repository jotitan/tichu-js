package fr.titan.tichu.model.ws;

import fr.titan.tichu.model.Player;

/**
 * Simple info about player
 */
public class PlayerWS {
    private String name;
    private Player.Orientation orientation;

    public PlayerWS() {
    }

    public PlayerWS(String name, Player.Orientation orientation) {
        this.name = name;
        this.orientation = orientation;
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
}
