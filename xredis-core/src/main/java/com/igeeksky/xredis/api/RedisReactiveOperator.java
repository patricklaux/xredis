package com.igeeksky.xredis.api;

import com.igeeksky.xredis.RedisScript;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.api.reactive.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Redis 响应式操作接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
@SuppressWarnings("unchecked, unused")
public interface RedisReactiveOperator<K, V> extends RedisMode,
        BaseRedisReactiveCommands<K, V>, RedisAclReactiveCommands<K, V>, RedisFunctionReactiveCommands<K, V>,
        RedisGeoReactiveCommands<K, V>, RedisHashReactiveCommands<K, V>, RedisHLLReactiveCommands<K, V>,
        RedisKeyReactiveCommands<K, V>, RedisListReactiveCommands<K, V>, RedisScriptingReactiveCommands<K, V>,
        RedisServerReactiveCommands<K, V>, RedisSetReactiveCommands<K, V>, RedisSortedSetReactiveCommands<K, V>,
        RedisStreamReactiveCommands<K, V>, RedisStringReactiveCommands<K, V>, RedisJsonReactiveCommands<K, V> {


    /**
     * 使用指定的键和参数执行 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    default <T> Flux<T> eval(RedisScript script, K[] keys, V... args) {
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
    default <T> Flux<T> evalReadOnly(RedisScript script, K[] keys, V... args) {
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
    default <T> Flux<T> evalsha(RedisScript script, K[] keys, V... args) {
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
    default <T> Flux<T> evalshaReadOnly(RedisScript script, K[] keys, V... args) {
        if (ArrayUtils.isEmpty(args)) {
            return this.evalshaReadOnly(script.getSha1(), script.getType(), keys);
        } else {
            return this.evalshaReadOnly(script.getSha1(), script.getType(), keys, args);
        }
    }

    /**
     * 将脚本加载到 Redis 服务器，并将 SHA1 摘要设置到 RedisScript 对象
     *
     * @param script 脚本对象。
     * @return {@code Mono<String>} – SHA1 摘要
     */
    default Mono<String> scriptLoad(RedisScript script) {
        return this.scriptLoad(script.getScript())
                .doOnSuccess(sha1 -> {
                    Assert.notNull(sha1, "script sha1 digest load failed.");
                    script.setSha1(sha1);
                });
    }

}
