package fr.titan.tichu.service.cache.game;

import java.io.IOException;
import java.util.Properties;
import java.util.Set;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.service.cache.RedisConfiguration;
import fr.titan.tichu.service.cache.message.MemoryMessageCache;
import fr.titan.tichu.service.cache.message.RedisMessageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Choose good implementation of cache : redis (shared) if available or memory (not shared)
 */
@Singleton
public class GameCacheImpl implements GameCache {
    private GameCache gameCache;

    final private Logger logger = LoggerFactory.getLogger(GameCacheImpl.class);

    @Inject
    public GameCacheImpl(RedisConfiguration redisConfiguration) {
        Object[] config = redisConfiguration.getConfiguration();
        if (config != null) {
            try {
                gameCache = new RedisGameCache((String) config[0], (Integer) config[1]);
            } catch (Exception e) {
                setDefault((String) config[0], (Integer) config[1]);
            }
        } else {
            setDefault(null, null);
        }
    }

    private void setDefault(String host, Integer port) {
        logger.info("No Redis found, use memory cache instead (" + host + ":" + port + ")");
        gameCache = new MemoryGameCache();
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
    public void heartbeat(String token) {
        gameCache.heartbeat(token);
    }

    @Override
    public Long lastHeartbeat(Player player) {
        return gameCache.lastHeartbeat(player);
    }

    @Override
    public Set<String> getGames() {
        return gameCache.getGames();
    }

    @Override
    public boolean createGame(Game game) throws Exception {
        return gameCache.createGame(game);
    }
}
