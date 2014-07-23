package tichu.service.cache.game;

import com.google.inject.ImplementedBy;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;

import java.util.Map;
import java.util.Set;

/**
 *
 */
@ImplementedBy(GameCacheImpl.class)
public interface GameCache {
    boolean createGame(Game game) throws Exception;

    boolean saveGame(Game game);

    Game getGame(String name);

    Game getGameByTokenPlayer(String token);

    boolean removeGame(String name);

    void close();

    void addPlayer(Player player, Game game);

    Player getPlayer(String token);

    /**
     * Player say frequently he's here with a HB let the player reconnect at non empty chair when server crash
     * 
     * @param token
     */
    void heartbeat(String token);

    Long lastHeartbeat(Player player);

    Set<String> getGames();

    Map<Integer, Set<String>> getFreeChairGames();

}
