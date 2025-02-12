package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.flow.RetrySink;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;

/**
 * 流相关信息
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamInfo<K, V> {

    private final XReadArgs readArgs;

    private volatile StreamOffset<K> offset;

    private final RetrySink<StreamMessage<K, V>> sink;

    /**
     * 构造器
     *
     * @param readArgs 读取参数
     * @param offset   读取偏移量
     * @param sink     数据池
     */
    public StreamInfo(XReadArgs readArgs, StreamOffset<K> offset, RetrySink<StreamMessage<K, V>> sink) {
        this.sink = sink;
        this.offset = offset;
        this.readArgs = readArgs;
    }

    /**
     * 获取读取参数
     *
     * @return 读取参数
     */
    public XReadArgs readArgs() {
        return readArgs;
    }

    /**
     * 获取读取偏移量
     *
     * @return 读取偏移量
     */
    public StreamOffset<K> offset() {
        return offset;
    }

    /**
     * 获取数据池
     *
     * @return 数据池
     */
    public RetrySink<StreamMessage<K, V>> sink() {
        return sink;
    }

    /**
     * 设置读取偏移量
     *
     * @param offset 读取偏移量
     * @return 流信息
     */
    public StreamInfo<K, V> offset(StreamOffset<K> offset) {
        this.offset = offset;
        return this;
    }

}
