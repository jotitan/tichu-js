package fr.titan.tichu.service;

import fr.titan.tichu.TichuClientCommunication;
import fr.titan.tichu.model.FoldType;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.ResponseType;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;

/**
 * User: Titan Date: 31/03/14 Time: 21:16
 */
public class GameServiceTest {

    private MockGameService gameService;

    private LinkedList<ResponseType> responses = new LinkedList<ResponseType>();

    public ResponseType getLast() {
        return responses.getLast();
    }

    public ResponseType getResponseAt(int pos) {
        return responses.get(responses.size() - pos - 1);
    }

    class MockGameService extends GameService {

        @Override
        protected void broadCast(Game game, ResponseType type, Object object) {
            System.out.println("MOCK BROADCAST " + game.getGame() + " " + type + " : " + object);
            responses.add(type);
        }

    }

    class TichuWebSocketMock implements TichuClientCommunication {
        private String player;

        public TichuWebSocketMock(String player) {
            this.player = player;
        }

        @Override
        public void send(ResponseType type, Object object) {
            System.out.println("MOCK " + this.player + " " + type);
            responses.add(type);
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

        Assert.assertEquals(ResponseType.PLAYER_SEATED, getLast());
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
        cc2.setToLeft(player2.getCards().get(0).toCardWS());
        cc2.setToRight(player2.getCards().get(1).toCardWS());
        cc2.setToPartner(player2.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player2, cc2);
        ChangeCards cc3 = new ChangeCards();
        cc3.setToLeft(player3.getCards().get(0).toCardWS());
        cc3.setToRight(player3.getCards().get(1).toCardWS());
        cc3.setToPartner(player3.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player3, cc3);
        ChangeCards cc4 = new ChangeCards();
        cc4.setToLeft(player4.getCards().get(0).toCardWS());
        cc4.setToRight(player4.getCards().get(1).toCardWS());
        cc4.setToPartner(player4.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player4, cc4);

        Assert.assertEquals(14, player1.getNbcard());
        Assert.assertEquals(14, player2.getNbcard());
        Assert.assertEquals(14, player3.getNbcard());
        Assert.assertEquals(14, player4.getNbcard());

        // Test play
        Player p = game.getCurrentPlayer();
        Fold fold = new Fold(FoldType.SINGLE, p.getCards().get(0).toCardWS().getValue(), p.getCards().get(0).toCardWS());
        fold.setPlayer(p.getOrientation());
        gameService.playFold(p, fold);

        Assert.assertEquals(13, p.getNbcard());
        Assert.assertNotSame(p, game.getCurrentPlayer());

        gameService.callTurn(game.getCurrentPlayer());
        gameService.callTurn(game.getCurrentPlayer());
        gameService.callTurn(game.getCurrentPlayer());

        Assert.assertEquals(ResponseType.TURN_WIN, getResponseAt(1));
        Assert.assertEquals(ResponseType.NEXT_PLAYER, getLast());
        Assert.assertEquals(1, game.getCurrentPlayer().getCardOfFolds().size());

        gameService.callTurn(game.getCurrentPlayer().equals(player1) ? player2 : player1);
        Assert.assertEquals(ResponseType.NOT_YOUR_TURN, getLast());

        gameService.callTurn(game.getCurrentPlayer());
        Assert.assertEquals(ResponseType.NO_CALL_WHEN_FIRST, getLast());
    }
}