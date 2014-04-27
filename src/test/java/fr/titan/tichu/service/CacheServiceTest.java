package fr.titan.tichu.service;

import fr.titan.tichu.model.Game;
import junit.framework.Assert;
import org.junit.Test;

/**
 * User: Titan
 * Date: 26/04/14
 * Time: 19:08
 */
public class CacheServiceTest {

    @Test
    public void testConnection(){
        CacheService cs = new CacheService("192.168.0.20",49154);
        Assert.assertTrue(cs.isConnected());
        cs.close();
    }

    @Test
    public void testInsert(){

        CacheService cs = new CacheService("192.168.0.20",49154);
        long begin = System.currentTimeMillis();

        for(int i = 0 ; i < 10 ; i++){
            Game game = new Game("Name" + i);
            cs.saveGame(game);
            Assert.assertNotNull(cs.getGame(game.getGame()));
        }

        /*
        cs.removeGame(1);
        Assert.assertNull(cs.getGame(1));
        */

        System.out.println((System.currentTimeMillis()-begin) + " ms");
        cs.close();
    }
}
