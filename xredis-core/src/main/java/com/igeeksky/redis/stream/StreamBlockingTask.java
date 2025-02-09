package com.igeeksky.redis.stream;

import com.igeeksky.redis.api.RedisOperator;
import com.igeeksky.redis.flow.RetrySink;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;

import java.util.Iterator;
import java.util.List;

/**
 * 阻塞读取流任务
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamBlockingTask<K, V> extends AbstractStreamTask<K, V> {

    private final RedisOperator<K, V> operator;

    /**
     * 构造函数
     *
     * @param operator RedisOperator
     */
    public StreamBlockingTask(RedisOperator<K, V> operator) {
        super();
        this.operator = operator;
    }

    protected void doPull() {
        Iterator<? extends StreamInfo<K, V>> iterator = streams.iterator();
        while (iterator.hasNext()) {
            StreamInfo<K, V> info = iterator.next();
            RetrySink<StreamMessage<K, V>> sink = info.sink();
            if (sink.isCancelled()) {
                iterator.remove();
                continue;
            }
            if (sink.isNotReady() || sink.isNotEnoughSpace()) {
                continue;
            }
            if (info instanceof StreamGroupInfo<K, V> groupInfo) {
                dispatch(info, this.xreadgroup(groupInfo));
            } else {
                dispatch(info, this.xread(info.readArgs(), info.offset()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private RedisFuture<List<StreamMessage<K, V>>> xread(XReadArgs args, StreamOffset<K> offset) {
        return this.operator.async().xread(args, offset);
    }

    @SuppressWarnings("unchecked")
    private RedisFuture<List<StreamMessage<K, V>>> xreadgroup(StreamGroupInfo<K, V> info) {
        return this.operator.async().xreadgroup(info.consumer(), info.readArgs(), info.offset());
    }

}
