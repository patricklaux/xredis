package com.igeeksky.redis.api;

import io.lettuce.core.api.reactive.*;

/**
 * Redis 响应式操作接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisReactiveOperator<K, V> extends RedisMode,
        BaseRedisReactiveCommands<K, V>, RedisAclReactiveCommands<K, V>, RedisFunctionReactiveCommands<K, V>,
        RedisGeoReactiveCommands<K, V>, RedisHashReactiveCommands<K, V>, RedisHLLReactiveCommands<K, V>,
        RedisKeyReactiveCommands<K, V>, RedisListReactiveCommands<K, V>, RedisScriptingReactiveCommands<K, V>,
        RedisServerReactiveCommands<K, V>, RedisSetReactiveCommands<K, V>, RedisSortedSetReactiveCommands<K, V>,
        RedisStreamReactiveCommands<K, V>, RedisStringReactiveCommands<K, V>, RedisJsonReactiveCommands<K, V> {

}
