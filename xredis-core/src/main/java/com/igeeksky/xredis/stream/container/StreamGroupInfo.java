package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.flow.RetrySink;
import io.lettuce.core.Consumer;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;

/**
 * 流信息（消费者组）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGroupInfo<K, V> extends StreamInfo<K, V> {

    private final Consumer<K> consumer;

    /**
     * 构造器
     *
     * @param readArgs 读取参数
     * @param offset   读取偏移量
     * @param sink     数据池
     * @param consumer 组名及消费者名称
     */
    public StreamGroupInfo(XReadArgs readArgs, StreamOffset<K> offset,
                           RetrySink<StreamMessage<K, V>> sink, Consumer<K> consumer) {
        super(readArgs, offset, sink);
        this.consumer = consumer;
    }

    /**
     * 获取消费者组名称及消费者名称
     *
     * @return 消费者组名称及消费者名称
     */
    public Consumer<K> consumer() {
        return consumer;
    }

}
