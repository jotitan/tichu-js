package fr.titan.tichu.service;

import com.google.common.collect.Lists;
import fr.titan.tichu.exception.CheatException;
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

    public GameWS getGame(String name) {
        Game game = games.getGame(name);
        return game != null ? game.toGameWS() : null;
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
                    player.setReconnect(player.getPlayerStatus().equals(PlayerStatus.DISCONNECTED));
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

    /*
     * Get context about the current game : - Connected players - Status of game
     */
    public ContextWS getContextGame(Player player) {
        ContextWS context = new ContextWS();
        context.setFolds(player.getGame().getFolds());
        for (Player p : player.getGame().getPlayers()) {
            PlayerWS playerWS = p.getPlayerWS();
            playerWS.setNbCard(p.getNbcard() > 0 ? p.isDistributeAllCards() ? 14 : 9 : 0);
            playerWS.setConnected(p.isConnected());
            context.addPlayer(playerWS);

            if (p.equals(player.getGame().getCurrentPlayer())) {
                context.setType(ResponseType.NEXT_PLAYER);
                context.setCurrentPlayer(playerWS);
            }
            /* Context for user */
            if (p.equals(player)) {
                context.setPlayerUser(playerWS);
                if (player.getCards() != null && player.getCards().size() > 0) {
                    List<Card> cards = player.getCards().size() != 14 || player.isDistributeAllCards() ? player.getCards() : player.getCards().subList(
                            0, 9);
                    for (Card card : cards) {
                        context.addCard(card.toCardWS());
                    }
                    if (!player.isDistributeAllCards()) {
                        context.setType(ResponseType.DISTRIBUTION_PART1);
                    } else {
                        if (context.getCurrentPlayer() == null && !player.getChangeCards().isComplete()) {
                            context.setType(ResponseType.CHANGE_CARD_MODE);
                        }
                    }

                }
            }
        }
        return context;
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

    public Player getPlayerByToken(String token) {
        return games.getPlayerByToken(token);
    }

    /* Show to the player the last 5 cards */
    public void getSuiteCards(Player player) {
        List<CardWS> cardsWS = Lists.newArrayList();
        for (Card card : player.getCards().subList(9, player.getCards().size())) {
            cardsWS.add(card.toCardWS());
        }
        player.getClient().send(ResponseType.DISTRIBUTION_PART2, cardsWS);
        player.getClient().send(ResponseType.CHANGE_CARD_MODE, null);
        player.setDistributeAllCards(true);
        broadCast(player.getGame(), ResponseType.SEE_ALL_CARDS, player.getPlayerWS());
    }

    public void makeAnnonce(Player player, AnnonceType annonce) {
        // Verifiy if player has already played a card
        switch (annonce) {
        case TICHU:
            if (!player.canTichu()) {
                player.getClient().send(ResponseType.ANNONCE_FORBIDDEN, annonce);
                return;
            }
            break;
        case GRAND_TICHU:
            if (player.isDistributeAllCards() || !player.canTichu()) {
                player.getClient().send(ResponseType.ANNONCE_FORBIDDEN, annonce);
                return;
            }
            break;
        }
        player.setAnnonce(annonce);
        broadCast(player.getGame(), ResponseType.PLAYER_ANNONCE, player.getPlayerWS());
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
            for (Card card : player.getCards().subList(0, 9)) {
                cardsWS.add(card.toCardWS());
            }
            player.getClient().send(ResponseType.DISTRIBUTION_PART1, cardsWS);
            player.setDistributeAllCards(false);
        }
    }

    enum Position {
        LEFT, CENTER, RIGHT
    };

    public void playerChangeCard(Player player, ChangeCards cards) {
        giveCardToPlayer(cards.getToLeft(), player, player.getOrientation().getLeft(), Position.RIGHT);
        giveCardToPlayer(cards.getToPartner(), player, player.getOrientation().getFace(), Position.CENTER);
        giveCardToPlayer(cards.getToRight(), player, player.getOrientation().getRight(), Position.LEFT);

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
            ChangeCards cc = player.getChangeCards();
            List<Card> cards = game.getCardPackage().getCards(cc.getToLeft(), cc.getToPartner(), cc.getToRight());
            for (Card card : cards) {
                player.addCard(card);
            }
            player.getClient().send(ResponseType.NEW_CARDS, player.getChangeCards());
        }
    }

    private boolean checkPlayersChangeCards(Game game) {
        for (Player player : game.getPlayers()) {
            if (!player.getChangeCards().isComplete()) {
                return false;
            }
        }
        return true;
    }

    private void giveCardToPlayer(CardWS cardWS, Player playerFrom, Player.Orientation to, Position position) {
        Card card = playerFrom.getGame().getCardPackage().getCard(cardWS);
        Player playerTo = playerFrom.getGame().getPlayer(to);
        playerFrom.giveCard(card);
        switch (position) {
        case LEFT:
            playerTo.getChangeCards().setToLeft(cardWS);
            break;
        case CENTER:
            playerTo.getChangeCards().setToPartner(cardWS);
            break;
        case RIGHT:
            playerTo.getChangeCards().setToRight(cardWS);
            break;
        }
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
        try {
            if (!game.verifyFold(fold, player)) {
                player.getClient().send(ResponseType.BAD_FOLD, "");
                return;
            }
        } catch (CheatException cheatEx) {
            broadCast(player.getGame(), ResponseType.CHEATER, player.getPlayerWS());
        }
        game.playFold(player, fold);
        broadCast(game, ResponseType.FOLD_PLAYED, fold);
        afterFold(game, player, fold);
    }

    /* Player doesn't play a fold */
    public void callTurn(Player player) {
        Game game = player.getGame();
        // Verifie mahjong
        if (!game.verifyCall(player)) {
            player.getClient().send(ResponseType.BAD_FOLD, "");
        }

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
            player.playFold(game.getCardPackage().getCards(fold.getCards()));
            if (player.ended()) {
                player.setEndPosition(game.getAndIncreaseEndPosition());
                if (player.win()) {
                    broadCast(game, ResponseType.ROUND_WIN, player.getPlayerWS());
                } else {
                    broadCast(game, ResponseType.PLAYER_END_ROUND, player.getPlayerWS());
                }
            }

            if (game.isRoundEnded()) {
                endRound(game);
                return;
            }
            if (game.isLastIsDog()) {
                broadCast(game, ResponseType.TURN_WIN, null);
            }
        }
        nextPlayer(game);
    }

    private void endRound(Game game) {
        GameWS gameWS = game.saveScore();
        Team team = game.getWinner();
        if (team != null) {
            broadCast(game, ResponseType.GAME_WIN, null);
            game.newGame();
        } else {
            // Send the score of the teams
            broadCast(game, ResponseType.SCORE, gameWS);
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
            System.out.println(e.getMessage());
            // Game ended, no nextPlayer
        }
    }

    /* When a turn is over, run a new turn */
    private void newTurn(Game game) {
        broadCast(game, ResponseType.TURN_WIN, game.getLastPlayer().getPlayerWS());
        game.newTurn();
    }
}
