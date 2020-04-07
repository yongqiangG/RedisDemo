import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 三种简单的连接池使用
 */
public class PoolTest {
    /**
     * 该方法未保证归还连接
     */
    public static void poolTest1(){
        JedisPool jedisPool = new JedisPool("39.101.193.132", 6379);
        Jedis jedis = jedisPool.getResource();
        jedis.auth("johnny");
        String ping = jedis.ping();
        System.out.println("ping = " + ping);
        jedis.close();
    }

    /**
     * 保证归还连接,但代码较为啰嗦
     */
    public static void poolTest2(){
        JedisPool jedisPool = new JedisPool("39.101.193.132", 6379);
        Jedis jedis = jedisPool.getResource();
        try{
            jedis.auth("johnny");
            String ping = jedis.ping();
            System.out.println("ping = " + ping);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
    }

    /**
     * try{}catch(){}finally{}的语法糖,语法简化,但本质上执行的底层代码还是一样的
     */
    public static void poolTest3(){
        JedisPool jedisPool = new JedisPool("39.101.193.132", 6379);
        try(Jedis jedis = jedisPool.getResource()){
            jedis.auth("johnny");
            String ping = jedis.ping();
            System.out.println("ping = " + ping);
        }
    }


    public static void main(String[] args) {
        //poolTest1();
        //poolTest2();
        poolTest3();
    }
}
