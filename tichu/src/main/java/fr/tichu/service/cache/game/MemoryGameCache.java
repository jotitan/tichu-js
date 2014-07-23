package tichu.service.cache.game;

import com.google.inject.Singleton;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public boolean removeGame(String game) {
        return gameByNames.remove(game) != null;
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

    @Override
    public Map<Integer, Set<String>> getFreeChairGames() {
        return null;
    }
}
