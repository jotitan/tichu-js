package fr.titan.tichu.service;

import fr.titan.tichu.model.*;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.ResponseType;
import fr.titan.tichu.rest.GameRest;

import javax.websocket.Session;
import java.util.ArrayList;
import java.util.List;

/**
 * ORDER : fold < turn < round < game une main (fold) dans un tour de jeu (turn) au sein d'une partie (round) d'une manche (game) en 1000 points
 */
public class GameService {
    public static Games games = new Games();

    public GameService() {

    }

    public void createGame(GameRequest gameRequest) throws Exception {
        Game game = new Game(gameRequest.getName());
        game.getPlayers().get(0).setName(gameRequest.getPlayerO());
        game.getPlayers().get(1).setName(gameRequest.getPlayerN());
        game.getPlayers().get(2).setName(gameRequest.getPlayerE());
        game.getPlayers().get(3).setName(gameRequest.getPlayerS());
        games.addGame(game);
    }

    public Player joinGame(String gameName, String name, String password) throws Exception {
        Game game = games.getGame(gameName);
        if (game == null) {
            throw new Exception("Game " + gameName + " doesn't exist");
        }
        for (Player player : game.getPlayers()) {
            if (player.getName().equals(name)) {
                if (player.getPlayerStatus().equals(PlayerStatus.FREE_CHAIR)) {
                    player.setPlayerStatus(PlayerStatus.AUTHENTICATE);
                    player.createToken(gameName);
                    games.addPlayerByToken(player);
                    return player;
                } else {
                    throw new Exception("The chair is not free anymore");
                }
            }
        }
        throw new Exception("No player with this name");
    }

    /**
     * When player connect to ws
     * 
     * @return
     */
    public Player connectGame(String token) {
        Player player = games.getPlayerByToken(token);
        if (player != null) {
            player.setPlayerStatus(PlayerStatus.CONNECTED);
            broadCast(player.getGame(), ResponseType.PLAYER_SEATED, player.getPlayerWS());
            checkTableFull(player.getGame());
        }
        return player;
    }

    private void broadCast(Game game, ResponseType type, Object object) {
        for (Player player : game.getPlayers()) {
            if (player.getPlayerStatus().equals(PlayerStatus.CONNECTED)) {
                player.getWebSocket().sendMessage(type, object);
            }
        }
    }

    public void checkTableFull(Game game) {
        if (game.canPlay()) {
            broadCast(game, ResponseType.GAME_CAN_RUN, "");
            distribute(game);
        }

    }

    public void distribute(Game game) {
        game.newRound();
        for (Player player : game.getPlayers()) {
            player.getWebSocket().sendMessage(ResponseType.DISTRIBUTION, player.getCards());
        }
    }

    /* Play a bomb */
    public void playBomb(Player player, Fold fold) {
        if (player.equals(player.getGame().getLastPlayer())) {
            // Impossible to bomb is game
        }
    }

    /* Play a classic fold */
    public void playFold(Player player, Fold fold) {
        Game game = player.getGame();
        if (!player.equals(game.getCurrentPlayer())) {
            player.getWebSocket().sendMessage(ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        if (!game.verifyFold(fold)) {
            player.getWebSocket().sendMessage(ResponseType.BAD_FOLD, "");
            return;
        }
        game.playFold(fold);
        broadCast(game, ResponseType.FOLD_PLAYED, fold);

        if (game.isTurnWin()) {
            newTurn(player.getGame());
        } else {
            nextPlayer(game);
        }
    }

    /* Player doesn't play a fold */
    public void callTurn(Player player) {
        Game game = player.getGame();
        if (!player.equals(game.getCurrentPlayer())) {
            player.getWebSocket().sendMessage(ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        broadCast(game, ResponseType.PLAYER_CALL, player.getPlayerWS());
        nextPlayer(game);
    }

    public void nextPlayer(Game game) {
        game.nextPlayer();
        broadCast(game, ResponseType.NEXT_PLAYER, game.getCurrentPlayer().getPlayerWS());
    }

    /* When a turn is over, run a new turn */
    private void newTurn(Game game) {
        broadCast(game, ResponseType.TURN_WIN, game.getLastPlayer());
        game.newTurn();
    }
}
