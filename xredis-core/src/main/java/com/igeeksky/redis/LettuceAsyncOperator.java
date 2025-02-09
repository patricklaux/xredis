package com.igeeksky.redis;

import com.igeeksky.redis.api.RedisAsyncOperator;
import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * 异步操作
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceAsyncOperator<K, V> extends RedisAsyncCommandsImpl<K, V> implements RedisAsyncOperator<K, V> {

    public LettuceAsyncOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    public LettuceAsyncOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec,
                                Mono<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

}
