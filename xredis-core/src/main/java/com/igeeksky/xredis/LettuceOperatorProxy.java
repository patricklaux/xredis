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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
        this.scriptLoad(RedisExpireScript.HSET_HPEXPIRE);
        this.scriptLoad(RedisExpireScript.HMSET_HPEXPIRE);
        this.scriptLoad(RedisExpireScript.HMSET_RANDOM_HPEXPIRE);
    }

    @Override
    public boolean isCluster() {
        return redisOperator.isCluster();
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
            int remain = size - i;
            byte[][] subKeys;
            if (remain >= batchSize) {
                subKeys = new byte[batchSize][];
                System.arraycopy(keys, i, subKeys, 0, batchSize);
                i += batchSize;
            } else {
                subKeys = new byte[remain][];
                System.arraycopy(keys, i, subKeys, 0, remain);
                i += remain;
            }
            future = combineLongFuture(future, this.redisOperator.async().del(subKeys));
        }
        return future;
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

    @Override
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
            int remain = size - i;
            if (remain >= batchSize) {
                subKeys = new byte[batchSize][];
                System.arraycopy(keys, i, subKeys, 0, batchSize);
                i += batchSize;
            } else {
                subKeys = new byte[remain][];
                System.arraycopy(keys, i, subKeys, 0, remain);
                i += remain;
            }
            future = combineKeyValues(future, this.redisOperator.async().mget(subKeys));
        }
        return future;
    }

    @Override
    public CompletableFuture<String> psetex(byte[] key, long milliseconds, byte[] value) {
        return this.redisOperator.async().psetex(key, milliseconds, value).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> psetex(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        if (this.isCluster()) {
            // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
            RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
            CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
            List<CompletionStage<String>> futures = new ArrayList<>(keyValues.size());
            for (ExpiryKeyValue<byte[], byte[]> kv : keyValues) {
                futures.add(async.psetex(kv.getKey(), kv.getTtl(), kv.getValue()));
            }
            return combineStringFutures(future, futures);
        }

        ScriptOutputType type = ScriptOutputType.STATUS;
        String sha1 = RedisExpireScript.PSETEX_RANDOM.getSha1();
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
            RedisFuture<String> redisFuture = this.redisOperator.async().evalsha(sha1, type, keys, args);
            return redisFuture.toCompletableFuture();
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
                futures.add(this.redisOperator.async().evalsha(sha1, type, keys, args));
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
            // TODO Lettuce bug：Pipeline 刷新时可能会遗漏命令，导致等待超时异常，待 Lettuce 后续修复再改用 Pipeline
            CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
            List<CompletionStage<String>> futures = new ArrayList<>(keyValues.size());
            for (KeyValue<byte[], byte[]> kv : keyValues) {
                futures.add(async.psetex(kv.getKey(), milliseconds, kv.getValue()));
            }
            return combineStringFutures(future, futures);
        }

        ScriptOutputType type = ScriptOutputType.STATUS;
        String sha1 = RedisExpireScript.PSETEX.getSha1();
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
            RedisFuture<String> redisFuture = this.redisOperator.async().evalsha(sha1, type, keys, args);
            return redisFuture.toCompletableFuture();
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
                futures.add(this.redisOperator.async().evalsha(sha1, type, keys, args));
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
        int size = fieldValues.size();
        // 当数据量低于阈值，直接保存
        if (size <= batchSize) {
            return this.redisOperator.async().hmset(key, fieldValues).toCompletableFuture();
        }
        // 当数据量超过阈值，分批保存
        CompletableFuture<String> future = CompletableFuture.completedFuture(OK);
        Map<byte[], byte[]> subKeyValues = Maps.newHashMap(batchSize);
        for (Map.Entry<byte[], byte[]> entry : fieldValues.entrySet()) {
            subKeyValues.put(entry.getKey(), entry.getValue());
            if (subKeyValues.size() >= batchSize) {
                future = combineStringFuture(future, this.redisOperator.async().hmset(key, subKeyValues));
                subKeyValues.clear();
            }
        }
        if (!subKeyValues.isEmpty()) {
            future = combineStringFuture(future, this.redisOperator.async().hmset(key, subKeyValues));
        }
        return future;
    }

    @Override
    public CompletableFuture<Long> hpset(byte[] key, long milliseconds, byte[] field, byte[] value) {
        String sha1 = RedisExpireScript.HSET_HPEXPIRE.getSha1();
        byte[][] keys = {key};
        byte[][] args = new byte[][]{CODEC.encode(Long.toString(milliseconds)), field, value};
        RedisFuture<Long> future = this.redisOperator.async().evalsha(sha1, ScriptOutputType.INTEGER, keys, args);
        return future.toCompletableFuture();
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
        byte[][] keys = {key};
        byte[] ttl = CODEC.encode(Long.toString(milliseconds));
        ScriptOutputType type = ScriptOutputType.MULTI;
        String sha1 = RedisExpireScript.HMSET_HPEXPIRE.getSha1();

        int size = fieldsValues.size();
        if (size <= batchSize) {
            byte[][] args = new byte[size * 2 + 1][];
            args[0] = ttl;
            int i = 1;
            for (KeyValue<byte[], byte[]> fieldValue : fieldsValues) {
                args[i++] = fieldValue.getKey();
                args[i++] = fieldValue.getValue();
            }
            RedisFuture<List<Long>> future = this.redisOperator.async().evalsha(sha1, type, keys, args);
            return future.toCompletableFuture();
        }

        List<CompletionStage<List<Long>>> futures = new ArrayList<>(size / batchSize + 1);
        int i = 1, j = 0, capacity = batchSize * 2 + 1;
        byte[][] args = new byte[capacity][];
        args[0] = ttl;
        for (KeyValue<byte[], byte[]> fieldValue : fieldsValues) {
            args[i++] = fieldValue.getKey();
            args[i++] = fieldValue.getValue();
            j++;
            if (i == capacity) {
                futures.add(this.redisOperator.async().evalsha(sha1, type, keys, args));
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
        byte[][] keys = {key};
        ScriptOutputType type = ScriptOutputType.MULTI;
        String sha1 = RedisExpireScript.HMSET_RANDOM_HPEXPIRE.getSha1();

        int size = expiryFieldsValues.size();
        if (size <= batchSize) {
            byte[][] args = new byte[size * 3][];
            int i = 0;
            for (ExpiryKeyValue<byte[], byte[]> expiryKeyValue : expiryFieldsValues) {
                args[i++] = CODEC.encode(Long.toString(expiryKeyValue.getTtl()));
                args[i++] = expiryKeyValue.getKey();
                args[i++] = expiryKeyValue.getValue();
            }
            RedisFuture<List<Long>> future = this.redisOperator.async().evalsha(sha1, type, keys, args);
            return future.toCompletableFuture();
        }

        List<CompletionStage<List<Long>>> futures = new ArrayList<>(size / batchSize + 1);
        int i = 0, j = 0, capacity = batchSize * 3;
        byte[][] args = new byte[capacity][];
        for (ExpiryKeyValue<byte[], byte[]> expiryKeyValue : expiryFieldsValues) {
            args[i++] = CODEC.encode(Long.toString(expiryKeyValue.getTtl()));
            args[i++] = expiryKeyValue.getKey();
            args[i++] = expiryKeyValue.getValue();
            j++;
            if (i == capacity) {
                futures.add(this.redisOperator.async().evalsha(sha1, type, keys, args));
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
        int size = fields.length;
        // 当数据量低于阈值，直接查询（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().hmget(key, fields).toCompletableFuture()
                    .thenApply(LettuceOperatorProxy::from);
        }
        // 当数据量超过阈值，分批查询
        CompletableFuture<List<KeyValue<byte[], byte[]>>> future = CompletableFuture.completedFuture(new ArrayList<>(size));
        for (int i = 0; i < size; ) {
            byte[][] subFields;
            int remain = size - i;
            if (remain >= batchSize) {
                subFields = new byte[batchSize][];
                System.arraycopy(fields, i, subFields, 0, batchSize);
                i += batchSize;
            } else {
                subFields = new byte[remain][];
                System.arraycopy(fields, i, subFields, 0, remain);
                i += remain;
            }
            future = combineKeyValues(future, this.redisOperator.async().hmget(key, subFields));
        }
        return future;
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
        return combineLongFuture(CompletableFuture.completedFuture(0L), futures);
    }

    @Override
    public CompletableFuture<Long> hdel(byte[] key, byte[]... fields) {
        int size = fields.length;
        // 当数据量低于阈值，直接删除（小于等于限定数量）
        if (size <= batchSize) {
            return this.redisOperator.async().hdel(key, fields).toCompletableFuture();
        }
        // 当数据量超过阈值，分批删除
        CompletableFuture<Long> future = CompletableFuture.completedFuture(0L);
        for (int i = 0; i < size; ) {
            byte[][] subFields;
            int remain = size - i;
            if (remain > batchSize) {
                subFields = new byte[batchSize][];
                System.arraycopy(fields, i, subFields, 0, batchSize);
                i += batchSize;
            } else {
                subFields = new byte[remain][];
                System.arraycopy(fields, i, subFields, 0, remain);
                i += remain;
            }
            future = combineLongFuture(future, this.redisOperator.async().hdel(key, subFields));
        }
        return future;
    }


    @Override
    public <T> T eval(RedisScript script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public <T> T evalReadOnly(RedisScript script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public <T> T evalsha(RedisScript script, byte[][] keys, byte[]... args) {
        return null;
    }

    @Override
    public <T> T evalshaReadOnly(RedisScript script, byte[][] keys, byte[]... args) {
        ScriptOutputType scriptOutputType = getScriptOutputType(script.getResultType());
        if (ArrayUtils.isEmpty(args)) {
            return this.redisOperator.sync().evalshaReadOnly(script.getSha1(), scriptOutputType, keys);
        }
        return this.redisOperator.sync().evalshaReadOnly(script.getSha1(), scriptOutputType, keys, args);
    }

    @Override
    public String scriptLoad(RedisScript script) {
        String sha1 = this.redisOperator.sync().scriptLoad(script.getScript());
        if (sha1 != null) {
            script.setSha1(sha1);
        } else {
            throw new RedisOperationException("Failed to load script: " + script.getScript());
        }
        return sha1;
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

    private static ScriptOutputType getScriptOutputType(ResultType resultType) {
        return switch (resultType) {
            case BOOLEAN -> ScriptOutputType.BOOLEAN;
            case INTEGER -> ScriptOutputType.INTEGER;
            case MULTI -> ScriptOutputType.MULTI;
            case STATUS -> ScriptOutputType.STATUS;
            case VALUE -> ScriptOutputType.VALUE;
        };
    }

    private CompletableFuture<List<KeyValue<byte[], byte[]>>> combineKeyValues(CompletableFuture<List<KeyValue<byte[], byte[]>>> future,
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
                                                             List<CompletionStage<Long>> stages) {
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
                                                                  List<CompletionStage<String>> stages) {
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
