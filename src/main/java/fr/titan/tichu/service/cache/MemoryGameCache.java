package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.model.ws.ResponseWS;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 */
public class MemoryGameCache implements GameCache {

    private Logger logger = LoggerFactory.getLogger(MemoryGameCache.class);

    private HashMap<String, Game> gameByNames = new HashMap<String, Game>();

    private HashMap<String, Player> playersByToken = new HashMap<String, Player>();

    private HashMap<String, Game> gamesByToken = new HashMap<String, Game>();

    @Override
    public Game getGame(String name) {
        return gameByNames.get(name);
    }

    @Override
    public boolean addGame(Game game) {
        if (gameByNames.containsKey(game.getGame())) {
            return false;
        }
        gameByNames.put(game.getGame(), game);
        return true;
    }

    public void removeGame(String game) {
        gameByNames.remove(game);
    }

    public void close() {
    }

    @Override
    public void addPlayer(Player player, Game game) {
        playersByToken.put(player.getToken(), player);
        gameByNames.put(player.getToken(), game);
    }

    @Override
    public Player getPlayer(String token) {
        return playersByToken.get(token);
    }

    @Override
    public Game getGameByTokenPlayer(String token) {
        return gameByNames.get(token);
    }
}
