import redis.clients.jedis.Jedis;

/**
 * jedis客户端连接测试
 */
public class ConnectTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("39.101.193.132");
        jedis.auth("johnny");
        String ping = jedis.ping();
        System.out.println("ping = " + ping);
    }
}
