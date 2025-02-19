package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisOperator;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * RedisOperator 实现类（非集群）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceOperator<K, V> implements RedisOperator<K, V> {

    private final LettuceSyncOperator<K, V> sync;
    private final LettuceAsyncOperator<K, V> async;
    private final LettuceReactiveOperator<K, V> reactive;
    private final StatefulRedisConnection<K, V> connection;

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     */
    public LettuceOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        this.connection = connection;
        this.sync = new LettuceSyncOperator<>(connection);
        this.async = new LettuceAsyncOperator<>(connection, codec);
        this.reactive = new LettuceReactiveOperator<>(connection, codec);
    }

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        this.connection = connection;
        this.sync = new LettuceSyncOperator<>(connection);
        this.async = new LettuceAsyncOperator<>(connection, codec, parser);
        this.reactive = new LettuceReactiveOperator<>(connection, codec, parser);
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
    public CompletableFuture<Void> closeAsync() {
        return connection.closeAsync();
    }

    @Override
    public boolean isCluster() {
        return false;
    }

}
