package fr.titan.tichu.service.cache;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;

/**
 *
 */
public class MemoryGameCache implements GameCache {

    private Logger logger = LoggerFactory.getLogger(MemoryGameCache.class);

    private HashMap<String, Game> gameByNames = new HashMap<>();

    private HashMap<String, Player> playersByToken = new HashMap<>();

    @Override
    public Game getGame(String name) {
        return gameByNames.get(name);
    }

    @Override
    public boolean saveGame(Game game) {
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

    @Override
    public void heartbeat(Player player) {
    }

    @Override
    public Long lastHeartbeat(Player player) {
        return null;
    }
}
