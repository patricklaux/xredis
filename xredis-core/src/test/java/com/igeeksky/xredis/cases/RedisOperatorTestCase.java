package com.igeeksky.xredis.cases;

import com.igeeksky.xredis.LettuceOperatorProxy;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.RedisOperatorProxy;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Redis 操作类公共测试用例
 *
 * @author patrick
 * @since 1.0.0 2024/5/31
 */
public class RedisOperatorTestCase {

    private final StringCodec codec = StringCodec.getInstance(StandardCharsets.UTF_8);

    private final RedisOperatorProxy operatorProxy;
    private final RedisOperator<byte[], byte[]> redisOperator;

    /**
     * 构造方法
     *
     * @param redisOperator RedisOperator
     */
    public RedisOperatorTestCase(RedisOperator<byte[], byte[]> redisOperator) {
        this.redisOperator = redisOperator;
        this.operatorProxy = new LettuceOperatorProxy(10000, redisOperator);
    }

    public boolean isCluster() {
        return redisOperator.isCluster();
    }

    /**
     * 测试非性能测试的所有方法
     */
    public void testAll() {
        get();
        mget();
        set();
        mset();
        del();
        hget();
        hmget();
        keys();
    }

    void get() {
        byte[] key = codec.encode("test-get");
        byte[] val = codec.encode("test-get-val");

        redisOperator.sync().del(key);
        redisOperator.sync().set(key, val);

        byte[] result = redisOperator.sync().get(key);

        Assertions.assertArrayEquals(val, result);

        redisOperator.sync().del(key);
    }

    void mget() {
        int size = 1024, limit = 768;

        String[] keys = LettuceTestHelper.createKeys(size, "test-mget:");
        byte[][] keysArray = LettuceTestHelper.toKeysArray(size, keys);

        // 删除 redis 中的 key-value
        redisOperator.sync().del(keysArray);

        // 保存 key-value 到 redis
        redisOperator.sync().mset(LettuceTestHelper.createKeyValues(limit, keysArray));

        // 读取 redis 数据
        Map<String, String> map = LettuceTestHelper.from(redisOperator.sync().mget(keysArray));

        // 验证读取数据是否正确
        LettuceTestHelper.validateValues(keys, map, limit);

        redisOperator.sync().del(keysArray);
    }

    void set() {
        byte[] bytes = codec.encode("test-set");

        redisOperator.sync().del(bytes);
        redisOperator.sync().set(bytes, bytes);

        byte[] result = redisOperator.sync().get(bytes);
        Assertions.assertArrayEquals(bytes, result);

        redisOperator.sync().del(bytes);
    }

    void mset() {
        int size = 3;
        String[] keys = LettuceTestHelper.createKeys(size, "test-mset:");
        byte[][] keyBytes = LettuceTestHelper.toKeysArray(size, keys);

        // 删除 redis 中的 key-value
        redisOperator.sync().del(keyBytes);

        // 保存 key-value 到 redis
        redisOperator.sync().mset(LettuceTestHelper.createKeyValues(size, keyBytes));

        // 读取 redis 数据
        Map<String, String> map = LettuceTestHelper.from(redisOperator.sync().mget(keyBytes));

        // 验证读取数据是否正确
        for (String key : keys) {
            Assertions.assertEquals(key, map.get(key));
        }

        redisOperator.sync().del(keyBytes);
    }

    void del() {
        del(500);
        del(501);
        del(502);
        del(999);
        del(1000);
        del(1001);
    }

    private void del(int size) {
        String prefix = "test-del-";
        String[] keys = LettuceTestHelper.createKeys(size, prefix);
        byte[][] keysBytes = LettuceTestHelper.toKeysArray(size, keys);

        for (byte[] key : keysBytes) {
            redisOperator.sync().set(key, key);
        }

        for (byte[] key : keysBytes) {
            Assertions.assertArrayEquals(key, redisOperator.sync().get(key));
        }

        redisOperator.sync().del(keysBytes);

        for (byte[] key : keysBytes) {
            Assertions.assertNull(redisOperator.sync().get(key));
        }
    }

    void hget() {
        byte[] key = codec.encode("test-hget");
        byte[] field = codec.encode("hget-field-1");

        redisOperator.sync().del(key);

        Assertions.assertNull(redisOperator.sync().hget(key, field));

        redisOperator.sync().hset(key, field, field);

        Assertions.assertArrayEquals(field, redisOperator.sync().hget(key, field));

        redisOperator.sync().del(key);
    }

    void hmget() {
        byte[] key = codec.encode("test-hmget");

        int size = 8, limit = 5;
        String[] fields = LettuceTestHelper.createKeys(size, "test-hmget-");
        byte[][] fieldBytes = LettuceTestHelper.toKeysArray(size, fields);

        // 删除 redis 中的 key-value
        redisOperator.sync().del(key);

        Map<byte[], byte[]> keyValues = LettuceTestHelper.createKeyValues(limit, fieldBytes);

        // 保存 key-value 到 redis
        redisOperator.sync().hmset(key, keyValues);

        // 读取 redis 数据
        Map<String, String> map = LettuceTestHelper.from(redisOperator.sync().hmget(key, fieldBytes));

        // 验证读取数据是否正确
        LettuceTestHelper.validateValues(fields, map, limit);

        redisOperator.sync().hdel(key, fieldBytes);

        Map<String, String> resultMap = LettuceTestHelper.from(redisOperator.sync().hmget(key, fieldBytes));
        for (int i = 0; i < size; i++) {
            Assertions.assertNull(resultMap.get(fields[i]));
        }
    }

    void keys() {
        int size = 10;
        String prefix = "test-clear:";
        byte[] matches = codec.encode(prefix + "*");

        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            byte[] keyBytes = codec.encode(prefix + i);
            keyValues.put(keyBytes, keyBytes);
        }

        redisOperator.sync().mset(keyValues);

        Map<String, String> result = getKeysResult(matches);
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Assertions.assertEquals(key, result.get(key));
        }

        operatorProxy.clear(matches);

        result = getKeysResult(matches);
        for (int i = 0; i < 10; i++) {
            String key = prefix + i;
            Assertions.assertNull(result.get(key));
        }
    }

    /**
     * 性能测试（200万数据，单线程，单链接，单个同步写）
     */
    public void psetexPerformance1() {
        int size = 2000000;
        String prefix = "test-psetex:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        this.createPsetexRunnable(size, prefix).run();

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, size);
    }

    /**
     * 性能测试（200万数据，10线程，单链接，单个同步写）
     */
    public void psetexPerformance2() {
        String prefix = "test-psetex:";
        operatorProxy.clear(codec.encode(prefix + "*"));

        Runnable runnable = this.createPsetexRunnable(200000, prefix);

        for (int i = 0; i < 10; i++) {
            new Thread(runnable).start();
        }

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        long clear = operatorProxy.clear(codec.encode(prefix + "*"));
        System.out.printf("clear: [%d] \n", clear);
        Assertions.assertEquals(clear, 2000000);
    }

    private Map<String, String> getKeysResult(byte[] matches) {
        List<byte[]> keys = redisOperator.sync().keys(matches);
        Map<String, String> result = Maps.newHashMap(keys.size());
        for (byte[] keyBytes : keys) {
            String key = codec.decode(keyBytes);
            result.put(key, key);
        }
        return result;
    }

    private Runnable createPsetexRunnable(int size, String prefix) {
        return () -> {
            long start = System.currentTimeMillis();
            for (int i = 0; i < size; i++) {
                byte[] temp = codec.encode(prefix + RandomUtils.nextString(18));
                redisOperator.sync().psetex(temp, RandomUtils.nextInt(2000000, 3000000), temp);
            }
            long end = System.currentTimeMillis();
            System.out.println("psetex-time:" + (end - start));
        };
    }

}