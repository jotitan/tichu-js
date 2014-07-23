package fr.tichu.model.rest;

import fr.titan.tichu.tools.StringUtils;

/**
 * User: Titan Date: 30/03/14 Time: 01:45
 */
public class GameRequest {
    private String name;
    private boolean publicGame;
    private String password;
    private String playerO;
    private String playerN;
    private String playerE;
    private String playerS;

    public GameRequest(String name, boolean publicGame, String playerO, String playerN, String playerE, String playerS) throws Exception {
        if (name == null || "".equals(name.trim())) {
            throw new Exception("Name of game is empty");
        }
        this.playerO = StringUtils.isEmpty(playerO) ? "Player O" : playerO;
        this.playerN = StringUtils.isEmpty(playerN) ? "Player N" : playerN;
        this.playerE = StringUtils.isEmpty(playerE) ? "Player E" : playerE;
        this.playerS = StringUtils.isEmpty(playerS) ? "Player S" : playerS;
        this.name = name;
        this.publicGame = publicGame;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPlayerO() {
        return playerO;
    }

    public void setPlayerO(String playerO) {
        this.playerO = playerO;
    }

    public String getPlayerN() {
        return playerN;
    }

    public void setPlayerN(String playerN) {
        this.playerN = playerN;
    }

    public String getPlayerE() {
        return playerE;
    }

    public void setPlayerE(String playerE) {
        this.playerE = playerE;
    }

    public String getPlayerS() {
        return playerS;
    }

    public void setPlayerS(String playerS) {
        this.playerS = playerS;
    }

    public boolean isPublicGame() {
        return publicGame;
    }

    public void setPublicGame(boolean publicGame) {
        this.publicGame = publicGame;
    }
}
