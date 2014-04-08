package fr.titan.tichu.model;

/**
 * Historic of score
 */
public class Score {
    private AnnonceType annonce;
    private boolean win;
    private boolean capot;
    private int score;
    private int cumulateScore;

    public Score() {
    }

    public Score(int score) {
        this.score = score;
    }

    public AnnonceType getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceType annonce) {
        this.annonce = annonce;
    }

    public boolean isWin() {
        return win;
    }

    public void setWin(boolean win) {
        this.win = win;
    }

    public boolean isCapot() {
        return capot;
    }

    public void setCapot(boolean capot) {
        this.capot = capot;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCumulateScore() {
        return cumulateScore;
    }

    public void setCumulateScore(int cumulateScore) {
        this.cumulateScore = cumulateScore;
    }
}
