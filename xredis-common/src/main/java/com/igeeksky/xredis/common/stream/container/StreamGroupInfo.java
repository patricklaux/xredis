package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.XGroupConsumer;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;

/**
 * 流信息（消费者组）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGroupInfo<K, V> extends StreamInfo<K, V> {

    private final XGroupConsumer<K> consumer;

    /**
     * 构造器
     *
     * @param options  读取选项
     * @param offset   读取偏移量
     * @param sink     数据池
     * @param consumer 消费者组名称及消费者名称
     */
    public StreamGroupInfo(XReadOptions options, XStreamOffset<K> offset,
                           RetrySink<XStreamMessage<K, V>> sink, XGroupConsumer<K> consumer) {
        super(options, offset, sink);
        this.consumer = consumer;
    }

    /**
     * 获取消费者组名称及消费者名称
     *
     * @return 消费者组名称及消费者名称
     */
    public XGroupConsumer<K> consumer() {
        return consumer;
    }

}
