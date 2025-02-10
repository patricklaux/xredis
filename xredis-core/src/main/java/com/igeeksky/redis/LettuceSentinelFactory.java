package com.igeeksky.redis;

import com.igeeksky.redis.api.RedisOperatorFactory;
import com.igeeksky.redis.config.LettuceSentinelConfig;
import com.igeeksky.redis.stream.StreamContainer;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import io.lettuce.core.masterreplica.MasterReplica;
import io.lettuce.core.masterreplica.StatefulRedisMasterReplicaConnection;
import io.lettuce.core.resource.ClientResources;
import reactor.core.publisher.Mono;

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

    /**
     * Lettuce Sentinel 客户端工厂
     *
     * @param config  配置信息
     * @param options 客户端选项
     * @param res     客户端资源
     */
    public LettuceSentinelFactory(LettuceSentinelConfig config, ClientOptions options, ClientResources res) {
        super();
        this.config = config;
        this.uri = redisUri(config);
        this.client = redisClient(options, res);
        this.jsonParser = options.getJsonParser();
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
    public <K, V> LettuceOperator<K, V> redisOperator(RedisCodec<K, V> codec) {
        StatefulRedisMasterReplicaConnection<K, V> connection = connect(codec, true);
        StatefulRedisMasterReplicaConnection<K, V> batchConnection = connect(codec, false);
        if (jsonParser != null) {
            return new LettuceOperator<>(connection, batchConnection, codec, jsonParser);
        }
        return new LettuceOperator<>(connection, batchConnection, codec);
    }

    @Override
    public <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler,
                                                        long interval) {
        return new StreamContainer<>(redisOperator(codec), scheduler, interval);
    }

    @Override
    public void shutdown() {
        client.shutdown();
    }

}