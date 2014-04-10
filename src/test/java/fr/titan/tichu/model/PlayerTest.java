package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.CardWS;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * Created with IntelliJ IDEA. User: 960963 Date: 02/04/14 Time: 17:47 To change this template use File | Settings | File Templates.
 */
public class PlayerTest {
    @Test
    public void testCreateToken() throws Exception {
        String base = "toto" + System.currentTimeMillis();
        MessageDigest md5 = MessageDigest.getInstance("md5");

        byte[] bytes = md5.digest(base.getBytes());
        BigInteger bi = new BigInteger(1, bytes);
        String token = String.format("%0" + (bytes.length << 1) + "X", bi);
        System.out.println(token);
    }

    @Test
    public void testFindCombinaisonWithValue() {
        CardPackage cp = new CardPackage();
        cp.createCards();
        Player p = new Player(null, null);

        p.addCard(cp.getCard(new CardWS(3, "red")));

        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.SINGLE, 2, null));
        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.PAIR, 2, null));

        p.addCard(cp.getCard(new CardWS(3, "blue")));
        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.PAIR, 2, null));
        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.PAIR, 4, null));
        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.BRELAN, 2, null));

        p.addCard(cp.getCard(new CardWS(3, "black")));
        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.BRELAN, 2, null));
        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.SQUAREBOMB, 2, null));

        p.addCard(cp.getCard(new CardWS(3, "green")));
        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.SQUAREBOMB, 2, null));

        p.addCard(cp.getCard(new CardWS(4, "blue")));
        p.addCard(cp.getCard(new CardWS(4, "green")));
        p.addCard(cp.getCard(new CardWS(5, "green")));
        p.addCard(cp.getCard(new CardWS(7, "red")));
        p.addCard(cp.getCard(new CardWS(8, "black")));

        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.STRAIGHT, 2, 5));

        p.addCard(cp.getCard(new CardWS(6, "green")));
        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.STRAIGHT, 2, 5));
        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.STRAIGHT, 2, 6));
        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.STRAIGHT, 2, 7));

        p.giveCard(cp.getCard(new CardWS(6, "green")));
        Assert.assertFalse(p.findCombinaisonWithValue(3, FoldType.STRAIGHT, 2, 6));

        p.addCard(cp.getCard(new CardWS(16, "white")));
        Assert.assertTrue(p.findCombinaisonWithValue(3, FoldType.STRAIGHT, 2, 6));

        p.resetCards();

        p.addCard(cp.getCard(new CardWS(5, "red")));
        Assert.assertFalse(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

        p.addCard(cp.getCard(new CardWS(5, "blue")));
        Assert.assertFalse(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

        p.addCard(cp.getCard(new CardWS(6, "blue")));
        Assert.assertFalse(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

        p.addCard(cp.getCard(new CardWS(6, "green")));
        Assert.assertTrue(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

        p.giveCard(cp.getCard(new CardWS(6, "blue")));
        Assert.assertFalse(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

        p.addCard(cp.getCard(new CardWS(16, "white")));
        Assert.assertTrue(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

        p.giveCard(cp.getCard(new CardWS(8, "blue")));
        p.giveCard(cp.getCard(new CardWS(11, "red")));
        Assert.assertTrue(p.findCombinaisonWithValue(5, FoldType.STRAIGHTPAIR, 4, 2));

    }
}
