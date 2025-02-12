package com.igeeksky.xredis.api;

import com.igeeksky.xredis.RedisScript;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;
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
@SuppressWarnings("unchecked, unused")
public interface RedisSyncOperator<K, V> extends RedisMode,
        BaseRedisCommands<K, V>, RedisAclCommands<K, V>, RedisFunctionCommands<K, V>,
        RedisGeoCommands<K, V>, RedisHashCommands<K, V>, RedisHLLCommands<K, V>,
        RedisKeyCommands<K, V>, RedisListCommands<K, V>, RedisScriptingCommands<K, V>,
        RedisServerCommands<K, V>, RedisSetCommands<K, V>, RedisSortedSetCommands<K, V>,
        RedisStreamCommands<K, V>, RedisStringCommands<K, V>, RedisJsonCommands<K, V> {


    /**
     * 使用指定的键和参数执行 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    default <T> T eval(RedisScript script, K[] keys, V... args) {
        if (ArrayUtils.isEmpty(args)) {
            return this.eval(script.getScript(), script.getType(), keys);
        } else {
            return this.eval(script.getScript(), script.getType(), keys, args);
        }
    }

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    default <T> T evalReadOnly(RedisScript script, K[] keys, V... args) {
        if (ArrayUtils.isEmpty(args)) {
            return this.evalReadOnly(script.getScript(), script.getType(), keys);
        } else {
            return this.evalReadOnly(script.getScript(), script.getType(), keys, args);
        }
    }

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    default <T> T evalsha(RedisScript script, K[] keys, V... args) {
        if (ArrayUtils.isEmpty(args)) {
            return this.evalsha(script.getSha1(), script.getType(), keys);
        } else {
            return this.evalsha(script.getSha1(), script.getType(), keys, args);
        }
    }

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    @SuppressWarnings("unchecked")
    default <T> T evalshaReadOnly(RedisScript script, K[] keys, V... args) {
        if (ArrayUtils.isEmpty(args)) {
            return this.evalshaReadOnly(script.getSha1(), script.getType(), keys);
        } else {
            return this.evalshaReadOnly(script.getSha1(), script.getType(), keys, args);
        }
    }

    /**
     * 将脚本加载到 Redis 服务器，并将 SHA1 摘要设置到 RedisScript 对象
     *
     * @param script 脚本对象
     * @return {@link String} – SHA1 摘要
     */
    default String scriptLoad(RedisScript script) {
        String sha1 = this.scriptLoad(script.getScript());
        Assert.notNull(sha1, "script sha1 digest load failed.");
        script.setSha1(sha1);
        return sha1;
    }

    /**
     * 禁止修改 autoFlush
     * <p>
     * 调用此方法将抛出异常。
     *
     * @param autoFlush state of autoFlush.
     */
    default void setAutoFlushCommands(boolean autoFlush) {
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
