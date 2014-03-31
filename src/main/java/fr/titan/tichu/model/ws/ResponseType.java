package fr.titan.tichu.model.ws;

/**
 * Type of message
 */
public enum ResponseType {
    CONNECTION_OK, CONNECTION_KO, PLAYER_SEATED, GAME_CAN_RUN, DISTRIBUTION, FOLD_PLAYED, NOT_YOUR_TURN, BAD_FOLD,TURN_WIN,PLAYER_CALL, NEXT_PLAYER;
}
