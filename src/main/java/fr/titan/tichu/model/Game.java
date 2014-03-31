package fr.titan.tichu.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Respresent a game
 */

public class Game {
    private String game;
    private String password;
    private List<Player> players = new ArrayList<Player>(4);

    private List<Card> cards;   // Game of cards

    public Game(){
        this.players.add(new Player(this,Player.Orientation.O));
        this.players.add(new Player(this,Player.Orientation.N));
        this.players.add(new Player(this,Player.Orientation.E));
        this.players.add(new Player(this,Player.Orientation.S));
        createCards();
    }

    public Game(String game){
        this();
        this.game = game;
    }

    public Game(String game, String password){
        this(game);
        this.password = password;
    }

    public String getGame() {
        return game;
    }

    public void createCards(){
        if(cards == null){
            cards = new ArrayList<Card>();
            for(String color : new String[]{"green","blue","red","black"}){
                for(int i = 2 ; i < 15 ; i++){
                    cards.add(new ValueCard(i,color));
                }
            }
            cards.add(SpecialCard.SpecialCardFactory.Mahjong.get());
            cards.add(SpecialCard.SpecialCardFactory.Dogs.get());
            cards.add(SpecialCard.SpecialCardFactory.Phoenix.get());
            cards.add(SpecialCard.SpecialCardFactory.Dragon.get());

        }
    }

    private void resetCards(){
        for(Player player : this.players){
            player.resetCards();
        }
        for(Card card : this.cards){
            card.setOwner(null);
        }
    }

    /**
     * Distribute cards to 4 people
     * @return
     */
    public void distribute(){
        // Reset cards of player
        resetCards();
        List<Card> copyCards = new ArrayList<Card>(this.cards);

        for(Player player : players){
            for(int i = 0 ; i < 14; i ++){
                int posCard = (int)Math.round(Math.random()*1000)%copyCards.size();
                player.addCard(copyCards.get(posCard));
                copyCards.remove(posCard);
            }
        }
    }

    /**
     * When all player are connected
     * @return
     */
    public boolean canPlay(){
        for(Player player : this.players){
            if(!player.getPlayerStatus().equals(PlayerStatus.CONNECTED)){
                return false;
            }
        }
        return true;
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

    public List<Card> getCards() {
        return cards;
    }
}
