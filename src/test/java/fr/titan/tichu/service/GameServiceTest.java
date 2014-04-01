package fr.titan.tichu.service;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.ResponseType;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * User: Titan Date: 31/03/14 Time: 21:16
 */
public class GameServiceTest {

    private GameService gameService;

    class MockGameService extends GameService {
        @Override
        protected void broadCast(Game game, ResponseType type, Object object) {
            System.out.println("MOCK BROADCAST " + game.getGame() + " " + type + " : " + object);
        }
    }

    class TichuWebSocketMock implements TichuClientCommunication {
        @Override
        public void send(ResponseType type, Object object) {
            System.out.println("MOCK " + " " + type);
        }
    }

    @Before
    public void init() {
        gameService = new MockGameService();
    }

    @Test
    public void testGame() throws Exception {
        Game game = gameService.createGame(new GameRequest("game1", "joueur1", "joueur2", "joueur3", "joueur4"));

        Player player2 = gameService.joinGame(game.getGame(), "joueur2", null);
        player2.setClientCommunication(new TichuWebSocketMock());
        Assert.assertEquals(PlayerStatus.AUTHENTICATE, player2.getPlayerStatus());
        gameService.connectGame(player2.getToken());
        Assert.assertEquals(PlayerStatus.CONNECTED, player2.getPlayerStatus());

        Assert.assertFalse(game.canPlay());

        Player player1 = gameService.joinGame(game.getGame(), "joueur1", null);
        player1.setClientCommunication(new TichuWebSocketMock());
        gameService.connectGame(player1.getToken());

        Player player3 = gameService.joinGame(game.getGame(), "joueur3", null);
        player3.setClientCommunication(new TichuWebSocketMock());
        gameService.connectGame(player3.getToken());

        Player player4 = gameService.joinGame(game.getGame(), "joueur4", null);
        player4.setClientCommunication(new TichuWebSocketMock());
        gameService.connectGame(player4.getToken());

        Assert.assertTrue(game.canPlay());

    }
}