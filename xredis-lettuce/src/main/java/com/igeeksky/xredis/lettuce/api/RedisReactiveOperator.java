package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.ConnectionMode;
import com.igeeksky.xredis.common.TimeConvertor;
import io.lettuce.core.api.reactive.*;
import reactor.core.publisher.Mono;

/**
 * Redis 响应式操作接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisReactiveOperator<K, V> extends ConnectionMode,
        BaseRedisReactiveCommands<K, V>, RedisAclReactiveCommands<K, V>, RedisFunctionReactiveCommands<K, V>,
        RedisGeoReactiveCommands<K, V>, RedisHashReactiveCommands<K, V>, RedisHLLReactiveCommands<K, V>,
        RedisKeyReactiveCommands<K, V>, RedisListReactiveCommands<K, V>, RedisScriptingReactiveCommands<K, V>,
        RedisServerReactiveCommands<K, V>, RedisSetReactiveCommands<K, V>, RedisSortedSetReactiveCommands<K, V>,
        RedisStreamReactiveCommands<K, V>, RedisStringReactiveCommands<K, V>, RedisJsonReactiveCommands<K, V> {

    /**
     * 获取 RedisServer 当前时间（秒）
     *
     * @param convertor 时间格式转换器
     * @return {@code long} – 当前时间（秒）
     */
    default Mono<Long> timeSeconds(TimeConvertor<V> convertor) {
        return this.time().collectList().map(convertor::seconds);
    }

    /**
     * 获取 RedisServer 当前时间（毫秒）
     *
     * @param convertor 时间格式转换器
     * @return {@code long} – 当前时间（毫秒）
     */
    default Mono<Long> timeMillis(TimeConvertor<V> convertor) {
        return this.time().collectList().map(convertor::milliseconds);
    }

    /**
     * 获取 RedisServer 当前时间（微秒）
     *
     * @param convertor 时间格式转换器
     * @return {@code long} – 当前时间（微秒）
     */
    default Mono<Long> timeMicros(TimeConvertor<V> convertor) {
        return this.time().collectList().map(convertor::microseconds);
    }

}
