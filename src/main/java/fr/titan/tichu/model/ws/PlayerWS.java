package fr.titan.tichu.model.ws;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import fr.titan.tichu.Orientation;
import fr.titan.tichu.model.AnnonceType;

/**
 * Simple info about player
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PlayerWS {
    private String name;
    private Orientation orientation;
    private String token;
    private int nbCard;
    private boolean connected;
    private int orderEnd = -1;
    private AnnonceType annonce;

    public PlayerWS() {
    }

    public PlayerWS(String name, Orientation orientation) {
        this.name = name;
        this.orientation = orientation;
    }

    public PlayerWS(String name, Orientation orientation, int orderEnd) {
        this(name, orientation);
        this.orderEnd = orderEnd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getNbCard() {
        return nbCard;
    }

    public void setNbCard(int nbCard) {
        this.nbCard = nbCard;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public AnnonceType getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceType annonce) {
        this.annonce = annonce;
    }

    @Override
    public String toString() {
        return "Player :" + "orientation=" + orientation + ", name=" + name;
    }
}
