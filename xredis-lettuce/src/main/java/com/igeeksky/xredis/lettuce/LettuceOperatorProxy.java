package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.Limit;
import com.igeeksky.xredis.common.Range;
import com.igeeksky.xredis.common.ScoredValue;
import com.igeeksky.xredis.common.*;
import com.igeeksky.xredis.lettuce.api.RedisAsyncOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * LettuceOperatorProxy
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceOperatorProxy implements RedisOperatorProxy {

    private static final StringCodec CODEC = StringCodec.getInstance(StandardCharsets.UTF_8);

    private final long timeout;
    private final int batchSize;
    private final boolean redisCompatible;
    private final RedisOperator<byte[], byte[]> redisOperator;

    /**
     * 使用默认的 {@code batchSize} 和 {@code syncTimeout}，创建 RedisOperatorProxy
     * <p>
     * batchSize 默认值：10000 <br>
     * timeout 默认值：60000
     * compatible 默认值：false
     *
     * @param redisOperator RedisOperator
     */
    public LettuceOperatorProxy(RedisOperator<byte[], byte[]> redisOperator) {
        this(60000, 10000, redisOperator);
    }

    /**
     * 使用指定的 {@code batchSize} 和 {@code syncTimeout}，创建 RedisOperatorProxy
     * <p>
     * compatible 默认值：false
     *
     * @param timeout       同步获取结果最大等待时长，单位：毫秒 <br>
     * @param batchSize     单批次提交数据的最大数量 <br>
     *                      如 batchSize 设为 10000，当 {@link RedisOperatorProxy} 接收到单次操作 100 万条数据的请求时，
     *                      会将数据切分为 100 份，每份 10000条数据，然后分 100 批次提交到 RedisServer。
     * @param redisOperator RedisOperator
     */
    public LettuceOperatorProxy(long timeout, int batchSize, RedisOperator<byte[], byte[]> redisOperator) {
        this(timeout, batchSize, false, redisOperator);
    }

    /**
     * 使用指定的 {@code batchSize} 、 {@code syncTimeout} 和 {@code compatible}，创建 RedisOperatorProxy
     *
     * @param timeout       同步获取结果最大等待时长，单位：毫秒 <br>
     * @param batchSize     单批次提交数据的最大数量 <br>
     *                      如 batchSize 设为 10000，当 {@link RedisOperatorProxy} 接收到单次操作 100 万条数据的请求时，
     *                      会将数据切分为 100 份，每份 10000条数据，然后分 100 批次提交到 RedisServer。
     * @param compatible    是否为兼容模式，如为 true，则不使用脚本操作数据。
     * @param redisOperator RedisOperator
     */
    public LettuceOperatorProxy(long timeout, int batchSize, boolean compatible,
                                RedisOperator<byte[], byte[]> redisOperator) {
        Assert.isTrue(timeout > 0, "timeout must be greater than 0");
        Assert.isTrue(batchSize > 0, "batchSize must be greater than 0");
        Assert.notNull(redisOperator, "redisOperator must not be null");
        this.timeout = timeout;
        this.batchSize = batchSize;
        this.redisCompatible = compatible;
        this.redisOperator = redisOperator;
    }

    @Override
    public boolean isCluster() {
        return redisOperator.isCluster();
    }

    @Override
    public long getBatchSize() {
        return this.batchSize;
    }

    @Override
    public long getTimeout() {
        return this.timeout;
    }

    @Override
    public CompletableFuture<String> infoAsync() {
        return this.redisOperator.async().info().toCompletableFuture();
    }

    @Override
    public String info() {
        return RedisHelper.get(infoAsync(), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> infoAsync(String section) {
        return this.redisOperator.async().info(section).toCompletableFuture();
    }

    @Override
    public String info(String section) {
        return RedisHelper.get(infoAsync(section), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> versionAsync() {
        return this.redisOperator.async().info("Server")
                .thenApply(serverInfo -> {
                    String[] array = serverInfo.split("\n");
                    for (String info : array) {
                        if (info.startsWith("redis_version")) {
                            return info.split(":")[1].trim();
                        }
                    }
                    return null;
                })
                .toCompletableFuture();
    }

    @Override
    public String version() {
        return RedisHelper.get(versionAsync(), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<byte[]>> timeAsync() {
        return this.redisOperator.async().time().toCompletableFuture();
    }

    @Override
    public List<byte[]> time() {
        return RedisHelper.get(timeAsync(), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> timeSecondsAsync() {
        return this.redisOperator.async().timeSeconds(ByteArrayTimeConvertor.getInstance());
    }

    @Override
    public Long timeSeconds() {
        return RedisHelper.get(timeSecondsAsync(), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> timeMillisAsync() {
        return this.redisOperator.async().timeMillis(ByteArrayTimeConvertor.getInstance());
    }

    @Override
    public Long timeMillis() {
        return RedisHelper.get(timeMillisAsync(), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> timeMicrosAsync() {
        return this.redisOperator.async().timeMicros(ByteArrayTimeConvertor.getInstance());
    }

    @Override
    public Long timeMicros() {
        return RedisHelper.get(timeMicrosAsync(), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    public CompletableFuture<Long> delAsync(byte[]... keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return CompletableFuture.completedFuture(0L);
        }

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
    public Long del(byte[]... keys) {
        return RedisHelper.get(delAsync(keys), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public Long clear(byte[] pattern) {
        Assert.notNull(pattern, "pattern must not be null");
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
    public CompletableFuture<String> setAsync(byte[] key, byte[] value) {
        return this.redisOperator.async().set(key, value).toCompletableFuture();
    }

    @Override
    public String set(byte[] key, byte[] value) {
        return RedisHelper.get(setAsync(key, value), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<byte[]> getAsync(byte[] key) {
        return this.redisOperator.async().get(key).toCompletableFuture();
    }

    @Override
    public byte[] get(byte[] key) {
        return RedisHelper.get(getAsync(key), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> msetAsync(Map<byte[], byte[]> keyValues) {
        if (Maps.isEmpty(keyValues)) {
            return CompletableFuture.completedFuture(OK);
        }

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
    public String mset(Map<byte[], byte[]> keyValues) {
        return RedisHelper.get(msetAsync(keyValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> mgetAsync(byte[][] keys) {
        if (keys == null || keys.length == 0) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

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
    public List<KeyValue<byte[], byte[]>> mget(byte[][] keys) {
        return RedisHelper.get(mgetAsync(keys), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> psetexAsync(byte[] key, long milliseconds, byte[] value) {
        return this.redisOperator.async().psetex(key, milliseconds, value).toCompletableFuture();
    }

    @Override
    public String psetex(byte[] key, long milliseconds, byte[] value) {
        return RedisHelper.get(psetexAsync(key, milliseconds, value), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> psetexAsync(List<ExpiryKeyValue<byte[], byte[]>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return CompletableFuture.completedFuture(OK);
        }

        if (this.isCluster() || this.redisCompatible) {
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
            return this.evalshaAsync(script, keys, args);
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
                futures.add(this.evalshaAsync(script, keys, args));
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
    public String psetex(List<ExpiryKeyValue<byte[], byte[]>> expiryKeyValues) {
        return RedisHelper.get(psetexAsync(expiryKeyValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> psetexAsync(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return CompletableFuture.completedFuture(OK);
        }

        if (this.isCluster() || this.redisCompatible) {
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
            return this.evalshaAsync(script, keys, args);
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
                futures.add(this.evalshaAsync(script, keys, args));
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
    public String psetex(List<KeyValue<byte[], byte[]>> keyValues, long milliseconds) {
        return RedisHelper.get(psetexAsync(keyValues, milliseconds), timeout, TimeUnit.MILLISECONDS, true, true);
    }


    @Override
    public CompletableFuture<Boolean> hsetAsync(byte[] key, byte[] field, byte[] value) {
        return this.redisOperator.async().hset(key, field, value).toCompletableFuture();
    }

    @Override
    public Boolean hset(byte[] key, byte[] field, byte[] value) {
        return RedisHelper.get(hsetAsync(key, field, value), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> hmsetAsync(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        if (Maps.isEmpty(keyFieldValues)) {
            return CompletableFuture.completedFuture(OK);
        }

        List<CompletionStage<String>> futures = new ArrayList<>(keyFieldValues.size());
        for (Map.Entry<byte[], Map<byte[], byte[]>> entry : keyFieldValues.entrySet()) {
            byte[] key = entry.getKey();
            Map<byte[], byte[]> fieldValues = entry.getValue();
            if (Maps.isNotEmpty(fieldValues)) {
                futures.add(this.hmsetAsync(key, fieldValues));
            }
        }
        return combineStringFutures(CompletableFuture.completedFuture(OK), futures);
    }

    @Override
    public String hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues) {
        return RedisHelper.get(hmsetAsync(keyFieldValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> hmsetAsync(byte[] key, Map<byte[], byte[]> fieldValues) {
        if (Maps.isEmpty(fieldValues)) {
            return CompletableFuture.completedFuture(OK);
        }

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
    public String hmset(byte[] key, Map<byte[], byte[]> fieldValues) {
        return RedisHelper.get(hmsetAsync(key, fieldValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> hpsetAsync(byte[] key, long milliseconds, byte[] field, byte[] value) {
        if (this.redisCompatible) {
            return this.hpsetCompatible(key, milliseconds, field, value).toCompletableFuture();
        }

        byte[][] keys = {key};
        byte[][] args = new byte[][]{CODEC.encode(Long.toString(milliseconds)), field, value};
        return this.evalshaAsync(RedisExpireScript.HSET_HPEXPIRE, keys, args);
    }

    /**
     * 兼容模式，不使用脚本：先执行 hset，再执行 hpexpire
     */
    private CompletionStage<Long> hpsetCompatible(byte[] key, long milliseconds, byte[] field, byte[] value) {
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        return async.hset(key, field, value)
                .thenCompose(b -> async.hpexpire(key, milliseconds, field)
                        .thenApply(states -> {
                            if (CollectionUtils.isEmpty(states)) {
                                return null;
                            }
                            return states.getFirst();
                        }));
    }

    @Override
    public Long hpset(byte[] key, long milliseconds, byte[] field, byte[] value) {
        return RedisHelper.get(hpsetAsync(key, milliseconds, field, value), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<Long>> hmpsetAsync(Map<byte[], List<KeyValue<byte[], byte[]>>> keysFieldsValues,
                                                     long milliseconds) {
        if (Maps.isEmpty(keysFieldsValues)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        int size = 0;
        List<CompletionStage<List<Long>>> futures = new ArrayList<>(keysFieldsValues.size());
        for (Map.Entry<byte[], List<KeyValue<byte[], byte[]>>> entry : keysFieldsValues.entrySet()) {
            byte[] key = entry.getKey();
            List<KeyValue<byte[], byte[]>> fieldsValues = entry.getValue();
            if (CollectionUtils.isNotEmpty(fieldsValues)) {
                size += fieldsValues.size();
                futures.add(this.hmpsetAsync(key, milliseconds, fieldsValues));
            }
        }
        return combineListLongFutures(CompletableFuture.completedFuture(new ArrayList<>(size)), futures);
    }

    @Override
    public List<Long> hmpset(Map<byte[], List<KeyValue<byte[], byte[]>>> keysFieldsValues, long milliseconds) {
        return RedisHelper.get(hmpsetAsync(keysFieldsValues, milliseconds), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<Long>> hmpsetAsync(byte[] key, long milliseconds,
                                                     List<KeyValue<byte[], byte[]>> fieldsValues) {
        if (CollectionUtils.isEmpty(fieldsValues)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (this.redisCompatible) {
            List<CompletionStage<Long>> futures = new ArrayList<>(fieldsValues.size());
            for (KeyValue<byte[], byte[]> keyValue : fieldsValues) {
                byte[] field = keyValue.getKey();
                byte[] value = keyValue.getValue();
                futures.add(this.hpsetCompatible(key, milliseconds, field, value));
            }
            List<Long> results = new ArrayList<>(fieldsValues.size());
            return combineListLongFuture(CompletableFuture.completedFuture(results), futures);
        }

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
            return this.evalshaAsync(script, keys, args);
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
                futures.add(this.evalshaAsync(script, keys, args));
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
    public List<Long> hmpset(byte[] key, long milliseconds, List<KeyValue<byte[], byte[]>> fieldsValues) {
        return RedisHelper.get(hmpsetAsync(key, milliseconds, fieldsValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<Long>> hmpsetAsync(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues) {
        if (Maps.isEmpty(expiryKeysFieldsValues)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        int size = 0;
        List<CompletionStage<List<Long>>> futures = new ArrayList<>(expiryKeysFieldsValues.size());
        for (Map.Entry<byte[], List<ExpiryKeyValue<byte[], byte[]>>> entry : expiryKeysFieldsValues.entrySet()) {
            byte[] key = entry.getKey();
            List<ExpiryKeyValue<byte[], byte[]>> fieldsValues = entry.getValue();
            if (CollectionUtils.isNotEmpty(fieldsValues)) {
                size += fieldsValues.size();
                futures.add(this.hmpsetAsync(key, fieldsValues));
            }
        }
        return combineListLongFutures(CompletableFuture.completedFuture(new ArrayList<>(size)), futures);
    }

    @Override
    public List<Long> hmpset(Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> expiryKeysFieldsValues) {
        return RedisHelper.get(hmpsetAsync(expiryKeysFieldsValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<Long>> hmpsetAsync(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues) {
        if (CollectionUtils.isEmpty(expiryFieldsValues)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        if (this.redisCompatible) {
            List<CompletionStage<Long>> futures = new ArrayList<>(expiryFieldsValues.size());
            for (ExpiryKeyValue<byte[], byte[]> expiryKeyValue : expiryFieldsValues) {
                byte[] field = expiryKeyValue.getKey();
                byte[] value = expiryKeyValue.getValue();
                long ttl = expiryKeyValue.getTtl();
                futures.add(this.hpsetCompatible(key, ttl, field, value));
            }
            List<Long> results = new ArrayList<>(expiryFieldsValues.size());
            return combineListLongFuture(CompletableFuture.completedFuture(results), futures);
        }

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
            return this.evalshaAsync(script, keys, args);
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
                futures.add(this.evalshaAsync(script, keys, args));
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
    public List<Long> hmpset(byte[] key, List<ExpiryKeyValue<byte[], byte[]>> expiryFieldsValues) {
        return RedisHelper.get(hmpsetAsync(key, expiryFieldsValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<byte[]> hgetAsync(byte[] key, byte[] field) {
        return this.redisOperator.async().hget(key, field).toCompletableFuture();
    }

    @Override
    public byte[] hget(byte[] key, byte[] field) {
        return RedisHelper.get(hgetAsync(key, field), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmgetAsync(Map<byte[], List<byte[]>> keyFields) {
        if (Maps.isEmpty(keyFields)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        int totalSize = 0;
        List<CompletionStage<List<KeyValue<byte[], byte[]>>>> futures = new ArrayList<>(keyFields.size());
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            byte[] key = entry.getKey();
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                totalSize += fields.size();
                futures.add(this.hmgetAsync(key, fields.toArray(new byte[fields.size()][])));
            }
        }
        return combineKeyValues(CompletableFuture.completedFuture(new ArrayList<>(totalSize)), futures);
    }

    @Override
    public List<KeyValue<byte[], byte[]>> hmget(Map<byte[], List<byte[]>> keyFields) {
        return RedisHelper.get(hmgetAsync(keyFields), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<KeyValue<byte[], byte[]>>> hmgetAsync(byte[] key, byte[]... fields) {
        if (ArrayUtils.isEmpty(fields)) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
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
    public List<KeyValue<byte[], byte[]>> hmget(byte[] key, byte[]... fields) {
        return RedisHelper.get(hmgetAsync(key, fields), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> hdelAsync(Map<byte[], List<byte[]>> keyFields) {
        if (Maps.isEmpty(keyFields)) {
            return CompletableFuture.completedFuture(0L);
        }
        List<CompletionStage<Long>> futures = new ArrayList<>(keyFields.size());
        for (Map.Entry<byte[], List<byte[]>> entry : keyFields.entrySet()) {
            List<byte[]> fields = entry.getValue();
            if (CollectionUtils.isNotEmpty(fields)) {
                futures.add(this.hdelAsync(entry.getKey(), fields.toArray(new byte[fields.size()][])));
            }
        }
        return combineLongFutures(CompletableFuture.completedFuture(0L), futures);
    }

    @Override
    public Long hdel(Map<byte[], List<byte[]>> keyFields) {
        return RedisHelper.get(hdelAsync(keyFields), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> hdelAsync(byte[] key, byte[]... fields) {
        if (ArrayUtils.isEmpty(fields)) {
            return CompletableFuture.completedFuture(0L);
        }
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
    public Long hdel(byte[] key, byte[]... fields) {
        return RedisHelper.get(hdelAsync(key, fields), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> zaddAsync(byte[] key, double score, byte[] member) {
        return this.redisOperator.async().zadd(key, score, member).toCompletableFuture();
    }

    @Override
    public Long zadd(byte[] key, double score, byte[] member) {
        return RedisHelper.get(zaddAsync(key, score, member), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<Long> zaddAsync(byte[] key, ScoredValue<byte[]>... scoredValues) {
        if (ArrayUtils.isEmpty(scoredValues)) {
            return CompletableFuture.completedFuture(0L);
        }
        RedisAsyncOperator<byte[], byte[]> async = this.redisOperator.async();
        int size = scoredValues.length;
        if (size <= batchSize) {
            return async.zadd(key, LettuceConvertor.toScoredValues(scoredValues))
                    .toCompletableFuture();
        }
        // 当数据量超过阈值，分批查询
        CompletableFuture<Long> future = CompletableFuture.completedFuture(0L);
        return combineLongFutures(future, this.splitApply(scoredValues, subValues -> async.zadd(key, LettuceConvertor.toScoredValues(subValues))));
    }

    @SafeVarargs
    @Override
    public final Long zadd(byte[] key, ScoredValue<byte[]>... scoredValues) {
        return RedisHelper.get(zaddAsync(key, scoredValues), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> zcardAsync(byte[] key) {
        return this.redisOperator.async().zcard(key).toCompletableFuture();
    }

    @Override
    public Long zcard(byte[] key) {
        return RedisHelper.get(zcardAsync(key), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<byte[]>> zrangebylexAsync(byte[] key, Range<byte[]> range) {
        return CompletableFuture.completedFuture(range)
                .thenApply(LettuceConvertor::toRange)
                .thenCompose(range1 -> this.redisOperator.async().zrangebylex(key, range1));
    }

    @Override
    public List<byte[]> zrangebylex(byte[] key, Range<byte[]> range) {
        return RedisHelper.get(zrangebylexAsync(key, range), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<byte[]>> zrangebylexAsync(byte[] key, Range<byte[]> range, Limit limit) {
        return CompletableFuture.completedFuture(range)
                .thenApply(LettuceConvertor::toRange)
                .thenCompose(range1 -> {
                    io.lettuce.core.Limit limit1 = LettuceConvertor.toLimit(limit);
                    return this.redisOperator.async().zrangebylex(key, range1, limit1);
                });
    }

    @Override
    public List<byte[]> zrangebylex(byte[] key, Range<byte[]> range, Limit limit) {
        return RedisHelper.get(zrangebylexAsync(key, range, limit), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<byte[]>> zrangebyscoreAsync(byte[] key, Range<? extends Number> range) {
        return CompletableFuture.completedFuture(range)
                .thenApply(LettuceConvertor::toRange)
                .thenCompose(range1 -> this.redisOperator.async().zrangebyscore(key, range1));
    }

    @Override
    public List<byte[]> zrangebyscore(byte[] key, Range<? extends Number> range) {
        return RedisHelper.get(zrangebyscoreAsync(key, range), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<List<byte[]>> zrangebyscoreAsync(byte[] key, Range<? extends Number> range, Limit limit) {
        return CompletableFuture.completedFuture(range)
                .thenApply(LettuceConvertor::toRange)
                .thenCompose(range1 -> {
                    io.lettuce.core.Limit limit1 = LettuceConvertor.toLimit(limit);
                    return this.redisOperator.async().zrangebyscore(key, range1, limit1);
                });
    }

    @Override
    public List<byte[]> zrangebyscore(byte[] key, Range<? extends Number> range, Limit limit) {
        return RedisHelper.get(zrangebyscoreAsync(key, range, limit), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<Long> zremAsync(byte[] key, byte[]... members) {
        return this.redisOperator.async().zrem(key, members).toCompletableFuture();
    }

    @Override
    public Long zrem(byte[] key, byte[]... members) {
        return RedisHelper.get(zremAsync(key, members), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public <T> CompletableFuture<T> evalAsync(RedisScript script, byte[][] keys, byte[]... args) {
        return CompletableFuture.completedFuture(script)
                .thenApply(sc -> getScriptOutputType(sc.getResultType()))
                .thenCompose(outputType -> {
                    if (ArrayUtils.isEmpty(args)) {
                        return this.redisOperator.async().eval(script.getScriptBytes(), outputType, keys);
                    } else {
                        return this.redisOperator.async().eval(script.getScriptBytes(), outputType, keys, args);
                    }
                });
    }

    @Override
    public <T> T eval(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalAsync(script, keys, args), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public <T> CompletableFuture<T> evalReadOnlyAsync(RedisScript script, byte[][] keys, byte[]... args) {
        return CompletableFuture.completedFuture(script)
                .thenApply(sc -> getScriptOutputType(sc.getResultType()))
                .thenCompose(outputType -> {
                    if (ArrayUtils.isEmpty(args)) {
                        return this.redisOperator.async().evalReadOnly(script.getScriptBytes(), outputType, keys);
                    } else {
                        return this.redisOperator.async().evalReadOnly(script.getScriptBytes(), outputType, keys, args);
                    }
                });
    }

    @Override
    public <T> T evalReadOnly(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalReadOnlyAsync(script, keys, args), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public <T> CompletableFuture<T> evalshaAsync(RedisScript script, byte[][] keys, byte[]... args) {
        CompletableFuture<T> future = CompletableFuture.completedFuture(script)
                .thenApply(sc -> getScriptOutputType(sc.getResultType()))
                .thenCompose(outputType -> {
                    if (ArrayUtils.isEmpty(args)) {
                        return this.redisOperator.async().evalsha(script.getSha1(), outputType, keys);
                    } else {
                        return this.redisOperator.async().evalsha(script.getSha1(), outputType, keys, args);
                    }
                });
        return future.exceptionallyCompose(e -> {
            if (e instanceof RedisNoScriptException || e.getCause() instanceof RedisNoScriptException) {
                return this.scriptLoadAsync(script)
                        .thenCompose(ignored -> this.evalAsync(script, keys, args));
            }
            return CompletableFuture.failedFuture(e);
        });
    }

    @Override
    public <T> T evalsha(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalshaAsync(script, keys, args), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public <T> CompletableFuture<T> evalshaReadOnlyAsync(RedisScript script, byte[][] keys, byte[]... args) {
        CompletableFuture<T> future = CompletableFuture.completedFuture(script)
                .thenApply(sc -> getScriptOutputType(sc.getResultType()))
                .thenCompose(outputType -> {
                    if (ArrayUtils.isEmpty(args)) {
                        return this.redisOperator.async().evalshaReadOnly(script.getSha1(), outputType, keys);
                    } else {
                        return this.redisOperator.async().evalshaReadOnly(script.getSha1(), outputType, keys, args);
                    }
                });
        return future.exceptionallyCompose(e -> {
            if (e instanceof RedisNoScriptException || e.getCause() instanceof RedisNoScriptException) {
                return this.scriptLoadAsync(script)
                        .thenCompose(ignored -> this.evalReadOnlyAsync(script, keys, args));
            }
            return CompletableFuture.failedFuture(e);
        });
    }

    @Override
    public <T> T evalshaReadOnly(RedisScript script, byte[][] keys, byte[]... args) {
        return RedisHelper.get(evalshaReadOnlyAsync(script, keys, args), timeout, TimeUnit.MILLISECONDS, true, true);
    }

    @Override
    public CompletableFuture<String> scriptLoadAsync(RedisScript script) {
        return CompletableFuture.completedFuture(script)
                .thenCompose(sc -> this.redisOperator.async()
                        .scriptLoad(sc.getScript())
                        .thenApply(sha1 -> {
                            if (sha1 != null) {
                                sc.setSha1(sha1);
                            } else {
                                throw new RedisOperationException("Failed to load script: " + sc);
                            }
                            return sha1;
                        }));
    }

    @Override
    public String scriptLoad(RedisScript script) {
        return RedisHelper.get(scriptLoadAsync(script), timeout, TimeUnit.MILLISECONDS, true, true);
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
    @SuppressWarnings("unchecked")
    private <R> List<CompletionStage<R>> splitApply(ScoredValue<byte[]>[] data,
                                                    Function<ScoredValue<byte[]>[], CompletionStage<R>> mapper) {
        int size = data.length;
        List<CompletionStage<R>> futures = new ArrayList<>(size / batchSize + 1);
        for (int i = 0; i < size; ) {
            ScoredValue<byte[]>[] partition;
            int remain = size - i;
            if (remain >= batchSize) {
                partition = new ScoredValue[batchSize];
                System.arraycopy(data, i, partition, 0, batchSize);
                i += batchSize;
            } else {
                partition = new ScoredValue[remain];
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

    private static CompletableFuture<List<Long>> combineListLongFuture(CompletableFuture<List<Long>> future,
                                                                       List<CompletionStage<Long>> stages) {
        for (CompletionStage<Long> stage : stages) {
            future = future.thenCombine(stage, (results, state) -> {
                results.add(state);
                return results;
            });
        }
        return future;
    }

    private static CompletableFuture<List<Long>> combineListLongFutures(CompletableFuture<List<Long>> future,
                                                                        List<CompletionStage<List<Long>>> stages) {
        for (CompletionStage<List<Long>> stage : stages) {
            future = future.thenCombine(stage, (results, states) -> {
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

    @Override
    public CompletableFuture<Void> closeAsync() {
        return redisOperator.closeAsync();
    }

}
