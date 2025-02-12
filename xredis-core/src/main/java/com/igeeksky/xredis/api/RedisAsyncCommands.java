package com.igeeksky.xredis.api;

import com.igeeksky.xredis.RedisScript;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.RedisFuture;
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
@SuppressWarnings("unchecked, unused")
public interface RedisAsyncCommands<K, V> extends RedisMode,
        BaseRedisAsyncCommands<K, V>, RedisAclAsyncCommands<K, V>, RedisFunctionAsyncCommands<K, V>,
        RedisGeoAsyncCommands<K, V>, RedisHashAsyncCommands<K, V>, RedisHLLAsyncCommands<K, V>,
        RedisKeyAsyncCommands<K, V>, RedisListAsyncCommands<K, V>, RedisScriptingAsyncCommands<K, V>,
        RedisServerAsyncCommands<K, V>, RedisSetAsyncCommands<K, V>, RedisSortedSetAsyncCommands<K, V>,
        RedisStreamAsyncCommands<K, V>, RedisStringAsyncCommands<K, V>, RedisJsonAsyncCommands<K, V> {

    /**
     * 使用指定的键和参数执行 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    default <T> CompletableFuture<T> eval(RedisScript script, K[] keys, V... args) {
        RedisFuture<T> redisFuture;
        if (ArrayUtils.isEmpty(args)) {
            redisFuture = this.eval(script.getScript(), script.getType(), keys);
        } else {
            redisFuture = this.eval(script.getScript(), script.getType(), keys, args);
        }
        return redisFuture.toCompletableFuture();
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
    default <T> CompletableFuture<T> evalReadOnly(RedisScript script, K[] keys, V... args) {
        RedisFuture<T> redisFuture;
        if (ArrayUtils.isEmpty(args)) {
            redisFuture = this.evalReadOnly(script.getScript(), script.getType(), keys);
        } else {
            redisFuture = this.evalReadOnly(script.getScript(), script.getType(), keys, args);
        }
        return redisFuture.toCompletableFuture();
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
    default <T> CompletableFuture<T> evalsha(RedisScript script, K[] keys, V... args) {
        RedisFuture<T> redisFuture;
        if (ArrayUtils.isEmpty(args)) {
            redisFuture = this.evalsha(script.getSha1(), script.getType(), keys);
        } else {
            redisFuture = this.evalsha(script.getSha1(), script.getType(), keys, args);
        }
        return redisFuture.toCompletableFuture();
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
    default <T> CompletableFuture<T> evalshaReadOnly(RedisScript script, K[] keys, V... args) {
        RedisFuture<T> redisFuture;
        if (ArrayUtils.isEmpty(args)) {
            redisFuture = this.evalshaReadOnly(script.getSha1(), script.getType(), keys);
        } else {
            redisFuture = this.evalshaReadOnly(script.getSha1(), script.getType(), keys, args);
        }
        return redisFuture.toCompletableFuture();
    }

    /**
     * 将脚本加载到 Redis 服务器，并将 SHA1 摘要设置到 RedisScript 对象
     *
     * @param script 脚本对象。
     * @return {@code CompletableFuture<String>} – SHA1 摘要
     */
    default CompletableFuture<String> scriptLoad(RedisScript script) {
        return this.scriptLoad(script.getScript())
                .toCompletableFuture()
                .thenApply(sha1 -> {
                    Assert.notNull(sha1, "script sha1 digest load failed.");
                    script.setSha1(sha1);
                    return sha1;
                });
    }

}
