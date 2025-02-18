package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 阻塞读取流任务
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamBlockingTask<K, V> extends AbstractStreamTask<K, V> {

    private final StreamOperator<K, V> operator;

    /**
     * 构造函数
     *
     * @param operator RedisOperator
     */
    public StreamBlockingTask(StreamOperator<K, V> operator) {
        super();
        this.operator = operator;
    }

    protected void doPull() {
        Iterator<? extends StreamInfo<K, V>> iterator = streams.iterator();
        while (iterator.hasNext()) {
            StreamInfo<K, V> info = iterator.next();
            RetrySink<XStreamMessage<K, V>> sink = info.sink();
            if (sink.isCancelled()) {
                iterator.remove();
                continue;
            }
            if (sink.isNotReady() || sink.isNotEnoughSpace()) {
                continue;
            }
            if (info instanceof StreamGroupInfo<K, V> groupInfo) {
                super.dispatch(info, this.xreadgroup(groupInfo));
            } else {
                super.dispatch(info, this.xread(info.options(), info.offset()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<List<XStreamMessage<K, V>>> xread(XReadOptions options, XStreamOffset<K> offset) {
        return this.operator.xread(options, offset);
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(StreamGroupInfo<K, V> info) {
        return this.operator.xreadgroup(info.consumer(), info.options(), info.offset());
    }

}
