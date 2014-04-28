package fr.titan.tichu.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * User: Titan
 * Date: 29/03/14
 * Time: 11:45
 */
public class Games {

    /* Used to get game context by token player */
    private HashMap<String,Player> playersByToken = new HashMap<String, Player>();

    private HashMap<String,Game> gameByNames = new HashMap<String,Game>();

    public void addGame(Game game)throws Exception{
        if(gameByNames.containsKey(game.getGame())){
            throw new Exception("Game with name " + game.getGame() + " already exists");
        }
        gameByNames.put(game.getGame(),game);
    }

    public void addPlayerByToken(Player player){
        playersByToken.put(player.getToken(),player);
    }

    public Game getGame(String game){
        return gameByNames.get(game);
    }

    public Player getPlayerByToken(String token){
        return playersByToken.get(token);
    }
}
