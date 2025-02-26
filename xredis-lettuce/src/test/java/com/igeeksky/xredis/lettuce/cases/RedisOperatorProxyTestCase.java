package com.igeeksky.xredis.lettuce.cases;

import com.igeeksky.xredis.common.Limit;
import com.igeeksky.xredis.common.Range;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xredis.common.ScoredValue;
import com.igeeksky.xredis.lettuce.LettuceOperatorProxy;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * RedisOperatorProxy 测试用例
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisOperatorProxyTestCase {

    private final StringCodec codec = StringCodec.getInstance(StandardCharsets.UTF_8);

    private final LettuceOperatorProxy operatorProxy;
    private final RedisOperator<byte[], byte[]> redisOperator;

    /**
     * 构造方法
     *
     * @param redisOperator RedisOperator
     */
    public RedisOperatorProxyTestCase(RedisOperator<byte[], byte[]> redisOperator) {
        this.redisOperator = redisOperator;
        this.operatorProxy = new LettuceOperatorProxy(10000, redisOperator);
    }

    /**
     * 测试非性能测试的所有方法
     */
    public void testAll() {
        set();
        get();
        mset();
        mget();
        psetex();
        psetex_random();

        hset();
        hmset();
        hmset2();
        hget();
        hmget();
        hmget2();
        hdel();
        hdel2();

        version();
    }

    public boolean isCluster() {
        return operatorProxy.isCluster();
    }

    public void clear(String prefix, int expectedSize) {
        long start = System.currentTimeMillis();
        long size = operatorProxy.clear(codec.encode(prefix + "*"));
        long cost = System.currentTimeMillis() - start;
        System.out.printf("clear-size: [%d] \n", size);
        System.out.printf("clear-cost: [%d] \n", cost);
        if (expectedSize >= 0) {
            Assertions.assertEquals(expectedSize, size);
        }
    }

    public void set() {
        set_get_del("test-set:");
    }

    public void get() {
        set_get_del("test-get:");
    }

    public void set_get_del(String prefix) {
        byte[] key = codec.encode(prefix + RandomUtils.nextString(5));
        byte[] val = codec.encode(prefix + RandomUtils.nextString(5));
        operatorProxy.del(key).join();

        String ok = operatorProxy.set(key, val).join();
        Assertions.assertEquals(RedisOperatorProxy.OK, ok);

        Assertions.assertArrayEquals(val, operatorProxy.get(key).join());

        operatorProxy.del(key).join();
    }

    public void mset() {
        mset_mget(9998, "test-mset:");
        mset_mget(9999, "test-mset:");
        mset_mget(10000, "test-mset:");
        mset_mget(10001, "test-mset:");
        mset_mget(10002, "test-mset:");
        mset_mget(19998, "test-mset:");
        mset_mget(19999, "test-mset:");
        mset_mget(20000, "test-mset:");
        mset_mget(20001, "test-mset:");
        mset_mget(20002, "test-mset:");
    }

    public void mget() {
        mset_mget(10, "test-mget:");
    }

    private void mset_mget(int size, String prefix) {
        String[] keys = LettuceTestHelper.createKeys(size, prefix);
        byte[][] keysArray = LettuceTestHelper.toKeysArray(keys.length, keys);
        Map<byte[], byte[]> keyValues = LettuceTestHelper.createKeyValues(size, keysArray);

        operatorProxy.del(keysArray).join();

        String ok = operatorProxy.mset(keyValues).join();
        Assertions.assertEquals(RedisOperatorProxy.OK, ok);

        List<KeyValue<byte[], byte[]>> results = operatorProxy.mget(keysArray).join();
        Assertions.assertEquals(size, results.size());

        Map<String, String> results1 = LettuceTestHelper.fromKeyValues(results);
        Assertions.assertEquals(size, results1.size());

        LettuceTestHelper.validateValues(keys, results1, size);

        operatorProxy.del(keysArray).join();
    }

    public void psetex() {
        byte[] key = codec.encode("test-psetex");
        byte[] value = codec.encode("test-psetex-value");
        operatorProxy.del(key).join();
        String ok = operatorProxy.psetex(key, RandomUtils.nextInt(1000000, 2000000), value).join();
        Assertions.assertEquals("OK", ok);

        Assertions.assertArrayEquals(value, operatorProxy.get(key).join());
        Long pttl = redisOperator.async().pttl(key).toCompletableFuture().join();
        Assertions.assertTrue(pttl > 900000);

        operatorProxy.del(key).join();
    }

    public void psetex_random() {
        psetex_random(9998);
        psetex_random(9999);
        psetex_random(10000);
        psetex_random(10001);
        psetex_random(10002);
        psetex_random(19998);
        psetex_random(19999);
        psetex_random(20000);
        psetex_random(20001);
        psetex_random(20002);
    }

    public void psetex_random(int size) {
        String prefix = "test-psetex-random:";
        String[] keys = LettuceTestHelper.createKeys(size, prefix);
        byte[][] keyBytes = LettuceTestHelper.toKeysArray(size, keys);

        // 删除 redis 中的已有数据
        // operatorProxy.del(keyBytes).join();

        List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keyValues.add(new ExpiryKeyValue<>(keyBytes[i], keyBytes[i], RandomUtils.nextInt(1000000, 2000000)));
        }

        // 保存 key-value 到 redis
        long start = System.currentTimeMillis();
        operatorProxy.psetex(keyValues).join();
        System.out.println("size: [" + size + "]\t psetex-random-cost: [" + (System.currentTimeMillis() - start) + "]");

        // 读取 redis 数据
        Map<String, String> map = LettuceTestHelper.fromKeyValues(operatorProxy.mget(keyBytes).join());

        // 验证读取数据是否正确
        LettuceTestHelper.validateValues(keys, map, size);

        // 删除数据，还原测试环境
        operatorProxy.del(keyBytes).join();
    }

    public void psetex3() {
        psetex(9998);
        psetex(9999);
        psetex(10000);
        psetex(10001);
        psetex(10002);
        psetex(19998);
        psetex(19999);
        psetex(20000);
        psetex(20001);
        psetex(20002);
    }

    public void psetex(int size) {
        String prefix = "test-psetex:";
        String[] keys = LettuceTestHelper.createKeys(size, prefix);
        byte[][] keyBytes = LettuceTestHelper.toKeysArray(size, keys);

        // 删除 redis 中的已有数据
        operatorProxy.del(keyBytes).join();

        List<KeyValue<byte[], byte[]>> keyValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keyValues.add(new KeyValue<>(keyBytes[i], keyBytes[i]));
        }

        long start = System.currentTimeMillis();
        // 保存 key-value 到 redis
        operatorProxy.psetex(keyValues, 10000000).join();
        System.out.println("size: [" + size + "]\t psetex-cost: [" + (System.currentTimeMillis() - start) + "]");

        // 读取 redis 数据
        Map<String, String> map = LettuceTestHelper.fromKeyValues(operatorProxy.mget(keyBytes).join());

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        // 删除数据，还原测试环境
        operatorProxy.del(keyBytes).join();
    }

    /**
     * 获取版本信息
     */
    void version() {
        String version = operatorProxy.version().join();
        System.out.println(version);
        Assertions.assertNotNull(version);

        String[] array = version.split("\\.");
        Assertions.assertTrue(array.length >= 3);
        Assertions.assertTrue(Integer.parseInt(array[0]) >= 5);
    }

    /**
     * 性能测试（1000万数据，单线程，批量保存 ）
     */
    public void msetPerformance1() {
        int size = 10000000;
        String prefix = "test-mset:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        this.createMsetRunnable(size, prefix).run();

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, size);
    }

    /**
     * 性能测试（1000万数据，双线程，批量保存）
     */
    public void msetPerformance2() {
        int size = 10000000;
        String prefix = "test-mset:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        Runnable runnable = this.createMsetRunnable(size / 2, prefix);

        for (int i = 0; i < 2; i++) {
            new Thread(runnable).start();
        }

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, size);
    }

    /**
     * 性能测试（1000万数据，单线程，单链接，pipeline 批量写入数据）
     */
    public void psetexPipelinePerformance1() {
        int size = 10000000;
        String prefix = "test-pipeline-psetex:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        createPsetexRunnable(size, prefix).run();

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, size);
    }

    /**
     * 性能测试（1000万数据，2线程，单链接）
     */
    public void psetexPipelinePerformance2() {
        int size = 10000000;
        String prefix = "test-psetex:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        CountDownLatch latch = new CountDownLatch(2);
        Runnable runnable = this.createPsetexRunnable(size / 2, prefix);

        for (int i = 0; i < 2; i++) {
            new Thread(runnable).start();
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        this.clear(prefix, size);
    }

    /**
     * pipeline 批量写入数据
     *
     * @param size 数据量
     * @return {@link Runnable} 线程任务
     */
    private Runnable createPsetexRunnable(int size, String prefix) {
        return () -> {
            long start = System.currentTimeMillis();
            int capacity = Math.min(50000, size);
            List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(capacity);
            for (int i = 0; i < size; i++) {
                byte[] temp = codec.encode(prefix + RandomUtils.nextString(18));
                keyValues.add(new ExpiryKeyValue<>(temp, temp, RandomUtils.nextInt(2000000, 3000000)));
                if (keyValues.size() == capacity) {
                    operatorProxy.psetex(keyValues).join();
                    keyValues.clear();
                    capacity = Math.min(50000, size - i - 1);
                }
            }
            long end = System.currentTimeMillis();
            System.out.printf("size: [%d], psetex-time: [%d] \n", size, end - start);
        };
    }

    private Runnable createMsetRunnable(int size, String prefix) {
        return () -> {
            long start = System.currentTimeMillis();
            Map<byte[], byte[]> keyValues = Maps.newHashMap(50000);
            for (int i = 0; i < size; ) {
                i++;
                byte[] temp = codec.encode(prefix + RandomUtils.nextString(18));
                keyValues.put(temp, temp);
                if (keyValues.size() == 50000 || i == size) {
                    operatorProxy.mset(keyValues).join();
                    keyValues.clear();
                }
            }
            long end = System.currentTimeMillis();
            System.out.println("mset-time:" + (end - start));
        };
    }


    public void hset() {
        byte[] key = codec.encode("test-hset");
        byte[] field = codec.encode("hset-field-1");
        byte[] value = codec.encode("hset-value-1");
        operatorProxy.del(key).join();
        Assertions.assertNull(operatorProxy.hget(key, field).join());

        Assertions.assertTrue(operatorProxy.hset(key, field, value).join());
        Assertions.assertArrayEquals(value, operatorProxy.hget(key, field).join());

        operatorProxy.del(key).join();
    }

    public void hget() {
        byte[] key = codec.encode("test-hget");
        byte[] field = codec.encode("hget-field-1");
        byte[] value = codec.encode("hget-value-1");
        operatorProxy.del(key).join();
        Assertions.assertNull(operatorProxy.hget(key, field).join());
        operatorProxy.hset(key, field, value).join();
        Assertions.assertArrayEquals(value, operatorProxy.hget(key, field).join());

        operatorProxy.del(key).join();
    }

    public void hdel() {
        int size = 10;
        byte[] key = codec.encode("test-hdel");
        String[] fields = LettuceTestHelper.createKeys(size, "hdel-field:");
        byte[][] fieldsArray = LettuceTestHelper.toKeysArray(fields.length, fields);
        Map<byte[], byte[]> fieldValues = LettuceTestHelper.createKeyValues(size, fieldsArray);

        operatorProxy.del(key).join();
        Assertions.assertEquals(0, operatorProxy.hdel(key, fieldsArray).join());

        operatorProxy.hmset(key, fieldValues).join();
        Assertions.assertEquals(size, operatorProxy.hdel(key, fieldsArray).join());
    }

    public void hdel2() {
        this.hmset_hmget_hdel_2(64, 100, "test-hdel:");
    }

    public void hmset() {
        hmset_hmget_del(100, "test-hmset:");
    }

    /**
     * 批量写入数据，批量读取数据，批量删除数据
     * <p>
     * 测试： {@code CompletableFuture<String> hmset(Map<byte[], Map<byte[], byte[]>> keyFieldValues)}
     */
    public void hmget() {
        hmset_hmget_del(100, "test-hmget:");
    }

    /**
     * 批量写入数据，批量读取数据
     * <p>
     * 测试：<p>
     * {@code CompletableFuture<String> hmset(byte[] key, Map<byte[], byte[]> fieldValues)}
     * <p>
     * {@code CompletableFuture<List<KeyValue<byte[], byte[]>>> hmget(byte[] key, byte[]... fields)}
     * <p>
     * {@code  CompletableFuture<Long> del(byte[]... keys)}
     *
     * @param size   fields 数据量
     * @param prefix 前缀
     */
    public void hmset_hmget_del(int size, String prefix) {
        byte[] key = codec.encode(prefix);
        String[] fields = LettuceTestHelper.createKeys(size, prefix + "-field:");
        byte[][] fieldsArray = LettuceTestHelper.toKeysArray(size, fields);

        operatorProxy.del(key).join();
        operatorProxy.hmset(key, LettuceTestHelper.createKeyValues(size, fieldsArray));

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(key, fieldsArray).join();
        Assertions.assertEquals(size, keyValues.size());

        Map<String, String> map = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields, map, size);

        operatorProxy.del(key).join();
    }

    public void hmset2() {
        int length = 100;
        String prefix = "test-hmset:";
        this.hmset_hmget_hdel_2(98, length, prefix);
        this.hmset_hmget_hdel_2(99, length, prefix);
        this.hmset_hmget_hdel_2(100, length, prefix);
        this.hmset_hmget_hdel_2(101, length, prefix);
        this.hmset_hmget_hdel_2(102, length, prefix);
        this.hmset_hmget_hdel_2(198, length, prefix);
        this.hmset_hmget_hdel_2(199, length, prefix);
        this.hmset_hmget_hdel_2(200, length, prefix);
        this.hmset_hmget_hdel_2(201, length, prefix);
        this.hmset_hmget_hdel_2(202, length, prefix);
    }

    /**
     * 测试 hmset、hmget、hdel
     * <p>
     * 形式：{@code Map<byte[], Map<byte[], byte[]>> keyFieldValues}
     *
     * @param size   key 集合数量（外层 map 容量）
     * @param length 单个 key 字段数量（内层 map 容量）
     * @param prefix 前缀
     */
    public void hmset_hmget_hdel_2(int size, int length, String prefix) {
        int total = size * length;
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = LettuceTestHelper.createKeyFieldValues(size, length, prefix);

        byte[][] keys = keyFieldValues.keySet().toArray(new byte[0][]);

        List<String> fields = new ArrayList<>(total);
        Map<byte[], List<byte[]>> keyFields = new HashMap<>();
        keyFieldValues.forEach((k, map) -> {
            keyFields.put(k, new ArrayList<>(map.keySet()));
            map.forEach((field, value) -> fields.add(codec.decode(field)));
        });

        operatorProxy.del(keys).join();

        long start = System.currentTimeMillis();
        operatorProxy.hmset(keyFieldValues).join();
        long time1 = System.currentTimeMillis();
        System.out.println("hmset cost: " + (time1 - start));

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(keyFields).join();
        Assertions.assertEquals(total, keyValues.size());
        long time2 = System.currentTimeMillis();
        System.out.println("hmget cost: " + (time2 - time1));

        Assertions.assertEquals(total, operatorProxy.hdel(keyFields).join());
        long end = System.currentTimeMillis();
        System.out.println("hdel cost: " + (end - time2));

        Map<String, String> result = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields, result, total);
        operatorProxy.del(keys).join();
    }

    public void hmget2() {
        this.hmset_hmget_hdel_2(64, 100, "test-hmget:");
    }

    public void hpset() {
        int size = 100;
        String[] keys = LettuceTestHelper.createKeys(size, "test-hpset:");
        byte[][] keysArray = LettuceTestHelper.toKeysArray(size, keys);

        operatorProxy.del(keysArray).join();

        for (int i = 0; i < size; i++) {
            byte[] key = codec.encode(keys[i]);
            operatorProxy.hpset(key, 100000, key, key).join();
            if (i == (size - 1)) {
                System.out.println(keys[i]);
            }
        }

        for (int i = 0; i < size; i++) {
            byte[] key = keysArray[i];
            List<Long> hpttl = redisOperator.sync().hpttl(key, key);
            Long first = hpttl.getFirst();
            System.out.println(codec.decode(key) + ": " + first);
            Assertions.assertTrue(first > 50000);
        }

    }

    public void hmpset() {
        this.hmpset_hmget_hdel(1, 9998, "test-hmpset:");
        this.hmpset_hmget_hdel(2, 9999, "test-hmpset:");
        this.hmpset_hmget_hdel(3, 10000, "test-hmpset:");
        this.hmpset_hmget_hdel(4, 10001, "test-hmpset:");
        this.hmpset_hmget_hdel(5, 10002, "test-hmpset:");
        this.hmpset_hmget_hdel(1, 19998, "test-hmpset:");
        this.hmpset_hmget_hdel(2, 19999, "test-hmpset:");
        this.hmpset_hmget_hdel(3, 20000, "test-hmpset:");
        this.hmpset_hmget_hdel(4, 20001, "test-hmpset:");
        this.hmpset_hmget_hdel(5, 20002, "test-hmpset:");
    }

    public void hmpset_hmget_hdel(int size, int length, String prefix) {
        int total = size * length;
        Map<byte[], List<KeyValue<byte[], byte[]>>> keyFieldValues = LettuceTestHelper.createKeyFieldValueList(size, length, prefix);

        byte[][] keys = keyFieldValues.keySet().toArray(new byte[size][]);

        List<String> fields = new ArrayList<>(total);
        Map<byte[], List<byte[]>> keyFields = new HashMap<>();

        keyFieldValues.forEach((k, fieldsValues) -> {
            keyFields.put(k, fieldsValues.stream().map(KeyValue::getKey).toList());
            fieldsValues.forEach(kv -> fields.add(codec.decode(kv.getKey())));
        });

        operatorProxy.del(keys).join();

        long start = System.currentTimeMillis();
        List<Long> states = operatorProxy.hmpset(keyFieldValues, 1000000).join();
        Assertions.assertEquals(total, states.size());
        for (Long state : states) {
            Assertions.assertEquals(1, state);
        }
        long time1 = System.currentTimeMillis();
        System.out.println("hmpset cost: " + (time1 - start));

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(keyFields).join();
        Assertions.assertEquals(total, keyValues.size());
        long time2 = System.currentTimeMillis();
        System.out.println("hmget cost: " + (time2 - time1));

        Assertions.assertEquals(total, operatorProxy.hdel(keyFields).join());
        long end = System.currentTimeMillis();
        System.out.println("hdel cost: " + (end - time2));

        Map<String, String> result = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields, result, total);
    }

    public void hmpset_random() {
        this.hmpset_hmget_hdel_random(1, 9998, "test-hmpset:");
        this.hmpset_hmget_hdel_random(2, 9999, "test-hmpset:");
        this.hmpset_hmget_hdel_random(3, 10000, "test-hmpset:");
        this.hmpset_hmget_hdel_random(4, 10001, "test-hmpset:");
        this.hmpset_hmget_hdel_random(5, 10002, "test-hmpset:");
        this.hmpset_hmget_hdel_random(1, 19998, "test-hmpset:");
        this.hmpset_hmget_hdel_random(2, 19999, "test-hmpset:");
        this.hmpset_hmget_hdel_random(3, 20000, "test-hmpset:");
        this.hmpset_hmget_hdel_random(4, 20001, "test-hmpset:");
        this.hmpset_hmget_hdel_random(5, 20002, "test-hmpset:");
    }

    public void hmpset_hmget_hdel_random(int size, int length, String prefix) {
        int total = size * length;
        Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> keyFieldValues = LettuceTestHelper.createExpireKeyFieldValueList(size, length, prefix);

        byte[][] keys = keyFieldValues.keySet().toArray(new byte[size][]);

        List<String> fields = new ArrayList<>(total);
        Map<byte[], List<byte[]>> keyFields = new HashMap<>();

        keyFieldValues.forEach((k, fieldsValues) -> {
            keyFields.put(k, fieldsValues.stream().map(KeyValue::getKey).toList());
            fieldsValues.forEach(kv -> fields.add(codec.decode(kv.getKey())));
        });

        operatorProxy.del(keys).join();

        long start = System.currentTimeMillis();
        List<Long> states = operatorProxy.hmpset(keyFieldValues).join();
        Assertions.assertEquals(total, states.size());
        for (Long state : states) {
            Assertions.assertEquals(1, state);
        }

        long time1 = System.currentTimeMillis();
        System.out.println("hmpset cost: " + (time1 - start));

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(keyFields).join();
        Assertions.assertEquals(total, keyValues.size());
        long time2 = System.currentTimeMillis();
        System.out.println("hmget cost: " + (time2 - time1));

        Assertions.assertEquals(total, operatorProxy.hdel(keyFields).join());
        long end = System.currentTimeMillis();
        System.out.println("hdel cost: " + (end - time2));

        Map<String, String> result = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields, result, total);

        operatorProxy.del(keys).join();
    }

    public void zrange() {
        String prefix = "test-zrange";
        byte[] key = codec.encode(prefix);
        operatorProxy.del(key).join();

        int length = 20000;
        List<ScoredValue<byte[]>> scoredValues = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            byte[] member = codec.encode(prefix + ":" + i + RandomUtils.nextString(10));
            scoredValues.add(ScoredValue.just(i, member));
        }
        @SuppressWarnings("unchecked")
        ScoredValue<byte[]>[] array = scoredValues.toArray(new ScoredValue[0]);
        Long zadd = operatorProxy.zadd(key, array).join();

        Assertions.assertEquals(scoredValues.size(), zadd);
        Assertions.assertEquals(scoredValues.size(), operatorProxy.zcard(key).join());

        List<byte[]> zrange = operatorProxy.zrangebyscore(key, Range.closed(0, 5000)).join();
        Assertions.assertEquals(5001, zrange.size());

        zrange = operatorProxy.zrangebyscore(key, Range.closed(0, 5000), Limit.from(1000)).join();
        Assertions.assertEquals(1000, zrange.size());

        operatorProxy.del(key).join();
    }

    public void zadd() {
        this.zadd_zrange_zrem_zcard("test-zadd:", 9998);
        this.zadd_zrange_zrem_zcard("test-zadd:", 9999);
        this.zadd_zrange_zrem_zcard("test-zadd:", 10000);
        this.zadd_zrange_zrem_zcard("test-zadd:", 10001);
        this.zadd_zrange_zrem_zcard("test-zadd:", 10002);
        this.zadd_zrange_zrem_zcard("test-zadd:", 19998);
        this.zadd_zrange_zrem_zcard("test-zadd:", 19999);
        this.zadd_zrange_zrem_zcard("test-zadd:", 20000);
        this.zadd_zrange_zrem_zcard("test-zadd:", 20001);
        this.zadd_zrange_zrem_zcard("test-zadd:", 20002);
        this.zadd_zrange_zrem_zcard("test-zadd:", 120002);
    }

    public void zadd_zrange_zrem_zcard(String prefix, int length) {
        byte[] key = codec.encode(prefix);
        operatorProxy.del(key).join();

        List<ScoredValue<byte[]>> scoredValues = LettuceTestHelper.createScoredValues(length, prefix);
        @SuppressWarnings("unchecked")
        ScoredValue<byte[]>[] array = scoredValues.toArray(new ScoredValue[0]);
        Long zadd = operatorProxy.zadd(key, array).join();
        Assertions.assertEquals(length, zadd);

        List<byte[]> zrange = operatorProxy.zrangebyscore(key, Range.closed(0, Double.MAX_VALUE)).join();
        // List<byte[]> zrange = redisOperator.async().zrangebyscore(key, io.lettuce.core.Range.create(0, Double.MAX_VALUE))
        //         .toCompletableFuture().join();
        Assertions.assertEquals(length, zrange.size());

        operatorProxy.zrem(key, zrange.toArray(new byte[0][]));

        Assertions.assertEquals(0, operatorProxy.zcard(key).join());
    }

}
