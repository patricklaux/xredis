package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.ConnectionMode;
import com.igeeksky.xtool.core.AsyncCloseable;

/**
 * Redis 操作入口
 * <p>
 * {@link RedisSyncOperator} {@link RedisAsyncOperator} {@link RedisReactiveOperator} 共用一条链接。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisOperator<K, V> extends ConnectionMode, AsyncCloseable {

    /**
     * 获取同步操作客户端
     *
     * @return RedisSyncOperator
     */
    RedisSyncOperator<K, V> sync();

    /**
     * 获取异步操作客户端
     *
     * @return RedisAsyncOperator
     */
    RedisAsyncOperator<K, V> async();

    /**
     * 获取响应式操作客户端
     *
     * @return RedisReactiveOperator
     */
    RedisReactiveOperator<K, V> reactive();

}
