package fr.titan.tichu.service;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.ChangeCards;
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
        private String player;
        public TichuWebSocketMock(String player){
            this.player = player;
        }
        @Override
        public void send(ResponseType type, Object object) {
            System.out.println("MOCK " + this.player + " " + type);
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
        player2.setClientCommunication(new TichuWebSocketMock(player2.getName()));
        Assert.assertEquals(PlayerStatus.AUTHENTICATE, player2.getPlayerStatus());
        gameService.connectGame(player2.getToken());
        Assert.assertEquals(PlayerStatus.CONNECTED, player2.getPlayerStatus());

        Assert.assertFalse(game.canPlay());

        Player player1 = gameService.joinGame(game.getGame(), "joueur1", null);
        player1.setClientCommunication(new TichuWebSocketMock(player1.getName()));
        gameService.connectGame(player1.getToken());

        Player player3 = gameService.joinGame(game.getGame(), "joueur3", null);
        player3.setClientCommunication(new TichuWebSocketMock(player3.getName()));
        gameService.connectGame(player3.getToken());

        Player player4 = gameService.joinGame(game.getGame(), "joueur4", null);
        player4.setClientCommunication(new TichuWebSocketMock(player4.getName()));
        gameService.connectGame(player4.getToken());

        Assert.assertTrue(game.canPlay());

        ChangeCards cc1 = new ChangeCards();
        cc1.setToLeft(player1.getCards().get(0).toCardWS());
        cc1.setToRight(player1.getCards().get(1).toCardWS());
        cc1.setToPartner(player1.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player1, cc1);
        ChangeCards cc2 = new ChangeCards();
        cc2.setToLeft(player1.getCards().get(0).toCardWS());
        cc2.setToRight(player1.getCards().get(1).toCardWS());
        cc2.setToPartner(player1.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player2, cc2);
        ChangeCards cc3 = new ChangeCards();
        cc3.setToLeft(player1.getCards().get(0).toCardWS());
        cc3.setToRight(player1.getCards().get(1).toCardWS());
        cc3.setToPartner(player1.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player3, cc3);
        ChangeCards cc4 = new ChangeCards();
        cc4.setToLeft(player1.getCards().get(0).toCardWS());
        cc4.setToRight(player1.getCards().get(1).toCardWS());
        cc4.setToPartner(player1.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player4, cc4);


    }
}