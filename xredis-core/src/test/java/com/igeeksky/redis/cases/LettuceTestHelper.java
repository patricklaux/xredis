package com.igeeksky.redis.cases;


import com.igeeksky.redis.LettuceClusterFactory;
import com.igeeksky.redis.LettuceSentinelFactory;
import com.igeeksky.redis.LettuceStandaloneFactory;
import com.igeeksky.redis.config.LettuceClusterConfig;
import com.igeeksky.redis.config.LettuceSentinelConfig;
import com.igeeksky.redis.config.LettuceStandaloneConfig;
import com.igeeksky.redis.props.LettuceCluster;
import com.igeeksky.redis.props.LettuceConfigHelper;
import com.igeeksky.redis.props.LettuceSentinel;
import com.igeeksky.redis.props.LettuceStandalone;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.RandomUtils;
import com.igeeksky.xtool.core.lang.codec.StringCodec;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.KeyValue;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.resource.ClientResources;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
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

    public static LettuceStandaloneFactory createStandaloneFactory() {
        LettuceStandalone standalone = new LettuceStandalone();
        standalone.setNode("127.0.0.1:6379");
        standalone.setNodes(List.of("127.0.0.1:6380"));
        standalone.setReadFrom("upstreamPreferred");

        LettuceStandaloneConfig standaloneConfig = LettuceConfigHelper.createConfig("test", standalone);
        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();

        return new LettuceStandaloneFactory(standaloneConfig, options, resources);
    }

    public static LettuceSentinelFactory createSentinelFactory() {
        LettuceSentinel sentinel = new LettuceSentinel();
        sentinel.setNodes(List.of("127.0.0.1:26379", "127.0.0.1:26380", "127.0.0.1:26381"));
        sentinel.setReadFrom("upstreamPreferred");
        sentinel.setMasterId("mymaster");

        ClientOptions options = ClientOptions.builder().build();
        ClientResources resources = ClientResources.builder().build();
        LettuceSentinelConfig config = LettuceConfigHelper.createConfig("lettuce", sentinel);

        return new LettuceSentinelFactory(config, options, resources);
    }

    public static LettuceClusterFactory createClusterConnectionFactory() {
        LettuceCluster cluster = new LettuceCluster();
        cluster.setNodes(List.of("127.0.0.1:7001", "127.0.0.1:7002", "127.0.0.1:7003"));
        cluster.setReadFrom("upstreamPreferred");

        ClientResources resources = ClientResources.builder().build();
        ClusterClientOptions options = ClusterClientOptions.builder().build();
        LettuceClusterConfig config = LettuceConfigHelper.createConfig("test", cluster);

        return new LettuceClusterFactory(config, options, resources);
    }

    public static byte[][] toKeysArray(int size, String[] keys) {
        byte[][] keyBytes = new byte[size][];
        for (int i = 0; i < size; i++) {
            keyBytes[i] = codec.encode(keys[i]);
        }
        return keyBytes;
    }

    public static Map<String, String> fromKeyValues(List<KeyValue<byte[], byte[]>> keyValues) {
        Map<String, String> map = Maps.newHashMap(keyValues.size());
        keyValues.forEach(keyValue -> {
            if (keyValue.hasValue()) {
                String field = codec.decode(keyValue.getKey());
                String value = codec.decode(keyValue.getValue());
                map.put(field, value);
            }
        });
        return map;
    }

    public static void validateValues(String[] keys, Map<String, String> map, int size, int limit) {
        for (int i = 0; i < size; i++) {
            String key = keys[i];
            if (i < limit) {
                Assertions.assertEquals(key, map.get(key));
            } else {
                Assertions.assertNull(map.get(key));
            }
        }
    }

    public static String[] createKeys(int size, String prefix) {
        String[] keys = new String[size];
        for (int i = 0; i < size; i++) {
            keys[i] = prefix + RandomUtils.nextString(18);
        }
        return keys;
    }

    public static Map<byte[], byte[]> createKeyValues(int size, byte[][] keyBytes) {
        Map<byte[], byte[]> keyValues = Maps.newHashMap(size);
        for (int i = 0; i < size; i++) {
            keyValues.put(keyBytes[i], keyBytes[i]);
        }
        return keyValues;
    }

    public static Map<byte[], Map<byte[], byte[]>> createKeyFieldValues(int size, int length, String prefix) {
        Map<byte[], Map<byte[], byte[]>> keyFieldValues = new HashMap<>();
        for (int i = 0; i < size; i++) {
            String key = prefix + i + RandomUtils.nextString(5);
            HashMap<byte[], byte[]> fieldValues = Maps.newHashMap(length);
            for (int j = 0; j < length; j++) {
                byte[] field = codec.encode(prefix + ":" + i + ":" + j + RandomUtils.nextString(5));
                fieldValues.put(field, field);
            }
            keyFieldValues.put(codec.encode(key), fieldValues);
        }
        return keyFieldValues;
    }

}
