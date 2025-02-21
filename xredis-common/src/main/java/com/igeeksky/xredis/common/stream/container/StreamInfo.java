package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xtool.core.collection.CollectionUtils;

import java.util.List;

/**
 * 流相关信息
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamInfo<K, V> {

    private final XReadOptions options;

    private volatile XStreamOffset<K> offset;

    private final RetrySink<XStreamMessage<K, V>> sink;

    /**
     * 创建 StreamInfo
     *
     * @param options 读选项
     * @param offset  读偏移
     * @param sink    数据池
     */
    public StreamInfo(XReadOptions options, XStreamOffset<K> offset, RetrySink<XStreamMessage<K, V>> sink) {
        this.sink = sink;
        this.offset = offset;
        this.options = options;
    }

    /**
     * 获取读选项
     *
     * @return 读选项
     */
    public XReadOptions getOptions() {
        return options;
    }

    /**
     * 获取读偏移
     *
     * @return 读偏移
     */
    public XStreamOffset<K> getOffset() {
        return offset;
    }

    /**
     * 获取数据池
     *
     * @return 数据池
     */
    public RetrySink<XStreamMessage<K, V>> getSink() {
        return sink;
    }

    /**
     * 接收消息并更新读偏移
     *
     * @param messages 消息列表
     */
    public void receive(List<XStreamMessage<K, V>> messages) {
        if (sink.isCancelled()) {
            return;
        }
        if (CollectionUtils.isEmpty(messages)) {
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
            K key = this.offset.getKey();
            this.offset = XStreamOffset.from(key, id);
        }
    }

}
