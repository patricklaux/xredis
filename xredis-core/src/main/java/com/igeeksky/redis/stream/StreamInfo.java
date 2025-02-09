package com.igeeksky.redis.stream;

import com.igeeksky.redis.flow.RetrySink;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;

/**
 * 流相关信息
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamInfo<K, V> {

    private final XReadArgs readArgs;

    private volatile StreamOffset<K> offset;

    private final RetrySink<StreamMessage<K, V>> sink;

    public StreamInfo(XReadArgs readArgs, StreamOffset<K> offset, RetrySink<StreamMessage<K, V>> sink) {
        this.sink = sink;
        this.offset = offset;
        this.readArgs = readArgs;
    }

    public XReadArgs readArgs() {
        return readArgs;
    }

    public StreamOffset<K> offset() {
        return offset;
    }

    public RetrySink<StreamMessage<K, V>> sink() {
        return sink;
    }

    public StreamInfo<K, V> offset(StreamOffset<K> offset) {
        this.offset = offset;
        return this;
    }

}
