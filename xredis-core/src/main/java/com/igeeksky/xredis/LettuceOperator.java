package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisOperator;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceOperator<K, V> implements RedisOperator<K, V> {

    private final StatefulRedisConnection<K, V> connection;
    private final StatefulRedisConnection<K, V> batchConnection;

    private final LettucePipeline<K, V> pipeline;
    private final LettuceSyncOperator<K, V> sync;
    private final LettuceAsyncOperator<K, V> async;
    private final LettuceReactiveOperator<K, V> reactive;

    /**
     * Standalone or Sentinel
     *
     * @param connection      立即提交命令连接（autoFlush = true）
     * @param batchConnection 批量提交命令连接（autoFlush = false）
     * @param codec           编解码器
     */
    public LettuceOperator(StatefulRedisConnection<K, V> connection,
                           StatefulRedisConnection<K, V> batchConnection,
                           RedisCodec<K, V> codec) {
        this.connection = connection;
        this.batchConnection = batchConnection;
        this.async = new LettuceAsyncOperator<>(connection, codec);
        this.sync = new LettuceSyncOperator<>(connection);
        this.reactive = new LettuceReactiveOperator<>(connection, codec);
        this.pipeline = new LettucePipeline<>(batchConnection, codec);
    }

    /**
     * Standalone or Sentinel
     *
     * @param connection      立即提交命令连接（autoFlush = true）
     * @param batchConnection 批量提交命令连接（autoFlush = false）
     * @param codec           编解码器
     * @param parser          json 解析器
     */
    public LettuceOperator(StatefulRedisConnection<K, V> connection,
                           StatefulRedisConnection<K, V> batchConnection,
                           RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        this.connection = connection;
        this.batchConnection = batchConnection;
        this.async = new LettuceAsyncOperator<>(connection, codec, parser);
        this.sync = new LettuceSyncOperator<>(connection);
        this.reactive = new LettuceReactiveOperator<>(connection, codec, parser);
        this.pipeline = new LettucePipeline<>(batchConnection, codec, parser);
    }

    @Override
    public LettuceSyncOperator<K, V> sync() {
        return sync;
    }

    @Override
    public LettuceAsyncOperator<K, V> async() {
        return async;
    }

    @Override
    public LettuceReactiveOperator<K, V> reactive() {
        return reactive;
    }

    @Override
    public LettucePipeline<K, V> pipeline() {
        return pipeline;
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return connection.closeAsync().thenCompose(vod -> batchConnection.closeAsync());
    }

}
