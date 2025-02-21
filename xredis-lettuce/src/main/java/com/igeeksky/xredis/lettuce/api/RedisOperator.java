package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.AsyncCloseable;
import com.igeeksky.xredis.common.ConnectionMode;

/**
 * Redis 操作入口
 * <p>
 * 此接口实现有两条 RedisServer 链接：<br>
 * {@link Pipeline} 独享一条链接；<br>
 * {@link RedisSyncOperator} {@link RedisAsyncOperator} {@link RedisReactiveOperator} 共用一条链接。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisOperator<K, V> extends ConnectionMode, AsyncCloseable {

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

}
