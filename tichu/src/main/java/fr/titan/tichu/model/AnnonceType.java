package fr.tichu.model;

/**
 * User: Titan Date: 27/03/14 Time: 15:19
 */
public enum AnnonceType {
    TICHU(100), GRAND_TICHU(200);

    AnnonceType(int score) {
        this.score = score;
    }

    private int score;

    public int getScore() {
        return score;
    }
}
