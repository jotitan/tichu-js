package fr.titan.tichu.model;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.junit.Test;

/**
 * User: Titan
 * Date: 29/03/14
 * Time: 17:08
 */
public class GameTest {

    @Test
    public void testDistribute(){
        Game game = new Game();
        game.createCards();
        Assert.assertEquals(56,game.getCards().size());

        game.distribute();
        for(Player player : game.getPlayers()){
            Assert.assertEquals(14,player.getCards().size());
        }
    }
}
