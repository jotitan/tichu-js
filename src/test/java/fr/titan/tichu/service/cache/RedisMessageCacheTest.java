package fr.titan.tichu.service.cache;

import fr.titan.tichu.Orientation;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.RedisMessageCache;
import fr.titan.tichu.service.mock.TichuWebSocketMock;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import redis.embedded.RedisServer;

import java.util.ArrayList;

/**
 *
 */
public class RedisMessageCacheTest {

    private RedisServer redisServer;

    @Before
    public void runRedisTest() throws Exception {
        redisServer = new RedisServer(6379);
        redisServer.start();
    }

    @Test
    public void testGetCache() {
        MessageCache messageCache = CacheFactory.getMessageCache("localhost", 6379);
        Assert.assertTrue(messageCache instanceof RedisMessageCache);
        Game game = new Game("Game test");

        Player player = new Player(game, Orientation.O);
        player.createToken(game.getGame());

        TichuWebSocketMock wsMock = new TichuWebSocketMock(player.getName(), new ArrayList<ResponseType>());

        messageCache.register(game.getGame(), player.getToken(), wsMock);

        messageCache.sendMessage(game, player, ResponseType.BAD_FOLD, "");
        messageCache.unregister(player.getToken());

    }

    @After
    public void stopRedisTest() throws Exception {
        redisServer.stop();
    }

}
