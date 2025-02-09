package com.igeeksky.redis;

import com.igeeksky.redis.api.Pipeline;
import com.igeeksky.redis.api.RedisOperator;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import io.lettuce.core.*;
import io.lettuce.core.output.KeyStreamingChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class RedisOperatorProxy {

    private static final String OK = "OK";

    private final int batchSize;
    private final RedisOperator<byte[], byte[]> redisOperator;

    public RedisOperatorProxy(int batchSize, RedisOperator<byte[], byte[]> redisOperator) {
        this.batchSize = batchSize;
        this.redisOperator = redisOperator;
    }

    public CompletableFuture<Boolean> hset(byte[] key, byte[] field, byte[] value) {
        return this.redisOperator.async().hset(key, field, value).toCompletableFuture();
    }

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
        if (count > 0) {
            pipeline.flushCommands();
        }
        return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
    }

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

    public CompletableFuture<Void> hpset(byte[] key, long milliseconds, byte[] field, byte[] value) {
        Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();
        RedisFuture<Boolean> hset = pipeline.hset(key, field, value);
        RedisFuture<List<Long>> hpexpire = pipeline.hpexpire(key, milliseconds, field);
        pipeline.flushCommands();
        return hset.toCompletableFuture().thenCombine(hpexpire, (status, result) -> null);
    }

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
    }

    public CompletableFuture<byte[]> hget(byte[] key, byte[] field) {
        return this.redisOperator.async().hget(key, field).toCompletableFuture();
    }

    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(byte[] key, byte[]... fields) {
        int size = fields.length;
        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().hmget(key, fields).toCompletableFuture();
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

    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(Map<byte[], List<byte[]>> keyFields, int totalSize) {
        int i = 0, count = 0;
        RedisFuture<List<KeyValue<byte[], byte[]>>>[] futures = new RedisFuture[keyFields.size()];

        Pipeline<byte[], byte[]> pipeline = this.redisOperator.pipeline();
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                byte[][] array = fields.toArray(new byte[0][]);
                futures[i++] = pipeline.hmget(key, array);
                if ((count += array.length) >= batchSize) {
                    pipeline.flushCommands();
                    count = 0;
                }
            }
        }
        pipeline.flushCommands();
        return combineKeyValues(CompletableFuture.completedFuture(new ArrayList<>(totalSize)), futures);
    }

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
        if (count > 0) {
            pipeline.flushCommands();
        }
        return combineLongFuture(CompletableFuture.completedFuture(0L), futures);
    }

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

    public CompletableFuture<String> set(byte[] key, byte[] value) {
        return this.redisOperator.async().set(key, value).toCompletableFuture();
    }

    public CompletableFuture<byte[]> get(byte[] key) {
        return this.redisOperator.async().get(key).toCompletableFuture();
    }

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

    public CompletableFuture<List<KeyValue<byte[], byte[]>>> mget(byte[][] keys) {
        int size = keys.length;

        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().mget(keys).toCompletableFuture();
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

    public CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value) {
        return this.redisOperator.async().psetex(key, milliseconds, value).toCompletableFuture();
    }

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
        return combineStringFutures(future, futures);
    }

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

    public CompletableFuture<List<byte[]>> keys(byte[] pattern) {
        return this.redisOperator.async().keys(pattern).toCompletableFuture();
    }

    public CompletableFuture<Long> keys(KeyStreamingChannel<byte[]> channel, byte[] pattern) {
        return this.redisOperator.async().keys(channel, pattern).toCompletableFuture();
    }

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
                future = combineKeyValues(future, stage);
            }
        }
        return future;
    }

    private CompletableFuture<List<KeyValue<byte[], byte[]>>> combineKeyValues(CompletableFuture<List<KeyValue<byte[], byte[]>>> future,
                                                                               CompletionStage<List<KeyValue<byte[], byte[]>>> stage) {
        return future.thenCombine(stage, (list, result) -> {
            if (CollectionUtils.isNotEmpty(result)) {
                list.addAll(result);
            }
            return list;
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

}
