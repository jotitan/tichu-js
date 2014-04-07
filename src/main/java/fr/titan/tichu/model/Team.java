package fr.titan.tichu.model;

/**
 *
 */
public class Team {
    private int score;
    private Player player1;
    private Player player2;

    public Team() {
    }

    public Team(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1.setPartner(this.player2);
        this.player2.setPartner(this.player1);
    }

    public void buildScore(boolean isOtherCapot) {
        int partialScore = (isOtherCapot) ? 200 : 0;
        if (!isOtherCapot) {
            partialScore += player1.getPointCards() + player2.getPointCards();
        }
        partialScore += getPointAnnonce(player1) + getPointAnnonce(player2);

        score += partialScore;
    }

    public boolean hasWon() {
        return score >= 1000;
    }

    public int getPointAnnonce(Player player) {
        if (player.getAnnonce() != null) {
            return player.win() ? player.getAnnonce().getScore() : player.getAnnonce().getScore() * -1;
        }
        return 0;
    }

    public boolean isCapot() {
        return player1.getCardOfFolds().size() == 0 && player2.getCardOfFolds().size() == 0;
    }

    public void resetScore() {
        this.score = 0;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Player getPlayer1() {
        return player1;
    }

    public void setPlayer1(Player player1) {
        this.player1 = player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public void setPlayer2(Player player2) {
        this.player2 = player2;
    }
}
