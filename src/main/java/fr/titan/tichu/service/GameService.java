package fr.titan.tichu.service;

import com.google.common.collect.Lists;
import fr.titan.tichu.model.*;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ORDER : fold < turn < round < game une main (fold) dans un tour de jeu (turn) au sein d'une partie (round) d'une manche (game) en 1000 points
 */
public class GameService {
    public static Games games = new Games();

    public GameService() {
    }

    public GameWS getGame(String game) {
        return games.getGame(game).toGameWS();
    }

    public Game createGame(GameRequest gameRequest) throws Exception {
        Game game = new Game(gameRequest.getName());
        game.getPlayers().get(0).setName(gameRequest.getPlayerO());
        game.getPlayers().get(1).setName(gameRequest.getPlayerN());
        game.getPlayers().get(2).setName(gameRequest.getPlayerE());
        game.getPlayers().get(3).setName(gameRequest.getPlayerS());
        games.addGame(game);
        return game;
    }

    public Player joinGame(String gameName, String name, String password) throws Exception {
        Game game = games.getGame(gameName);
        if (game == null) {
            throw new Exception("Game " + gameName + " doesn't exist");
        }
        for (Player player : game.getPlayers()) {
            if (player.getName().equals(name)) {
                if (player.getPlayerStatus().equals(PlayerStatus.FREE_CHAIR) || player.getPlayerStatus().equals(PlayerStatus.DISCONNECTED)) {
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
        }
        return player;
    }

    protected void broadCast(Game game, ResponseType type, Object object) {
        for (Player player : game.getPlayers()) {
            if (player.getPlayerStatus().equals(PlayerStatus.CONNECTED) && player.getClient() != null) {
                player.getClient().send(type, object);
            }
        }
    }

    public void checkTableComplete(Game game) {
        if (game.canPlay()) {
            broadCast(game, ResponseType.GAME_MODE, "");
            distribute(game);
        }
    }

    private void distribute(Game game) {
        game.newRound();
        for (Player player : game.getPlayers()) {
            List<CardWS> cardsWS = Lists.newArrayList();
            for (Card card : player.getCards()) {
                cardsWS.add(card.toCardWS());
            }
            player.getClient().send(ResponseType.DISTRIBUTION, cardsWS);
        }
        broadCast(game, ResponseType.CHANGE_CARD_MODE, null);
    }

    public void playerChangeCard(Player player, ChangeCards cards) {
        giveCardToPlayer(cards.getToLeft(), player, player.getOrientation().getLeft());
        giveCardToPlayer(cards.getToPartner(), player, player.getOrientation().getFace());
        giveCardToPlayer(cards.getToRight(), player, player.getOrientation().getRight());

        player.getClient().send(ResponseType.CARDS_CHANGED, null);

        if (checkPlayersChangeCards(player.getGame())) {
            sendSwapCards(player.getGame());
            nextPlayer(player.getGame());
        }

    }

    /**
     * Send to each player his swap cards
     * 
     * @param game
     */
    private void sendSwapCards(Game game) {
        for (Player player : game.getPlayers()) {
            Fold fold = new Fold();
            for (Card card : player.getChangeCards()) {
                fold.addCard(card.toCardWS());
            }
            player.getClient().send(ResponseType.NEW_CARDS, fold);
        }
    }

    private boolean checkPlayersChangeCards(Game game) {
        for (Player player : game.getPlayers()) {
            if (player.getChangeCards().size() != 3) {
                return false;
            }
        }
        return true;
    }

    private void giveCardToPlayer(CardWS cardWS, Player playerFrom, Player.Orientation to) {
        Card card = playerFrom.getGame().getCardPackage().getCard(cardWS);
        Player playerTo = playerFrom.getGame().getPlayer(to);
        playerFrom.giveCard(card, playerTo);
    }

    private List<Card> getCardsFromFold(Game game, Fold fold) {
        List<Card> cards = new ArrayList<Card>();
        for (CardWS card : fold.getCards()) {
            cards.add(game.getCardPackage().getCard(card));
        }
        return cards;
    }

    /** Play a bomb */
    public void playBomb(Player player, Fold fold) {
        fold.setPlayer(player.getOrientation());
        Game game = player.getGame();
        if (fold.isBomb()) {
            player.getGame().setCurrentPlayer(player);
            if (game.verifyBomb(fold)) {
                game.playFold(player, fold);
                broadCast(game, ResponseType.BOMB_PLAYED, fold);
                afterFold(game, player, fold);
            } else {
                player.getClient().send(ResponseType.BAD_FOLD, fold);
            }
        } else {
            player.getClient().send(ResponseType.BAD_FOLD, fold);
        }
    }

    /** Play a classic fold */
    public void playFold(Player player, Fold fold) {
        fold.setPlayer(player.getOrientation());
        Game game = player.getGame();
        if (!player.equals(game.getCurrentPlayer())) {
            player.getClient().send(ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        if (!game.verifyFold(fold)) {
            player.getClient().send(ResponseType.BAD_FOLD, "");
            return;
        }
        game.playFold(player, fold);
        broadCast(game, ResponseType.FOLD_PLAYED, fold);
        afterFold(game, player, fold);
    }

    /* Player doesn't play a fold */
    public void callTurn(Player player) {
        Game game = player.getGame();
        if (!player.equals(game.getCurrentPlayer())) {
            player.getClient().send(ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        /* Impossible de call when first */
        if (game.getLastPlayer() == null) {
            player.getClient().send(ResponseType.NO_CALL_WHEN_FIRST, "");
            return;
        }
        broadCast(game, ResponseType.CALL_PLAYED, player.getPlayerWS());
        afterFold(game, player, null);
    }

    private void afterFold(Game game, Player player, Fold fold) {
        if (fold != null) {
            player.playFold(getCardsFromFold(game, fold));
            if (player.ended()) {
                player.setEndPosition(game.getAndIncreaseEndPosition());
                if (player.win()) {
                    broadCast(game, ResponseType.TURN_WIN, player.getPlayerWS());
                } else {
                    broadCast(game, ResponseType.PLAYER_END_ROUND, player.getPlayerWS());
                }
            }
            if (game.isRoundEnded()) {
                endRound(game);
                return;
            }
        }
        nextPlayer(game);
    }

    private void endRound(Game game) {
        game.saveScore();
        Team team = game.getWinner();
        if (team != null) {
            broadCast(game, ResponseType.GAME_WIN, null);
            game.newGame();
        } else {
            broadCast(game, ResponseType.SCORE, null);
            game.newRound();
        }
        distribute(game);
    }

    private void nextPlayer(Game game) {
        try {
            game.nextPlayer();
            if (game.isTurnWin()) {
                newTurn(game);
            }
            broadCast(game, ResponseType.NEXT_PLAYER, game.getCurrentPlayer().getPlayerWS());
        } catch (Exception e) {
            // Game ended, no nextPlayer
        }
    }

    /* When a turn is over, run a new turn */
    private void newTurn(Game game) {
        broadCast(game, ResponseType.TURN_WIN, game.getLastPlayer().getPlayerWS());
        game.newTurn();
    }
}
