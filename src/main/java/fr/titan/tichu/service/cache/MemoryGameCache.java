package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;

import java.util.HashMap;

/**
 *
 */
public class MemoryGameCache implements GameCache{
    private HashMap<String,Game> gameByNames = new HashMap<String,Game>();

    private HashMap<String,Player> playersByToken = new HashMap<String, Player>();

    @Override
    public Game getGame(String name) {
        return gameByNames.get(name);
    }

    @Override
    public boolean addGame(Game game) {
        if(gameByNames.containsKey(game.getGame())){
            return false;
        }
        gameByNames.put(game.getGame(),game);
        return true;
    }

    public void removeGame(String game){
        gameByNames.remove(game);
    }

    public void close(){}

    @Override
    public void addPlayer(Player player) {
        playersByToken.put(player.getToken(),player);
    }

    @Override
    public Player getPlayer(String token) {
        return playersByToken.get(token);
    }
}
