package com.igeeksky.xredis.lettuce.cases;


import com.igeeksky.xredis.common.ScoredValue;
import com.igeeksky.xredis.lettuce.LettuceSentinelFactory;
import com.igeeksky.xredis.lettuce.LettuceStandaloneFactory;
import com.igeeksky.xredis.lettuce.LettuceClusterFactory;
import com.igeeksky.xredis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.xredis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.xredis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceConfigHelper;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import com.igeeksky.xtool.core.ExpiryKeyValue;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.lettuce.core.resource.ClientResources;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lettuce 测试辅助类
 *
 * @author patrick
 * @since 0.0.4 2024/5/30
 */
public class LettuceTestHelper {

    private static final StringCodec codec = StringCodec.getInstance(StandardCharsets.UTF_8);

    /**
     * 创建 standalone 连接
     *
     * @return LettuceStandaloneFactory
     */
    public static LettuceStandaloneFactory createStandaloneFactory() {
        LettuceStandalone standalone = new LettuceStandalone();

        standalone.setNode("192.168.50.157:9221");

        // standalone.setNode("192.168.50.157:6379");
        // standalone.setNodes(List.of("192.168.50.157:6378"));

        // standalone.setNode("127.0.0.1:6379");
        // standalone.setNodes(List.of("127.0.0.1:6378"));

        standalone.setReadFrom("upstream");

        LettuceStandaloneConfig standaloneConfig = LettuceConfigHelper.createConfig("test", standalone);
        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceStandaloneFactory(standaloneConfig, options, resources);
    }

    /**
     * 创建 sentinel 连接
     *
     * @return LettuceSentinelFactory
     */
    public static LettuceSentinelFactory createSentinelFactory() {
        LettuceSentinel sentinel = new LettuceSentinel();
        sentinel.setNodes(List.of("192.168.50.157:26377", "192.168.50.157:26378", "192.168.50.157:26379"));
        sentinel.setReadFrom("upstream");
        sentinel.setMasterId("mymaster");

        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();
        LettuceSentinelConfig config = LettuceConfigHelper.createConfig("lettuce", sentinel);

        return new LettuceSentinelFactory(config, options, resources);
    }

    /**
     * 创建 cluster 连接
     *
     * @return LettuceClusterFactory
     */
    public static LettuceClusterFactory createClusterConnectionFactory() {
        LettuceCluster cluster = new LettuceCluster();
        cluster.setNodes(List.of("192.168.50.157:7001", "192.168.50.157:7002", "192.168.50.157:7003",
                "192.168.50.157:7004", "192.168.50.157:7005", "192.168.50.157:7006"));
        cluster.setReadFrom("upstreamPreferred");

        ClientResources resources = ClientResources.builder().build();
        ClusterClientOptions options = ClusterClientOptions.builder().protocolVersion(ProtocolVersion.RESP3).build();
        LettuceClusterConfig config = LettuceConfigHelper.createConfig("test", cluster);

        return new LettuceClusterFactory(config, options, resources);
    }

    /**
     * 将传入的 String[] 转换为 byte[][]
     *
     * @param size key 数量
     * @param keys key 数组
     * @return keyBytes
     */
    public static byte[][] toKeysArray(int size, String[] keys) {
        byte[][] keysArray = new byte[size][];
        for (int i = 0; i < size; i++) {
            keysArray[i] = codec.encode(keys[i]);
        }
        return keysArray;
    }

    /**
     * 将传入的 {@code List<KeyValue>} 转换为 {@code Map<String, String>}
     *
     * @param keyValues keyValue 列表
     * @return map
     */
    public static Map<String, String> from(List<io.lettuce.core.KeyValue<byte[], byte[]>> keyValues) {
        Map<String, String> map = Maps.newHashMap(keyValues.size());
        keyValues.forEach(keyValue -> {
            if (keyValue != null && keyValue.hasValue()) {
                map.put(codec.decode(keyValue.getKey()), codec.decode(keyValue.getValue()));
            }
        });
        return map;
    }

    /**
     * 将传入的 {@code List<KeyValue>} 转换为 {@code Map<String, String>}
     *
     * @param keyValues keyValue 列表
     * @return map
     */
    public static Map<String, String> fromKeyValues(List<KeyValue<byte[], byte[]>> keyValues) {
        Map<String, String> map = Maps.newHashMap(keyValues.size());
        keyValues.forEach(keyValue -> {
            if (keyValue != null && keyValue.hasValue()) {
                map.put(codec.decode(keyValue.getKey()), codec.decode(keyValue.getValue()));
            }
        });
        return map;
    }

    /**
     * 验证传入的 {@code Map<String, String>} 是否与传入的 {@code String[]} 完全匹配
     *
     * @param keys  key 数组
     * @param map   map
     * @param limit 验证长度
     */
    public static void validateValues(String[] keys, Map<String, String> map, int limit) {
        int i = 0;
        for (String key : keys) {
            if (i < limit) {
                Assertions.assertEquals(key, map.get(key));
            } else {
                Assertions.assertNull(map.get(key));
            }
            i++;
        }
    }

    /**
     * 验证传入的 {@code Map<String, String>} 是否与传入的 {@code String[]} 完全匹配
     *
     * @param keys  key 数组
     * @param map   map
     * @param limit 验证长度
     */
    public static void validateValues(List<String> keys, Map<String, String> map, int limit) {
        int i = 0;
        for (String key : keys) {
            if (i < limit) {
                Assertions.assertEquals(key, map.get(key));
            } else {
                Assertions.assertNull(map.get(key));
            }
            i++;
        }
    }

    /**
     * 创建 key 数组
     *
     * @param size   key 数组长度
     * @param prefix key 前缀
     * @return key 数组
     */
    public static String[] createKeys(int size, String prefix) {
        String[] keys = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = prefix + RandomUtils.nextString(18);
        }
        return keys;
    }

    /**
     * 使用传入的数组创建键值对（键与值相同）
     *
     * @param size      转换数量
     * @param keysArray 键数组
     * @return KeyValues 键值对（键与值相同）
     */
    public static Map<byte[], byte[]> createKeyValues(int size, byte[][] keysArray) {
        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            keyValues.put(keysArray[i], keysArray[i]);
        }
        return keyValues;
    }

    /**
     * 创建 键-字段-值 集合
     *
     * @param size   键数量
     * @param length 字段数量
     * @param prefix 前缀
     * @return key 键-字段-值 集合
     */
    public static Map<byte[], Map<byte[], byte[]>> createKeyFieldValues(int size, int length, String prefix) {
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = prefix + i + RandomUtils.nextString(18);
            HashMap<byte[], byte[]> fieldValues = Maps.newHashMap(length);
            for (int j = 0; j < length; j++) {
                byte[] field = codec.encode(prefix + ":" + i + ":" + j + RandomUtils.nextString(5));
                fieldValues.put(field, field);
            }
            keyFieldValues.put(codec.encode(key), fieldValues);
        }
        return keyFieldValues;
    }

    /**
     * 创建 键-字段-值 集合
     *
     * @param size   键数量
     * @param length 字段数量
     * @param prefix 前缀
     * @return key 键-字段-值 集合
     */
    public static Map<byte[], List<KeyValue<byte[], byte[]>>> createKeyFieldValueList(int size, int length, String prefix) {
        Map<byte[], List<KeyValue<byte[], byte[]>>> keyFieldValues = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = prefix + i + RandomUtils.nextString(18);
            List<KeyValue<byte[], byte[]>> fieldValues = new ArrayList<>(length);
            for (int j = 0; j < length; j++) {
                byte[] field = codec.encode(prefix + ":" + i + ":" + j + RandomUtils.nextString(5));
                fieldValues.add(KeyValue.create(field, field));
            }
            keyFieldValues.put(codec.encode(key), fieldValues);
        }
        return keyFieldValues;
    }


    /**
     * 创建 键-字段-值 集合
     *
     * @param size   键数量
     * @param length 字段数量
     * @param prefix 前缀
     * @return key 键-字段-值 集合
     */
    public static Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> createExpireKeyFieldValueList(int size, int length, String prefix) {
        Map<byte[], List<ExpiryKeyValue<byte[], byte[]>>> keyFieldValues = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = prefix + i + RandomUtils.nextString(18);
            List<ExpiryKeyValue<byte[], byte[]>> fieldValues = new ArrayList<>(length);
            for (int j = 0; j < length; j++) {
                byte[] field = codec.encode(prefix + ":" + i + ":" + j + RandomUtils.nextString(5));
                fieldValues.add(ExpiryKeyValue.create(field, field, RandomUtils.nextLong(100000000, 1000000000)));
            }
            keyFieldValues.put(codec.encode(key), fieldValues);
        }
        return keyFieldValues;
    }

    /**
     * 创建 键-分值-成员 集合
     *
     * @param size   键数量
     * @param length 成员数量
     * @param prefix 前缀
     * @return key 键-分值-成员 集合
     */
    public static Map<byte[], List<ScoredValue<byte[]>>> createKeyScoredValueList(int size, int length, String prefix) {
        Map<byte[], List<ScoredValue<byte[]>>> keyScoredValues = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = prefix + i + RandomUtils.nextString(18);
            List<ScoredValue<byte[]>> scoredValues = createScoredValues(length, prefix);
            keyScoredValues.put(codec.encode(key), scoredValues);
        }
        return keyScoredValues;
    }

    /**
     * 创建 分值-成员 列表
     *
     * @param length 成员数量
     * @param prefix 前缀
     * @return key 键-字段-值 集合
     */
    public static List<ScoredValue<byte[]>> createScoredValues(int length, String prefix) {
        List<ScoredValue<byte[]>> scoredValues = new ArrayList<>(length);
        for (int j = 0; j < length; j++) {
            byte[] member = codec.encode(prefix + ":" + j + RandomUtils.nextString(10));
            scoredValues.add(ScoredValue.just(RandomUtils.nextDouble(10000, 100000), member));
        }
        return scoredValues;
    }

}
