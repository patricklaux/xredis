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
     * 获取 Redis 客户端工厂的 ID
     *
     * @return Redis 客户端工厂的 ID
     */
    String getId();

    /**
     * 创建新的 Pipeline
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec 编解码器
     * @return {@link Pipeline} – Redis 管道操作
     */
    <K, V> Pipeline<K, V> pipeline(RedisCodec<K, V> codec);

    /**
     * 创建新的 RedisOperator
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec 编解码器
     * @return {@linkplain RedisOperator} – Redis 客户端
     */
    <K, V> RedisOperator<K, V> redisOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 StreamOperator
     *
     * @param <K>   键类型
     * @param <V>   值类型
     * @param codec 编解码器
     * @return {@linkplain StreamOperator} – 流操作客户端
     */
    <K, V> StreamOperator<K, V> streamOperator(RedisCodec<K, V> codec);

    /**
     * 创建新的 StreamContainer（仅适用于非消费者组）
     * <p>
     * 所有 Stream 拉取消息使用公共的 {@link ReadOptions}，拉取消息时会将所有流合并到一个 xread 命令进行读取，以减少命令阻塞。
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param codec     编解码器
     * @param period  流任务执行间隔，单位毫秒
     * @param options   拉取流消息时采用的公共参数
     * @param scheduler 定时任务调度器
     * @return {@linkplain StreamContainer} – 使用公共读取参数的流容器
     */
    <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler,
                                                 long period, ReadOptions options);

    /**
     * 创建新的 StreamGenericContainer
     * <p>
     * 每个 Stream 拉取消息使用独立的 {@link ReadOptions}，并且独立发送命令并等待结果，
     * 读取操作是串行的，如果有多个 Stream，可能会因命令阻塞而出现较大时延。
     *
     * @param codec     RedisCodec
     * @param period  流任务执行间隔，单位毫秒
     * @param scheduler 定时任务调度器
     * @param <K>       键类型
     * @param <V>       值类型
     * @return {@linkplain StreamGenericContainer} – 使用独立读取参数的流容器
     */
    <K, V> StreamGenericContainer<K, V> streamGenericContainer(RedisCodec<K, V> codec,
                                                               ScheduledExecutorService scheduler, long period);

}