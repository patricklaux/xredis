package com.igeeksky.redis.stream;

import java.util.Map;

/**
 * Redis 流消息编解码器
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/8/30
 */
public interface StreamCodec<K, V, T> {

    /**
     * 编码 Redis 流的消息
     *
     * @param message 消息
     * @return 编码后的消息
     */
    Map<K, V> encodeMsg(T message);

    /**
     * 解码 Redis 流的消息
     *
     * @param body 编码后的消息
     * @return 解码后的消息
     */
    T decodeMsg(Map<K, V> body);

}
