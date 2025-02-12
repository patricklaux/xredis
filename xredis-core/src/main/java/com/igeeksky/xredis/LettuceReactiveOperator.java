package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisReactiveOperator;
import io.lettuce.core.RedisReactiveCommandsImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * RedisReactiveOperator 实现类（非集群）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceReactiveOperator<K, V> extends RedisReactiveCommandsImpl<K, V>
        implements RedisReactiveOperator<K, V> {

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     */
    public LettuceReactiveOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceReactiveOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

}
