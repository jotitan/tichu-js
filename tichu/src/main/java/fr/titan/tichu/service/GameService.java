package fr.titan.tichu.service;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import fr.titan.tichu.Orientation;
import fr.titan.tichu.exception.CheatException;
import fr.titan.tichu.model.*;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.*;
import fr.titan.tichu.service.cache.game.GameCache;
import fr.titan.tichu.service.cache.message.MessageCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ORDER : fold < turn < round < game une main (fold) dans un tour de jeu (turn) au sein d'une partie (round) d'une manche (game) en 1000 points
 */
@Singleton
public class GameService {
    @Inject
    public GameCache cacheService;
    @Inject
    public MessageCache messageCache;

    private Logger logger = LoggerFactory.getLogger(GameService.class);

    public GameService() {
        logger.info("Create game service");
    }

    public Game getGame(String name) {
        return cacheService.getGame(name);
    }

    public GameWS getGameWS(String name) {
        Game game = cacheService.getGame(name);
        return game != null ? game.toGameWS() : null;
    }

    public boolean removeGame(String name) {
        return cacheService.removeGame(name);
    }

    public Game createGame(GameRequest gameRequest) throws Exception {
        Game game = new Game(gameRequest.getName());
        game.getPlayers().get(0).setName(gameRequest.getPlayerO());
        game.getPlayers().get(1).setName(gameRequest.getPlayerN());
        game.getPlayers().get(2).setName(gameRequest.getPlayerE());
        game.getPlayers().get(3).setName(gameRequest.getPlayerS());
        game.setPublicGame(gameRequest.isPublicGame());
        cacheService.createGame(game);
        return game;
    }

    /**
     * When player connect to ws
     * 
     * @return
     */
    public Player connectGame(String token) {
        Game game = cacheService.getGameByTokenPlayer(token);
        if (game == null) {
            return null;
        }
        Player player = game.getPlayerByToken(token);
        if (player != null
                && (player.getPlayerStatus().equals(PlayerStatus.AUTHENTICATE) || (player.getPlayerStatus().equals(PlayerStatus.CONNECTED) && canForceReconnect(player)))) {
            player.setPlayerStatus(PlayerStatus.CONNECTED);
            cacheService.saveGame(game);
            broadCast(game, ResponseType.PLAYER_SEATED, player.getPlayerWS());
        }
        return player;
    }

    public Player joinGame(String gameName, String name, String password) throws Exception {
        Game game = cacheService.getGame(gameName);
        if (game == null) {
            throw new Exception("Game " + gameName + " doesn't exist");
        }
        for (Player player : game.getPlayers()) {
            if (player.getName().equals(name)) {
                if (player.getPlayerStatus().equals(PlayerStatus.FREE_CHAIR) || player.getPlayerStatus().equals(PlayerStatus.DISCONNECTED)
                        || canForceReconnect(player)) {
                    player.setPlayerStatus(PlayerStatus.AUTHENTICATE);
                    // Keep the token if already exist
                    if (player.getToken() == null) {
                        player.createToken(gameName);
                    }
                    cacheService.addPlayer(player, game);
                    cacheService.saveGame(game);
                    cacheService.heartbeat(player.getToken());
                    return player;
                } else {
                    throw new Exception("The chair is not free anymore");
                }
            }
        }
        throw new Exception("No player with this name");
    }

    public Set<String> getGames() {
        return cacheService.getGames();
    }

    public Map<Integer, Set<String>> getFreeChairGames() {
        return cacheService.getFreeChairGames();
    }

    /**
     * Check the heartbeat. If heartbeat exist and greater than 1000ms, permet reconnect
     * 
     * @param player
     * @return
     */
    private boolean canForceReconnect(Player player) {
        Long time = cacheService.lastHeartbeat(player);
        return time != null && (System.currentTimeMillis() - time > 1000);
    }

    /*
     * Get context about the current game : - Connected players - Status of game
     */
    public ContextWS getContextGame(String token) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);

        ContextWS context = new ContextWS(game, player);
        return context;
    }

    public Player getPlayerByToken(String token) {
        return cacheService.getPlayer(token);
    }

    /* Show to the player the last 5 cards */
    public void getSuiteCards(String token) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);

        List<CardWS> cardsWS = Lists.newArrayList();
        for (Card card : player.getCards().subList(9, player.getCards().size())) {
            cardsWS.add(card.toCardWS());
        }
        uniCast(game, player, ResponseType.DISTRIBUTION_PART2, cardsWS);
        uniCast(game, player, ResponseType.CHANGE_CARD_MODE, null);
        player.setDistributeAllCards(true);
        cacheService.saveGame(game);
        broadCast(game, ResponseType.SEE_ALL_CARDS, player.getPlayerWS());
    }

    public void makeAnnonce(String token, AnnonceType annonce) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);
        // Verifiy if player has already played a card and if partner make an annonce
        switch (annonce) {
        case TICHU:
            if (!player.canTichu()) {
                uniCast(game, player, ResponseType.ANNONCE_FORBIDDEN, annonce);
                return;
            }
            break;
        case GRAND_TICHU:
            if (player.getDistributeAllCards() || !player.canTichu()) {
                uniCast(game, player, ResponseType.ANNONCE_FORBIDDEN, annonce);
                return;
            }
            break;
        }
        player.setAnnonce(annonce);
        cacheService.saveGame(game);
        broadCast(game, ResponseType.PLAYER_ANNONCE, player.getPlayerWS());
    }

    protected void broadCast(String token, ResponseType type, Object object) {
        Game game = cacheService.getGameByTokenPlayer(token);
        broadCast(game, type, object);
    }

    protected void uniCast(Game game, Player player, ResponseType type, Object object) {
        messageCache.sendMessage(game, player, type, object);
    }

    protected void broadCast(Game game, ResponseType type, Object object) {
        messageCache.sendMessageToAll(game, type, object);
    }

    public void checkTableComplete(String token) {
        Game game = cacheService.getGameByTokenPlayer(token);
        if (game.canPlay()) {
            broadCast(game, ResponseType.GAME_MODE, "");
            distribute(game);
            cacheService.saveGame(game);
        }
    }

    private void distribute(Game game) {
        game.newRound();
        for (Player player : game.getPlayers()) {
            List<CardWS> cardsWS = Lists.newArrayList();
            for (Card card : player.getCards().subList(0, 9)) {
                cardsWS.add(card.toCardWS());
            }
            uniCast(game, player, ResponseType.DISTRIBUTION_PART1, cardsWS);
            player.setDistributeAllCards(false);
        }
    }

    enum Position {
        LEFT, CENTER, RIGHT
    };

    public void playerChangeCard(String token, ChangeCards cards) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);

        giveCardToPlayer(game, cards.getToLeft(), player, player.getOrientation().getLeft(), Position.RIGHT);
        giveCardToPlayer(game, cards.getToPartner(), player, player.getOrientation().getFace(), Position.CENTER);
        giveCardToPlayer(game, cards.getToRight(), player, player.getOrientation().getRight(), Position.LEFT);

        uniCast(game, player, ResponseType.CARDS_CHANGED, null);

        if (checkPlayersChangeCards(game)) {
            sendSwapCards(game);
            nextPlayer(game);
        }
        cacheService.saveGame(game);
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
            uniCast(game, player, ResponseType.NEW_CARDS, player.getChangeCards());
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

    private void giveCardToPlayer(Game game, CardWS cardWS, Player playerFrom, Orientation to, Position position) {
        Card card = game.getCardPackage().getCard(cardWS);
        Player playerTo = game.getPlayer(to);
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
    public void playBomb(String token, Fold fold) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);
        fold.setPlayer(player.getOrientation());

        if (fold.isBomb()) {
            game.setCurrentPlayer(player);
            if (game.verifyBomb(fold)) {
                game.playFold(player, fold);
                broadCast(game, ResponseType.BOMB_PLAYED, fold);
                afterFold(game, player, fold);
            } else {
                uniCast(game, player, ResponseType.BAD_FOLD, fold);
            }
        } else {
            uniCast(game, player, ResponseType.BAD_FOLD, fold);
        }
        cacheService.saveGame(game);
    }

    /** Play a classic fold */
    public void playFold(String token, Fold fold) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);
        fold.setPlayer(player.getOrientation());
        if (!player.equals(game.getCurrentPlayer())) {
            uniCast(game, player, ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        try {
            AnalyseFoldType analyse = game.verifyFold(fold, player);
            if (!analyse.equals(AnalyseFoldType.OK)) {
                uniCast(game, player, ResponseType.BAD_FOLD, analyse);
                return;
            }
        } catch (CheatException cheatEx) {
            broadCast(game, ResponseType.CHEATER, player.getPlayerWS());
        }
        game.playFold(player, fold);
        broadCast(game, ResponseType.FOLD_PLAYED, fold);
        afterFold(game, player, fold);
        cacheService.saveGame(game);
    }

    /* Player doesn't play a fold */
    public void callTurn(String token) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = game.getPlayerByToken(token);
        // Verifie mahjong
        AnalyseFoldType analyse = game.verifyCall(player);
        if (analyse != AnalyseFoldType.OK) {
            uniCast(game, player, ResponseType.BAD_FOLD, analyse);
            return;
        }

        if (!player.equals(game.getCurrentPlayer())) {
            uniCast(game, player, ResponseType.NOT_YOUR_TURN, "");
            return;
        }
        /* Impossible de call when first */
        if (game.getLastPlayer() == null) {
            uniCast(game, player, ResponseType.NO_CALL_WHEN_FIRST, "");
            return;
        }
        broadCast(game, ResponseType.CALL_PLAYED, player.getPlayerWS());
        afterFold(game, player, null);
        cacheService.saveGame(game);
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
                Team teamCapot = game.isCapot();
                if (teamCapot != null) {
                    broadCast(game, ResponseType.CAPOT, teamCapot.getPlayer1().getName() + " - " + teamCapot.getPlayer2().getName());
                }
                endRound(game);
                return;
            }
            if (game.isLastIsDog()) {
                game.newTurn();
                broadCast(game, ResponseType.TURN_WIN, null);
                broadCast(game, ResponseType.NEXT_PLAYER, game.getCurrentPlayer().getPlayerWS());
            } else
                nextPlayer(game);
        } else {
            player.playCall();
            nextPlayer(game);
        }
    }

    private void endRound(Game game) {
        GameWS gameWS = game.saveScore();
        Team team = game.getWinner();
        broadCast(game, ResponseType.SCORE, gameWS);
        if (team != null) {
            // Propose to play a new game
            broadCast(game, ResponseType.GAME_WIN, team.getOrder());
            game.newGame();
        }
        distribute(game);
    }

    private void nextPlayer(Game game) {
        try {
            game.nextPlayer();
            if (game.isTurnWin()) {
                if (!newTurn(game)) {
                    // Dragon case
                    return;
                }
            }
            broadCast(game, ResponseType.NEXT_PLAYER, game.getCurrentPlayer().getPlayerWS());
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    /* When a turn is over, run a new turn */
    private boolean newTurn(Game game) {
        // If dragon last fold, ask player where to give the fold
        if (game.isLastIsDragon()) {
            uniCast(game, game.getLastPlayer(), ResponseType.WHERE_GIVE_FOLD, null);
            return false;
        } else {
            broadCast(game, ResponseType.TURN_WIN, game.getLastPlayer().getPlayerWS());
            game.newTurn();
            return true;
        }
    }

    /* When dragon win the turn, have to give cards to an ennemy */
    /**
     * @param playerToGive
     *            : left or right
     */
    public void giveFoldAfterDragon(String token, String playerToGive) {
        Game game = cacheService.getGameByTokenPlayer(token);
        Player player = "left".equals(playerToGive) ? game.getPlayer(game.getCurrentPlayer().getOrientation().getLeft()) : game.getPlayer(game
                .getCurrentPlayer().getOrientation().getRight());

        player.addCardsOfFold(game.getCardOfFolds());
        game.setCardOfFolds(new ArrayList<Card>());
        broadCast(game, ResponseType.GIVE_FOLD_DRAGON, player.getPlayerWS());
        broadCast(game, ResponseType.TURN_WIN, game.getLastPlayer().getPlayerWS());
        game.newTurn();
        broadCast(game, ResponseType.NEXT_PLAYER, game.getCurrentPlayer().getPlayerWS());
        cacheService.saveGame(game);
    }

    public void playerDisconnect(String token) {
        Game game = cacheService.getGameByTokenPlayer(token);
        if (game != null) {
            Player player = game.getPlayerByToken(token);
            if (player != null) {
                player.setPlayerStatus(PlayerStatus.DISCONNECTED);
                cacheService.saveGame(game);
                broadCast(token, ResponseType.PLAYER_DISCONNECTED, player.getPlayerWS());
            }
        }
    }

    public void setCacheService(GameCache cacheService) {
        this.cacheService = cacheService;
    }

    public void setMessageCache(MessageCache messageCache) {
        this.messageCache = messageCache;
    }
}
