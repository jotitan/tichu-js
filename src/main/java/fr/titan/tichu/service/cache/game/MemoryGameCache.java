package fr.titan.tichu.service.cache.game;

import java.util.HashMap;
import java.util.Set;

import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;

/**
 *
 */
@Singleton
public class MemoryGameCache implements GameCache {

    private Logger logger = LoggerFactory.getLogger(MemoryGameCache.class);

    private static HashMap<String, Game> gameByNames = new HashMap<>();

    private static HashMap<String, Player> playersByToken = new HashMap<>();

    @Override
    public Game getGame(String name) {
        return gameByNames.get(name);
    }

    @Override
    public boolean createGame(Game game) throws Exception {
        return saveGame(game);
    }

    @Override
    public boolean saveGame(Game game) {
        if (gameByNames.containsKey(game.getGame())) {

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
    public void heartbeat(String token) {
    }

    @Override
    public Long lastHeartbeat(Player player) {
        return null;
    }

    @Override
    public Set<String> getGames() {
        return gameByNames.keySet();
    }
}
