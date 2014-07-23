package fr.titan.tichu.service.cache;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import redis.embedded.RedisServer;

import com.google.common.collect.Lists;

import fr.titan.tichu.Orientation;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.message.MemoryMessageCache;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.RedisMessageCache;
import fr.titan.tichu.service.mock.TichuWebSocketMock;

/**
 * Test redis implementation of chat
 */
public class ChatRedisTest {

    private RedisServer redisServer;

    @Before
    public void startRedis() throws Exception {
        redisServer = new RedisServer(7500);

    }

    @Test
    public void testCommunicateRedis() throws Exception {
        redisServer.start();
        MessageCache messageCache = CacheFactory.getMessageCache("localhost", 7500);
        Assert.assertTrue(messageCache instanceof RedisMessageCache);

        testCommunicate(messageCache);
    }

    @Test
    public void testCommunicateMemory() throws Exception {
        redisServer.stop();
        CacheFactory.resetCaches();
        MessageCache messageCache = CacheFactory.getMessageCache("localhost", 7500);
        Assert.assertTrue(messageCache instanceof MemoryMessageCache);

        testCommunicate(messageCache);
    }

    private void testCommunicate(MessageCache messageCache) {
        Game game = new Game("Game");
        Player player = new Player(game, Orientation.O);
        player.createToken(game.getGame());
        game.getPlayers().set(2, player);

        List<ResponseType> responses = Lists.newLinkedList();

        messageCache.registerChat(player, new TichuWebSocketMock(player.getName(), responses));
        try {
            Thread.sleep(200); // Wait async message
        } catch (Exception e) {
        }
        messageCache.sendMessageToAll(game, ResponseType.CHAT, "Message de test");
        try {
            Thread.sleep(200); // Wait async message
        } catch (Exception e) {
        }
        Assert.assertEquals(1, responses.size());
        messageCache.sendMessageToAll(game, ResponseType.BAD_FOLD, "");
        try {
            Thread.sleep(200); // Wait async message
        } catch (Exception e) {
        }
        Assert.assertEquals("Message different not not send", 1, responses.size());
    }

    @After
    public void stopRedis() throws Exception {
        redisServer.stop();
    }
}
