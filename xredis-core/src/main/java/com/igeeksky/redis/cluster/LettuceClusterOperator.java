package com.igeeksky.redis.cluster;

import com.igeeksky.redis.api.RedisOperator;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * LettuceClusterOperator
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterOperator<K, V> implements RedisOperator<K, V> {

    private final StatefulRedisClusterConnection<K, V> connection;
    private final StatefulRedisClusterConnection<K, V> batchConnection;
    private final LettuceClusterPipeline<K, V> pipeline;
    private final LettuceClusterSyncOperator<K, V> sync;
    private final LettuceClusterAsyncOperator<K, V> async;
    private final LettuceClusterReactiveOperator<K, V> reactive;

    /**
     * cluster operator constructor
     *
     * @param connection      立即提交命令连接（autoFlush = true）
     * @param batchConnection 批量提交命令连接（autoFlush = false）
     * @param codec           编解码器
     */
    public LettuceClusterOperator(StatefulRedisClusterConnection<K, V> connection,
                                  StatefulRedisClusterConnection<K, V> batchConnection,
                                  RedisCodec<K, V> codec) {
        this.connection = connection;
        this.batchConnection = batchConnection;
        this.sync = new LettuceClusterSyncOperator<>(connection);
        this.async = new LettuceClusterAsyncOperator<>(connection, codec);
        this.reactive = new LettuceClusterReactiveOperator<>(connection, codec);
        this.pipeline = new LettuceClusterPipeline<>(batchConnection, codec);
    }

    /**
     * cluster operator constructor
     *
     * @param connection      立即提交命令连接（autoFlush = true）
     * @param batchConnection 批量提交命令连接（autoFlush = false）
     * @param codec           编解码器
     * @param parser          json 解析器
     */
    public LettuceClusterOperator(StatefulRedisClusterConnection<K, V> connection,
                                  StatefulRedisClusterConnection<K, V> batchConnection,
                                  RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        this.connection = connection;
        this.batchConnection = batchConnection;
        this.sync = new LettuceClusterSyncOperator<>(connection);
        this.async = new LettuceClusterAsyncOperator<>(connection, codec, parser);
        this.reactive = new LettuceClusterReactiveOperator<>(connection, codec, parser);
        this.pipeline = new LettuceClusterPipeline<>(batchConnection, codec, parser);
    }

    @Override
    public LettuceClusterSyncOperator<K, V> sync() {
        return sync;
    }

    @Override
    public LettuceClusterAsyncOperator<K, V> async() {
        return async;
    }

    @Override
    public LettuceClusterReactiveOperator<K, V> reactive() {
        return reactive;
    }

    @Override
    public LettuceClusterPipeline<K, V> pipeline() {
        return pipeline;
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return connection.closeAsync().thenCompose(vod -> batchConnection.closeAsync());
    }

}
