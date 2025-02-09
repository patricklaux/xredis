package com.igeeksky.redis.stream;

import com.igeeksky.redis.flow.RetrySink;
import io.lettuce.core.Consumer;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGroupInfo<K, V> extends StreamInfo<K, V> {

    private final Consumer<K> consumer;

    public StreamGroupInfo(XReadArgs readArgs, StreamOffset<K> offset,
                           RetrySink<StreamMessage<K, V>> sink, Consumer<K> consumer) {
        super(readArgs, offset, sink);
        this.consumer = consumer;
    }

    public Consumer<K> consumer() {
        return consumer;
    }

}
