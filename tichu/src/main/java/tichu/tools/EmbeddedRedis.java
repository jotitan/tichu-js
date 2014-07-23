package tichu.tools;

import com.google.common.base.Joiner;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.embedded.RedisServer;

import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

/**
 *
 */
public class EmbeddedRedis {
    private RedisServer server;
    private JedisPool jedisPool;
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
                embed.keys("*");
                break;
            case "exit":
                System.out.println("Bye");
                break;
            case "help":
                embed.help();
                break;
            default:
                if (value.startsWith("keys ")) {
                    embed.keys(value.substring(5) + "*");
                }
                if (value.startsWith("show ")) {
                    embed.show(value.substring(5));
                }
                if (value.startsWith("save ")) {
                    embed.save(value.substring(5));
                }
                if (value.startsWith("restore ")) {
                    embed.restore(value.substring(8));
                }
                if (value.startsWith("delete ")) {
                    embed.delete(value.substring(7));
                }
            }
        } while (!"exit".equals(value));

        embed.stop();
    }

    public EmbeddedRedis() throws Exception {
        Properties p = new Properties();
        p.load(getClass().getResourceAsStream("/tichu.properties"));
        if("localhost".equals(p.getProperty("redis.host"))){
            // Local server
            server = new RedisServer(Integer.valueOf(p.getProperty("redis.port")));
            server.start();
        }
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(12);
        if(p.containsKey("redis.pass") && !"".equals(p.getProperty("redis.pass").trim())){
            jedisPool = new JedisPool(config,p.getProperty("redis.host"),Integer.valueOf(p.getProperty("redis.port")),1000,p.getProperty("redis.pass"));
        }else{
            jedisPool = new JedisPool(config,p.getProperty("redis.host"),Integer.valueOf(p.getProperty("redis.port")),1000);
        }

        jedis = jedisPool.getResource();
        if(jedis.isConnected()){
            System.out.println("Connected on " + p.getProperty("redis.host") + ":" + p.getProperty("redis.port"));
        }
    }

    public void help() {
        StringBuilder builder = new StringBuilder();
        builder.append("delete (usage : delete key) : ").append("Delete specific key").append("\n").append("exit : ").append("Shutdown redis")
                .append("\n").append("keys : ").append("List all keys").append("\n").append("reset : ").append("Delete all keys").append("\n")
                .append("restore (usage : restore pattern) : ").append("Restore all keys previously save with pattern").append("\n")
                .append("save (usage : save pattern) : ").append("Save all keys previously matching pattern (add .save to key)").append("\n")
                .append("show (usage : show key) : ").append("Show the value of a key").append("\n").append("stats : ").append("Show number of keys")
                .append("\n");
        System.out.println(builder.toString());

    }

    public void stop() throws Exception {
        jedisPool.returnResource(jedis);
        if(server != null){
            server.stop();
        }
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

    /**
     * Save keys by pattern. Suffix with .old
     * 
     * @param pattern
     */
    public void save(String pattern) {
        Set<String> keys = jedis.keys(pattern);
        for (String key : keys) {
            // Don't save key if represent of save (end with .save)
            if (!key.endsWith(".save")) {
                String saveKey = key + ".save";
                copyTo(key, saveKey);
            }
        }
        System.err.println(keys.size() + " keys saved");
    }

    private void copyTo(String keyFrom, String keyTo) {
        jedis.del(keyTo);
        switch (jedis.type(keyFrom)) {
        case "string":
            jedis.set(keyTo.getBytes(), jedis.get(keyFrom.getBytes()));
            break;
        case "set":
            jedis.sadd(keyTo, jedis.smembers(keyFrom).toArray(new String[] {}));
            break;
        case "list":
            jedis.rpush(keyTo, jedis.lrange(keyFrom, 0, jedis.llen(keyFrom)).toArray(new String[] {}));
            break;
        }
    }

    public void delete(String key) {
        if (jedis.del(key) > 0) {
            System.err.println(key + " deleted");
        }

    }

    public void restore(String pattern) {
        Set<String> keys = jedis.keys(pattern + ".save");
        for (String key : keys) {
            String restoreKey = key.replace(".save", "");
            copyTo(key, restoreKey);
        }
        System.err.println(keys.size() + " keys restored");
    }

    private void keys(String pattern) {
        System.out.println(Joiner.on(" , ").join(jedis.keys(pattern)));
    }

    private void showStats() {
        Set<String> keys = jedis.keys("*");
        System.out.println("We have " + keys.size() + " keys in redis");
    }

    private void resetKeys() {
        String[] keys = jedis.keys("*").toArray(new String[] {});
        if (keys.length > 0) {
            long size = jedis.del(keys);
            System.err.println("Delete " + size + " keys, database empty");
        }
    }
}
