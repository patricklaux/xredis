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

    /**
     * Redis 命令成功响应状态值
     */
    String OK = "OK";

    // -------------------------- server command start -----------------------

    /**
     * 判断当前 Redis 连接是否为集群模式
     *
     * @return {@code true} 表示集群模式，{@code false} 表示非集群模式
     */
    boolean isCluster();

    /**
     * 获取单批次命令提交数量阈值
     * <p>
     * 如 batchSize 设为 10000，当 {@link RedisOperatorProxy} 接收到单次操作 100 万条数据的请求时，
     * 会将数据切分为 100 份，每份 10000条数据，然后分 100 批次提交到 RedisServer。
     *
     * @return 单批次命令提交数量阈值
     */
    long getBatchSize();

    /**
     * RedisServer 信息
     *
     * @return {@link String} – RedisServer 信息
     */
    CompletableFuture<String> info();

    /**
     * RedisServer 指定段的信息
     *
     * @param section 段名
     * @return {@link String} – RedisServer 指定段的信息
     */
    CompletableFuture<String> info(String section);

    /**
     * RedisServer 版本信息
     *
     * @return 版本信息
     */
    CompletableFuture<String> version();

    /**
     * RedisServer 当前时间
     *
     * @return {@link List} – 包含两个元素：1.unix time seconds；2.microseconds。
     */
    CompletableFuture<List<byte[]>> time();

    /**
     * RedisServer 当前时间（秒）
     *
     * @return {@code long} – 当前时间（秒）
     */
    CompletableFuture<Long> timeSeconds();

    /**
     * RedisServer 当前时间（毫秒）
     *
     * @return {@code long} – 当前时间（毫秒）
     */
    CompletableFuture<Long> timeMillis();

    /**
     * RedisServer 当前时间（微秒）
     *
     * @return {@code long} – 当前时间（微秒）
     */
    CompletableFuture<Long> timeMicros();

    // -------------------------- server command end -------------------------


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
     * @return {@code CompletableFuture<List<KeyValue<byte[], byte[]>>>} – 键值对列表
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> mget(byte[][] keys);

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param value        值
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value);

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     * <p>
     * 每个键有独立的过期时间
     *
     * @param keyValues （键-值-过期时间）列表
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> psetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues);

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     * <p>
     * 所有键有相同的过期时间
     *
     * @param keyValues    键值对列表
     * @param milliseconds 过期时间（毫秒）
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> psetex(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds);

    // -------------------------- string command end -------------------------


    // -------------------------- hash command start -------------------------

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
     * @return {@code CompletableFuture<Long>} – 设置结果，值表示的状态见 {@code HPEXPIRE} 命令
     */
    CompletableFuture<Long> hpset(byte[] key, long milliseconds, byte[] field, byte[] value);

    /**
     * 异步设置 Redis-Hash 的字段值，并设置字段的过期时间
     * <p>
     * 所有字段有相同的过期时间
     *
     * @param key          Redis-Hash 的键
     * @param fieldsValues {@code 字段-值} 列表
     * @param milliseconds 过期时间（毫秒）
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(byte[] key, long milliseconds, List<KeyValue<byte[], byte[]>> fieldsValues);

    /**
     * 异步设置 Redis-Hash 的字段值，并设置字段的过期时间
     * <p>
     * 每个字段有独立的过期时间
     *
     * @param key                Redis-Hash 的键
     * @param expiryFieldsValues {@code 字段-值-过期时间（毫秒）} 列表
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues);

    /**
     * 异步设置 Redis-Hash 的字段值，并设置字段的过期时间
     * <p>
     * 每个字段有独立的过期时间
     *
     * @param expiryKeysFieldsValues {@code 键：字段-值-过期时间（毫秒）} 列表
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues);

    /**
     * 异步设置 Redis-Hash 的字段值，并设置字段的过期时间
     * <p>
     * 所有字段有相同的过期时间
     *
     * @param keysFieldsValues {@code 键：字段-值} 列表
     * @param milliseconds     过期时间（毫秒）
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
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


    // -------------------------- sorted set command start -------------------

    /**
     * 添加 Redis-SortedSet 的成员并设置分值（或更新已有成员的分值）
     *
     * @param key    Redis-SortedSet 的键
     * @param member Redis-SortedSet 的成员
     * @param score  Redis-SortedSet 的成员对应的分值
     * @return {@code CompletableFuture<Long>} – 新增成员数量，值表示的状态详见 {@code ZADD} 命令
     * @see <a href="https://redis.io/docs/latest/commands/zadd/">ZADD</a>
     */
    CompletableFuture<Long> zadd(byte[] key, double score, byte[] member);

    /**
     * 添加 Redis-SortedSet 的成员并设置分值（或更新已有成员的分值）
     *
     * @param key          Redis-SortedSet 的键
     * @param scoredValues {@code 分值-成员} 列表
     * @return {@code CompletableFuture<Long>} – 新增成员数量，值表示的状态详见 {@code ZADD} 命令
     * @see <a href="https://redis.io/docs/latest/commands/zadd/">ZADD</a>
     */
    CompletableFuture<Long> zadd(byte[] key, ScoredValue<byte[]>... scoredValues);

    /**
     * 获取 Redis-SortedSet 的成员数量
     *
     * @param key 键
     * @return {@code CompletableFuture<Long>} – 成员数量
     * @see <a href="https://redis.io/docs/latest/commands/zcard/">ZCARD</a>
     */
    CompletableFuture<Long> zcard(byte[] key);

    /**
     * 根据给定的键字母序范围，返回成员列表
     *
     * @param key   键
     * @param range 键字母序范围
     * @return {@code CompletableFuture<List<byte[]>>} – 给定键字母序范围的成员列表
     */
    CompletableFuture<List<byte[]>> zrangebylex(byte[] key, Range<byte[]> range);

    /**
     * 根据给定的键字母序范围，返回成员列表
     *
     * @param key   键
     * @param range 键字母序范围
     * @param limit 限定条件
     * @return {@code CompletableFuture<List<byte[]>>} – 给定键字母序范围的成员列表
     */
    CompletableFuture<List<byte[]>> zrangebylex(byte[] key, Range<byte[]> range, Limit limit);

    /**
     * 根据给定的分值范围，返回成员列表
     *
     * @param key   键
     * @param range 分值范围
     * @return {@code CompletableFuture<List<byte[]>>} – 给定分值范围的成员列表
     */
    CompletableFuture<List<byte[]>> zrangebyscore(byte[] key, Range<? extends Number> range);

    /**
     * 根据给定的分值范围，返回成员列表
     *
     * @param key   键
     * @param range 分值范围
     * @param limit 限定条件
     * @return {@code CompletableFuture<List<byte[]>>} – 给定分值范围的成员列表
     */
    CompletableFuture<List<byte[]>> zrangebyscore(byte[] key, Range<? extends Number> range, Limit limit);

    /**
     * 删除成员
     *
     * @param key     键
     * @param members 成员列表
     * @return {@code CompletableFuture<Long>} – 删除的成员数量
     * @see <a href="https://redis.io/docs/latest/commands/zrem/">ZREM</a>
     */
    CompletableFuture<Long> zrem(byte[] key, byte[]... members);

    // -------------------------- sorted set command end ---------------------


    // -------------------------- script command start -----------------------

    /**
     * 使用指定的键集和参数执行 Script
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> CompletableFuture<T> eval(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键集和参数执行只读的 Script
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> CompletableFuture<T> evalReadOnly(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键集和参数执行 Script
     * <p>
     * 可能问题： <p>
     * 1. RedisServer 可能未加载此脚本；<br>
     * 2. RedisServer 的 SHA1 摘要 与 RedisScript 对象的 SHA1 摘要不一致。
     * <p>
     * 因此，如果出现 {@code RedisNoScriptException} 异常，实现类需：
     * 1.先执行 {@link #scriptLoad(RedisScript)} 方法；<br>
     * 2.再转而执行 {@link #eval(RedisScript, byte[][], byte[]...)} 方法。
     * <p>
     * 总之，尽可能确保：<br>
     * 1. 数据操作成功；<br>
     * 2. RedisScript 对象的 SHA1 摘要与 RedisServer 的 SHA1 摘要一致，下一次再用同一 RedisScript 对象调用此方法时，不再出现异常。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> CompletableFuture<T> evalsha(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 使用指定的键集和参数执行只读的 Script
     * <p>
     * 可能问题： <p>
     * 1. RedisServer 可能未加载此脚本；<br>
     * 2. RedisServer 的 SHA1 摘要 与 RedisScript 对象的 SHA1 摘要不一致。
     * <p>
     * 因此，如果出现 {@code RedisNoScriptException} 异常，实现类需：
     * 1.先执行 {@link #scriptLoad(RedisScript)} 方法；<br>
     * 2.再转而执行 {@link #evalReadOnly(RedisScript, byte[][], byte[]...)} 方法。
     * <p>
     * 总之，尽可能确保：<br>
     * 1. 数据操作成功；<br>
     * 2. RedisScript 对象的 SHA1 摘要与 RedisServer 的 SHA1 摘要一致，下一次再用同一 RedisScript 对象调用此方法时，不再出现异常。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   脚本参数列表
     * @return 脚本执行结果
     */
    <T> CompletableFuture<T> evalshaReadOnly(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * 加载 Script
     * <p>
     * 1.Script 加载到 Redis 服务器；<br>
     * 2.RedisServer 返回的 SHA1 摘要设置到 RedisScript 对象。
     *
     * @param script 脚本对象
     * @return {@link String} – SHA1 摘要
     */
    CompletableFuture<String> scriptLoad(RedisScript script);

    // -------------------------- script command end -------------------------
}
