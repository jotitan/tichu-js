package tichu.model;

import com.google.common.collect.Lists;

import java.io.Serializable;
import java.util.List;

/**
 *
 */
public class Team implements Serializable {
    private String name;
    private int score;
    /* Order of team (first of second) */
    private int order;
    private Player player1;
    private Player player2;

    private static final int WIN_LIMIT = 1000;

    private List<Score> scores = Lists.newLinkedList();

    public Team() {
    }

    public Team(Player player1, Player player2, int order) {
        this.player1 = player1;
        this.player2 = player2;
        this.player1.setPartner(this.player2);
        this.player2.setPartner(this.player1);
        this.order = order;
    }

    public Score buildScore(boolean isOtherCapot) {
        int partialScore = (isOtherCapot) ? 200 : 0;
        if (!isOtherCapot && !isCapot()) {
            partialScore += player1.getPointCards() + player2.getPointCards();
        }
        int pointAnnonce = getPointAnnonce(player1) + getPointAnnonce(player2);
        partialScore += pointAnnonce;
        score += partialScore;

        // keep historic
        Score scoreHisto = new Score(partialScore);
        scoreHisto.setCapot(isCapot());
        scoreHisto.setAnnonce(getAnnonce());
        // If annonce and win the annonce (point positive)
        scoreHisto.setWin(pointAnnonce >= 0);
        scoreHisto.setCumulateScore(score);
        scores.add(scoreHisto);

        return scoreHisto;
    }

    private AnnonceType getAnnonce() {
        return player1.getAnnonce() != null ? player1.getAnnonce() : player2.getAnnonce() != null ? player2.getAnnonce() : null;
    }

    public boolean hasWon() {
        return score >= WIN_LIMIT;
    }

    public int getPointAnnonce(Player player) {
        if (player.getAnnonce() != null) {
            return player.win() ? player.getAnnonce().getScore() : player.getAnnonce().getScore() * -1;
        }
        return 0;
    }

    public boolean isCapot() {
        return player1.getCards().size() > 0 && player2.getCards().size() > 0;
    }

    public boolean hasFinished(){
        return player1.ended() && player2.ended();
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

    public List<Score> getScores() {
        return scores;
    }

    public int getOrder() {
        return order;
    }
}
