package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.common.stream.container.StreamGenericContainer;
import com.igeeksky.xredis.lettuce.config.LettuceSentinelConfig;
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