package com.igeeksky.redis;

import com.igeeksky.redis.api.RedisOperatorFactory;
import com.igeeksky.redis.cluster.LettuceClusterOperator;
import com.igeeksky.redis.config.LettuceClusterConfig;
import com.igeeksky.redis.config.RedisNode;
import com.igeeksky.redis.stream.StreamContainer;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Lettuce 集群客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceClusterFactory implements RedisOperatorFactory {

    private final RedisClusterClient client;
    private final LettuceClusterConfig config;

    /**
     * 构造函数
     *
     * @param config  集群配置
     * @param options 集群客户端选项
     * @param res     客户端线程资源
     */
    public LettuceClusterFactory(LettuceClusterConfig config, ClusterClientOptions options, ClientResources res) {
        super();
        this.config = config;
        this.client = redisClient(options, res);
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
        var connection = client.connect(codec);
        connection.setReadFrom(config.getReadFrom());
        connection.setAutoFlushCommands(autoFlush);
        return connection;
    }

    @Override
    public <K, V> LettuceClusterOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> connection = connect(codec, true);
        StatefulRedisClusterConnection<K, V> batchConnection = connect(codec, false);
        return new LettuceClusterOperator<>(connection, batchConnection, codec);
    }

    @Override
    public <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler,
                                                        long interval) {
        return new StreamContainer<>(redisOperator(codec), scheduler, interval);
    }

    @Override
    public void close() {
        client.shutdown();
    }

}