package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.cases.LettuceTestHelper;
import com.igeeksky.xredis.lettuce.cases.RedisOperatorProxyTestCase;
import com.igeeksky.xredis.lettuce.cases.RedisOperatorTestCase;
import io.lettuce.core.codec.ByteArrayCodec;
import org.junit.jupiter.api.*;

/**
 * Lettuce Standalone 操作测试
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/5/8
 */
class LettuceStandaloneOperatorTest {

    private static RedisOperatorFactory factory;
    private static RedisOperator<byte[], byte[]> redisOperator;
    private static RedisOperatorTestCase redisTestCase;
    private static RedisOperatorProxyTestCase redisProxyTestCase;

    @BeforeAll
    public static void beforeAll() {
        factory = LettuceTestHelper.createStandaloneFactory();
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
        Assertions.assertFalse(redisTestCase.isCluster());
        Assertions.assertFalse(redisProxyTestCase.isCluster());
    }

    @Test
    void psetex() {
        redisProxyTestCase.psetex();
    }

    @Test
    void psetex2() {
        redisProxyTestCase.psetex_random();
    }

    @Test
    @Disabled
    void psetex3() {
        redisProxyTestCase.psetex3();
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地 Redis，单线程，Script 方式 <br>
     * size: [10000000]	 psetex-cost: [35039]
     */
    @Test
    @Disabled
    void psetex_performance() {
        redisProxyTestCase.psetex(10000000);
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地 Redis，单线程，Script 方式 <br>
     * size: [10000000]	 psetex-random-cost: [45364]
     * <p>
     * 1000 万数据，本地 Redis，单线程，Async 方式 <br>
     * size: [10000000]	 psetex-random-cost: [45644]
     *
     * <p>
     * 本地测试，网络无瓶颈，Standalone 模式下，
     * RedisServer 只能单线程处理命令，Script 和 Async 方式性能几乎完全一致。
     * <br>
     * Cluster 模式下，因为可以并行处理命令，Async 下处理，耗时仅 20765，反而比 Script 快一倍。
     * <p>
     * 网络 Redis，Script : size: [3000000]	 psetex-random-cost: [41876] <br>
     * 网络 Redis，Async  : size: [3000000]	 psetex-random-cost: [49294]
     */
    @Test
    @Disabled
    void psetex_random() {
        redisProxyTestCase.psetex_random(10000000);
    }

    @Test
    void mset() {
        redisProxyTestCase.mset();
        // LockSupport.parkNanos(1000 * 1000 * 1000);
    }

    @Test
    void mget() {
        redisProxyTestCase.mget();
    }

    @Test
    void hset() {
        redisProxyTestCase.hset();
    }

    @Test
    void hmset2() {
        redisProxyTestCase.hmset2();
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
        redisProxyTestCase.hmget2();
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

    /**
     * 特殊测试
     * <p>
     * Redis Hash 字段设置值 及 过期时间（RedisServer 版本需大于 7.4.0）
     */
    @Test
    @Disabled
    void hmpset_random() {
        redisProxyTestCase.hmpset_random();
    }

    /**
     * 特殊测试 和 性能测试
     * <p>
     * Redis Hash 字段设置值 及 过期时间（RedisServer 版本需大于 7.4.0）
     * <p>
     * 1000 万数据，本地 redis，单线程，单链接，Pipeline 批处理
     */
    @Test
    @Disabled
    void hmpset_hmget_hdel() {
        redisProxyTestCase.hmpset_hmget_hdel(1000, 10000, "test-hmpset:");
    }

    /**
     * 性能测试
     * <p>
     * 200万数据，本地redis，单线程性能测试时长约 162137 ms
     */
    @Test
    @Disabled
    void psetexPerformance1() {
        redisTestCase.psetexPerformance1();
    }

    /**
     * 性能测试
     * <p>
     * 200万数据，本地redis，10线程性能测试，单个线程耗时约 33135 ms，总耗时约为 33135 * 10
     * <p>
     * 200万数据，本地redis，单线程性能测试时长约 162137 ms
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
     * 1000 万数据，本地redis，单线程批处理，性能测试
     * size: [10000000], psetex-time: [63363]
     */
    @Test
    @Disabled
    void psetexPipelinePerformance1() {
        redisProxyTestCase.psetexPipelinePerformance1();
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地redis，2线程批处理，性能测试时长约 35830 + 35922 毫秒
     * <p>
     * 1000 万数据，本地redis，单线程批处理，性能测试时长约 64745 毫秒
     * <p>
     * 测试结果表明：
     * 批处理能够有效提高性能，此时的性能瓶颈在于跨进程跨网络数据交换，与及 redis 本身的命令执行性能；
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
     * 1000 万数据，本地redis，单线程批处理，性能测试时长约 20167 毫秒
     */
    @Test
    @Disabled
    void msetPerformance1() {
        redisProxyTestCase.msetPerformance1();
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地redis，2线程批处理，性能测试时长约 13448 + 13533 毫秒
     * <p>
     * 1000 万数据，本地redis，单线程批处理，性能测试时长约 20167 毫秒
     */
    @Test
    @Disabled
    void msetPerformance2() {
        redisProxyTestCase.msetPerformance2();
    }

    /**
     * 性能测试
     * <p>
     * 1000 万数据，本地 redis，单线程，单链接，批处理
     * <p>
     * hmset cost: 6738 <p>
     * hmget cost: 7421 <p>
     * hdel cost: 4284
     */
    @Test
    @Disabled
    void hmset_hmget_hdel_performance() {
        redisProxyTestCase.hmset_hmget_hdel_2(1000, 10000, "test-hmset:");
    }

}