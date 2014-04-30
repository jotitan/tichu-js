package fr.titan.tichu.service.cache;

import com.google.inject.ImplementedBy;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;

/**
 *
 */
@ImplementedBy(GameCacheImpl.class)
public interface GameCache {
    boolean saveGame(Game game);

    Game getGame(String name);

    Game getGameByTokenPlayer(String token);

    void removeGame(String name);

    void close();

    void addPlayer(Player player, Game game);

    Player getPlayer(String token);

    /**
     * Player say frequently he's here with a HB let the player reconnect at non empty chair when server crash
     * 
     * @param player
     */
    void heartbeat(Player player);

    Long lastHeartbeat(Player player);

}
