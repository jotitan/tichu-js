package fr.titan.tichu.model.ws;

/**
 * Type of message
 */
public enum ResponseType {
    CONNECTION_OK("Player well connected"),
    CONNECTION_KO("Player not connected"),
    PLAYER_SEATED("Player seat on table"),
    GAME_CAN_RUN("All player here, game begin"),
    DISTRIBUTION("Game distributed"),
    CHANGE_CARD("Players have to change cards"),
    FOLD_PLAYED("Player play a hand"),
    NOT_YOUR_TURN("Player can't play now"),
    BAD_FOLD("Imposible to play this combination now"),
    TURN_WIN("A player win the turn"),
    PLAYER_CALL("Player doesn'y play"),
    NEXT_PLAYER("Next player to play"),
    SCORE("Score of the round");

    ResponseType(String description){
        this.description = description;
    }

    private String description;
}
