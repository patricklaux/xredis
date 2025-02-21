package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.ConnectionMode;
import com.igeeksky.xredis.common.TimeConvertor;
import io.lettuce.core.api.async.*;

import java.util.concurrent.CompletableFuture;

/**
 * 异步命令接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisAsyncCommands<K, V> extends ConnectionMode,
        BaseRedisAsyncCommands<K, V>, RedisAclAsyncCommands<K, V>, RedisFunctionAsyncCommands<K, V>,
        RedisGeoAsyncCommands<K, V>, RedisHashAsyncCommands<K, V>, RedisHLLAsyncCommands<K, V>,
        RedisKeyAsyncCommands<K, V>, RedisListAsyncCommands<K, V>, RedisScriptingAsyncCommands<K, V>,
        RedisServerAsyncCommands<K, V>, RedisSetAsyncCommands<K, V>, RedisSortedSetAsyncCommands<K, V>,
        RedisStreamAsyncCommands<K, V>, RedisStringAsyncCommands<K, V>, RedisJsonAsyncCommands<K, V> {

    /**
     * 获取 RedisServer 当前时间（秒）
     *
     * @return {@code long} – 当前时间（秒）
     */
    default CompletableFuture<Long> timeSeconds(TimeConvertor<V> convertor) {
        return this.time().thenApply(convertor::seconds).toCompletableFuture();
    }

    /**
     * 获取 RedisServer 当前时间（毫秒）
     *
     * @return {@code long} – 当前时间（毫秒）
     */
    default CompletableFuture<Long> timeMillis(TimeConvertor<V> convertor) {
        return this.time().thenApply(convertor::milliseconds).toCompletableFuture();
    }

    /**
     * 获取 RedisServer 当前时间（微秒）
     *
     * @return {@code long} – 当前时间（微秒）
     */
    default CompletableFuture<Long> timeMicros(TimeConvertor<V> convertor) {
        return this.time().thenApply(convertor::microseconds).toCompletableFuture();
    }

}
