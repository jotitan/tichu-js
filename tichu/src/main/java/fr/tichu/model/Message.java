package tichu.model;

import java.util.List;

/**
 * User: Titan
 * Date: 27/03/14
 * Time: 15:19
 */
public class Message {

    private String player;  // Id SESSION

    private AnnonceType annonce;

    private List<String> fold;

    public AnnonceType getAnnonce() {
        return annonce;
    }

    public void setAnnonce(AnnonceType annonce) {
        this.annonce = annonce;
    }

    public List<String> getFold() {
        return fold;
    }

    public void setFold(List<String> fold) {
        this.fold = fold;
    }

    public boolean isAnnonce(){
        return this.annonce != null;
    }

    public boolean isFold(){
        return this.fold != null && this.fold.size() > 0;
    }
}
