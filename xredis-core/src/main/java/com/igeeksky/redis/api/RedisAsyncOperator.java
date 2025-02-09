package com.igeeksky.redis.api;

/**
 * Redis 异步操作接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisAsyncOperator<K, V> extends RedisAsyncCommands<K, V> {

}
