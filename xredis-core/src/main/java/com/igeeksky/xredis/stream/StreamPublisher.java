package com.igeeksky.xredis.stream;


import com.igeeksky.xredis.api.RedisAsyncOperator;
import com.igeeksky.xredis.common.RedisOperationException;
import io.lettuce.core.XAddArgs;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 流消息发布者
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @param <T> 原消息类型
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-12
 */
public class StreamPublisher<K, V, T> {

    private final K stream;
    private final XAddArgs args;
    private final StreamCodec<K, V, T> codec;
    private final RedisAsyncOperator<K, V> async;

    /**
     * 构造器
     *
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
     * 发布消息（使用公共的 XAddArgs）
     *
     * @param message 消息
     * @return 消息ID
     */
    public CompletableFuture<String> publish(T message) {
        return this.publish(message, this.args);
    }

    /**
     * 发布消息（使用传入的 XAddArgs）
     *
     * @param message 消息
     * @param args    添加选项
     * @return 消息ID
     */
    public CompletableFuture<String> publish(T message, XAddArgs args) {
        if (message == null) {
            return CompletableFuture.failedFuture(new RedisOperationException("message must not be null."));
        }
        Map<K, V> body = codec.encodeMsg(message);
        if (body == null) {
            return CompletableFuture.failedFuture(new RedisOperationException("message convert to body failed."));
        }
        if (args == null) {
            return async.xadd(stream, body).toCompletableFuture();
        }
        return async.xadd(stream, args, body).toCompletableFuture();
    }

}