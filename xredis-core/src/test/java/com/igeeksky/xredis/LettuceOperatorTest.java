package com.igeeksky.xredis;

import com.igeeksky.xredis.api.Pipeline;
import com.igeeksky.xredis.api.RedisSyncOperator;
import com.igeeksky.xtool.core.lang.LongValue;
import io.lettuce.core.*;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
class LettuceOperatorTest {

    private static RedisClient redisClient;
    private static ClientResources resources;
    private static LettuceOperator<String, String> redisOperator;

    @BeforeAll
    static void beforeAll() {
        RedisURI uri = RedisURI.builder().withHost("127.0.0.1").withPort(6380).build();
        resources = DefaultClientResources.create();
        redisClient = RedisClient.create(resources);
        redisClient.setOptions(ClientOptions.create());

        RedisCodec<String, String> codec = StringCodec.UTF8;
        StatefulRedisConnection<String, String> connection = redisClient.connect(codec, uri);
        StatefulRedisConnection<String, String> batchConnection = redisClient.connect(codec, uri);
        connection.setAutoFlushCommands(true);
        batchConnection.setAutoFlushCommands(false);

        redisOperator = new LettuceOperator<>(connection, batchConnection, codec);
    }

    @AfterAll
    static void afterAll() {
        redisOperator.closeAsync()
                .thenAccept(v -> {
                    redisClient.shutdown();
                    resources.shutdown();
                });
    }

    @Test
    void getConnection() {
        StatefulConnection<String, String> connection = redisOperator.sync().getConnection();
        StatefulConnection<String, String> connection1 = redisOperator.async().getConnection();
        StatefulConnection<String, String> connection2 = redisOperator.reactive().getConnection();
        StatefulConnection<String, String> connection3 = redisOperator.pipeline().getConnection();

        StatefulRedisConnection<String, String> statefulConnection = redisOperator.sync().getStatefulConnection();
        StatefulRedisConnection<String, String> statefulConnection1 = redisOperator.async().getStatefulConnection();
        StatefulRedisConnection<String, String> statefulConnection2 = redisOperator.reactive().getStatefulConnection();
        StatefulRedisConnection<String, String> statefulConnection3 = redisOperator.pipeline().getStatefulConnection();

        Assertions.assertSame(connection, connection1);
        Assertions.assertSame(connection, connection2);
        Assertions.assertSame(connection, statefulConnection);
        Assertions.assertSame(connection, statefulConnection1);
        Assertions.assertSame(connection, statefulConnection2);
        Assertions.assertSame(connection3, statefulConnection3);
        Assertions.assertNotSame(connection, connection3);
    }

    @Test
    void syncSetAndGet() {
        RedisSyncOperator<String, String> sync = redisOperator.sync();

        int size = 100;
        List<String> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(sync.set("key" + i, "value" + i));
        }

        System.out.println(list.size());

        for (int i = 0; i < size; i++) {
            if (i % 10 == 0) {
                System.out.println(list.get(i));
            }
        }

        for (int i = 0; i < size; i++) {
            if (i % 10 == 0) {
                System.out.println(sync.get("key" + i));
            }
        }
    }

    @Test
    void asyncSetAndGet() {
        int size = 100000;
        List<RedisFuture<String>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(redisOperator.async().set("key" + i, "value" + i));
        }

        System.out.println(list.size());

        for (int i = 0; i < size; i++) {
            list.get(i).toCompletableFuture().join();
        }

        for (int i = 0; i < size; i++) {
            if (i % 10000 == 0) {
                System.out.println(redisOperator.async().get("key" + i).toCompletableFuture().join());
            }
        }
    }

    @Test
    void reactiveSetAndGet() {
        int size = 100000;
        List<Mono<String>> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(redisOperator.reactive().set("key" + i, "value" + i));
        }

        System.out.println(list.size());

        for (int i = 0; i < size; i++) {
            list.get(i).block();
        }

        for (int i = 0; i < size; i++) {
            if (i % 10000 == 0) {
                System.out.println(redisOperator.reactive().get("key" + i).block());
            }
        }
    }

    @Test
    void pipelineSetAndGet() throws ExecutionException, InterruptedException {
        redisOperator.sync().flushdb();

        int size = 100000;
        List<RedisFuture<String>> futures = new ArrayList<>(size);
        Pipeline<String, String> pipeline = redisOperator.pipeline();
        for (int i = 0; i < size; i++) {
            futures.add(pipeline.set("key" + i, "value" + i));
        }
        pipeline.flushCommands();

        System.out.println(futures.size());

        for (int i = 0; i < size; i++) {
            futures.get(i).get();
        }

        for (int i = 0; i < size; i++) {
            if (i % 10000 == 0) {
                System.out.println(redisOperator.async().get("key" + i).get());
            }
        }
    }

    @Test
    void asyncXread() throws ExecutionException, InterruptedException {
        String stream = "my-stream";
        LettuceAsyncOperator<String, String> async = redisOperator.async();
        async.del(stream);

        async.xadd(stream, "key1", "value1");
        async.xadd(stream, "key1", "value2");

        Thread.sleep(10);

        XReadArgs readArgs = XReadArgs.Builder.block(5000);
        StreamOffset<String> offset = StreamOffset.from(stream, System.currentTimeMillis() + "-0");
        RedisFuture<List<StreamMessage<String, String>>> xread = async.xread(readArgs, offset);

        // 测试执行阻塞命令后对其它命令的影响
        long start = System.currentTimeMillis();
        RedisFuture<String> future = async.get("key1");

        System.out.println(future.get());
        long end = System.currentTimeMillis();
        System.out.println(end - start);

        Assertions.assertTrue(end - start > 4000);

        System.out.println(xread.get());
    }

    /**
     * 测试连续执行两个阻塞命令，Lettuce 是否可以正确返回结果
     * <p>
     * 命令提交顺序：xread1 ——> xread2
     * 额外线程发送消息到 stream2
     * 如果 Lettuce 客户端的 两个 xread 命令是同时提交的，那么将会先返回 xread2 的结果。
     * 测试结果表明 Lettuce 客户端执行两个阻塞命令时，如果命令提交顺序是 xread1 ——> xread2，那么一定会先返回 xread1 的结果。
     * 底层链接估计检测到提交的是阻塞命令，则不会继续执行其它命令，而是等待并得到当前阻塞命令的返回结果后再发送后续命令。
     * 因此，底层可以保证结果正确，不会将 xread2 的结果当成 xread1 的结果。
     */
    @Test
    void asyncXread1() {
        String stream1 = "stream1";
        String stream2 = "stream2";
        LettuceAsyncOperator<String, String> async = redisOperator.async();
        async.del(stream1);
        async.del(stream2);

        XReadArgs readArgs1 = XReadArgs.Builder.block(2000);
        StreamOffset<String> offset1 = StreamOffset.from(stream1, System.currentTimeMillis() + "-0");
        CompletableFuture<List<StreamMessage<String, String>>> future1 = async.xread(readArgs1, offset1).toCompletableFuture();

        XReadArgs readArgs2 = XReadArgs.Builder.block(2000);
        StreamOffset<String> offset2 = StreamOffset.from(stream2, System.currentTimeMillis() + "-0");
        CompletableFuture<List<StreamMessage<String, String>>> future2 = async.xread(readArgs2, offset2).toCompletableFuture();

        new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("stream2-add-message-start");
            redisOperator.pipeline().xadd(stream2, "stream2-key1", "stream2-value1");
            redisOperator.pipeline().flushCommands();
            System.out.println("stream2-add-message-end");
        }).start();

        LongValue resultTime1 = new LongValue();
        LongValue resultTime2 = new LongValue();
        new Thread(() -> {
            List<StreamMessage<String, String>> result1 = future1.join();
            resultTime1.set(System.currentTimeMillis());
            System.out.println("result1:" + resultTime1.get());
            System.out.println("result1:" + result1);
            Assertions.assertEquals(0, result1.size());
        }).start();

        new Thread(() -> {
            List<StreamMessage<String, String>> result2 = future2.join();
            resultTime2.set(System.currentTimeMillis());
            System.out.println("result2:" + resultTime2.get());
            System.out.println("result2:" + result2);
            Assertions.assertEquals(1, result2.size());
            Assertions.assertEquals(Map.of("stream2-key1", "stream2-value1"), result2.getFirst().getBody());
        }).start();

        LockSupport.parkNanos(TimeUnit.SECONDS.toNanos(5));

        Assertions.assertTrue(resultTime1.get() < resultTime2.get());
    }

    @Test
    void asyncMulti() throws ExecutionException, InterruptedException {
        LettuceAsyncOperator<String, String> async = redisOperator.async();

        async.multi();
        RedisFuture<String> set1 = async.set("key1", "value1");
        RedisFuture<String> set2 = async.set("key2", "value2");
        TransactionResult result = async.exec().get();

        Assertions.assertEquals("OK", result.get(0).toString());
        Assertions.assertEquals("OK", result.get(1).toString());

        Assertions.assertEquals("OK", set1.get());
        Assertions.assertEquals("OK", set2.get());
    }

    @Test
    void syncMulti() {
        LettuceSyncOperator<String, String> sync = redisOperator.sync();

        sync.multi();
        String set1 = sync.set("key1", "value1");
        String set2 = sync.set("key2", "value2");
        TransactionResult result = sync.exec();

        Assertions.assertEquals("OK", result.get(0).toString());
        Assertions.assertEquals("OK", result.get(1).toString());

        Assertions.assertNull(set1);
        Assertions.assertNull(set2);
    }

    @Test
    void autoFlush() {
        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.sync().setAutoFlushCommands(false));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.sync().setAutoFlushCommands(true));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.async().setAutoFlushCommands(false));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.async().setAutoFlushCommands(true));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.reactive().setAutoFlushCommands(false));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.reactive().setAutoFlushCommands(true));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.pipeline().setAutoFlushCommands(false));

        Assertions.assertThrowsExactly(UnsupportedOperationException.class,
                () -> redisOperator.pipeline().setAutoFlushCommands(true));
    }

}