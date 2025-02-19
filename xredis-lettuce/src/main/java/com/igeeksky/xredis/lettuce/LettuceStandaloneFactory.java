package com.igeeksky.xredis.lettuce;


import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.common.RedisConfigException;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.common.stream.container.StreamGenericContainer;
import com.igeeksky.xredis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xredis.lettuce.config.RedisNode;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
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
    private final Mono<JsonParser> jsonParser;
    private final ExecutorService executor;

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
        this.jsonParser = options.getJsonParser();
        this.executor = LettuceHelper.getVirtualThreadPerTaskExecutor();
    }

    private static RedisClient redisClient(ClientResources resources, ClientOptions options) {
        RedisClient redisClient = RedisClient.create(resources);
        redisClient.setOptions(options);
        return redisClient;
    }

    @Override
    public <K, V> LettucePipeline<K, V> pipeline(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> batchConnection = connect(codec, false);
        if (jsonParser != null) {
            return new LettucePipeline<>(batchConnection, codec, jsonParser);
        }
        return new LettucePipeline<>(batchConnection, codec);
    }

    @Override
    public <K, V> LettuceOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> connection = connect(codec, true);
        if (jsonParser != null) {
            return new LettuceOperator<>(connection, codec, jsonParser);
        }
        return new LettuceOperator<>(connection, codec);
    }

    @Override
    public <K, V> LettuceStreamOperator<K, V> streamOperator(RedisCodec<K, V> codec) {
        return new LettuceStreamOperator<>(this.redisOperator(codec));
    }

    @Override
    public <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler,
                                                        long interval, ReadOptions options) {
        long quietPeriod = config.getShutdownQuietPeriod();
        long timeout = config.getShutdownTimeout();
        LettuceStreamOperator<K, V> streamOperator = this.streamOperator(codec);
        return new StreamContainer<>(streamOperator, executor, scheduler, quietPeriod, timeout, interval, options);
    }

    @Override
    public <K, V> StreamGenericContainer<K, V> streamGenericContainer(RedisCodec<K, V> codec,
                                                                      ScheduledExecutorService scheduler,
                                                                      long interval) {
        long quietPeriod = config.getShutdownQuietPeriod();
        long timeout = config.getShutdownTimeout();
        LettuceStreamOperator<K, V> streamOperator = this.streamOperator(codec);
        return new StreamGenericContainer<>(streamOperator, executor, scheduler, quietPeriod, timeout, interval);
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
    public void shutdown() {
        long timeout = config.getShutdownTimeout();
        long quietPeriod = config.getShutdownQuietPeriod();
        LettuceHelper.shutdown(quietPeriod, timeout, executor, client);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync() {
        long timeout = config.getShutdownTimeout();
        long quietPeriod = config.getShutdownQuietPeriod();
        return LettuceHelper.shutdownAsync(quietPeriod, timeout, executor, client);
    }

}