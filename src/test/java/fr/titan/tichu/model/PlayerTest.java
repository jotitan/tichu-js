package fr.titan.tichu.model;

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
}
