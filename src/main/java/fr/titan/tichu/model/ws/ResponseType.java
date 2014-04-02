package fr.titan.tichu.model.ws;

/**
 * Type of message
 */
public enum ResponseType {
    CONNECTION_OK("Player well connected"), CONNECTION_KO("Player not connected"), PLAYER_SEATED("Player seat on table"), GAME_CAN_RUN(
            "All player here, game begin"), DISTRIBUTION("Game distributed"), NEW_CARDS("The 3 cards swap with others players"),
    CHANGE_CARD_MODE("Players have to change cards"), CARDS_CHANGED("Received change card of player"),FOLD_PLAYED(
            "Player play a hand"), BOMB_PLAYED("Player play a bomb"), NOT_YOUR_TURN("Player can't play now"), BAD_FOLD(
            "Imposible to play this combination now"), TURN_WIN("A player win the turn"), PLAYER_CALL("Player doesn'y play"), PLAYER_END_TURN(
            "A player end the turn"), NEXT_PLAYER("Next player to play"), SCORE("Score of the round");

    ResponseType(String description) {
        this.description = description;
    }

    private String description;
}
