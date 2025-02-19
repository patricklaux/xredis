package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisAsyncOperator;
import io.lettuce.core.RedisAsyncCommandsImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * 异步操作
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceAsyncOperator<K, V> extends RedisAsyncCommandsImpl<K, V> implements RedisAsyncOperator<K, V> {

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     */
    public LettuceAsyncOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceAsyncOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec,
                                Mono<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

    @Override
    public boolean isCluster() {
        return false;
    }

}
