package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * LettuceClusterOperator
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterOperator<K, V> implements RedisOperator<K, V> {

    private final LettuceClusterSyncOperator<K, V> sync;
    private final LettuceClusterAsyncOperator<K, V> async;
    private final LettuceClusterReactiveOperator<K, V> reactive;
    private final StatefulRedisClusterConnection<K, V> connection;

    /**
     * cluster operator constructor
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     */
    public LettuceClusterOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec) {
        this.connection = connection;
        this.sync = new LettuceClusterSyncOperator<>(connection);
        this.async = new LettuceClusterAsyncOperator<>(connection, codec);
        this.reactive = new LettuceClusterReactiveOperator<>(connection, codec);
    }

    /**
     * cluster operator constructor
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceClusterOperator(StatefulRedisClusterConnection<K, V> connection,
                                  RedisCodec<K, V> codec, Supplier<JsonParser> parser) {
        this.connection = connection;
        this.sync = new LettuceClusterSyncOperator<>(connection);
        this.async = new LettuceClusterAsyncOperator<>(connection, codec, parser);
        this.reactive = new LettuceClusterReactiveOperator<>(connection, codec, parser);
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
    public boolean isCluster() {
        return true;
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return connection.closeAsync();
    }

}
