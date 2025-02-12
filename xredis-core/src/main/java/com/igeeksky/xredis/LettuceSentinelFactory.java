package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisOperatorFactory;
import com.igeeksky.xredis.config.LettuceSentinelConfig;
import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xredis.stream.container.StreamContainer;
import com.igeeksky.xredis.stream.container.StreamGenericContainer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Lettuce Sentinel 客户端工厂
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-01
 */
public final class LettuceSentinelFactory implements RedisOperatorFactory {

    private final RedisURI uri;
    private final RedisClient client;
    private final LettuceSentinelConfig config;
    private final Mono<JsonParser> jsonParser;
    private final ExecutorService executor;

    /**
     * Lettuce Sentinel 客户端工厂
     *
     * @param config  配置信息
     * @param options 客户端选项
     * @param res     客户端资源
     */
    public LettuceSentinelFactory(LettuceSentinelConfig config, ClientOptions options, ClientResources res) {
        this.config = config;
        this.uri = redisUri(config);
        this.client = redisClient(options, res);
        this.jsonParser = options.getJsonParser();
        this.executor = LettuceHelper.getVirtualThreadPerTaskExecutor();
    }

    private static RedisURI redisUri(LettuceSentinelConfig config) {
        return LettuceHelper.sentinelURIBuilder(config);
    }

    private static RedisClient redisClient(ClientOptions options, ClientResources res) {
        RedisClient redisClient = RedisClient.create(res);
        redisClient.setOptions(options);
        return redisClient;
    }

    private <K, V> StatefulRedisMasterReplicaConnection<K, V> connect(RedisCodec<K, V> codec, boolean autoFlush) {
        StatefulRedisMasterReplicaConnection<K, V> connection = MasterReplica.connect(client, codec, uri);
        connection.setReadFrom(config.getReadFrom());
        connection.setAutoFlushCommands(autoFlush);
        return connection;
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
    public <K, V> LettuceSyncOperator<K, V> redisSyncOperator(RedisCodec<K, V> codec) {
        return new LettuceSyncOperator<>(connect(codec, true));
    }

    @Override
    public <K, V> LettuceAsyncOperator<K, V> redisAsyncOperator(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> connection = connect(codec, true);
        if (jsonParser != null) {
            return new LettuceAsyncOperator<>(connection, codec, jsonParser);
        }
        return new LettuceAsyncOperator<>(connection, codec);
    }

    @Override
    public <K, V> LettuceReactiveOperator<K, V> redisReactiveOperator(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> connection = connect(codec, true);
        if (jsonParser != null) {
            return new LettuceReactiveOperator<>(connection, codec, jsonParser);
        }
        return new LettuceReactiveOperator<>(connection, codec);
    }

    @Override
    public <K, V> LettuceOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> connection = connect(codec, true);
        StatefulRedisMasterReplicaConnection<K, V> batchConnection = connect(codec, false);
        if (jsonParser != null) {
            return new LettuceOperator<>(connection, batchConnection, codec, jsonParser);
        }
        return new LettuceOperator<>(connection, batchConnection, codec);
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