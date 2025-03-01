package com.igeeksky.xredis.common;


import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * RedisOperatorProxy
 * <p>
 * 主要是为了简化数据批量操作，及屏蔽 {@code RedisOperator} 的底层具体实现。
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
     * 判断当前 Redis 连接是否为集群连接
     * <p>
     * 此方法仅判断连接是否为集群连接，而不是 RedisServer 是否为集群节点。<br>
     * 譬如，如果使用 {@code standalone} 方式创建到 Redis 集群某个节点的连接，返回的结果为 {@code false}。
     *
     * @return 如果为集群连接，返回 {@code true} ，否则返回 {@code false}
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
     * 异步转同步阻塞超时时间
     * <p>
     * 默认值：60000 单位：毫秒
     * <p>
     * 如果调用同步接口，会先调用异步接口获取 {@link CompletableFuture}，
     * 然后再调用 {@code future.get(timeout, TimeUnit.MILLISECONDS)} 方法等待数据处理完成。<p>
     * <b>注意：</b><p>
     * 1、当调用同步接口时，如果异步操作未在设定超时时间内完成或线程被中断，会抛出异常。<br>
     * 2、当调用同步接口时，请根据单次操作数据量、网络拥堵情况、RedisServer 处理能力等适当调整超时时间。
     *
     * @return {@link Long} – 异步转同步阻塞超时时间
     */
    long getSyncTimeout();

    /**
     * 获取 RedisServer 信息（异步）
     *
     * @return {@link String} – RedisServer 信息
     */
    CompletableFuture<String> info();

    /**
     * 获取 RedisServer 信息（同步）
     *
     * @return {@link String} – RedisServer 信息
     */
    default String infoSync() {
        return RedisHelper.get(info(), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 指定段的信息（异步）
     *
     * @param section 段名
     * @return {@link String} – RedisServer 指定段的信息
     */
    CompletableFuture<String> info(String section);

    /**
     * 获取 RedisServer 指定段的信息（同步）
     *
     * @param section 段名
     * @return {@link String} – RedisServer 指定段的信息
     */
    default String infoSync(String section) {
        return RedisHelper.get(info(section), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 版本信息（异步）
     *
     * @return 版本信息
     */
    CompletableFuture<String> version();

    /**
     * 获取 RedisServer 版本信息（同步）
     *
     * @return 版本信息
     */
    default String versionSync() {
        return RedisHelper.get(version(), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @return {@link List} – 包含两个元素：1.unix time seconds；2.microseconds。
     */
    CompletableFuture<List<byte[]>> time();

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @return {@link List} – 包含两个元素：1.unix time seconds；2.microseconds。
     */
    default List<byte[]> timeSync() {
        return RedisHelper.get(time(), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @return {@code long} – 当前时间（秒）
     */
    CompletableFuture<Long> timeSeconds();

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @return {@code long} – 当前时间（秒）
     */
    default Long timeSecondsSync() {
        return RedisHelper.get(timeSeconds(), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @return {@code long} – 当前时间（毫秒）
     */
    CompletableFuture<Long> timeMillis();

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @return {@code long} – 当前时间（毫秒）
     */
    default Long timeMillisSync() {
        return RedisHelper.get(timeMillis(), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @return {@code long} – 当前时间（微秒）
     */
    CompletableFuture<Long> timeMicros();

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @return {@code long} – 当前时间（微秒）
     */
    default Long timeMicrosSync() {
        return RedisHelper.get(timeMicros(), getSyncTimeout());
    }

    // -------------------------- server command end -------------------------


    // -------------------------- key command start --------------------------

    /**
     * Redis-Key：批量删除键（异步）
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除。
     *
     * @param keys 键
     * @return {@link Long} – 删除数量
     */
    CompletableFuture<Long> del(byte[]... keys);

    /**
     * Redis-Key：批量删除键（同步）
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除。
     *
     * @param keys 键
     * @return {@link Long} – 删除数量
     */
    default Long delSync(byte[]... keys) {
        return RedisHelper.get(del(keys), getSyncTimeout());
    }

    /**
     * Redis-Key：清理匹配指定模式的键集（同步）
     * <p>
     * 内部使用 ScanCursor 扫描，避免使用 KEYS 命令，避免内存消耗过大
     *
     * @param pattern 模式
     * @return {@link Long} – 清理数量
     */
    Long clear(byte[] pattern);

    // -------------------------- key command end ----------------------------


    // -------------------------- string command start -----------------------

    /**
     * Redis-String：设置键对应的值（异步）
     *
     * @param key   键
     * @param value 值
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> set(byte[] key, byte[] value);

    /**
     * Redis-String：设置键对应的值（同步）
     *
     * @param key   键
     * @param value 值
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String setSync(byte[] key, byte[] value) {
        return RedisHelper.get(set(key, value), getSyncTimeout());
    }

    /**
     * Redis-String：获取键对应的值（异步）
     *
     * @param key 键
     * @return {@code byte[]} – 值
     */
    CompletableFuture<byte[]> get(byte[] key);

    /**
     * Redis-String：获取键对应的值（同步）
     *
     * @param key 键
     * @return {@code byte[]} – 值
     */
    default byte[] getSync(byte[] key) {
        return RedisHelper.get(get(key), getSyncTimeout());
    }

    /**
     * Redis-String：批量设置键的值（异步）
     * <p>
     * 如单次设置的数据量超过 batchSize，则分批次提交数据，然后再合并结果。
     *
     * @param keyValues 键值对集合
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> mset(Map<byte[], byte[]> keyValues);

    /**
     * Redis-String：批量设置键的值（异步）
     * <p>
     * 如单次设置的数据量超过 batchSize，则分批次提交数据，然后再合并结果。
     *
     * @param keyValues 键值对集合
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String msetSync(Map<byte[], byte[]> keyValues) {
        return RedisHelper.get(mset(keyValues), getSyncTimeout());
    }

    /**
     * Redis-String：批量获取键对应的值（异步）
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次查询数据，然后再合并返回。
     *
     * @param keys 键列表
     * @return {@code CompletableFuture<List<KeyValue<byte[], byte[]>>>} – 键值对列表
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> mget(byte[][] keys);

    /**
     * Redis-String：批量获取键对应的值（同步）
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次查询数据，然后再合并返回。
     *
     * @param keys 键列表
     * @return {@code List<KeyValue<byte[], byte[]>>} – 键值对列表
     */
    default List<KeyValue<byte[], byte[]>> mgetSync(byte[][] keys) {
        return RedisHelper.get(mget(keys), getSyncTimeout());
    }

    /**
     * Redis-String：设置键对应的值和过期时间（异步）
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param value        值
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value);

    /**
     * Redis-String：设置键对应的值和过期时间（同步）
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param value        值
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String psetexSync(byte[] key, long milliseconds, byte[] value) {
        return RedisHelper.get(psetex(key, milliseconds, value), getSyncTimeout());
    }

    /**
     * Redis-String：批量设置键对应的值和过期时间（异步）
     * <p>
     * 每个键有独立的过期时间
     *
     * @param expiryKeyValues {@code List<ExpiryKeyValue<键, 值, 过期时间>>}
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> psetex(List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValues);

    /**
     * Redis-String：批量设置键对应的值和过期时间（同步）
     * <p>
     * 每个键有独立的过期时间
     *
     * @param expiryKeyValues {@code List<ExpiryKeyValue<键, 值, 过期时间>>}
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String psetexSync(List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValues) {
        return RedisHelper.get(psetex(expiryKeyValues), getSyncTimeout());
    }

    /**
     * Redis-String：批量设置键对应的值和过期时间（异步）
     * <p>
     * 所有键有相同的过期时间
     *
     * @param keyValues    {@code List<KeyValue<键, 值>>}
     * @param milliseconds 过期时间（毫秒）
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> psetex(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds);

    /**
     * Redis-String：批量设置键对应的值和过期时间（同步）
     * <p>
     * 所有键有相同的过期时间
     *
     * @param keyValues    {@code List<KeyValue<键, 值>>}
     * @param milliseconds 过期时间（毫秒）
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String psetexSync(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds) {
        return RedisHelper.get(psetex(keyValues, milliseconds), getSyncTimeout());
    }

    // -------------------------- string command end -------------------------


    // -------------------------- hash command start -------------------------

    /**
     * Redis-Hash：设置字段值（异步）
     * <p>
     * 如果字段已存在，则覆盖该字段的值
     *
     * @param key   Redis-Hash 键
     * @param field Redis-Hash 字段
     * @param value Redis-Hash 字段对应的值
     * @return {@code CompletableFuture<Boolean>} – 如果命令执行成功，则返回 true
     */
    CompletableFuture<Boolean> hset(byte[] key, byte[] field, byte[] value);

    /**
     * Redis-Hash：设置字段值（同步）
     * <p>
     * 如果字段已存在，则覆盖该字段的值
     *
     * @param key   Redis-Hash 键
     * @param field Redis-Hash 字段
     * @param value Redis-Hash 字段对应的值
     * @return {@link Boolean} – 如果命令执行成功，则返回 true
     */
    default Boolean hsetSync(byte[] key, byte[] field, byte[] value) {
        return RedisHelper.get(hset(key, field, value), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量设置字段值（异步）
     * <p>
     * 支持操作多个 Hash 表
     *
     * @param keyFieldValues {@code Map<键, Map<字段, 值>>}
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues);

    /**
     * Redis-Hash：批量设置字段值（同步）
     * <p>
     * 支持操作多个 Hash 表
     *
     * @param keyFieldValues {@code Map<键, Map<字段, 值>>}
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String hmsetSync(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        return RedisHelper.get(hmset(keyFieldValues), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量设置字段值（异步）
     * <p>
     * 支持操作单个 Hash 表
     *
     * @param key         键
     * @param fieldValues {@code Map<字段, 值>}
     * @return {@code CompletableFuture<String>} – 如果命令执行成功，则返回 OK
     */
    CompletableFuture<String> hmset(byte[] key, Map<byte[], byte[]> fieldValues);

    /**
     * Redis-Hash：批量设置字段值（同步）
     * <p>
     * 支持操作单个 Hash 表
     *
     * @param key         键
     * @param fieldValues {@code Map<字段, 值>}
     * @return {@link String} – 如果命令执行成功，则返回 OK
     */
    default String hmsetSync(byte[] key, Map<byte[], byte[]> fieldValues) {
        return RedisHelper.get(hmset(key, fieldValues), getSyncTimeout());
    }

    /**
     * Redis-Hash：设置字段的值及字段过期时间（异步）
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param field        字段
     * @param value        字段对应的值
     * @return {@code CompletableFuture<Long>} – 设置结果，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<Long> hpset(byte[] key, long milliseconds, byte[] field, byte[] value);

    /**
     * Redis-Hash：设置字段的值及字段过期时间（同步）
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param field        字段
     * @param value        字段对应的值
     * @return {@link Long} – 设置结果，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    default Long hpsetSync(byte[] key, long milliseconds, byte[] field, byte[] value) {
        return RedisHelper.get(hpset(key, milliseconds, field, value), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（异步）
     * <p>
     * 所有字段使用相同的过期时间，支持操作单个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param key          Redis-Hash 键
     * @param fieldsValues {@code List<KeyValue<字段, 值>}
     * @param milliseconds 过期时间（毫秒）
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(byte[] key, long milliseconds, List<KeyValue<byte[], byte[]>> fieldsValues);

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（同步）
     * <p>
     * 所有字段使用相同的过期时间，支持操作单个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param key          Redis-Hash 键
     * @param fieldsValues {@code List<KeyValue<字段, 值>}
     * @param milliseconds 过期时间（毫秒）
     * @return {@link List<Long>} – 设置结果列表，值表示的状态见 {@code}
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    default List<Long> hmpsetSync(byte[] key, long milliseconds, List<KeyValue<byte[], byte[]>> fieldsValues) {
        return RedisHelper.get(hmpset(key, milliseconds, fieldsValues), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（异步）
     * <p>
     * 每个字段使用独立的过期时间，支持操作单个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param key                Redis-Hash 键
     * @param expiryFieldsValues {@code List<ExpiryKeyValue<字段, 值, 过期时间>}
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues);

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（同步）
     * <p>
     * 每个字段使用独立的过期时间，支持操作单个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param key                Redis-Hash 键
     * @param expiryFieldsValues {@code List<ExpiryKeyValue<字段, 值, 过期时间>}
     * @return {@link List<Long>} –设置结果列表，值表示的状态见 {@code HPEXPIRE}
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    default List<Long> hmpsetSync(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues) {
        return RedisHelper.get(hmpset(key, expiryFieldsValues), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（异步）
     * <p>
     * 每个字段使用独立的过期时间，支持操作多个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param expiryKeysFieldsValues {@code Map<键, List<ExpiryKeyValue<字段, 值, 过期时间>>>}
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues);

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（同步）
     * <p>
     * 每个字段使用独立的过期时间，支持操作多个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param expiryKeysFieldsValues {@code Map<键, List<ExpiryKeyValue<字段, 值, 过期时间>>>}
     * @return {@link List<Long>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    default List<Long> hmpsetSync(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues) {
        return RedisHelper.get(hmpset(expiryKeysFieldsValues), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（异步）
     * <p>
     * 所有字段使用相同的过期时间，支持操作多个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param keysFieldsValues {@code Map<键, List<KeyValue<字段, 值>>>} 列表
     * @param milliseconds     过期时间（毫秒）
     * @return {@code CompletableFuture<List<Long>>} – 设置结果列表，值表示的状态见 {@code HPEXPIRE} 命令
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    CompletableFuture<List<Long>> hmpset(Map<byte[], List<KeyValue<byte[], byte[]>>> keysFieldsValues, long milliseconds);

    /**
     * Redis-Hash：批量设置字段的值及字段过期时间（同步）
     * <p>
     * 所有字段使用相同的过期时间，支持操作多个 Hash 表
     * <p>
     * 注意：RedisServer 版本需大于等于 7.4.0
     *
     * @param keysFieldsValues {@code Map<键, List<KeyValue<字段, 值>>>} 列表
     * @param milliseconds     过期时间（毫秒）
     * @return {@link List<Long>} –设置结果列表，值表示的状态见 {@code HPEXPIRE}
     * @see <a href="https://redis.io/docs/latest/commands/hpexpire/">HPEXPIRE</a>
     */
    default List<Long> hmpsetSync(Map<byte[], List<KeyValue<byte[], byte[]>>> keysFieldsValues, long milliseconds) {
        return RedisHelper.get(hmpset(keysFieldsValues, milliseconds), getSyncTimeout());
    }

    /**
     * Redis-Hash：获取字段对应的值（异步）
     *
     * @param key   键
     * @param field 字段
     * @return {@code CompletableFuture<byte[]>} – 字段对应的值
     */
    CompletableFuture<byte[]> hget(byte[] key, byte[] field);

    /**
     * Redis-Hash：获取字段对应的值（同步）
     *
     * @param key   键
     * @param field 字段
     * @return {@code byte[]} – 字段对应的值
     */
    default byte[] hgetSync(byte[] key, byte[] field) {
        return RedisHelper.get(hget(key, field), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量获取字段对应的的值（异步）
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并返回。
     *
     * @param key    Redis-Hash 键
     * @param fields Redis-Hash 字段列表
     * @return {@code CompletableFuture<List<KeyValue<字段, 值>>>}
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(byte[] key, byte[]... fields);

    /**
     * Redis-Hash：批量获取字段对应的值（同步）
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并返回。
     *
     * @param key    Redis-Hash 键
     * @param fields Redis-Hash 字段列表
     * @return {@code List<KeyValue<字段, 值>>}
     */
    default List<KeyValue<byte[], byte[]>> hmgetSync(byte[] key, byte[]... fields) {
        return RedisHelper.get(hmget(key, fields), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量获取字段对应的值（异步）
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并结果。
     *
     * @param keyFields Redis-Hash 键及对应的字段集合
     * @return {@code CompletableFuture<List<KeyValue<字段, 值>>>} <br>
     * 返回结果不区分是从哪个 Key 获取的字段和值，如果要区分不同的键，请使用不同的键分别调用 {@link #hmget(byte[], byte[]...)}
     */
    CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(Map<byte[], List<byte[]>> keyFields);

    /**
     * Redis-Hash：批量获取字段对应的值（同步）
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并结果。
     *
     * @param keyFields Redis-Hash 键及对应的字段集合
     * @return {@code List<KeyValue<字段, 值>>} <br>
     * 返回结果不区分是从哪个 Key 获取的字段和值，如果要区分不同的键，请使用不同的键分别调用 {@link #hmget(byte[], byte[]...)}
     */
    default List<KeyValue<byte[], byte[]>> hmgetSync(Map<byte[], List<byte[]>> keyFields) {
        return RedisHelper.get(hmget(keyFields), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量删除键对应的字段列表（异步）
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除数据再合并结果。
     *
     * @param keyFields {@code Map<键, List<字段>>}
     * @return {@code CompletableFuture<Long>} – 删除数量
     */
    CompletableFuture<Long> hdel(Map<byte[], List<byte[]>> keyFields);

    /**
     * Redis-Hash：批量删除键对应的字段列表（同步）
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除数据再合并结果。
     *
     * @param keyFields {@code Map<键, List<字段>>}
     * @return {@link Long} – 删除数量
     */
    default Long hdelSync(Map<byte[], List<byte[]>> keyFields) {
        return RedisHelper.get(hdel(keyFields), getSyncTimeout());
    }

    /**
     * Redis-Hash：批量删除键对应的字段列表（异步）
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除数据再合并结果。
     *
     * @param key    键
     * @param fields 字段列表
     * @return {@code CompletableFuture<Long>} – 删除数量
     */
    CompletableFuture<Long> hdel(byte[] key, byte[]... fields);

    /**
     * Redis-Hash：批量删除键对应的字段列表（同步）
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除数据再合并结果。
     *
     * @param key    键
     * @param fields 字段列表
     * @return {@link Long} – 删除数量
     */
    default Long hdelSync(byte[] key, byte[]... fields) {
        return RedisHelper.get(hdel(key, fields), getSyncTimeout());
    }

    // -------------------------- hash command end ---------------------------


    // -------------------------- sorted set command start -------------------

    /**
     * Redis-SortedSet：添加成员并设置分值（或更新已有成员的分值）（异步）
     *
     * @param key    键
     * @param member 成员
     * @param score  成员分值
     * @return {@code CompletableFuture<Long>} – 新增成员数量，值表示的状态详见 {@code ZADD} 命令
     * @see <a href="https://redis.io/docs/latest/commands/zadd/">ZADD</a>
     */
    CompletableFuture<Long> zadd(byte[] key, double score, byte[] member);

    /**
     * Redis-SortedSet：添加成员并设置分值（或更新已有成员的分值）（同步）
     *
     * @param key    键
     * @param member 成员
     * @param score  成员分值
     * @return {@link Long} – 新增成员数量，值表示的状态详见 {@code ZADD} 命令
     * @see <a href="https://redis.io/docs/latest/commands/zadd/">ZADD</a>
     */
    default Long zaddSync(byte[] key, double score, byte[] member) {
        return RedisHelper.get(zadd(key, score, member), getSyncTimeout());
    }

    /**
     * 批量添加成员并设置分值（或更新已有成员的分值）（异步）
     *
     * @param key          键
     * @param scoredValues {@code ScoredValue<成员，分值>} 列表
     * @return {@code CompletableFuture<Long>} – 新增成员数量，值表示的状态详见 {@code ZADD} 命令
     * @see <a href="https://redis.io/docs/latest/commands/zadd/">ZADD</a>
     */
    CompletableFuture<Long> zadd(byte[] key, ScoredValue<byte[]>... scoredValues);

    /**
     * 批量添加成员并设置分值（或更新已有成员的分值）（同步）
     *
     * @param key          键
     * @param scoredValues {@code ScoredValue<成员，分值>} 列表
     * @return {@link Long} – 新增成员数量，值表示的状态详见 {@code ZADD} 命令
     * @see <a href="https://redis.io/docs/latest/commands/zadd/">ZADD</a>
     */
    default Long zaddSync(byte[] key, ScoredValue<byte[]>... scoredValues) {
        return RedisHelper.get(zadd(key, scoredValues), getSyncTimeout());
    }

    /**
     * 获取指定 Redis-SortedSet 的基数（成员数量）（异步）
     *
     * @param key 键
     * @return {@code CompletableFuture<Long>} – 此 SortedSet 的基数（成员数量），如键不存在返回 0。
     * @see <a href="https://redis.io/docs/latest/commands/zcard/">ZCARD</a>
     */
    CompletableFuture<Long> zcard(byte[] key);

    /**
     * 获取指定 Redis-SortedSet 的基数（成员数量）（同步）
     *
     * @param key 键
     * @return {@link Long} – 此 SortedSet 的基数（成员数量），如键不存在返回 0。
     * @see <a href="https://redis.io/docs/latest/commands/zcard/">ZCARD</a>
     */
    default Long zcardSync(byte[] key) {
        return RedisHelper.get(zcard(key), getSyncTimeout());
    }

    /**
     * 根据给定的字典序范围，返回成员列表（异步）
     *
     * @param key   键
     * @param range 成员的字典序范围
     * @return {@code CompletableFuture<List<byte[]>>} – 给定字典序范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebylex/">ZRANGEBYLEX</a>
     */
    CompletableFuture<List<byte[]>> zrangebylex(byte[] key, Range<byte[]> range);

    /**
     * 根据给定的字典序范围，返回成员列表（同步）
     *
     * @param key   键
     * @param range 成员的字典序范围
     * @return {@code List<byte[]>} – 给定字典序范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebylex/">ZRANGEBYLEX</a>
     */
    default List<byte[]> zrangebylexSync(byte[] key, Range<byte[]> range) {
        return RedisHelper.get(zrangebylex(key, range), getSyncTimeout());
    }

    /**
     * 根据给定的字典序范围，返回成员列表（异步）
     *
     * @param key   键
     * @param range 成员的字典序范围
     * @param limit 限定条件：读偏移和读取数量
     * @return {@code CompletableFuture<List<byte[]>>} – 给定字典序范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebylex/">ZRANGEBYLEX</a>
     */
    CompletableFuture<List<byte[]>> zrangebylex(byte[] key, Range<byte[]> range, Limit limit);

    /**
     * 根据给定的字典序范围，返回成员列表（同步）
     *
     * @param key   键
     * @param range 成员的字典序范围
     * @param limit 限定条件：读偏移和读取数量
     * @return {@code List<byte[]>} – 给定字典序范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebylex/">ZRANGEBYLEX</a>
     */
    default List<byte[]> zrangebylexSync(byte[] key, Range<byte[]> range, Limit limit) {
        return RedisHelper.get(zrangebylex(key, range, limit), getSyncTimeout());
    }

    /**
     * 根据给定的分值范围，返回成员列表（异步）
     *
     * @param key   键
     * @param range 分值范围
     * @return {@code CompletableFuture<List<byte[]>>} – 给定分值范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebyscore/">ZRANGEBYSCORE</a>
     */
    CompletableFuture<List<byte[]>> zrangebyscore(byte[] key, Range<? extends Number> range);

    /**
     * 根据给定的分值范围，返回成员列表（同步）
     *
     * @param key   键
     * @param range 分值范围
     * @return {@code List<byte[]>} – 给定分值范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebyscore/">ZRANGEBYSCORE</a>
     */
    default List<byte[]> zrangebyscoreSync(byte[] key, Range<? extends Number> range) {
        return RedisHelper.get(zrangebyscore(key, range), getSyncTimeout());
    }

    /**
     * 根据给定的分值范围，返回成员列表（异步）
     *
     * @param key   键
     * @param range 分值范围
     * @param limit 限定条件：读偏移和读取数量
     * @return {@code CompletableFuture<List<byte[]>>} – 给定分值范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebyscore/">ZRANGEBYSCORE</a>
     */
    CompletableFuture<List<byte[]>> zrangebyscore(byte[] key, Range<? extends Number> range, Limit limit);

    /**
     * 根据给定的分值范围，返回成员列表（同步）
     *
     * @param key   键
     * @param range 分值范围
     * @param limit 限定条件：读偏移和读取数量
     * @return {@code List<byte[]>} – 给定分值范围的成员列表
     * @see <a href="https://redis.io/docs/latest/commands/zrangebyscore/">ZRANGEBYSCORE</a>
     */
    default List<byte[]> zrangebyscoreSync(byte[] key, Range<? extends Number> range, Limit limit) {
        return RedisHelper.get(zrangebyscore(key, range, limit), getSyncTimeout());
    }

    /**
     * 删除成员（异步）
     *
     * @param key     键
     * @param members 成员列表
     * @return {@code CompletableFuture<Long>} – 删除的成员数量
     * @see <a href="https://redis.io/docs/latest/commands/zrem/">ZREM</a>
     */
    CompletableFuture<Long> zrem(byte[] key, byte[]... members);

    /**
     * 删除成员（同步）
     *
     * @param key     键
     * @param members 成员列表
     * @return {@code Long} – 删除的成员数量
     * @see <a href="https://redis.io/docs/latest/commands/zrem/">ZREM</a>
     */
    default Long zremSync(byte[] key, byte[]... members) {
        return RedisHelper.get(zrem(key, members), getSyncTimeout());
    }

    // -------------------------- sorted set command end ---------------------


    // -------------------------- script command start -----------------------

    /**
     * Redis-Script：使用指定的键集和参数执行 Script（异步）
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     * @see <a href="https://redis.io/docs/latest/commands/eval/">EVAL</a>
     */
    <T> CompletableFuture<T> eval(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * Redis-Script：使用指定的键集和参数执行 Script（同步）
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     * @see <a href="https://redis.io/docs/latest/commands/eval/">EVAL</a>
     */
    default <T> T evalSync(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(eval(script, keys, args), getSyncTimeout());
    }

    /**
     * Redis-Script：使用指定的键集和参数执行只读的 Script（异步）
     * <p>
     * RedisServer available since: 7.0.0
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     * @see <a href="https://redis.io/docs/latest/commands/eval_ro/">EVAL_RO</a>
     */
    <T> CompletableFuture<T> evalReadOnly(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * Redis-Script：使用指定的键集和参数执行只读的 Script（同步）
     * <p>
     * RedisServer available since: 7.0.0
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     * @see <a href="https://redis.io/docs/latest/commands/eval_ro/">EVAL_RO</a>
     */
    default <T> T evalReadOnlySync(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalReadOnly(script, keys, args), getSyncTimeout());
    }

    /**
     * Redis-Script：使用指定的键集和参数执行 Script（异步）
     * <p>
     * 可能问题： <p>
     * 1. RedisServer 可能未加载此脚本；<br>
     * 2. RedisServer 的 SHA1 摘要 与 RedisScript 对象的 SHA1 摘要不一致。
     * <p>
     * 因此，如果出现 {@code RedisNoScriptException} 异常，实现类需：
     * 1.先执行 {@link #scriptLoad(RedisScript)} 方法；<br>
     * 2.再转而执行 {@link #eval(RedisScript, byte[][], byte[]...)} 方法。
     * <p>
     * 总之，尽可能实现：<br>
     * 1. 数据操作成功；<br>
     * 2. 调用此方法后，RedisScript 对象的 SHA1 摘要与 RedisServer 的 SHA1 摘要保持一致，
     * 下一次再用同一 RedisScript 对象调用此方法时，不再出现异常。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     */
    <T> CompletableFuture<T> evalsha(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * Redis-Script：使用指定的键集和参数执行 Script（同步）
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     */
    default <T> T evalshaSync(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalsha(script, keys, args), getSyncTimeout());
    }

    /**
     * Redis-Script：使用指定的键集和参数执行只读的 Script（异步）
     * <p>
     * RedisServer available since: 7.0.0
     * <p>
     * 可能问题： <p>
     * 1. RedisServer 可能未加载此脚本；<br>
     * 2. RedisServer 的 SHA1 摘要 与 RedisScript 对象的 SHA1 摘要不一致。
     * <p>
     * 因此，如果出现 {@code RedisNoScriptException} 异常，实现类需：
     * 1.先执行 {@link #scriptLoad(RedisScript)} 方法；<br>
     * 2.再转而执行 {@link #evalReadOnly(RedisScript, byte[][], byte[]...)} 方法。
     * <p>
     * 总之，尽可能实现：<br>
     * 1. 数据操作成功；<br>
     * 2. 调用此方法后，RedisScript 对象的 SHA1 摘要与 RedisServer 的 SHA1 摘要保持一致，
     * 下一次再用同一 RedisScript 对象调用此方法时，不再出现异常。
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     * @see <a href="https://redis.io/docs/latest/commands/evalsha_ro/">EVALSHA_RO</a>
     */
    <T> CompletableFuture<T> evalshaReadOnly(RedisScript script, byte[][] keys, byte[]... args);

    /**
     * Redis-Script：使用指定的键集和参数执行只读的 Script（同步）
     * <p>
     * RedisServer available since: 7.0.0
     *
     * @param <T>    返回结果类型
     * @param script 脚本对象
     * @param keys   键列表
     * @param args   参数列表
     * @return 脚本执行结果
     * @see <a href="https://redis.io/docs/latest/commands/evalsha_ro/">EVALSHA_RO</a>
     */
    default <T> T evalshaReadOnlySync(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalshaReadOnly(script, keys, args), getSyncTimeout());
    }

    /**
     * Redis-Script：加载 Script（异步）
     * <p>
     * 1.Script 加载到 Redis 服务器；<br>
     * 2.RedisServer 返回的 SHA1 摘要设置到 RedisScript 对象。
     *
     * @param script 脚本对象
     * @return {@link String} – SHA1 摘要
     */
    CompletableFuture<String> scriptLoad(RedisScript script);

    /**
     * Redis-Script：加载 Script（同步）
     * <p>
     * 1.Script 加载到 Redis 服务器；<br>
     * 2.RedisServer 返回的 SHA1 摘要设置到 RedisScript 对象。
     *
     * @param script 脚本对象
     * @return {@link String} – SHA1 摘要
     */
    default String scriptLoadSync(RedisScript script) {
        return RedisHelper.get(scriptLoad(script), getSyncTimeout());
    }

    // -------------------------- script command end -------------------------
}
