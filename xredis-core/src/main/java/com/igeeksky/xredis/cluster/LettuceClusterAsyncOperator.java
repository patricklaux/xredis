package com.igeeksky.xredis.cluster;

import com.igeeksky.xredis.api.RedisAsyncOperator;
import io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterAsyncOperator<K, V> extends RedisAdvancedClusterAsyncCommandsImpl<K, V>
        implements RedisAsyncOperator<K, V> {

    public LettuceClusterAsyncOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    public LettuceClusterAsyncOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

}
