package fr.titan.tichu.model;

import fr.titan.tichu.ws.TichuWebSocket;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Titan
 * Date: 29/03/14
 * Time: 11:47
 */
public class Player {
    private List<Card> cards = new ArrayList<Card>();
    private Orientation orientation;
    private String name;
    private PlayerStatus playerStatus = PlayerStatus.FREE_CHAIR;
    private String token; // To verify identity
    private Game game;
    private TichuWebSocket webSocket;

    enum Orientation{
        O,E,S,N;
    }

    public Player(Game game,Orientation orientation){
        this.game = game;
        this.orientation = orientation;
    }

    public int getNbcard(){
        return cards.size();
    }

    public void addCard(Card card){
        this.cards.add(card);
        card.setOwner(this);
    }

    public void resetCards(){
        cards = new ArrayList<Card>();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        this.cards = cards;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PlayerStatus getPlayerStatus() {
        return playerStatus;
    }

    public void setPlayerStatus(PlayerStatus playerStatus) {
        this.playerStatus = playerStatus;
    }

    public void createToken(String game){
        try{
            String base = game + orientation + System.currentTimeMillis();
            MessageDigest md5 = MessageDigest.getInstance("md5");
            this.token = new String(md5.digest(base.getBytes()));
        }   catch(Exception e){

        }
    }

    public String getToken() {
        return token;
    }

    public TichuWebSocket getWebSocket() {
        return webSocket;
    }

    public void setWebSocket(TichuWebSocket webSocket) {
        this.webSocket = webSocket;
    }
}
