import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.UUID;

/**
 * 分布式锁测试
 */
public class LockTest {
    /**
     * 锁可能得不到释放,形成死锁
     */
    public static void lockTest1(){
        Redis redis = new Redis();
        redis.execute(jedis -> {
            final Long lock = jedis.setnx("k", "v");
            if(lock==1){
                //拿到锁了
                jedis.set("name","johnny");
                final String name = jedis.get("name");
                System.out.println(Thread.currentThread().getName()+"拿到了name => " + name);
                jedis.del("k");
            }else{
                //没拿到锁,有人占用锁
                System.out.println(Thread.currentThread().getName()+"没拿到锁");
            }
        });
    }

    /**
     * 加上锁的过期时间,避免执行业务时出现异常,锁没有及时释放
     * 但是由于获取锁和设置过期时间并非原子操作,依然有可能出现在两个操作之间出现异常造成死锁
     */
    public static void lockTest2(){
        Redis redis = new Redis();
        redis.execute(jedis -> {
            final Long lock = jedis.setnx("k", "v");
            if(lock==1){
                //拿到了锁
                jedis.expire("k",5);
                jedis.set("name", "johnny");
                final String name = jedis.get("name");
                System.out.println(Thread.currentThread().getName()+"拿到了name=>"+name);
                jedis.del("k");
            }else{
                //未拿到锁
                System.out.println(Thread.currentThread().getName()+"没拿到锁");
            }
        });
    }

    /**
     * 合并获取锁和设置过期时间两个操作,避免非原子操作造成的死锁问题
     * 但是如果业务代码执行时间超过了过期时间,造成第一个线程释放了第二个线程的锁,可能会造成线程之间的并发问题
     */
    public static void lockTest3(){
        Redis redis = new Redis();
        redis.execute(jedis -> {
            final String set = jedis.set("k", "v", new SetParams().nx().ex(5));
            if(set!=null&&"OK".equals(set)){
                //拿到锁
                jedis.set("name","johnny");
                final String name = jedis.get("name");
                System.out.println("name => " + name);
                jedis.del("k");
            }else{
                //没拿到锁
                System.out.println("没拿到锁");
            }
        });
    }

    /**
     * 利用uuid解决线程之间释放了非自己线程的锁造成错误的问题
     * 但是在释放锁的时候,要先获取到锁的value并且和uuid比较,有三个操作,显然非原子操作,依然可能造成死锁的问题
     */
    public static void lockTest4(){
        Redis redis = new Redis();
        redis.execute(jedis -> {
            final String value = UUID.randomUUID().toString();
            final String set = jedis.set("k", value, new SetParams().nx().ex(5));
            if(set!=null&&"OK".equals(set)){
                jedis.set("name","johnny");
                final String name = jedis.get("name");
                System.out.println("name = " + name);
                //释放锁的时候不是原子操作
                if(value==jedis.get("k")){
                    jedis.del("k");
                }else{
                    System.out.println("不是自己的锁,不用释放");
                }
            }else{
                //没拿到锁
                System.out.println("没拿到锁");
            }
        });
    }

    /**
     * Lua脚本的SHA1和:b8059ba43af6ffe8bed3db65bac35d452f8115d8
     * 利用Lua脚本将删除锁的非原子操作合并成原子操作
     * Lua脚本可以存在在服务端,利用SHA1值进行调用
     */
    public static void lockTest5(){
        Redis redis = new Redis();
        redis.execute(jedis -> {
            final String value = UUID.randomUUID().toString();
            final String k = jedis.set("k", value, new SetParams().nx().ex(5));
            if(k!=null&&"OK".equals(k)){
                jedis.set("name","johnny");
                final String name = jedis.get("name");
                System.out.println("name = " + name);
                jedis.evalsha("b8059ba43af6ffe8bed3db65bac35d452f8115d8", Arrays.asList("k"),Arrays.asList(value));
            }else{
                System.out.println(Thread.currentThread().getName()+"没拿到锁");
            }
        });
    }

    public static void main(String[] args) {
        //多线程测试
        new Thread(()->{lockTest5();},"A").start();
        new Thread(()->{lockTest5();},"B").start();
        new Thread(()->{lockTest5();},"C").start();
    }
}
