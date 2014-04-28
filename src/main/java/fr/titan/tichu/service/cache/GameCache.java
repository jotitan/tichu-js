package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;

/**
 *
 */
public interface GameCache {
    boolean addGame(Game game);

    Game getGame(String name);

    void removeGame(String name);

    void close();

    void addPlayer(Player player);

    Player getPlayer(String token);
}
