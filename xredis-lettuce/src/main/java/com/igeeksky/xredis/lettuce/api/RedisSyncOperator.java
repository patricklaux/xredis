package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.ConnectionMode;
import com.igeeksky.xredis.common.TimeConvertor;
import io.lettuce.core.api.StatefulConnection;
import io.lettuce.core.api.sync.*;

/**
 * Redis 同步操作接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisSyncOperator<K, V> extends ConnectionMode,
        BaseRedisCommands<K, V>, RedisAclCommands<K, V>, RedisFunctionCommands<K, V>,
        RedisGeoCommands<K, V>, RedisHashCommands<K, V>, RedisHLLCommands<K, V>,
        RedisKeyCommands<K, V>, RedisListCommands<K, V>, RedisScriptingCommands<K, V>,
        RedisServerCommands<K, V>, RedisSetCommands<K, V>, RedisSortedSetCommands<K, V>,
        RedisStreamCommands<K, V>, RedisStringCommands<K, V>, RedisJsonCommands<K, V> {

    /**
     * 获取 RedisServer 当前时间（秒）
     *
     * @return {@code long} – 当前时间（秒）
     */
    default Long timeSeconds(TimeConvertor<V> convertor) {
        return convertor.seconds(this.time());
    }

    /**
     * 获取 RedisServer 当前时间（毫秒）
     *
     * @return {@code long} – 当前时间（毫秒）
     */
    default Long timeMillis(TimeConvertor<V> convertor) {
        return convertor.milliseconds(this.time());
    }

    /**
     * 获取 RedisServer 当前时间（微秒）
     *
     * @return {@code long} – 当前时间（微秒）
     */
    default Long timeMicros(TimeConvertor<V> convertor) {
        return convertor.microseconds(this.time());
    }

    /**
     * 禁止修改 autoFlush
     * <p>
     * 调用此方法将抛出异常。
     *
     * @param ignoredAutoFlush state of autoFlush.
     */
    default void setAutoFlushCommands(boolean ignoredAutoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

    /**
     * 获取当前 Redis 连接
     *
     * @return {@link StatefulConnection} 连接
     */
    StatefulConnection<K, V> getConnection();

}
