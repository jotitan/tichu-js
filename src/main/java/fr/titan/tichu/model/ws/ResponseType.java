package fr.titan.tichu.model.ws;

/**
 * Type of message
 */
public enum ResponseType {
    CONNECTION_OK("Player well connected"), CONNECTION_KO("Player not connected"), PLAYER_DISCONNECTED("A player disconnected, game pause"), PLAYER_SEATED(
            "Player seat on table"), GAME_MODE("All player here, game begin"), DISTRIBUTION("Game distributed"), DISTRIBUTION_PART1("Send 9 first cards"), DISTRIBUTION_PART2(
            "Send 5 last cards"), SEE_ALL_CARDS("When a user have seen his 14 cards"), CHANGE_CARD_MODE("Players have to change cards"), CARDS_CHANGED(
            "Received change card of player"), NEW_CARDS("The 3 cards swap with others players"), FOLD_PLAYED("Player play a hand"), BOMB_PLAYED(
            "Player play a bomb"), CALL_PLAYED("Player doesn'y play"), NOT_YOUR_TURN("Player can't play now"), NO_CALL_WHEN_FIRST(
            "No call accepted when player play first"), BAD_FOLD("Imposible to play this combination now"), NEXT_PLAYER("Next player to play"), ROUND_WIN(
            "When a player win the round"), TURN_WIN("A player win the turn"), PLAYER_END_ROUND("A player end the turn"), SCORE("Score of the round"), GAME_WIN(
            "A team has won"), ANNONCE_FORBIDDEN("When player can't make annonce"), CHEATER("When a player try to cheat"), PLAYER_ANNONCE(
            "Player make an annonce"), EMPTY("Use for test"),CHAT("Chat message");

    ResponseType(String description) {
        this.description = description;
    }

    private String description;
}
