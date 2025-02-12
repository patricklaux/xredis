package com.igeeksky.xredis.api;

import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xredis.stream.container.StreamContainer;
import com.igeeksky.xredis.stream.container.StreamGenericContainer;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import io.lettuce.core.codec.RedisCodec;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperatorFactory {

    /**
     * 创建新的 Redis 批操作客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@link Pipeline} – Redis 客户端
     */
    <K, V> Pipeline<K, V> pipeline(RedisCodec<K, V> codec);

    /**
     * 创建新的 Redis 同步操作客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@link RedisSyncOperator} – Redis 同步操作客户端
     */
    <K, V> RedisSyncOperator<K, V> redisSyncOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 Redis 异步操作客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@link RedisAsyncOperator} – Redis 异步操作客户端
     */
    <K, V> RedisAsyncOperator<K, V> redisAsyncOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 Redis 响应式操作客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@link RedisReactiveOperator} – Redis 响应式操作客户端
     */
    <K, V> RedisReactiveOperator<K, V> redisReactiveOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 Redis 客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@linkplain RedisOperator} – Redis 客户端
     */
    <K, V> RedisOperator<K, V> redisOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 Redis 流容器
     *
     * @param codec     RedisCodec
     * @param interval  流任务执行间隔，单位毫秒
     * @param scheduler 定时任务调度器
     * @param <K>       键类型
     * @param <V>       值类型
     * @return {@linkplain StreamContainer} – Redis 流容器
     */
    <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, long interval,
                                                 ScheduledExecutorService scheduler);

    /**
     * 创建新的 Redis 流容器（仅适用于非消费者组）
     * <p>
     * 使用公共的 XReadArgs 参数，将所有流合并到一个 xread 命令进行读取，以减少命令阻塞。
     *
     * @param codec     RedisCodec
     * @param interval  流任务执行间隔，单位毫秒
     * @param options   拉取流消息时采用的公共参数
     * @param scheduler 定时任务调度器
     * @param <K>       键类型
     * @param <V>       值类型
     * @return {@linkplain StreamGenericContainer} – Redis 流容器
     */
    <K, V> StreamGenericContainer<K, V> streamGenericContainer(RedisCodec<K, V> codec, long interval,
                                                               XReadOptions options, ScheduledExecutorService scheduler);


    /**
     * 创建新的虚拟线程池
     *
     * @return {@linkplain ExecutorService} – 虚拟线程池
     */
    default ExecutorService newVirtualThreadPerTaskExecutor() {
        return Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("stream-thread-"));
    }

    /**
     * 关闭 Redis 客户端工厂
     */
    void shutdown();

    /**
     * 关闭 Redis 客户端工厂（异步）
     *
     * @return {@linkplain CompletableFuture} – 关闭结果
     */
    CompletableFuture<Void> shutdownAsync();

}