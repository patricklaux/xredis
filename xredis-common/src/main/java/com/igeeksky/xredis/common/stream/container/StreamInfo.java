package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;

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
     * 构造器
     *
     * @param options 读取选项
     * @param offset  读取偏移量
     * @param sink    数据池
     */
    public StreamInfo(XReadOptions options, XStreamOffset<K> offset, RetrySink<XStreamMessage<K, V>> sink) {
        this.sink = sink;
        this.offset = offset;
        this.options = options;
    }

    /**
     * 获取读取选项
     *
     * @return 读取选项
     */
    public XReadOptions options() {
        return options;
    }

    /**
     * 获取读取偏移量
     *
     * @return 读取偏移量
     */
    public XStreamOffset<K> offset() {
        return offset;
    }

    /**
     * 获取数据池
     *
     * @return 数据池
     */
    public RetrySink<XStreamMessage<K, V>> sink() {
        return sink;
    }

    /**
     * 设置读取偏移量
     *
     * @param offset 读取偏移量
     * @return 流信息
     */
    public StreamInfo<K, V> offset(XStreamOffset<K> offset) {
        this.offset = offset;
        return this;
    }

}
