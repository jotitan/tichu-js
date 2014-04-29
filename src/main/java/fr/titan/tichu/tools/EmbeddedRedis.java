package fr.titan.tichu.tools;

import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import java.util.Scanner;
import java.util.Set;

/**
 *
 */
public class EmbeddedRedis {
    private RedisServer server;
    private Jedis jedis;

    public static void main(String[] args) throws Exception {
        EmbeddedRedis embed = new EmbeddedRedis();
        Scanner scanner = new Scanner(System.in);
        String value = null;
        do {
            System.out.println("Command ?");
            value = scanner.nextLine();
            switch (value) {
            case "stats":
                embed.showStats();
                break;
            case "reset":
                embed.resetKeys();
                break;
            case "keys":
                embed.keys();
                break;

            }
        } while (!"exit".equals(value));

        embed.stop();
    }

    public EmbeddedRedis() throws Exception {
        server = new RedisServer(7600);
        server.start();
        jedis = new Jedis("localhost", 7600);
    }

    public void stop() throws Exception {
        jedis.close();
        server.stop();
    }

    private void keys() {
        for (String key : jedis.keys("*")) {
            System.out.print(key + " , ");
        }
        System.out.println("");
    }

    private void showStats() {
        Set<String> keys = jedis.keys("*");
        System.out.println("We have " + keys.size() + " keys in redis");
    }

    private void resetKeys() {
        jedis.del(jedis.keys("*").toArray(new String[] {}));
    }
}
