package fr.titan.tichu.service.cache;

import java.io.IOException;
import java.util.Properties;

import com.google.inject.Singleton;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage Redis cache to save game information
 */
@Singleton
public class GameCacheImpl implements GameCache {
    private GameCache gameCache;

    final private Logger logger = LoggerFactory.getLogger(GameCacheImpl.class);

    public GameCacheImpl() {
        Properties p = new Properties();
        try {
            p.load(GameCacheImpl.class.getResourceAsStream("/tichu.properties"));

        } catch (IOException ioex) {
            logger.error("Error when loading redis properties, default configuration");
        } catch (Exception e) {
        }
        String host = p.getProperty("redis.host");
        int port = Integer.valueOf(p.getProperty("redis.port"));
        try {
            gameCache = new RedisGameCache(host, port);
        } catch (Exception e) {
            logger.info("No Redis found, use memory cache instead");
            gameCache = new MemoryGameCache();
        }
    }

    @Override
    public boolean saveGame(Game game) {
        return gameCache.saveGame(game);
    }

    @Override
    public Game getGame(String name) {
        return gameCache.getGame(name);
    }

    @Override
    public Game getGameByTokenPlayer(String token) {
        return gameCache.getGameByTokenPlayer(token);
    }

    @Override
    public void removeGame(String name) {
        gameCache.removeGame(name);
    }

    @Override
    public void close() {
        gameCache.close();
    }

    @Override
    public void addPlayer(Player player, Game game) {
        gameCache.addPlayer(player, game);
    }

    @Override
    public Player getPlayer(String token) {
        return gameCache.getPlayer(token);
    }

    @Override
    public void heartbeat(Player player) {
        gameCache.heartbeat(player);
    }

    @Override
    public Long lastHeartbeat(Player player) {
        return gameCache.lastHeartbeat(player);
    }
}
