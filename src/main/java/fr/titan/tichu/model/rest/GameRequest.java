package fr.titan.tichu.model.rest;

/**
 * User: Titan Date: 30/03/14 Time: 01:45
 */
public class GameRequest {
    private String name;
    private String password;
    private String playerO;
    private String playerN;
    private String playerE;
    private String playerS;

    public GameRequest(String name, String playerO, String playerN, String playerE, String playerS) {
        this.playerO = playerO != null ? playerO : "Player O";
        this.playerN = playerN != null ? playerN : "Player N";
        this.playerE = playerE != null ? playerE : "Player E";
        this.playerS = playerS != null ? playerS : "Player S";
        this.name = name;
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
}
