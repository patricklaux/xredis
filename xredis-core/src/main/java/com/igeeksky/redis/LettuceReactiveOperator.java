package com.igeeksky.redis;

import com.igeeksky.redis.api.RedisReactiveOperator;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceReactiveOperator<K, V> extends RedisReactiveCommandsImpl<K, V>
        implements RedisReactiveOperator<K, V> {

    public LettuceReactiveOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    public LettuceReactiveOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

}
