package com.igeeksky.xredis.cases;

import com.igeeksky.xredis.ExpiryKeyFieldValue;
import com.igeeksky.xredis.RedisOperatorProxy;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import io.lettuce.core.KeyValue;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisOperatorProxyTestCase {

    private final StringCodec codec = StringCodec.getInstance(StandardCharsets.UTF_8);

    private final RedisOperatorProxy operatorProxy;
    private final RedisOperator<byte[], byte[]> redisOperator;

    public RedisOperatorProxyTestCase(RedisOperator<byte[], byte[]> redisOperator) {
        this.redisOperator = redisOperator;
        this.operatorProxy = new RedisOperatorProxy(10000, redisOperator);
    }

    public void testAll() {
        hset();
        hmset();
        hmset2();
        hget();
        hmget();
        hmget2();
        hdel();
        hdel2();
        set();
        mset();
        psetex();
        psetex2();
        get();
        mget();
        del();
        keys();
        keys2();
        clear();
        version();
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

    public void hmget() {
        int size = 100;
        byte[] key = codec.encode("test-hmget");
        String[] fields = LettuceTestHelper.createKeys(size, "hmget-field:");
        byte[][] fieldsArray = LettuceTestHelper.toKeysArray(size, fields);

        operatorProxy.del(key).join();
        operatorProxy.hmset(key, LettuceTestHelper.createKeyValues(size, fieldsArray));

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(key, fieldsArray).join();
        Assertions.assertEquals(size, keyValues.size());

        Map<String, String> map = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields, map, size, size);

        operatorProxy.del(key).join();
    }

    public void hmget2() {
        this.hmset_hmget_hdel_2(64, 100, "test-hmget:");
    }

    public void hmset() {

    }

    void hmset2() {
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

        byte[][] keys = keyFieldValues.keySet().toArray(new byte[size][]);

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

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(keyFields, total).join();
        Assertions.assertEquals(total, keyValues.size());
        long time2 = System.currentTimeMillis();
        System.out.println("hmget cost: " + (time2 - time1));

        Assertions.assertEquals(total, operatorProxy.hdel(keyFields).join());
        long end = System.currentTimeMillis();
        System.out.println("hdel cost: " + (end - time2));

        Map<String, String> result = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields.toArray(new String[0]), result, total, total);
        operatorProxy.del(keys).join();
    }


    public void hpset() {
        int size = 100;
        String[] keys = LettuceTestHelper.createKeys(size, "test-hpset:");
        byte[][] keysArray = LettuceTestHelper.toKeysArray(size, keys);

        operatorProxy.del(keysArray).join();

        for (int i = 0; i < size; i++) {
            byte[] key = codec.encode(keys[i]);
            operatorProxy.hpset(key, 100000, key, key).join();
        }

        for (int i = 0; i < size; i++) {
            byte[] key = keysArray[i];
            operatorProxy.hpset(key, 100000, key, key).join();
        }

        for (int i = 0; i < size; i++) {
            byte[] key = keysArray[i];
            List<Long> hpttl = redisOperator.sync().hpttl(key, key);
            Assertions.assertTrue(hpttl.getFirst() > 50000);
        }

    }

    public void hmpset() {
        this.hmpset_hmget_hdel(64, 100, "test-hmpset:");
    }

    public void hmpset_hmget_hdel(int size, int length, String prefix) {
        int total = size * length;
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = LettuceTestHelper.createKeyFieldValues(size, length, prefix);

        byte[][] keys = keyFieldValues.keySet().toArray(new byte[size][]);

        List<String> fields = new ArrayList<>(total);
        List<ExpiryKeyFieldValue> expiryKeyFieldValues = new ArrayList<>(total);
        Map<byte[], List<byte[]>> keyFields = new HashMap<>();
        keyFieldValues.forEach((k, map) -> {
            keyFields.put(k, new ArrayList<>(map.keySet()));
            map.forEach((field, value) -> {
                fields.add(codec.decode(field));
                expiryKeyFieldValues.add(ExpiryKeyFieldValue.create(k, field, value, 100000));
            });
        });

        operatorProxy.del(keys).join();

        long start = System.currentTimeMillis();
        operatorProxy.hmpset(expiryKeyFieldValues).join();
        long time1 = System.currentTimeMillis();
        System.out.println("hmpset cost: " + (time1 - start));

        List<KeyValue<byte[], byte[]>> keyValues = operatorProxy.hmget(keyFields, total).join();
        Assertions.assertEquals(total, keyValues.size());
        long time2 = System.currentTimeMillis();
        System.out.println("hmget cost: " + (time2 - time1));

        Assertions.assertEquals(total, operatorProxy.hdel(keyFields).join());
        long end = System.currentTimeMillis();
        System.out.println("hdel cost: " + (end - time2));

        Map<String, String> result = LettuceTestHelper.fromKeyValues(keyValues);
        LettuceTestHelper.validateValues(fields.toArray(new String[0]), result, total, total);
        operatorProxy.del(keys).join();
    }

    void del() {
    }

    void keys() {
    }

    public void keys2() {
        int size = 10;
        String prefix = "test-keys:";
        String[] keys = LettuceTestHelper.createKeys(size, prefix);
        byte[][] keysArray = LettuceTestHelper.toKeysArray(size, keys);

        this.clear(prefix);

        for (byte[] key : keysArray) {
            operatorProxy.set(key, key).join();
        }

        operatorProxy.keys(k -> {
                            String key = codec.decode(k);
                            System.out.println(key);
                            Assertions.assertTrue(key.startsWith(prefix));
                        },
                        codec.encode(prefix + "*"))
                .thenAccept(count -> Assertions.assertEquals(size, count))
                .thenCompose(v -> operatorProxy.del(keysArray))
                .join();

    }

    void get() {
    }

    void mget() {
    }

    void mset() {
    }

    void set() {
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

    void psetex2() {
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

    private void psetex(int size) {
        String prefix = "test-psetex:";
        String[] keys = LettuceTestHelper.createKeys(size, prefix);
        byte[][] keyBytes = LettuceTestHelper.toKeysArray(size, keys);

        // 删除 redis 中的 key-value
        operatorProxy.del(keyBytes).join();

        List<ExpiryKeyValue<byte[], byte[]>> keyValues = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            keyValues.add(new ExpiryKeyValue<>(keyBytes[i], keyBytes[i], RandomUtils.nextInt(1000000, 2000000)));
        }

        // 保存 key-value 到 redis
        operatorProxy.psetex(keyValues).join();

        // 读取 redis 数据
        Map<String, String> map = LettuceTestHelper.fromKeyValues(operatorProxy.mget(keyBytes).join());

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        List<byte[]> matchKeys = operatorProxy.keys(codec.encode(prefix + "*")).join();
        Assertions.assertEquals(keys.length, matchKeys.size());

        operatorProxy.del(keyBytes).join();

        matchKeys = operatorProxy.keys(codec.encode(prefix + "*")).join();
        Assertions.assertEquals(0, matchKeys.size());
    }

    void clear() {
        String prefix = "pipeline-psetex:";
        createPsetexPipelineRunnable(10, prefix).run();

        long clear1 = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear1: [%d] \n", clear1);
        Assertions.assertTrue(clear1 > 0);

        long clear2 = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear1: [%d] \n", clear2);
        Assertions.assertEquals(0, clear2);
    }

    void clear(String prefix) {
        long start = System.currentTimeMillis();
        System.out.printf("clear: [%d] \n", operatorProxy.clear(codec.encode(prefix + "*")));
        System.out.println("clear cost: " + (System.currentTimeMillis() - start));
    }

    /**
     * 获取版本信息
     */
    void version() {
        String version = operatorProxy.version();
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
        String prefix = "pipeline-psetex:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        createPsetexPipelineRunnable(size, prefix).run();

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, size);
    }

    /**
     * 性能测试（1000万数据，2线程，单链接，pipeline 批量写入数据）
     */
    public void psetexPipelinePerformance2() {
        int size = 10000000;
        String prefix = "pipeline-psetex:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        Runnable runnable = createPsetexPipelineRunnable(size / 2, prefix);

        for (int i = 0; i < 2; i++) {
            new Thread(runnable).start();
        }

        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, size);
    }

    /**
     * pipeline 批量写入数据
     *
     * @param size 数据量
     * @return {@link Runnable} 线程任务
     */
    private Runnable createPsetexPipelineRunnable(int size, String prefix) {
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
                    capacity = Math.min(10000, size - i - 1);
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

}
