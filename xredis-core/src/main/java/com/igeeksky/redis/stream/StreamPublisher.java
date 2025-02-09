package com.igeeksky.redis.stream;


import com.igeeksky.redis.api.RedisAsyncOperator;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.XAddArgs;

import java.util.Map;

/**
 * 流消息发布者
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class StreamPublisher<K, V, T> {

    private final K stream;
    private final XAddArgs args;
    private final StreamCodec<K, V, T> codec;
    private final RedisAsyncOperator<K, V> async;

    /**
     * @param stream  流名称
     * @param options 流消息添加选项
     * @param codec   流消息编解码器
     * @param async   Redis 管道（批量命令提交）
     */
    public StreamPublisher(K stream, XAddOptions options, StreamCodec<K, V, T> codec, RedisAsyncOperator<K, V> async) {
        this.codec = codec;
        this.async = async;
        this.stream = stream;
        this.args = StreamConvertor.to(options);
    }

    /**
     * 发布消息（使用内部的 XAddArgs）
     *
     * @param message 消息
     * @return 消息ID
     */
    public RedisFuture<String> publish(T message) {
        return this.publish(message, this.args);
    }

    /**
     * 发布消息（使用传入的 XAddArgs）
     *
     * @param message 消息
     * @param args    添加选项
     * @return 消息ID
     */
    public RedisFuture<String> publish(T message, XAddArgs args) {
        Map<K, V> body = codec.encodeMsg(message);
        return (args == null) ? async.xadd(stream, body) : async.xadd(stream, args, body);
    }

}