package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.flow.RetrySink;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;

import java.util.List;

/**
 * 流消息拉取及消费任务
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface StreamTask<K, V> extends Runnable {

    /**
     * 消费消息
     */
    void run();

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
     * 将拉取到的消息发送到 Sink 缓存
     *
     * @param info     流信息
     * @param messages 消息列表
     */
    default void sendToSink(StreamInfo<K, V> info, List<StreamMessage<K, V>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        RetrySink<StreamMessage<K, V>> sink = info.sink();
        if (sink.isCancelled()) {
            return;
        }
        String id = null;
        for (StreamMessage<K, V> message : messages) {
            if (sink.next(message)) {
                id = message.getId();
            } else {
                break;
            }
        }
        // 更新读偏移
        if (id != null) {
            info.offset(XReadArgs.StreamOffset.from(info.offset().getName(), id));
        }
    }

}
