package fr.titan.tichu.service.cache;

import fr.titan.tichu.model.Game;
import fr.titan.tichu.model.Player;
import fr.titan.tichu.service.ObjectHelper;
import redis.clients.jedis.Jedis;

/**
 *
 */
public class RedisGameCache implements GameCache{

    private Jedis jedis;

    public RedisGameCache(String host, int port)throws Exception{
        jedis = new Jedis(host,port);
        try{
            jedis.connect();
        }catch(Exception e){
            throw new Exception("Cannot connect to redis server");
        }
    }

    @Override
    public boolean addGame(Game game) {
        String key = "game:" + game.getGame();
        // If first insert, save token user
        return "OK".equals(jedis.set(key.getBytes(), ObjectHelper.serialize(game)));
    }

    @Override
    public Game getGame(String name) {
        String key = "game:" + name;
        byte[] game = jedis.get(key.getBytes());
        if(game == null){
            return null;
        }
        return ObjectHelper.deserialize(Game.class, game);
    }

    @Override
    public void removeGame(String name) {
        String key = "game:" + name;
        jedis.del(key);
    }

    public void close(){
        jedis.close();
    }

    @Override
    public void addPlayer(Player player) {
        String key = "player:" + player.getToken();
        jedis.set(key.getBytes(),ObjectHelper.serialize(player));
    }

    @Override
    public Player getPlayer(String token) {
        String key = "player:" + token;
        byte[] player = jedis.get(key.getBytes());
        if(player == null){
            return null;
        }
        return ObjectHelper.deserialize(Player.class,player);
    }
}
