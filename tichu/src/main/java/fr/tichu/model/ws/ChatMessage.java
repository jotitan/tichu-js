package tichu.model.ws;

/**
 *
 */
public class ChatMessage {
    private String player;
    private String message;

    public ChatMessage(String player, String message) {
        this.player = player;
        this.message = message;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
