package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xtool.core.tuple.Tuple2;
import com.igeeksky.xtool.core.tuple.Tuples;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 无阻塞读取流任务
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamNonBlockingTask<K, V> extends AbstractStreamTask<K, V> {

    private final StreamOperator<K, V> operator;

    /**
     * 无阻塞读取流任务
     *
     * @param operator RedisOperator
     */
    public StreamNonBlockingTask(StreamOperator<K, V> operator) {
        super();
        this.operator = operator;
    }

    protected void doPull() {
        List<Tuple2<StreamInfo<K, V>, CompletableFuture<List<XStreamMessage<K, V>>>>> tuples = new ArrayList<>(streams.size());
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
                tuples.add(Tuples.of(info, this.xreadgroup(groupInfo)));
            } else {
                tuples.add(Tuples.of(info, this.xread(info)));
            }
        }
        tuples.forEach(tuple -> super.dispatch(tuple.getT1(), tuple.getT2()));
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<List<XStreamMessage<K, V>>> xread(StreamInfo<K, V> info) {
        return this.operator.xread(info.options(), info.offset());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(StreamGroupInfo<K, V> info) {
        return this.operator.xreadgroup(info.consumer(), info.options(), info.offset());
    }

}
