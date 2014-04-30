package fr.titan.tichu.tools;

import com.google.common.base.Joiner;
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
            System.out.print("# ");
            value = scanner.nextLine();
            switch (value.toLowerCase()) {
            case "stats":
                embed.showStats();
                break;
            case "reset":
                embed.resetKeys();
                break;
            case "keys":
                embed.keys();
                break;
            case "exit":
                System.out.println("Bye");
                break;
            case "help":
                System.out.println("reset\nstats\nkeys\nshow key\nexit");
                break;
            default:
                if (value.startsWith("show ")) {
                    embed.show(value.substring(5));
                }
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

    public void show(String key) {
        switch (jedis.type(key)) {
        case "string":
            System.out.println(key + " : " + jedis.get(key));
            break;
        case "list":
            System.out.println("LIST : " + key + " (" + jedis.llen(key) + "): " + Joiner.on(",").join(jedis.lrange(key, 0, 10)));
            break;
        case "set":
            System.out.println("SET : " + key + " (" + jedis.smembers(key).size() + "): " + Joiner.on(",").join(jedis.smembers(key)));
            break;
        default:
            System.out.println("Unknown type for " + key);
        }
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
