package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.lettuce.cluster.LettuceClusterOperator;
import com.igeeksky.xredis.lettuce.cluster.LettuceClusterPipeline;
import com.igeeksky.xredis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.xredis.lettuce.config.RedisNode;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Lettuce 集群客户端工厂
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public final class LettuceClusterFactory extends AbstractLettuceFactory {

    private final RedisClusterClient client;
    private final LettuceClusterConfig config;
    private final Supplier<JsonParser> jsonParser;

    /**
     * 构造函数
     *
     * @param config  集群配置
     * @param options 集群客户端选项
     * @param res     客户端线程资源
     */
    public LettuceClusterFactory(LettuceClusterConfig config, ClusterClientOptions options, ClientResources res) {
        super(config);
        this.config = config;
        this.client = redisClient(options, res);
        this.jsonParser = options.getJsonParser();
    }

    @Override
    public <K, V> LettuceClusterPipeline<K, V> pipeline(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> batchConnection = this.connect(codec, false);
        if (jsonParser != null) {
            return new LettuceClusterPipeline<>(batchConnection, codec, jsonParser);
        }
        return new LettuceClusterPipeline<>(batchConnection, codec);
    }

    @Override
    public <K, V> LettuceClusterOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> connection = this.connect(codec, true);
        if (jsonParser != null) {
            return new LettuceClusterOperator<>(connection, codec, jsonParser);
        }
        return new LettuceClusterOperator<>(connection, codec);
    }

    @Override
    protected AbstractRedisClient getClient() {
        return client;
    }

    private RedisClusterClient redisClient(ClusterClientOptions options, ClientResources res) {
        List<RedisNode> nodes = config.getNodes();
        List<RedisURI> redisURIs = new ArrayList<>(nodes.size());
        for (RedisNode node : nodes) {
            redisURIs.add(LettuceHelper.redisURI(config, node));
        }

        RedisClusterClient redisClient = RedisClusterClient.create(res, redisURIs);
        redisClient.setOptions(options);
        return redisClient;
    }

    private <K, V> StatefulRedisClusterConnection<K, V> connect(RedisCodec<K, V> codec, boolean autoFlush) {
        StatefulRedisClusterConnection<K, V> connection = client.connect(codec);
        connection.setReadFrom(config.getReadFrom());
        connection.setAutoFlushCommands(autoFlush);
        return connection;
    }

}