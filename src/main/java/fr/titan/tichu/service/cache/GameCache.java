package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;

/**
 *
 */
public interface GameCache {
    boolean addGame(Game game);

    Game getGame(String name);

    Game getGameByTokenPlayer(String token);

    void removeGame(String name);

    void close();

    void addPlayer(Player player, Game game);

    Player getPlayer(String token);


}
