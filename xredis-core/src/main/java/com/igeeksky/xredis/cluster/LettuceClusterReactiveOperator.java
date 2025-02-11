package com.igeeksky.xredis.cluster;

import com.igeeksky.xredis.api.RedisReactiveOperator;
import io.lettuce.core.cluster.RedisAdvancedClusterReactiveCommandsImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterReactiveOperator<K, V> extends RedisAdvancedClusterReactiveCommandsImpl<K, V> implements RedisReactiveOperator<K, V> {

    public LettuceClusterReactiveOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    public LettuceClusterReactiveOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
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
