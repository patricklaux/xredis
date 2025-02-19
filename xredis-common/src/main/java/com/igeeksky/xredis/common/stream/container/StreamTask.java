package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xtool.core.collection.CollectionUtils;

import java.util.List;

/**
 * 流消息拉取及消费任务
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface StreamTask<K, V> {

    /**
     * 添加流
     *
     * @param info 流相关信息
     */
    void add(StreamInfo<K, V> info);

    /**
     * 拉取消息
     */
    void pull();

    /**
     * 将拉取的消息推送给 Sink
     *
     * @param info     流信息
     * @param messages 消息列表
     */
    default void push(StreamInfo<K, V> info, List<XStreamMessage<K, V>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        RetrySink<XStreamMessage<K, V>> sink = info.sink();
        if (sink.isCancelled()) {
            return;
        }
        String id = null;
        for (XStreamMessage<K, V> message : messages) {
            if (sink.next(message)) {
                id = message.id();
            } else {
                break;
            }
        }
        // 更新读偏移
        if (id != null) {
            K key = info.offset().getKey();
            info.offset(XStreamOffset.from(key, id));
        }
    }

    /**
     * 消费消息
     */
    void consume();

}
