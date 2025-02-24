package com.igeeksky.xredis.common.stream;


import com.igeeksky.xredis.common.RedisOperationException;
import com.igeeksky.xtool.core.AsyncCloseable;

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
public class StreamPublisher<K, V, T> implements AsyncCloseable {

    private final K stream;
    private final XAddOptions options;
    private final StreamOperator<K, V> operator;
    private final StreamCodec<K, V, T> streamCodec;

    /**
     * 构造器
     *
     * @param stream      流名称
     * @param options     流消息添加选项
     * @param operator    流操作
     * @param streamCodec 流消息编解码器
     */
    public StreamPublisher(K stream, XAddOptions options, StreamOperator<K, V> operator, StreamCodec<K, V, T> streamCodec) {
        this.stream = stream;
        this.options = options;
        this.operator = operator;
        this.streamCodec = streamCodec;
    }

    /**
     * 发布消息（使用公共的 XAddArgs）
     *
     * @param message 消息
     * @return 消息ID
     */
    public CompletableFuture<String> publish(T message) {
        return this.publish(message, this.options);
    }

    /**
     * 发布消息（使用传入的 XAddArgs）
     *
     * @param message 流消息
     * @param options 流消息添加选项
     * @return 消息ID
     */
    public CompletableFuture<String> publish(T message, XAddOptions options) {
        if (message == null) {
            return CompletableFuture.failedFuture(new RedisOperationException("message must not be null."));
        }
        Map<K, V> body = streamCodec.encodeMsg(message);
        if (body == null) {
            return CompletableFuture.failedFuture(new RedisOperationException("message convert to body failed."));
        }
        if (options == null) {
            return operator.xadd(stream, body).toCompletableFuture();
        }
        return operator.xadd(stream, options, body).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return operator.closeAsync();
    }

}