package com.igeeksky.xredis.api;

import io.lettuce.core.api.async.*;

/**
 * 异步命令接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisAsyncCommands<K, V> extends RedisMode,
        BaseRedisAsyncCommands<K, V>, RedisAclAsyncCommands<K, V>, RedisFunctionAsyncCommands<K, V>,
        RedisGeoAsyncCommands<K, V>, RedisHashAsyncCommands<K, V>, RedisHLLAsyncCommands<K, V>,
        RedisKeyAsyncCommands<K, V>, RedisListAsyncCommands<K, V>, RedisScriptingAsyncCommands<K, V>,
        RedisServerAsyncCommands<K, V>, RedisSetAsyncCommands<K, V>, RedisSortedSetAsyncCommands<K, V>,
        RedisStreamAsyncCommands<K, V>, RedisStringAsyncCommands<K, V>, RedisJsonAsyncCommands<K, V> {



}
