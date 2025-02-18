package com.igeeksky.xredis.common.stream;

import java.util.Map;

/**
 * Redis 流消息
 *
 * @param stream  Stream 名称
 * @param id   Stream 元素的 ID
 * @param body Stream 元素的 body，一个元素可以包含多个 field 和 value
 * @author Patrick.Lau
 * @see <a href="https://redis.io/docs/reference/protocol-spec/#streams">Streams</a>
 * @since 1.0.0 2024/7/20
 */
public record XStreamMessage<K, V>(K stream, String id, Map<K, V> body) {

}