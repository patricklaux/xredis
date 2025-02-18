package com.igeeksky.xredis.common.stream;


import com.igeeksky.xredis.common.RedisOperationException;

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
    private final XAddOptions options;
    private final StreamCodec<K, V, T> codec;
    private final StreamOperator<K, V> operator;

    /**
     * 构造器
     *
     * @param stream   流名称
     * @param options  流消息添加选项
     * @param codec    流消息编解码器
     * @param operator Redis 管道（批量命令提交）
     */
    public StreamPublisher(K stream, XAddOptions options, StreamCodec<K, V, T> codec,
                           StreamOperator<K, V> operator) {
        this.codec = codec;
        this.operator = operator;
        this.stream = stream;
        this.options = options;
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
        Map<K, V> body = codec.encodeMsg(message);
        if (body == null) {
            return CompletableFuture.failedFuture(new RedisOperationException("message convert to body failed."));
        }
        if (options == null) {
            return operator.xadd(stream, body).toCompletableFuture();
        }
        return operator.xadd(stream, options, body).toCompletableFuture();
    }

}