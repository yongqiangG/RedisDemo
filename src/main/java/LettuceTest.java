import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

/**
 * 简单的Lettuce测试
 */
public class LettuceTest {

    public static void main(String[] args) {
        RedisClient redisClient = RedisClient.create("redis://johnny@39.101.193.132");
        StatefulRedisConnection<String, String> connect = redisClient.connect();
        final RedisCommands<String, String> sync = connect.sync();
        sync.set("name","johnny");
        final String name = sync.get("name");
        System.out.println("name = " + name);
    }
}
