package com.igeeksky.xredis;

import com.igeeksky.xredis.api.Pipeline;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.ExpiryKeyFieldValue;
import com.igeeksky.xredis.common.RedisOperationException;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import io.lettuce.core.KeyScanCursor;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.ScanArgs;
import io.lettuce.core.ScanCursor;
import io.lettuce.core.output.KeyStreamingChannel;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * RedisOperatorProxy（简化批量数据读写）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class LettuceOperatorProxy implements com.igeeksky.xredis.common.RedisOperatorProxy {

    private final int batchSize;
    private final RedisOperator<byte[], byte[]> redisOperator;

    /**
     * 创建 RedisOperatorProxy
     *
     * @param batchSize     命令提交数量阈值（如 batchSize 为 10000，写入 100 万数据会分 100 批次提交到 Redis）
     * @param redisOperator RedisOperator
     */
    public LettuceOperatorProxy(int batchSize, RedisOperator<byte[], byte[]> redisOperator) {
        this.batchSize = batchSize;
        this.redisOperator = redisOperator;
    }

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
    public CompletableFuture<Boolean> hset(byte[] key, byte[] field, byte[] value) {
        return this.redisOperator.async().hset(key, field, value).toCompletableFuture();
    }

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
    public CompletableFuture<String> hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        int i = 0, count = 0, size = keyFieldValues.size();
        CompletionStage<String>[] futures = new RedisFuture[size];

        Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();
        for (Map.Entry<byte[], Map<byte[], byte[]>> entry : keyFieldValues.entrySet()) {
            byte[] key = entry.getKey();
            Map<byte[], byte[]> fieldValues = entry.getValue();
            if (Maps.isNotEmpty(fieldValues)) {
                if (fieldValues.size() > batchSize) {
                    futures[i++] = this.hmset(key, fieldValues);
                } else {
                    futures[i++] = pipeline.hmset(key, fieldValues);
                    if ((count += fieldValues.size()) >= batchSize) {
                        pipeline.flushCommands();
                        count = 0;
                    }
                }
            }
        }
        pipeline.flushCommands();
        return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
        // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
    }

    /**
     * 异步批量设置 Redis-Hash 的字段值
     * <p>
     * 如单次提交的数据量超过 batchSize，则分批次提交数据。
     *
     * @param key         Redis-Hash 的键
     * @param fieldValues 字段与值的映射
     * @return 返回表示异步操作完成的Future对象
     */
    public CompletableFuture<String> hmset(byte[] key, Map<byte[], byte[]> fieldValues) {
        int size = fieldValues.size();
        // 当数据量低于阈值，直接保存（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().hmset(key, fieldValues).toCompletableFuture();
        }
        // 当数据量超过阈值，分批保存
        CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(batchSize);
        for (Map.Entry<byte[], byte[]> entry : fieldValues.entrySet()) {
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() == batchSize) {
                future = combineStringFuture(future, this.redisOperator.async().hmset(key, subKeyValues));
                subKeyValues.clear();
            }
        }
        if (!subKeyValues.isEmpty()) {
            future = combineStringFuture(future, this.redisOperator.async().hmset(key, subKeyValues));
            subKeyValues.clear();
        }
        return future;
    }

    /**
     * 异步设置 Redis-Hash 键的值及过期时间
     *
     * @param key          Hash 键
     * @param milliseconds 过期时间（毫秒）
     * @param field        Hash 字段
     * @param value        Hash 字段值
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<Long> hpset(byte[] key, long milliseconds, byte[] field, byte[] value) {
        // TODO Lettuce bug：Pipeline 命令并不一定按照顺序提交，有可能先 hpexpire 后 hset，导致 hpexpire 结果返回 -2
        // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
        RedisFuture<Boolean> hset = this.redisOperator.async().hset(key, field, value);
        RedisFuture<List<Long>> hpexpire = this.redisOperator.async().hpexpire(key, milliseconds, field);
        return hset.toCompletableFuture().thenCombine(hpexpire, (status, result) -> {
            if (CollectionUtils.isEmpty(result)) {
                throw new RedisOperationException("hpexpire failed.");
            }
            return result.getFirst();
        });
    }

    /**
     * 异步批量设置 Redis-Hash 键的值及过期时间
     * <p>
     * 如单次提交的数据量超过 batchSize，则分批次提交数据。
     *
     * @param keyFieldsValues 包含 Key 及其对应 Field-Value 映射的字典，其中 Key 为 Hash 键，Value 为 Field-Value 映射
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<Void> hmpset(List<ExpiryKeyFieldValue> keyFieldsValues) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        int size = keyFieldsValues.size();
        if (size <= batchSize) {
            return this.hmpset(future, keyFieldsValues);
        }
        for (int i = 0; i < size; ) {
            int remaining = size - i;
            if (remaining > batchSize) {
                future = this.hmpset(future, keyFieldsValues.subList(i, i += batchSize));
            } else {
                future = this.hmpset(future, keyFieldsValues.subList(i, i = size));
            }
        }
        return future;
    }

    private CompletableFuture<Void> hmpset(CompletableFuture<Void> future, List<ExpiryKeyFieldValue> keyFieldsValues) {
        Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();
        List<CompletionStage<?>> stages = new ArrayList<>(keyFieldsValues.size() * 2);
        for (ExpiryKeyFieldValue keyFieldValue : keyFieldsValues) {
            byte[] key = keyFieldValue.getKey();
            byte[] field = keyFieldValue.getField();
            stages.add(pipeline.hset(key, field, keyFieldValue.getValue()));
            stages.add(pipeline.hpexpire(key, keyFieldValue.getTtl(), field));
        }
        pipeline.flushCommands();
        for (CompletionStage<?> stage : stages) {
            future = future.thenCombine(stage, (v, ignored) -> v);
        }
        return future;
        // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
    }

    /**
     * 异步获取 Redis-Hash 键的值
     *
     * @param key   Redis-Hash 的键
     * @param field Redis-Hash 的字段
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<byte[]> hget(byte[] key, byte[] field) {
        return this.redisOperator.async().hget(key, field).toCompletableFuture();
    }

    /**
     * 异步批量获取 Redis-Hash 键的值
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并返回。
     *
     * @param keyFields Redis-Hash 键及对应的字段集合
     * @param totalSize Redis-Hash 键及对应的字段集合的总数量，用于创建返回结果集时指定容量
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(Map<byte[], List<byte[]>> keyFields, int totalSize) {
        // int i = 0, count = 0;
        // RedisFuture<List<KeyValue<byte[], byte[]>>>[] futures = new RedisFuture[keyFields.size()];
        //
        // Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();
        // for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
        //     byte[] key = entry.getKey();
        //     List<byte[]> fields = entry.getValue();
        //     if (CollectionUtils.isNotEmpty(fields)) {
        //         byte[][] array = fields.toArray(new byte[0][]);
        //         futures[i++] = pipeline.hmget(key, array);
        //         if ((count += array.length) >= batchSize) {
        //             pipeline.flushCommands();
        //             count = 0;
        //         }
        //     }
        // }
        // pipeline.flushCommands();
        // CompletableFuture<List<KeyValue<byte[], byte[]>>> future =
        //         combineKeyValues(CompletableFuture.completedFuture(new ArrayList<>(totalSize)), futures);
        // pipeline.flushCommands();
        // return future;

        // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline

        int i = 0;
        CompletableFuture<List<KeyValue<byte[], byte[]>>>[] futures = new CompletableFuture[keyFields.size()];
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                futures[i++] = this.hmget(key, fields.toArray(new byte[0][]));
            }
        }
        return combineKeyValues(CompletableFuture.completedFuture(new ArrayList<>(totalSize)), futures);
    }

    /**
     * 异步批量获取 Redis-Hash 键的值
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次获取数据再合并返回。
     *
     * @param key    Redis-Hash 的键
     * @param fields Redis-Hash 的字段列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(byte[] key, byte[]... fields) {
        int size = fields.length;
        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().hmget(key, fields).toCompletableFuture()
                    .thenApply(LettuceOperatorProxy::from);
        }
        // 当数据量超过阈值，分批查询
        CompletableFuture<List<KeyValue<byte[], byte[]>>> future = CompletableFuture.completedFuture(new ArrayList<>(size));
        for (int i = 0; i < size; ) {
            int remaining = size - i;
            byte[][] subFields;
            if (remaining >= batchSize) {
                subFields = new byte[batchSize][];
                System.arraycopy(fields, i, subFields, 0, batchSize);
                i += batchSize;
            } else {
                subFields = new byte[remaining][];
                System.arraycopy(fields, i, subFields, 0, remaining);
                i += remaining;
            }
            future = combineKeyValues(future, this.redisOperator.async().hmget(key, subFields));
        }
        return future;
    }

    /**
     * 异步批量删除 Redis-Hash 键的值
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除数据。
     *
     * @param keyFields 包含 Key 及其对应 field 列表，其中 Key 为 Hash 键，Value 为 field 列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<Long> hdel(Map<byte[], List<byte[]>> keyFields) {
        int i = 0, count = 0;
        CompletionStage<Long>[] futures = new CompletionStage[keyFields.size()];
        Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            byte[][] fields = entry.getValue().toArray(new byte[0][]);
            if (fields.length > batchSize) {
                futures[i++] = this.hdel(key, fields);
            } else {
                futures[i++] = pipeline.hdel(key, fields);
                if ((count += fields.length) >= batchSize) {
                    pipeline.flushCommands();
                    count = 0;
                }
            }
        }
        pipeline.flushCommands();
        return combineLongFuture(CompletableFuture.completedFuture(0L), futures);
        // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
    }

    /**
     * 异步批量删除 Redis-Hash 键对应的字段列表
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除。
     *
     * @param key    Redis-Hash 键
     * @param fields Redis-Hash 键对应的字段列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<Long> hdel(byte[] key, byte[]... fields) {
        int size = fields.length;
        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().hdel(key, fields).toCompletableFuture();
        }
        // 当数据量超过阈值，分批删除
        CompletableFuture<Long> future = CompletableFuture.completedFuture(0L);
        for (int i = 0; i < size; ) {
            int remaining = size - i;
            if (remaining > batchSize) {
                byte[][] subFields = new byte[batchSize][];
                System.arraycopy(fields, i, subFields, 0, batchSize);
                future = combineLongFuture(future, this.redisOperator.async().hdel(key, subFields));
                i += batchSize;
            } else {
                byte[][] subFields = new byte[remaining][];
                System.arraycopy(fields, i, subFields, 0, remaining);
                future = combineLongFuture(future, this.redisOperator.async().hdel(key, subFields));
                i = size;
            }
        }
        return future;
    }

    /**
     * 异步设置 Redis-String 键的值
     *
     * @param key   键
     * @param value 值
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<String> set(byte[] key, byte[] value) {
        return this.redisOperator.async().set(key, value).toCompletableFuture();
    }

    /**
     * 异步获取 Redis-String 键的值
     *
     * @param key 键
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步获取的值
     */
    public CompletableFuture<byte[]> get(byte[] key) {
        return this.redisOperator.async().get(key).toCompletableFuture();
    }

    /**
     * 异步批量设置 Redis-String 键的值
     * <p>
     * 如单次存储的数据量超过 batchSize，则分批次存储。
     *
     * @param keyValues Redis-String 键及对应的值集合
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<String> mset(Map<byte[], byte[]> keyValues) {
        int size = keyValues.size();
        // 当数据量低于阈值，直接存储（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().mset(keyValues).toCompletableFuture();
        }

        // 当数据量超过阈值，分批存储
        CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(batchSize);
        for (Map.Entry<byte[], byte[]> entry : keyValues.entrySet()) {
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() == batchSize) {
                future = combineStringFuture(future, this.redisOperator.async().mset(subKeyValues));
                subKeyValues.clear();
            }
        }
        if (!subKeyValues.isEmpty()) {
            future = combineStringFuture(future, this.redisOperator.async().mset(subKeyValues));
        }
        return future;
    }

    /**
     * 异步批量获取键对应的值
     * <p>
     * 如单次获取的数据量超过 batchSize，则分批次查询数据，然后再合并返回。
     *
     * @param keys 键列表
     * @return 返回一个 {@code CompletableFuture<List<V>>} 对象，表示异步操作的结果
     */
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> mget(byte[][] keys) {
        int size = keys.length;

        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().mget(keys).toCompletableFuture().thenApply(LettuceOperatorProxy::from);
        }

        // 当数据量超过阈值，分批查询
        CompletableFuture<List<KeyValue<byte[], byte[]>>> future = CompletableFuture.completedFuture(new ArrayList<>(size));
        for (int i = 0; i < size; ) {
            byte[][] subKeys;
            int remaining = size - i;
            if (remaining >= batchSize) {
                subKeys = new byte[batchSize][];
                System.arraycopy(keys, i, subKeys, 0, batchSize);
                i += batchSize;
            } else {
                subKeys = new byte[remaining][];
                System.arraycopy(keys, i, subKeys, 0, remaining);
                i += remaining;
            }
            future = combineKeyValues(future, this.redisOperator.async().mget(subKeys));
        }

        return future;
    }

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     *
     * @param key          键
     * @param milliseconds 过期时间（毫秒）
     * @param value        值
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value) {
        return this.redisOperator.async().psetex(key, milliseconds, value).toCompletableFuture();
    }

    /**
     * 异步设置 Redis-String 键的值，并设置过期时间
     *
     * @param keyValues （键-值-过期时间）列表
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<String> psetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        int i = 0, j = 0;
        int size = keyValues.size();
        int capacity = Math.min(batchSize, size);

        Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();

        CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
        RedisFuture<String>[] futures = new RedisFuture[capacity];
        for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
            i++;
            futures[j++] = pipeline.psetex(kv.getKey(), kv.getTtl(), kv.getValue());
            if (j == capacity) {
                pipeline.flushCommands();
                future = combineStringFutures(future, futures);
                int remaining = size - i;
                if (remaining > 0 && remaining < capacity) {
                    capacity = remaining;
                    futures = new RedisFuture[capacity];
                }
                j = 0;
            }
        }
        pipeline.flushCommands();
        return combineStringFutures(future, futures);
        // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
    }

    /**
     * 异步批量删除键
     * <p>
     * 如单次删除的数据量超过 batchSize，则分批次删除。
     *
     * @param keys 键
     * @return 返回一个 {@link CompletableFuture} 对象，表示异步操作的结果
     */
    public CompletableFuture<Long> del(byte[]... keys) {
        int size = keys.length;

        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().del(keys).toCompletableFuture();
        }

        // 当数据量超过阈值，分批删除
        CompletableFuture<Long> future = CompletableFuture.completedFuture(0L);
        for (int i = 0; i < size; ) {
            int remaining = size - i;
            byte[][] subKeys;
            if (remaining >= batchSize) {
                subKeys = new byte[batchSize][];
                System.arraycopy(keys, i, subKeys, 0, batchSize);
                i += batchSize;
            } else {
                subKeys = new byte[remaining][];
                System.arraycopy(keys, i, subKeys, 0, remaining);
                i += remaining;
            }
            future = combineLongFuture(future, this.redisOperator.async().del(subKeys));
        }

        return future;
    }

    /**
     * 异步获取匹配传入的 pattern 的键
     *
     * @param pattern 匹配模式
     * @return 返回一个 {@link CompletableFuture} 对象，表示匹配的键列表
     */
    public CompletableFuture<List<byte[]>> keys(byte[] pattern) {
        return this.redisOperator.async().keys(pattern).toCompletableFuture();
    }

    /**
     * 异步获取匹配传入的 pattern 的键并使用传入的 channel 进行逐个处理
     * <p>
     * 避免大量数据装到一个列表中返回，减少内存消耗
     *
     * @param channel 消费者
     * @param pattern 匹配模式
     * @return 返回一个 {@link CompletableFuture} 对象，表示匹配数量
     */
    public CompletableFuture<Long> keys(KeyStreamingChannel<byte[]> channel, byte[] pattern) {
        return this.redisOperator.async().keys(channel, pattern).toCompletableFuture();
    }

    /**
     * 清理匹配指定模式的 key
     * <p>
     * 内部使用 ScanCursor 扫描，避免使用 KEYS 命令，避免内存消耗过大
     *
     * @param pattern 模式
     * @return 清理数量
     */
    public long clear(byte[] pattern) {
        return doClear(pattern, 0, 0);
    }

    private long doClear(byte[] pattern, long num, int times) {
        if (times > 1) {
            return num;
        }
        ScanCursor cursor = ScanCursor.INITIAL;
        ScanArgs args = ScanArgs.Builder.matches(pattern).limit(batchSize);
        while (!cursor.isFinished()) {
            KeyScanCursor<byte[]> keyScanCursor = this.redisOperator.sync().scan(cursor, args);
            List<byte[]> keys = keyScanCursor.getKeys();
            if (!keys.isEmpty()) {
                Long result = this.redisOperator.sync().del(keys.toArray(new byte[keys.size()][]));
                if (result != null) {
                    num += result;
                }
            }
            cursor = keyScanCursor;
        }
        return doClear(pattern, num, ++times);
    }

    /**
     * 获取 Redis 版本信息
     *
     * @return 版本信息
     */
    public String version() {
        String serverInfo = this.redisOperator.sync().info("Server");
        String[] array = serverInfo.split("\n");
        for (String info : array) {
            if (info.startsWith("redis_version")) {
                return info.split(":")[1].trim();
            }
        }
        return null;
    }

    private CompletableFuture<List<KeyValue<byte[], byte[]>>> combineKeyValues(CompletableFuture<List<KeyValue<byte[], byte[]>>> future,
                                                                               CompletionStage<List<KeyValue<byte[], byte[]>>>[] stages) {
        for (CompletionStage<List<KeyValue<byte[], byte[]>>> stage : stages) {
            if (stage != null) {
                future = future.thenCombine(stage, (results, keyValues) -> {
                    if (CollectionUtils.isNotEmpty(keyValues)) {
                        results.addAll(keyValues);
                    }
                    return results;
                });
            }
        }
        return future;
    }

    private CompletableFuture<List<KeyValue<byte[], byte[]>>> combineKeyValues(CompletableFuture<List<KeyValue<byte[], byte[]>>> future,
                                                                               CompletionStage<List<io.lettuce.core.KeyValue<byte[], byte[]>>> stage) {
        return future.thenCombine(stage, (results, keyValues) -> {
            if (CollectionUtils.isNotEmpty(keyValues)) {
                results.addAll(from(keyValues));
            }
            return results;
        });
    }

    private static CompletableFuture<Long> combineLongFuture(CompletableFuture<Long> future,
                                                             CompletionStage<Long>[] stages) {
        for (CompletionStage<Long> stage : stages) {
            if (stage != null) {
                future = combineLongFuture(future, stage);
            }
        }
        return future;
    }

    private static CompletableFuture<Long> combineLongFuture(CompletableFuture<Long> future,
                                                             CompletionStage<Long> stage) {
        return future.thenCombine(stage, (cnt, result) -> {
            if (result != null) {
                return cnt + result;
            }
            return cnt;
        });
    }

    private static CompletableFuture<String> combineStringFutures(CompletableFuture<String> future,
                                                                  CompletionStage<String>[] stages) {
        for (CompletionStage<String> stage : stages) {
            if (stage != null) {
                future = combineStringFuture(future, stage);
            }
        }
        return future;
    }

    private static CompletableFuture<String> combineStringFuture(CompletableFuture<String> future,
                                                                 CompletionStage<String> stage) {
        return future.thenCombine(stage, (original, result) -> {
            if (!Objects.equals(OK, result)) {
                return result;
            }
            return original;
        });
    }

    private static List<KeyValue<byte[], byte[]>> from(List<io.lettuce.core.KeyValue<byte[], byte[]>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyList();
        }
        List<KeyValue<byte[], byte[]>> results = new ArrayList<>(keyValues.size());
        keyValues.forEach(keyValue -> {
            if (keyValue != null && keyValue.hasValue()) {
                results.add(KeyValue.create(keyValue.getKey(), keyValue.getValue()));
            }
        });
        return results;
    }

}
