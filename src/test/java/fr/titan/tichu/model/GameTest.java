package fr.titan.tichu.model;

import fr.titan.tichu.model.ws.PlayerWS;
import fr.titan.tichu.model.ws.RequestType;
import fr.titan.tichu.model.ws.RequestWS;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

/**
 * User: Titan Date: 29/03/14 Time: 17:08
 */
public class GameTest {

    @Test
    public void testDistribute() {
        Game game = new Game();
        game.createCards();
        Assert.assertEquals(56, game.getCardPackage().getCards().size());

        game.distribute();
        for (Player player : game.getPlayers()) {
            Assert.assertEquals(14, player.getCards().size());
        }
    }

    @Test
    public void testObjectMapper() throws Exception {
        ObjectMapper om = new ObjectMapper();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        om.writer().writeValue(out, "Petit test");
        System.out.println(new String(out.toByteArray()));
    }

    @Test
    public void testReadObjectMapper() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String value = "{\"type\":\"FOLD\",\"value\":\"{\\\"name\\\":\\\"Toto\\\",\\\"orientation\\\":\\\"O\\\"}\"}";

        RequestWS request = om.reader(RequestWS.class).readValue(value);
        PlayerWS player = null;

        if (request.getType().equals(RequestType.FOLD)) {
            player = om.reader(PlayerWS.class).readValue(request.getValue());
        }
        System.out.println(player);

    }

}
