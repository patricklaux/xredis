package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.common.stream.container.StreamGenericContainer;
import com.igeeksky.xtool.core.GracefulShutdown;
import io.lettuce.core.codec.RedisCodec;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperatorFactory extends GracefulShutdown {

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
     * 创建新的 Redis 客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@linkplain RedisOperator} – Redis 客户端
     */
    <K, V> RedisOperator<K, V> redisOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 Redis 流操作客户端
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec RedisCodec
     * @return {@linkplain StreamOperator} – Redis 客户端
     */
    <K, V> StreamOperator<K, V> streamOperator(RedisCodec<K, V> codec);

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
     * @return {@linkplain StreamContainer} – Redis 流容器
     */
    <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler, long interval, ReadOptions options);

    /**
     * 创建新的 Redis 流容器
     *
     * @param codec     RedisCodec
     * @param interval  流任务执行间隔，单位毫秒
     * @param scheduler 定时任务调度器
     * @param <K>       键类型
     * @param <V>       值类型
     * @return {@linkplain StreamGenericContainer} – Redis 流容器
     */
    <K, V> StreamGenericContainer<K, V> streamGenericContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler, long interval);

}