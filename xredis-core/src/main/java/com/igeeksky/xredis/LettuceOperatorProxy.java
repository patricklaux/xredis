package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisAsyncOperator;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.*;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import io.lettuce.core.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * RedisOperatorProxy（简化批量数据读写）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceOperatorProxy implements RedisOperatorProxy {

    private static final StringCodec CODEC = StringCodec.getInstance(StandardCharsets.UTF_8);

    private final int batchSize;
    private final RedisOperator<byte[], byte[]> redisOperator;

    /**
     * 创建 RedisOperatorProxy
     *
     * @param batchSize     命令提交数量阈值（如 batchSize 为 10000，写入 100 万数据会分 100 批次提交到 Redis）
     * @param redisOperator RedisOperator
     */
    public LettuceOperatorProxy(int batchSize, RedisOperator<byte[], byte[]> redisOperator) {
        Assert.isTrue(batchSize > 0, "batchSize must be greater than 0");
        Assert.notNull(redisOperator, "redisOperator must not be null");
        this.batchSize = batchSize;
        this.redisOperator = redisOperator;
    }

    @Override
    public boolean isCluster() {
        return redisOperator.isCluster();
    }

    public CompletableFuture<Long> del(byte[]... keys) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = keys.length;
        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= batchSize) {
            return async.del(keys).toCompletableFuture();
        }
        // 当数据量超过阈值，分批删除
        return combineLongFutures(CompletableFuture.completedFuture(0L), this.splitApply(keys, async::del));
    }

    @Override
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


    @Override
    public CompletableFuture<String> set(byte[] key, byte[] value) {
        return this.redisOperator.async().set(key, value).toCompletableFuture();
    }

    @Override
    public CompletableFuture<byte[]> get(byte[] key) {
        return this.redisOperator.async().get(key).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> mset(Map<byte[], byte[]> keyValues) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = keyValues.size();
        // 当数据量低于阈值，直接存储（小于等于限定数量）
        if (size <= batchSize) {
            return async.mset(keyValues).toCompletableFuture();
        }
        // 当数据量超过阈值，分批存储
        return combineStringFutures(CompletableFuture.completedFuture(OK), this.splitApply(keyValues, async::mset));
    }

    @Override
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> mget(byte[][] keys) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = keys.length;
        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return async.mget(keys).toCompletableFuture().thenApply(LettuceConvertor::fromKeyValues);
        }
        // 当数据量超过阈值，分批查询
        CompletableFuture<List<KeyValue<byte[], byte[]>>> future = CompletableFuture.completedFuture(new ArrayList<>(size));
        return combineLettuceKeyValues(future, this.splitApply(keys, async::mget));
    }

    @Override
    public CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value) {
        return this.redisOperator.async().psetex(key, milliseconds, value).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> psetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        if (this.isCluster()) {
            RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
            List<CompletionStage<String>> futures = new ArrayList<>(keyValues.size());
            for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
                futures.add(async.psetex(kv.getKey(), kv.getTtl(), kv.getValue()));
            }
            return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
        }

        RedisScript script = RedisExpireScript.PSETEX_RANDOM;

        int size = keyValues.size();
        if (size <= batchSize) {
            byte[][] keys = new byte[size][];
            byte[][] args = new byte[size * 2][];
            for (int i = 0, j = 0; i < size; i++) {
                ExpiryKeyValue<byte[], byte[]> kv = keyValues.get(i);
                keys[i] = kv.getKey();
                args[j++] = CODEC.encode(Long.toString(kv.getTtl()));
                args[j++] = kv.getValue();
            }
            return this.evalsha(script, keys, args);
        }

        int i = 0, j = 0, k = 0, capacity = batchSize;
        byte[][] keys = new byte[capacity][];
        byte[][] args = new byte[capacity * 2][];
        List<CompletionStage<String>> futures = new ArrayList<>(size / capacity + 1);
        for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
            keys[i++] = kv.getKey();
            args[j++] = CODEC.encode(Long.toString(kv.getTtl()));
            args[j++] = kv.getValue();
            k++;
            if (i == capacity) {
                futures.add(this.evalsha(script, keys, args));
                int remain = size - k;
                if (remain > 0 && remain < capacity) {
                    capacity = remain;
                    keys = new byte[capacity][];
                    args = new byte[capacity * 2][];
                }
                i = 0;
                j = 0;
            }
        }
        return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
    }

    @Override
    public CompletableFuture<String> psetex(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds) {
        if (this.isCluster()) {
            RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
            CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
            List<CompletionStage<String>> futures = new ArrayList<>(keyValues.size());
            for (KeyValue<byte[], byte[]> kv : keyValues) {
                futures.add(async.psetex(kv.getKey(), milliseconds, kv.getValue()));
            }
            return combineStringFutures(future, futures);
        }

        RedisScript script = RedisExpireScript.PSETEX;

        byte[] ttl = CODEC.encode(Long.toString(milliseconds));
        int size = keyValues.size();
        if (size <= batchSize) {
            byte[][] keys = new byte[size][];
            byte[][] args = new byte[size + 1][];
            args[0] = ttl;
            for (int i = 0; i < size; i++) {
                KeyValue<byte[], byte[]> kv = keyValues.get(i);
                keys[i] = kv.getKey();
                args[i + 1] = kv.getValue();
            }
            return this.evalsha(script, keys, args);
        }

        int i = 0, j = 0, capacity = batchSize;
        byte[][] keys = new byte[capacity][];
        byte[][] args = new byte[capacity + 1][];
        args[0] = ttl;
        List<CompletionStage<String>> futures = new ArrayList<>(size / capacity + 1);
        for (KeyValue<byte[], byte[]> kv : keyValues) {
            keys[i] = kv.getKey();
            args[i + 1] = kv.getValue();
            i++;
            j++;
            if (i == capacity) {
                futures.add(this.evalsha(script, keys, args));
                int remain = size - j;
                if (remain > 0 && remain < capacity) {
                    capacity = remain;
                    keys = new byte[capacity][];
                    args = new byte[capacity + 1][];
                    args[0] = ttl;
                }
                i = 0;
            }
        }
        return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
    }


    @Override
    public CompletableFuture<Boolean> hset(byte[] key, byte[] field, byte[] value) {
        return this.redisOperator.async().hset(key, field, value).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        List<CompletionStage<String>> futures = new ArrayList<>(keyFieldValues.size());
        for (Map.Entry<byte[], Map<byte[], byte[]>> entry : keyFieldValues.entrySet()) {
            byte[] key = entry.getKey();
            Map<byte[], byte[]> fieldValues = entry.getValue();
            if (Maps.isNotEmpty(fieldValues)) {
                futures.add(this.hmset(key, fieldValues));
            }
        }
        return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
    }

    @Override
    public CompletableFuture<String> hmset(byte[] key, Map<byte[], byte[]> fieldValues) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = fieldValues.size();
        // 当数据量低于阈值，直接保存
        if (size <= batchSize) {
            return async.hmset(key, fieldValues).toCompletableFuture();
        }
        // 当数据量超过阈值，分批保存
        CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
        return combineStringFutures(future, this.splitApply(fieldValues, partition -> async.hmset(key, partition)));
    }

    @Override
    public CompletableFuture<Long> hpset(byte[] key, long milliseconds, byte[] field, byte[] value) {
        byte[][] keys = {key};
        byte[][] args = new byte[][]{CODEC.encode(Long.toString(milliseconds)), field, value};
        return this.evalsha(RedisExpireScript.HSET_HPEXPIRE, keys, args);
    }

    @Override
    public CompletableFuture<List<Long>> hmpset(Map<byte[], List<KeyValue<byte[], byte[]>>> keysFieldsValues, long milliseconds) {
        int size = 0;
        List<CompletionStage<List<Long>>> futures = new ArrayList<>(keysFieldsValues.size());
        for (Map.Entry<byte[], List<KeyValue<byte[], byte[]>>> entry : keysFieldsValues.entrySet()) {
            byte[] key = entry.getKey();
            List<KeyValue<byte[], byte[]>> fieldsValues = entry.getValue();
            if (CollectionUtils.isNotEmpty(fieldsValues)) {
                size += fieldsValues.size();
                futures.add(this.hmpset(key, milliseconds, fieldsValues));
            }
        }
        return combineListLongFutures(CompletableFuture.completedFuture(new ArrayList<>(size)), futures);
    }

    @Override
    public CompletableFuture<List<Long>> hmpset(byte[] key, long milliseconds, List<KeyValue<byte[], byte[]>> fieldsValues) {
        RedisScript script = RedisExpireScript.HMSET_HPEXPIRE;

        byte[][] keys = {key};
        byte[] ttl = CODEC.encode(Long.toString(milliseconds));
        int size = fieldsValues.size();
        // 当数据量低于阈值，直接保存
        if (size <= batchSize) {
            byte[][] args = new byte[size * 2 + 1][];
            args[0] = ttl;
            int i = 1;
            for (KeyValue<byte[], byte[]> fieldValue : fieldsValues) {
                args[i++] = fieldValue.getKey();
                args[i++] = fieldValue.getValue();
            }
            return this.evalsha(script, keys, args);
        }

        // 当数据量超过阈值，分批保存
        List<CompletionStage<List<Long>>> futures = new ArrayList<>(size / batchSize + 1);
        int i = 1, j = 0, capacity = batchSize * 2 + 1;
        byte[][] args = new byte[capacity][];
        args[0] = ttl;
        for (KeyValue<byte[], byte[]> fieldValue : fieldsValues) {
            args[i++] = fieldValue.getKey();
            args[i++] = fieldValue.getValue();
            j++;
            if (i == capacity) {
                futures.add(this.evalsha(script, keys, args));
                i = 1;
                int remain = size - j;
                if (remain > 0 && remain < capacity) {
                    capacity = remain * 2 + 1;
                    args = new byte[capacity][];
                    args[0] = ttl;
                }
            }
        }
        return combineListLongFutures(CompletableFuture.completedFuture(new ArrayList<>(size)), futures);
    }

    @Override
    public CompletableFuture<List<Long>> hmpset(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues) {
        int size = 0;
        List<CompletionStage<List<Long>>> futures = new ArrayList<>(expiryKeysFieldsValues.size());
        for (Map.Entry<byte[], List<ExpiryKeyValue<byte[], byte[]>>> entry : expiryKeysFieldsValues.entrySet()) {
            byte[] key = entry.getKey();
            List<ExpiryKeyValue<byte[], byte[]>> fieldsValues = entry.getValue();
            if (CollectionUtils.isNotEmpty(fieldsValues)) {
                size += fieldsValues.size();
                futures.add(this.hmpset(key, fieldsValues));
            }
        }
        return combineListLongFutures(CompletableFuture.completedFuture(new ArrayList<>(size)), futures);
    }

    @Override
    public CompletableFuture<List<Long>> hmpset(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues) {
        RedisScript script = RedisExpireScript.HMSET_HPEXPIRE_RANDOM;

        byte[][] keys = {key};
        int size = expiryFieldsValues.size();
        // 当数据量低于阈值，直接保存
        if (size <= batchSize) {
            byte[][] args = new byte[size * 3][];
            int i = 0;
            for (ExpiryKeyValue<byte[], byte[]> expiryKeyValue : expiryFieldsValues) {
                args[i++] = CODEC.encode(Long.toString(expiryKeyValue.getTtl()));
                args[i++] = expiryKeyValue.getKey();
                args[i++] = expiryKeyValue.getValue();
            }
            return this.evalsha(script, keys, args);
        }
        // 当数据量超过阈值，分批保存
        List<CompletionStage<List<Long>>> futures = new ArrayList<>(size / batchSize + 1);
        int i = 0, j = 0, capacity = batchSize * 3;
        byte[][] args = new byte[capacity][];
        for (ExpiryKeyValue<byte[], byte[]> expiryKeyValue : expiryFieldsValues) {
            args[i++] = CODEC.encode(Long.toString(expiryKeyValue.getTtl()));
            args[i++] = expiryKeyValue.getKey();
            args[i++] = expiryKeyValue.getValue();
            j++;
            if (i == capacity) {
                futures.add(this.evalsha(script, keys, args));
                i = 0;
                int remain = size - j;
                if (remain > 0 && remain < capacity) {
                    capacity = remain * 3;
                    args = new byte[capacity][];
                }
            }
        }
        return combineListLongFutures(CompletableFuture.completedFuture(new ArrayList<>(size)), futures);
    }

    @Override
    public CompletableFuture<byte[]> hget(byte[] key, byte[] field) {
        return this.redisOperator.async().hget(key, field).toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(Map<byte[], List<byte[]>> keyFields) {
        int totalSize = 0;
        List<CompletionStage<List<KeyValue<byte[], byte[]>>>> futures = new ArrayList<>(keyFields.size());
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                totalSize += fields.size();
                futures.add(this.hmget(key, fields.toArray(new byte[0][])));
            }
        }
        return combineKeyValues(CompletableFuture.completedFuture(new ArrayList<>(totalSize)), futures);
    }

    @Override
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(byte[] key, byte[]... fields) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = fields.length;
        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return async.hmget(key, fields).toCompletableFuture()
                    .thenApply(LettuceConvertor::fromKeyValues);
        }
        // 当数据量超过阈值，分批查询
        CompletableFuture<List<KeyValue<byte[], byte[]>>> future = CompletableFuture.completedFuture(new ArrayList<>(size));
        return combineLettuceKeyValues(future, this.splitApply(fields, subFields -> async.hmget(key, subFields)));
    }

    @Override
    public CompletableFuture<Long> hdel(Map<byte[], List<byte[]>> keyFields) {
        List<CompletionStage<Long>> futures = new ArrayList<>(keyFields.size());
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                futures.add(this.hdel(entry.getKey(), fields.toArray(new byte[0][])));
            }
        }
        return combineLongFutures(CompletableFuture.completedFuture(0L), futures);
    }

    @Override
    public CompletableFuture<Long> hdel(byte[] key, byte[]... fields) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = fields.length;
        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= batchSize) {
            return async.hdel(key, fields).toCompletableFuture();
        }
        // 当数据量超过阈值，分批删除
        CompletableFuture<Long> future = CompletableFuture.completedFuture(0L);
        return combineLongFutures(future, this.splitApply(fields, subFields -> async.hdel(key, subFields)));
    }

    @Override
    public <T> CompletableFuture<T> eval(RedisScript script, byte[][] keys, byte[]... args) {
        ScriptOutputType scriptOutputType = getScriptOutputType(script.getResultType());
        RedisFuture<T> future;
        if (ArrayUtils.isEmpty(args)) {
            future = this.redisOperator.async().eval(script.getScriptBytes(), scriptOutputType, keys);
        } else {
            future = this.redisOperator.async().eval(script.getScriptBytes(), scriptOutputType, keys, args);
        }
        return future.toCompletableFuture();
    }

    @Override
    public <T> CompletableFuture<T> evalReadOnly(RedisScript script, byte[][] keys, byte[]... args) {
        ScriptOutputType scriptOutputType = getScriptOutputType(script.getResultType());
        RedisFuture<T> future;
        if (ArrayUtils.isEmpty(args)) {
            future = this.redisOperator.async().evalReadOnly(script.getScriptBytes(), scriptOutputType, keys);
        } else {
            future = this.redisOperator.async().evalReadOnly(script.getScriptBytes(), scriptOutputType, keys, args);
        }
        return future.toCompletableFuture();
    }

    @Override
    public <T> CompletableFuture<T> evalsha(RedisScript script, byte[][] keys, byte[]... args) {
        ScriptOutputType scriptOutputType = getScriptOutputType(script.getResultType());
        RedisFuture<T> future;
        if (ArrayUtils.isEmpty(args)) {
            future = this.redisOperator.async().evalsha(script.getSha1(), scriptOutputType, keys);
        } else {
            future = this.redisOperator.async().evalsha(script.getSha1(), scriptOutputType, keys, args);
        }
        return future.toCompletableFuture()
                .exceptionallyCompose(e -> {
                    if (e instanceof RedisNoScriptException) {
                        return this.scriptLoad(script)
                                .thenCompose(ignored -> this.eval(script, keys, args));
                    }
                    return CompletableFuture.failedFuture(e);
                });
    }

    @Override
    public <T> CompletableFuture<T> evalshaReadOnly(RedisScript script, byte[][] keys, byte[]... args) {
        ScriptOutputType scriptOutputType = getScriptOutputType(script.getResultType());
        RedisFuture<T> future;
        if (ArrayUtils.isEmpty(args)) {
            future = this.redisOperator.async().evalshaReadOnly(script.getSha1(), scriptOutputType, keys);
        } else {
            future = this.redisOperator.async().evalshaReadOnly(script.getSha1(), scriptOutputType, keys, args);
        }
        return future.toCompletableFuture()
                .exceptionallyCompose(e -> {
                    if (e instanceof RedisNoScriptException) {
                        return this.scriptLoad(script)
                                .thenCompose(ignored -> this.evalReadOnly(script, keys, args));
                    }
                    return CompletableFuture.failedFuture(e);
                });
    }

    @Override
    public CompletableFuture<String> scriptLoad(RedisScript script) {
        return this.redisOperator.async()
                .scriptLoad(script.getScript())
                .toCompletableFuture()
                .thenApply(sha1 -> {
                    if (sha1 != null) {
                        script.setSha1(sha1);
                    } else {
                        throw new RedisOperationException("Failed to load script: " + script.getScript());
                    }
                    return sha1;
                });
    }

    @Override
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

    /**
     * 按 batchSize 分割数据，并执行异步操作
     *
     * @param data   待分割的数据
     * @param mapper 执行异步操作的函数
     * @param <R>    返回数据类型
     * @return {@code List<CompletionStage<R>>} 待合并的异步操作结果
     */
    private <R> List<CompletionStage<R>> splitApply(byte[][] data, Function<byte[][], CompletionStage<R>> mapper) {
        int size = data.length;
        List<CompletionStage<R>> futures = new ArrayList<>(size / batchSize + 1);
        for (int i = 0; i < size; ) {
            byte[][] partition;
            int remain = size - i;
            if (remain >= batchSize) {
                partition = new byte[batchSize][];
                System.arraycopy(data, i, partition, 0, batchSize);
                i += batchSize;
            } else {
                partition = new byte[remain][];
                System.arraycopy(data, i, partition, 0, remain);
                i += remain;
            }
            futures.add(mapper.apply(partition));
        }
        return futures;
    }


    /**
     * 按 batchSize 分割数据，并执行异步操作
     *
     * @param data   待分割的数据
     * @param mapper 执行异步操作的函数
     * @param <R>    返回数据类型
     * @return {@code List<CompletionStage<R>>} 待合并的异步操作结果
     */
    private <R> List<CompletionStage<R>> splitApply(Map<byte[], byte[]> data,
                                                    Function<Map<byte[], byte[]>, CompletionStage<R>> mapper) {
        int size = data.size();
        List<CompletionStage<R>> futures = new ArrayList<>(size / batchSize + 1);
        Map<byte[], byte[]> partition = Maps.newHashMap(batchSize);
        for (Map.Entry<byte[], byte[]> entry : data.entrySet()) {
            partition.put(entry.getKey(), entry.getValue());
            if (partition.size() >= batchSize) {
                futures.add(mapper.apply(partition));
                partition = Maps.newHashMap(batchSize);
            }
        }
        if (!partition.isEmpty()) {
            futures.add(mapper.apply(partition));
        }
        return futures;
    }


    private static ScriptOutputType getScriptOutputType(ResultType resultType) {
        return switch (resultType) {
            case BOOLEAN -> ScriptOutputType.BOOLEAN;
            case INTEGER -> ScriptOutputType.INTEGER;
            case MULTI -> ScriptOutputType.MULTI;
            case STATUS -> ScriptOutputType.STATUS;
            case VALUE -> ScriptOutputType.VALUE;
        };
    }

    private static CompletableFuture<List<KeyValue<byte[], byte[]>>> combineKeyValues(CompletableFuture<List<KeyValue<byte[], byte[]>>> future,
                                                                                      List<CompletionStage<List<KeyValue<byte[], byte[]>>>> stages) {
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

    private static CompletableFuture<List<KeyValue<byte[], byte[]>>> combineLettuceKeyValues(CompletableFuture<List<KeyValue<byte[], byte[]>>> future,
                                                                                             List<CompletionStage<List<io.lettuce.core.KeyValue<byte[], byte[]>>>> stages) {
        for (CompletionStage<List<io.lettuce.core.KeyValue<byte[], byte[]>>> stage : stages) {
            if (stage != null) {
                future = future.thenCombine(stage, (results, keyValues) -> {
                    if (CollectionUtils.isNotEmpty(keyValues)) {
                        results.addAll(LettuceConvertor.fromKeyValues(keyValues));
                    }
                    return results;
                });
            }
        }
        return future;
    }

    private static CompletableFuture<String> combineStringFutures(CompletableFuture<String> future,
                                                                  List<CompletionStage<String>> stages) {
        for (CompletionStage<String> stage : stages) {
            if (stage != null) {
                future = future.thenCombine(stage, (original, result) -> {
                    if (!Objects.equals(OK, result)) {
                        return result;
                    }
                    return original;
                });
            }
        }
        return future;
    }

    private static CompletableFuture<List<Long>> combineListLongFutures(CompletableFuture<List<Long>> future,
                                                                        List<CompletionStage<List<Long>>> futures) {
        for (CompletionStage<List<Long>> future1 : futures) {
            future = future.thenCombine(future1, (results, states) -> {
                if (CollectionUtils.isNotEmpty(states)) {
                    results.addAll(states);
                }
                return results;
            });
        }
        return future;
    }

    private static CompletableFuture<Long> combineLongFutures(CompletableFuture<Long> future,
                                                              List<CompletionStage<Long>> stages) {
        for (CompletionStage<Long> stage : stages) {
            if (stage != null) {
                future = future.thenCombine(stage, (cnt, result) -> {
                    if (result != null) {
                        return cnt + result;
                    }
                    return cnt;
                });
            }
        }
        return future;
    }

}
