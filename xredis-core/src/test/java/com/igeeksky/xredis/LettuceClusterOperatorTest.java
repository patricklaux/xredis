package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.api.RedisOperatorFactory;
import com.igeeksky.xredis.cases.LettuceTestHelper;
import com.igeeksky.xredis.cases.RedisOperatorProxyTestCase;
import com.igeeksky.xredis.cases.RedisOperatorTestCase;
import io.lettuce.core.codec.ByteArrayCodec;
import org.junit.jupiter.api.*;

/**
 * Lettuce 集群操作测试
 *
 * @author patrick
 * @since 0.0.4 2024/5/8
 */
class LettuceClusterOperatorTest {

    private static RedisOperatorFactory factory;
    private static RedisOperator<byte[], byte[]> redisOperator;
    private static RedisOperatorTestCase redisTestCase;
    private static RedisOperatorProxyTestCase redisProxyTestCase;

    @BeforeAll
    public static void beforeAll() {
        factory = LettuceTestHelper.createClusterConnectionFactory();
        redisOperator = factory.redisOperator(ByteArrayCodec.INSTANCE);
        redisTestCase = new RedisOperatorTestCase(redisOperator);
        redisProxyTestCase = new RedisOperatorProxyTestCase(redisOperator);
    }

    @AfterAll
    public static void afterAll() {
        redisOperator.closeAsync().thenAccept(r -> factory.shutdown());
    }

    @Test
    @Disabled
    void testAll() {
        redisTestCase.testAll();
        redisProxyTestCase.testAll();
    }

    @Test
    void isCluster() {
        Assertions.assertTrue(redisTestCase.isCluster());
    }

    @Test
    void hset() {
        redisProxyTestCase.hset();
    }

    @Test
    void hmset2() {
        for (int i = 0; i < 10; i++) {
            redisProxyTestCase.hmset2();
        }
    }

    @Test
    void hget() {
        redisProxyTestCase.hget();
    }

    @Test
    void hdel() {
        redisProxyTestCase.hdel();
    }

    @Test
    void hdel2() {
        redisProxyTestCase.hdel2();
    }

    @Test
    void hmget() {
        redisProxyTestCase.hmget();
    }

    @Test
    void hmget2() {
        for (int i = 0; i < 10; i++) {
            redisProxyTestCase.hmget2();
        }
    }


    @Test
    void psetex() {
        redisProxyTestCase.psetex();
    }

    /**
     * 特殊测试
     * <p>
     * Redis Hash 字段设置值 及 过期时间
     * <p>
     * 注意：Redis 版本需大于 7.4.0
     */
    @Test
    @Disabled
    void hpset() {
        redisProxyTestCase.hpset();
    }

    /**
     * 特殊测试
     * <p>
     * Redis Hash 字段设置值 及 过期时间（RedisServer 版本需大于 7.4.0）
     */
    @Test
    @Disabled
    void hmpset() {
        redisProxyTestCase.hmpset();
    }

    @Test
    void clear() {
        redisProxyTestCase.clear("test-*");
    }

    /**
     * 性能测试
     * <p>
     * 200万数据，本机redis，单线程性能测试时长约 310128 ms
     */
    @Test
    @Disabled
    void psetexPerformance1() {
        redisTestCase.psetexPerformance1();
    }

    /**
     * 性能测试
     * <p>
     * 200万数据，本地redis，10线程性能测试，单个线程耗时约 20798 ms，总耗时约为 20798 * 10
     * <p>
     * 200万数据，本地redis，单线程性能测试时长约 310128 ms
     * <p>
     * 测试结果表明，即使只有一个 Lettuce 连接，依然可以提供并行处理能力，从而降低时长。
     * 因此，Lettuce 在不使用事务(MULTI)及阻塞命令 (BLPOP……)时，无需开启连接池。
     */
    @Test
    @Disabled
    void psetexPerformance2() {
        redisTestCase.psetexPerformance2();
    }

    /**
     * 性能测试
     * <p>
     * 1000万数据，单线程，本地redis，单线程批处理，性能测试时长约 42288 毫秒
     */
    @Test
    @Disabled
    void psetexPipelinePerformance1() {
        redisProxyTestCase.psetexPipelinePerformance1();
    }

    /**
     * 性能测试
     * <p>
     * 1000万数据，本地redis，2线程批处理，耗时约 18915 + 18931 秒
     * <p>
     * 1000万数据，本地redis，单线程批处理，耗时约 42288 毫秒
     * <p>
     * 测试结果表明：
     * 批处理能够有效提高性能，此时的性能瓶颈在于跨进程跨网络数据交换；
     * Lettuce 在不使用事务(MULTI)及阻塞命令 (,BLPOP……)时，无需开启连接池
     */
    @Test
    @Disabled
    void psetexPipelinePerformance2() {
        redisProxyTestCase.psetexPipelinePerformance2();
    }

    /**
     * 性能测试
     * <p>
     * 1000万数据，单线程，本地redis，单线程批处理，性能测试时长约 22919 毫秒
     */
    @Test
    @Disabled
    void msetPerformance1() {
        redisProxyTestCase.msetPerformance1();
    }

    /**
     * 性能测试
     * <p>
     * 1000万数据，本地redis，2线程批处理，耗时约 21187 + 21270 毫秒
     * <p>
     * 1000万数据，本地redis，单线程批处理，耗时约 22919 毫秒
     * <p>
     * 测试结果表明：
     * 批处理能够有效提高性能，此时的性能瓶颈是在于跨进程跨网络数据交换；
     * Lettuce 在不使用事务(MULTI)及阻塞命令 (,BLPOP……)时，无需开启连接池
     */
    @Test
    @Disabled
    void msetPerformance2() {
        redisProxyTestCase.msetPerformance2();
    }

}