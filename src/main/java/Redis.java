import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 连接池操作封装
 */
public class Redis {
    private JedisPool jedisPool;

    public Redis(){
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        //最大空闲数
        config.setMaxIdle(300);
        //最大连接数
        config.setMaxTotal(1000);
        //最大等待时长
        config.setMaxWaitMillis(30000);
        //空闲时检查有效性
        config.setTestOnBorrow(true);
        this.jedisPool = new JedisPool(config,"39.101.193.132",6379,30000,"johnny");
    }
    public void execute(CallWithJedis callWithJedis){
        try(Jedis jedis = jedisPool.getResource()){
            callWithJedis.call(jedis);
        }
    }

    public static void main(String[] args) {
        Redis redis=new Redis();
        redis.execute(jedis -> {
            System.out.println(jedis.ping());
        });
    }
}
