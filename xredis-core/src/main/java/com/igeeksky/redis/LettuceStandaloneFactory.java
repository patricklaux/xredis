package com.igeeksky.redis;


import com.igeeksky.redis.api.RedisOperatorFactory;
import com.igeeksky.redis.config.LettuceStandaloneConfig;
import com.igeeksky.redis.config.RedisNode;
import com.igeeksky.redis.stream.StreamContainer;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Lettuce Standalone 客户端工厂
 * <p>
 * 单主机模式 和 副本集模式
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceStandaloneFactory implements RedisOperatorFactory {

    private final RedisClient client;
    private final LettuceStandaloneConfig config;

    /**
     * Lettuce Standalone 客户端工厂
     *
     * @param config    配置信息
     * @param options   客户端选项
     * @param resources 客户端资源
     */
    public LettuceStandaloneFactory(LettuceStandaloneConfig config, ClientOptions options, ClientResources resources) {
        this.config = config;
        this.client = redisClient(resources, options);
    }

    private static RedisClient redisClient(ClientResources resources, ClientOptions options) {
        RedisClient redisClient = RedisClient.create(resources);
        redisClient.setOptions(options);
        return redisClient;
    }

    @Override
    public <K, V> LettuceOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> connection = connect(codec, true);
        StatefulRedisMasterReplicaConnection<K, V> batchConnection = connect(codec, false);
        return new LettuceOperator<>(connection, batchConnection, codec);
    }

    @Override
    public <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler,
                                                        long interval) {
        return new StreamContainer<>(redisOperator(codec), scheduler, interval);
    }

    private <K, V> StatefulRedisMasterReplicaConnection<K, V> connect(RedisCodec<K, V> codec, boolean autoFlush) {
        RedisURI redisURI = null;
        if (config.getNode() != null) {
            redisURI = LettuceHelper.redisURI(config, config.getNode());
        }

        // 创建 Standalone[主从连接]，未配置副本节点，动态拓扑结构，主动发现副本集
        List<RedisNode> nodes = config.getNodes();
        if (CollectionUtils.isEmpty(nodes)) {
            if (redisURI != null) {
                StatefulRedisMasterReplicaConnection<K, V> conn = MasterReplica.connect(client, codec, redisURI);
                conn.setReadFrom(config.getReadFrom());
                conn.setAutoFlushCommands(autoFlush);
                return conn;
            }
            throw new RedisConfigException("Redis standalone: id:[" + config.getId() + "] No nodes configured");
        }

        // 创建 Standalone[主从连接]，已配置副本节点，静态拓扑结构
        List<RedisURI> redisURIS = new ArrayList<>();
        if (redisURI != null) {
            redisURIS.add(redisURI);
        }

        for (RedisNode node : nodes) {
            redisURIS.add(LettuceHelper.redisURI(config, node));
        }

        StatefulRedisMasterReplicaConnection<K, V> conn = MasterReplica.connect(client, codec, redisURIS);
        conn.setReadFrom(config.getReadFrom());
        conn.setAutoFlushCommands(autoFlush);
        return conn;
    }

    @Override
    public void close() {
        client.shutdown();
    }

}