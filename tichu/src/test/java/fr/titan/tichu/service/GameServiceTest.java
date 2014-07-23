package fr.titan.tichu.service;

import fr.titan.tichu.model.FoldType;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.service.cache.CacheFactory;
import fr.titan.tichu.service.cache.message.MessageCache;
import fr.titan.tichu.service.cache.message.MessageCacheImpl;
import fr.titan.tichu.service.mock.TichuWebSocketMock;
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

    @Before
    public void init() {
        gameService = new MockGameService();
    }

    @Test
    public void testGame() throws Exception {
        Game game = gameService.createGame(new GameRequest("game1", true, "joueur1", "joueur2", "joueur3", "joueur4"));
        MessageCache messageCache = CacheFactory.getMessageCache(null, 0);

        Player player2 = gameService.joinGame(game.getGame(), "joueur2", null);
        messageCache.register(game.getGame(), player2.getToken(), new TichuWebSocketMock(player2.getName(), responses));
        Assert.assertEquals(PlayerStatus.AUTHENTICATE, player2.getPlayerStatus());
        gameService.connectGame(player2.getToken());

        Assert.assertEquals(ResponseType.PLAYER_SEATED, getLast());
        Assert.assertEquals(PlayerStatus.CONNECTED, player2.getPlayerStatus());

        Assert.assertFalse(game.canPlay());

        Player player1 = gameService.joinGame(game.getGame(), "joueur1", null);
        messageCache.register(game.getGame(), player1.getToken(), new TichuWebSocketMock(player1.getName(), responses));
        gameService.connectGame(player1.getToken());

        Player player3 = gameService.joinGame(game.getGame(), "joueur3", null);
        messageCache.register(game.getGame(), player3.getToken(), new TichuWebSocketMock(player3.getName(), responses));
        gameService.connectGame(player3.getToken());

        Player player4 = gameService.joinGame(game.getGame(), "joueur4", null);
        messageCache.register(game.getGame(), player4.getToken(), new TichuWebSocketMock(player4.getName(), responses));
        gameService.connectGame(player4.getToken());

        gameService.checkTableComplete(player4.getToken());

        Assert.assertTrue(game.canPlay());

        ChangeCards cc1 = new ChangeCards();
        cc1.setToLeft(player1.getCards().get(0).toCardWS());
        cc1.setToRight(player1.getCards().get(1).toCardWS());
        cc1.setToPartner(player1.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player1.getToken(), cc1);
        ChangeCards cc2 = new ChangeCards();
        cc2.setToLeft(player2.getCards().get(0).toCardWS());
        cc2.setToRight(player2.getCards().get(1).toCardWS());
        cc2.setToPartner(player2.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player2.getToken(), cc2);
        ChangeCards cc3 = new ChangeCards();
        cc3.setToLeft(player3.getCards().get(0).toCardWS());
        cc3.setToRight(player3.getCards().get(1).toCardWS());
        cc3.setToPartner(player3.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player3.getToken(), cc3);
        ChangeCards cc4 = new ChangeCards();
        cc4.setToLeft(player4.getCards().get(0).toCardWS());
        cc4.setToRight(player4.getCards().get(1).toCardWS());
        cc4.setToPartner(player4.getCards().get(7).toCardWS());
        gameService.playerChangeCard(player4.getToken(), cc4);

        Assert.assertEquals(14, player1.getNbcard());
        Assert.assertEquals(14, player2.getNbcard());
        Assert.assertEquals(14, player3.getNbcard());
        Assert.assertEquals(14, player4.getNbcard());

        // Test play
        Player p = game.getCurrentPlayer();
        Fold fold = new Fold(FoldType.SINGLE, (float) p.getCards().get(0).toCardWS().getValue(), p.getCards().get(0).toCardWS());
        fold.setPlayer(p.getOrientation());
        gameService.playFold(p.getToken(), fold);

        Assert.assertEquals(13, p.getNbcard());
        Assert.assertNotSame(p, game.getCurrentPlayer());

        gameService.callTurn(game.getCurrentPlayer().getToken());
        gameService.callTurn(game.getCurrentPlayer().getToken());
        gameService.callTurn(game.getCurrentPlayer().getToken());

        Assert.assertEquals(ResponseType.TURN_WIN, getResponseAt(1));
        Assert.assertEquals(ResponseType.NEXT_PLAYER, getLast());
        Assert.assertEquals(1, game.getCurrentPlayer().getCardOfFolds().size());

        gameService.callTurn(game.getCurrentPlayer().equals(player1) ? player2.getToken() : player1.getToken());
        Assert.assertEquals(ResponseType.NOT_YOUR_TURN, getLast());

        gameService.callTurn(game.getCurrentPlayer().getToken());
        Assert.assertEquals(ResponseType.NO_CALL_WHEN_FIRST, getLast());
    }
}