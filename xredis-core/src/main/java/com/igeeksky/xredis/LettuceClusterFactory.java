package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisOperatorFactory;
import com.igeeksky.xredis.cluster.*;
import com.igeeksky.xredis.config.LettuceClusterConfig;
import com.igeeksky.xredis.config.RedisNode;
import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xredis.stream.container.StreamContainer;
import com.igeeksky.xredis.stream.container.StreamGenericContainer;
import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.resource.ClientResources;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Lettuce 集群客户端工厂
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public final class LettuceClusterFactory implements RedisOperatorFactory {

    private final RedisClusterClient client;
    private final LettuceClusterConfig config;
    private final Mono<JsonParser> jsonParser;
    private final ExecutorService executor;

    /**
     * 构造函数
     *
     * @param config  集群配置
     * @param options 集群客户端选项
     * @param res     客户端线程资源
     */
    public LettuceClusterFactory(LettuceClusterConfig config, ClusterClientOptions options, ClientResources res) {
        this.config = config;
        this.client = redisClient(options, res);
        this.jsonParser = options.getJsonParser();
        this.executor = LettuceHelper.getVirtualThreadPerTaskExecutor();
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

    @Override
    public <K, V> LettuceClusterPipeline<K, V> pipeline(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> batchConnection = connect(codec, false);
        if (jsonParser != null) {
            return new LettuceClusterPipeline<>(batchConnection, codec, jsonParser);
        }
        return new LettuceClusterPipeline<>(batchConnection, codec);
    }

    @Override
    public <K, V> LettuceClusterSyncOperator<K, V> redisSyncOperator(RedisCodec<K, V> codec) {
        return new LettuceClusterSyncOperator<>(connect(codec, true));
    }

    @Override
    public <K, V> LettuceClusterAsyncOperator<K, V> redisAsyncOperator(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> connection = connect(codec, true);
        if (jsonParser != null) {
            return new LettuceClusterAsyncOperator<>(connection, codec, jsonParser);
        }
        return new LettuceClusterAsyncOperator<>(connection, codec);
    }

    @Override
    public <K, V> LettuceClusterReactiveOperator<K, V> redisReactiveOperator(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> connection = connect(codec, true);
        if (jsonParser != null) {
            return new LettuceClusterReactiveOperator<>(connection, codec, jsonParser);
        }
        return new LettuceClusterReactiveOperator<>(connection, codec);
    }

    @Override
    public <K, V> LettuceClusterOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisClusterConnection<K, V> connection = connect(codec, true);
        StatefulRedisClusterConnection<K, V> batchConnection = connect(codec, false);
        if (jsonParser != null) {
            return new LettuceClusterOperator<>(connection, batchConnection, codec, jsonParser);
        }
        return new LettuceClusterOperator<>(connection, batchConnection, codec);
    }

    @Override
    public <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, long interval,
                                                        ScheduledExecutorService scheduler) {
        return new StreamContainer<>(redisOperator(codec), interval, executor, scheduler);
    }

    @Override
    public <K, V> StreamGenericContainer<K, V> streamGenericContainer(RedisCodec<K, V> codec, long interval,
                                                                      XReadOptions options,
                                                                      ScheduledExecutorService scheduler) {
        return new StreamGenericContainer<>(redisOperator(codec), interval, options, executor, scheduler);
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