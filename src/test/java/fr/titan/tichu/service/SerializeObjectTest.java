package fr.titan.tichu.service;

import fr.titan.tichu.model.Game;
import junit.framework.Assert;
import org.junit.Test;

/**
 * User: Titan
 * Date: 26/04/14
 * Time: 23:32
 */
public class SerializeObjectTest {

    @Test
    public void testSerialize(){
        Game game = new Game("Name");

        byte[] result = ObjectHelper.serialize(game);
        Assert.assertNotNull(result);

        Game game2 = ObjectHelper.deserialize(Game.class,result);
        Assert.assertNotNull(game2);
    }
}