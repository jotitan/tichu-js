package fr.titan.tichu.service;

import fr.titan.tichu.model.*;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.CardWS;
import fr.titan.tichu.model.ws.ChangeCards;
import fr.titan.tichu.model.ws.Fold;
import fr.titan.tichu.model.ws.ResponseType;

import java.util.ArrayList;
import java.util.List;

/**
 * ORDER : fold < turn < round < game une main (fold) dans un tour de jeu (turn) au sein d'une partie (round) d'une manche (game) en 1000 points
 */
public class GameService {
    public static Games games = new Games();

    public GameService() {

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

    protected void broadCast(Game game, ResponseType type, Object object) {
        for (Player player : game.getPlayers()) {
            if (player.getPlayerStatus().equals(PlayerStatus.CONNECTED)) {
                player.getClient().send(type, object);
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
            player.getClient().send(ResponseType.DISTRIBUTION, player.getCards());
        }
        broadCast(game, ResponseType.CHANGE_CARD_MODE, null);
    }

    public void playerChangeCard(Player player, ChangeCards cards) {
        giveCardToPlayer(cards.getToLeft(), player, player.getOrientation().getLeft());
        giveCardToPlayer(cards.getToPartner(), player, player.getOrientation().getFace());
        giveCardToPlayer(cards.getToRight(), player, player.getOrientation().getRight());

        if (checkPlayersChangeCards(player.getGame())) {
            sendSwapCards(player.getGame());
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

    public void cardChanged(Game game) {

        nextPlayer(game);
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
        Game game = player.getGame();
        if (fold.isBomb()) {
            player.getGame().setCurrentPlayer(player);
            if (game.verifyBomb(fold)) {
                game.playFold(fold);
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
        Game game = player.getGame();
        if (!player.equals(game.getCurrentPlayer())) {
            player.getClient().send(ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        if (!game.verifyFold(fold)) {
            player.getClient().send(ResponseType.BAD_FOLD, "");
            return;
        }
        game.playFold(fold);
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
        broadCast(game, ResponseType.PLAYER_CALL, player.getPlayerWS());
        nextPlayer(game);
    }

    private void afterFold(Game game, Player player, Fold fold) {
        player.playFold(getCardsFromFold(game, fold));
        if (player.ended()) {
            player.setEndPosition(game.getAndIncreaseEndPosition());
            if (player.win()) {
                broadCast(game, ResponseType.TURN_WIN, player.getPlayerWS());
            } else {
                broadCast(game, ResponseType.PLAYER_END_TURN, player.getPlayerWS());
            }
        }
        if (game.isRoundEnded()) {
            endRound(game);
            return;
        }

        if (game.isTurnWin()) {
            newTurn(player.getGame());
        } else {
            nextPlayer(game);
        }
    }

    public void endRound(Game game) {
        game.saveScore();
        game.newRound();
    }

    public void nextPlayer(Game game) {
        try {
            game.nextPlayer();
            broadCast(game, ResponseType.NEXT_PLAYER, game.getCurrentPlayer().getPlayerWS());
        } catch (Exception e) {
            // Game ended, no nextPlayer
        }
    }

    /* When a turn is over, run a new turn */
    private void newTurn(Game game) {
        broadCast(game, ResponseType.TURN_WIN, game.getLastPlayer());
        game.newTurn();
    }
}
