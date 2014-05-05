package fr.titan.tichu.rest;

import com.google.inject.Inject;
import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.rest.ResponseRest;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.model.ws.GameWS;
import fr.titan.tichu.model.ws.PlayerWS;
import fr.titan.tichu.service.GameService;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;

/**
 * Methods to create a game, get users ...
 */

@Path("/game")
public class GameRest {

    private Logger logger = LoggerFactory.getLogger(GameRest.class);

    @Inject
    private GameService gameService;// = new GameService();

    public GameRest() {
    }

    @GET
    @Path("/delete")
    public Response deleteGame(Game game) {
        logger.info("DELETE " + game.getGame());
        return Response.status(200).build();
    }

    @GET
    @Path("/info/{name}")
    public Response getInfoGame(@PathParam("name") String name, @QueryParam("callback") String callback) {
        logger.info("INFO " + name);
        GameWS game = gameService.getGameWS(name);
        if (game != null) {
            return buildResponse(game, callback);
        } else {
            return buildResponse(new ResponseRest(0, "No game with this name"), callback);
        }
    }

    @GET
    @Path("/create")
    // @Consumes("application/json")
    @Produces("application/json")
    public Response createGame(@QueryParam("name") String name, @QueryParam("playerO") String pO, @QueryParam("playerN") String pN,
            @QueryParam("playerE") String pE, @QueryParam("playerS") String pS, @QueryParam("callback") String callback) {
        logger.info("CREATE " + name);
        GameRequest game = new GameRequest(name, pO, pN, pE, pS);
        try {
            gameService.createGame(game);
        } catch (Exception e) {
            return buildResponse(new ResponseRest(0, e.getMessage()), callback);
        }
        return buildResponse(new ResponseRest(1), callback);
    }

    /**
     * 
     * @param game
     *            Name of the game
     * @param name
     *            Name of the player
     * @param password
     *            Password (optional) to access the game
     * @return Error when game doesn't exist, name not present on game or bad password
     */
    @GET
    @Path("/join")
    @Produces("application/json")
    public Response registerGame(@QueryParam("game") String game, @QueryParam("name") String name, @QueryParam("password") String password,
            @QueryParam("callback") String callback) {

        try {
            Player player = gameService.joinGame(game, name, password);
            PlayerWS playerWS = player.getPlayerWS();
            playerWS.setToken(player.getToken());
            logger.info("JOIN GAME " + game + " BY " + name);

            return buildResponse(playerWS, callback);
        } catch (Exception e) {
            ResponseRest response = new ResponseRest(0, e.getMessage());
            logger.error("Error when Join game : " + e.getMessage());
            return buildResponse(response, callback);
        }
    }

    private Response buildResponse(Object response, String callback) {
        if (callback != null) {
            return Response.status(200).entity(buildJSONP(callback, response)).build();
        } else {
            return Response.status(200).entity(response).build();
        }
    }

    private String buildJSONP(String callback, Object o) {
        ObjectMapper om = new ObjectMapper();
        ByteArrayOutputStream tab = new ByteArrayOutputStream();
        try {
            om.writer().writeValue(tab, o);
            return callback + "(" + new String(tab.toByteArray()) + ")";
        } catch (Exception e) {
            logger.error("Error JSON " + e.getMessage());
            return "";
        }
    }
}
