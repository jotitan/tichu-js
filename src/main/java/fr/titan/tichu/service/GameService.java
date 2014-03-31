package fr.titan.tichu.service;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Games;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.model.PlayerStatus;
import fr.titan.tichu.model.rest.GameRequest;
import fr.titan.tichu.rest.GameRest;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class GameService {
    public static Games games = new Games();

    public GameService(){

    }

    public void createGame(GameRequest gameRequest)throws Exception{
        Game game = new Game(gameRequest.getName());
        game.getPlayers().get(0).setName(gameRequest.getPlayerO());
        game.getPlayers().get(1).setName(gameRequest.getPlayerN());
        game.getPlayers().get(2).setName(gameRequest.getPlayerE());
        game.getPlayers().get(3).setName(gameRequest.getPlayerS());
        games.addGame(game);
    }

    public Player joinGame(String gameName,String name,String password) throws Exception{
        Game game = games.getGame(gameName);
        if(game == null){
            throw new Exception("Game " + gameName + " doesn't exist");
        }
        for(Player player : game.getPlayers()){
            if(player.getName().equals(name)){
                if(player.getPlayerStatus().equals(PlayerStatus.FREE_CHAIR)){
                    player.setPlayerStatus(PlayerStatus.AUTHENTICATE);
                    player.createToken(gameName);
                    games.addPlayerByToken(player);
                    return player;
                }  else{
                    throw new Exception("The chair is not free anymore");
                }
            }
        }
        throw new Exception("No player with this name");
    }

    /**
     * When player connect to ws
     * @return
     */
    public Player connectGame(String token){
        Player player = games.getPlayerByToken(token);
        if(player!=null){
            player.setPlayerStatus(PlayerStatus.CONNECTED);
        }
        return player;
    }

    public void checkGame(Game game,Player player){
        game.canPlay();

    }

}
