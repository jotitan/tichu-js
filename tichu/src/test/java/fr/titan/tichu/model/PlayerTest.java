package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.CardWS;
import junit.framework.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * 
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

        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.SINGLE, 2F, null));
        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.PAIR, 2F, null));

        p.addCard(cp.getCard(new CardWS(3, "blue")));
        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.PAIR, 2F, null));
        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.PAIR, 4F, null));
        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.BRELAN, 2F, null));

        p.addCard(cp.getCard(new CardWS(3, "black")));
        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.BRELAN, 2F, null));
        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.SQUAREBOMB, 2F, null));

        p.addCard(cp.getCard(new CardWS(3, "green")));
        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.SQUAREBOMB, 2F, null));

        p.giveCard(cp.getCard(new CardWS(3, "green")));

        p.addCard(cp.getCard(new CardWS(4, "blue")));
        p.addCard(cp.getCard(new CardWS(4, "green")));
        p.addCard(cp.getCard(new CardWS(5, "green")));
        p.addCard(cp.getCard(new CardWS(7, "red")));
        p.addCard(cp.getCard(new CardWS(8, "black")));

        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.STRAIGHT, 2F, 5));

        p.addCard(cp.getCard(new CardWS(6, "green")));
        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.STRAIGHT, 2F, 5));
        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.STRAIGHT, 2F, 6));
        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.STRAIGHT, 2F, 7));

        p.giveCard(cp.getCard(new CardWS(6, "green")));
        Assert.assertFalse(p.canPlayMahjongValue(3F, FoldType.STRAIGHT, 2F, 6));

        p.addCard(cp.getCard(new CardWS(16, "white")));
        Assert.assertTrue(p.canPlayMahjongValue(3F, FoldType.STRAIGHT, 2F, 6));

        p.resetCards();

        p.addCard(cp.getCard(new CardWS(5, "red")));
        Assert.assertFalse(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.addCard(cp.getCard(new CardWS(5, "blue")));
        Assert.assertFalse(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.addCard(cp.getCard(new CardWS(6, "blue")));
        Assert.assertFalse(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.addCard(cp.getCard(new CardWS(6, "green")));
        Assert.assertTrue(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.giveCard(cp.getCard(new CardWS(6, "blue")));
        Assert.assertFalse(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.addCard(cp.getCard(new CardWS(16, "white")));
        Assert.assertTrue(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.giveCard(cp.getCard(new CardWS(8, "blue")));
        p.giveCard(cp.getCard(new CardWS(11, "red")));
        Assert.assertTrue(p.canPlayMahjongValue(5F, FoldType.STRAIGHTPAIR, 4F, 2));

        p.resetCards();
        p.addCard(cp.getCard(new CardWS(4, "red")));
        p.addCard(cp.getCard(new CardWS(4, "black")));
        p.addCard(cp.getCard(new CardWS(4, "blue")));
        p.addCard(cp.getCard(new CardWS(6, "blue")));
        p.addCard(cp.getCard(new CardWS(6, "red")));

        Assert.assertTrue(p.canPlayMahjongValue(6F, FoldType.FULLHOUSE, 3F, null));
        Assert.assertFalse(p.canPlayMahjongValue(6F, FoldType.FULLHOUSE, 4F, null));
        Assert.assertFalse(p.canPlayMahjongValue(5F, FoldType.FULLHOUSE, null, null));

        p.giveCard(cp.getCard(new CardWS(4, "blue")));
        p.addCard(cp.getCard(new CardWS(6, "black")));
        Assert.assertTrue(p.canPlayMahjongValue(6F, FoldType.FULLHOUSE, 4F, null));
        Assert.assertTrue(p.canPlayMahjongValue(6F, FoldType.FULLHOUSE, 5F, null));
        Assert.assertTrue(p.canPlayMahjongValue(4F, FoldType.FULLHOUSE, 5F, null));

        p.giveCard(cp.getCard(new CardWS(6, "black")));
        Assert.assertFalse(p.canPlayMahjongValue(6F, FoldType.FULLHOUSE, 4F, null));

        p.addCard(cp.getCard(new CardWS(16, "white")));
        Assert.assertTrue(p.canPlayMahjongValue(6F, FoldType.FULLHOUSE, 4F, null));
        Assert.assertTrue(p.canPlayMahjongValue(4F, FoldType.FULLHOUSE, 5F, null));

        p.resetCards();
        p.addCard(cp.getCard(new CardWS(4, "red")));
        p.addCard(cp.getCard(new CardWS(6, "blue")));
        p.addCard(cp.getCard(new CardWS(6, "red")));
        p.addCard(cp.getCard(new CardWS(16, "white")));

        Assert.assertFalse(p.canPlayMahjongValue(4F, FoldType.FULLHOUSE, 5F, null));

        p.addCard(cp.getCard(new CardWS(6, "black")));
        Assert.assertTrue(p.canPlayMahjongValue(4F, FoldType.FULLHOUSE, 5F, null));

        p.resetCards();
        p.addCard(cp.getCard(new CardWS(8, "blue")));
        p.addCard(cp.getCard(new CardWS(9, "green")));
        p.addCard(cp.getCard(new CardWS(9, "blue")));
        p.addCard(cp.getCard(new CardWS(10, "blue")));
        p.addCard(cp.getCard(new CardWS(11, "blue")));
        p.addCard(cp.getCard(new CardWS(12, "blue")));

        Assert.assertTrue(p.canPlayMahjongValue(10F, FoldType.STRAIGHTBOMB, 3F, 5));
        Assert.assertFalse(p.canPlayMahjongValue(5F, FoldType.STRAIGHTBOMB, 3F, 5));
        Assert.assertFalse(p.canPlayMahjongValue(13F, FoldType.STRAIGHTBOMB, 3F, 5));

        p.giveCard(cp.getCard(new CardWS(12, "blue")));
        p.addCard(cp.getCard(new CardWS(16, "white")));
        Assert.assertFalse(p.canPlayMahjongValue(10F, FoldType.STRAIGHTBOMB, 3F, 5));

        p.resetCards();

        p.addCard(cp.getCard(new CardWS(9, "green")));
        p.addCard(cp.getCard(new CardWS(9, "blue")));
        p.addCard(cp.getCard(new CardWS(10, "blue")));
        p.addCard(cp.getCard(new CardWS(11, "blue")));
        p.addCard(cp.getCard(new CardWS(12, "blue")));
        Assert.assertFalse(p.canPlayMahjongValue(9F, FoldType.SINGLE, 11F, null));

        p.addCard(cp.getCard(new CardWS(8, "red")));
        Assert.assertFalse(p.canPlayMahjongValue(9F, FoldType.SINGLE, 11F, null));

        p.addCard(cp.getCard(new CardWS(8, "blue")));
        Assert.assertTrue(p.canPlayMahjongValue(9F, FoldType.SINGLE, 11F, null));

    }
}
