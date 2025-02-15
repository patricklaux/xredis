package com.igeeksky.xredis.common;


import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RedisOperatorProxy（简化批量数据读写）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisOperatorProxy {

    String OK = "OK";

    boolean isCluster();

    // -------------------------- key command start --------------------------

    /**
     * 异步批量删除键
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除。
     *
     * @param keys 键
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<Long> del(byte[]... keys);

    /**
     * 清理匹配指定模式的 key
     * <p>
     * 内部使用 ScanCursor 扫描，避免使用 KEYS 命令，避免内存消耗过大
     *
     * @param pattern 模式
     * @return 清理数量
     */
    long clear(byte[] pattern);

    // -------------------------- key command end ----------------------------


    // -------------------------- string command start -----------------------

    /**
     * 异步设置 Redis-String 键的值
     *
     * @param key   键
     * @param value 值
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<String> set(byte[] key, byte[] value);

    /**
     * 异步获取 Redis-String 键的值
     *
     * @param key 键
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步获取的值
     */
    CompletableFuture<byte[]> get(byte[] key);

    /**
     * 异步批量设置 Redis-String 键的值
     * <p>
     * 如单次存储的数据量超过 batchSize，则分批次存储。
     *
     * @param keyValues Redis-String 键及对应的值集合
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<String> mset(Map<byte[], byte[]> keyValues);

    /**
     * 异步批量获取键对应的值
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次查询数据，然后再合并返回。
     *
     * @param keys 键列表
     * @return 返回一个 {@code CompletableFuture<List<V>>} 对象，表示异步操作的结果
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> mget(byte[][] keys);

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param value        值
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value);

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     *
     * @param keyValues （键-值-过期时间）列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<String> psetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues);

    // -------------------------- string command end -------------------------


    // -------------------------- hash command start -------------------------

    CompletableFuture<String> psetex(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds);

    /**
     * 异步将指定的值设置到指定的哈希字段中
     * 如果字段已存在，会覆盖该字段的值
     *
     * @param key   Redis-Hash 的键
     * @param field Redis-Hash 的字段
     * @param value Redis-Hash 的字段值
     * @return 返回一个CompletableFuture，表示异步操作的结果
     * 完成后，结果为 true 表示设置成功，false 表示设置失败
     */
    CompletableFuture<Boolean> hset(byte[] key, byte[] field, byte[] value);

    /**
     * 异步批量设置 Redis-Hash 值
     * <p>
     * 支持多个 Hash 键批量设置多个 Hash 值
     * <p>
     * 该方法通过 Pipeline 机制优化了多个 Hash值的批量设置操作，旨在减少网络往返次数，提高操作效率。
     * <p>
     * 如单次提交的数据量超过 batchSize，则分批次提交数据。
     *
     * @param keyFieldValues 包含Key及其对应Field-Value映射的字典，其中 Key为 Hash的键名，Value为 Field-Value 映射
     * @return 返回一个CompletableFuture对象，表示异步操作的结果
     */
    CompletableFuture<String> hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues);

    /**
     * 异步批量设置 Redis-Hash 的字段值
     * <p>
     * 如单次提交的数据量超过 batchSize，则分批次提交数据。
     *
     * @param key         Redis-Hash 的键
     * @param fieldValues 字段与值的映射
     * @return 返回表示异步操作完成的Future对象
     */
    CompletableFuture<String> hmset(byte[] key, Map<byte[], byte[]> fieldValues);

    /**
     * 异步设置 Redis-Hash 键的值及过期时间
     *
     * @param key          Hash 键
     * @param milliseconds 过期时间（毫秒）
     * @param field        Hash 字段
     * @param value        Hash 字段值
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<Long> hpset(byte[] key, long milliseconds, byte[] field, byte[] value);

    CompletableFuture<List<Long>> hmpset(byte[] key, long milliseconds, List<KeyValue<byte[], byte[]>> fieldsValues);

    CompletableFuture<List<Long>> hmpset(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues);

    CompletableFuture<List<Long>> hmpset(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues);

    CompletableFuture<List<Long>> hmpset(Map<byte[], List<KeyValue<byte[], byte[]>>> keysFieldsValues, long milliseconds);

    /**
     * 异步获取 Redis-Hash 键的值
     *
     * @param key   Redis-Hash 的键
     * @param field Redis-Hash 的字段
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<byte[]> hget(byte[] key, byte[] field);

    /**
     * 异步批量获取 Redis-Hash 键的值
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并返回。
     *
     * @param key    Redis-Hash 的键
     * @param fields Redis-Hash 的字段列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(byte[] key, byte[]... fields);

    /**
     * 异步批量获取 Redis-Hash 键的值
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并返回。
     *
     * @param keyFields Redis-Hash 键及对应的字段集合
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(Map<byte[], List<byte[]>> keyFields);

    /**
     * 异步批量删除 Redis-Hash 键的值
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除数据。
     *
     * @param keyFields 包含 Key 及其对应 field 列表，其中 Key 为 Hash 键，Value 为 field 列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<Long> hdel(Map<byte[], List<byte[]>> keyFields);

    /**
     * 异步批量删除 Redis-Hash 键对应的字段列表
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除。
     *
     * @param key    Redis-Hash 键
     * @param fields Redis-Hash 键对应的字段列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    CompletableFuture<Long> hdel(byte[] key, byte[]... fields);

    // -------------------------- hash command end ---------------------------

    // -------------------------- script command start -----------------------

    /**
     * 使用指定的键和参数执行 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> T eval(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> T evalReadOnly(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> T evalsha(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键和参数执行只读的 Lua 脚本。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> T evalshaReadOnly(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 将脚本加载到 Redis 服务器，并将 SHA1 摘要设置到 RedisScript 对象
     *
     * @param script 脚本对象
     * @return {@link String} – SHA1 摘要
     */
    String scriptLoad(RedisScript script);

    // -------------------------- script command end -------------------------

    /**
     * 获取 Redis 版本信息
     *
     * @return 版本信息
     */
    String version();

}
