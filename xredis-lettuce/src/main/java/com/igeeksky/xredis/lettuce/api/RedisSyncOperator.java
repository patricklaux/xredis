package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.RedisMode;
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
public interface RedisSyncOperator<K, V> extends RedisMode,
        BaseRedisCommands<K, V>, RedisAclCommands<K, V>, RedisFunctionCommands<K, V>,
        RedisGeoCommands<K, V>, RedisHashCommands<K, V>, RedisHLLCommands<K, V>,
        RedisKeyCommands<K, V>, RedisListCommands<K, V>, RedisScriptingCommands<K, V>,
        RedisServerCommands<K, V>, RedisSetCommands<K, V>, RedisSortedSetCommands<K, V>,
        RedisStreamCommands<K, V>, RedisStringCommands<K, V>, RedisJsonCommands<K, V> {

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
