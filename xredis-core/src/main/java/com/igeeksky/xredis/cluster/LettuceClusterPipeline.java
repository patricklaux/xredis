package com.igeeksky.xredis.cluster;

import com.igeeksky.xredis.api.Pipeline;
import io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

/**
 * 管道接口实现（集群模式）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterPipeline<K, V> extends RedisAdvancedClusterAsyncCommandsImpl<K, V> implements Pipeline<K, V> {

    private final StatefulRedisClusterConnection<K, V> connection;

    /**
     * cluster operator constructor
     *
     * @param connection 批量提交命令连接（autoFlush = false）
     * @param codec      编解码器
     */
    public LettuceClusterPipeline(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
        this.connection = connection;
    }

    /**
     * cluster operator constructor
     *
     * @param connection 批量提交命令连接（autoFlush = false）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceClusterPipeline(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        super(connection, codec, parser);
        this.connection = connection;
    }

    @Override
    public void flushCommands() {
        connection.flushCommands();
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException(
                "Pipeline doesn't support change auto flush mode, it must be false." +
                        "If you want to flush commands immediately, please use redis-*-operator."
        );
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