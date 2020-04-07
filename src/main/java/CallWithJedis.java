import redis.clients.jedis.Jedis;

/**
 * 通过接口实现约束
 */
public interface CallWithJedis {
    void call(Jedis jedis);
}
