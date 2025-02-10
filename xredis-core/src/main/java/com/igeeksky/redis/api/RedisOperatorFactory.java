package com.igeeksky.redis.api;

import com.igeeksky.redis.stream.StreamContainer;
import io.lettuce.core.codec.RedisCodec;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Redis 客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public interface RedisOperatorFactory {

    /**
     * 获取 Redis 客户端
     *
     * @return {@linkplain RedisOperator} – Redis 客户端
     */
    <K, V> RedisOperator<K, V> redisOperator(RedisCodec<K, V> codec);

    /**
     * 获取 Redis 流客户端
     *
     * @return {@linkplain StreamContainer} – Redis 流客户端
     */
    <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler, long interval);

    /**
     * 关闭 Redis 客户端工厂
     */
    void shutdown();

}