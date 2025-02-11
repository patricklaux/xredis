package com.igeeksky.xredis.api;

import io.lettuce.core.api.AsyncCloseable;

/**
 * Redis 操作入口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisOperator<K, V> extends RedisMode, AsyncCloseable {

    /**
     * 同步操作
     *
     * @return RedisSyncOperator
     */
    RedisSyncOperator<K, V> sync();

    /**
     * 异步操作
     *
     * @return RedisAsyncOperator
     */
    RedisAsyncOperator<K, V> async();

    /**
     * 响应式操作
     *
     * @return RedisReactiveOperator
     */
    RedisReactiveOperator<K, V> reactive();

    /**
     * 批量操作
     *
     * @return Pipeline
     */
    Pipeline<K, V> pipeline();

}
