package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xtool.core.tuple.Tuple2;
import com.igeeksky.xtool.core.tuple.Tuples;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 无阻塞读取流任务
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamNonBlockingTask<K, V> extends AbstractStreamTask<K, V> {

    private final RedisOperator<K, V> operator;

    /**
     * 无阻塞读取流任务
     *
     * @param operator RedisOperator
     */
    public StreamNonBlockingTask(RedisOperator<K, V> operator) {
        super();
        this.operator = operator;
    }

    protected void doPull() {
        List<Tuple2<StreamInfo<K, V>, RedisFuture<List<StreamMessage<K, V>>>>> tuples = new ArrayList<>(streams.size());
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
                tuples.add(Tuples.of(info, this.xreadgroup(groupInfo)));
            } else {
                tuples.add(Tuples.of(info, this.xread(info)));
            }
        }

        this.operator.pipeline().flushCommands();
        tuples.forEach(tuple -> super.dispatch(tuple.getT1(), tuple.getT2()));
    }

    @SuppressWarnings("unchecked")
    private RedisFuture<List<StreamMessage<K, V>>> xread(StreamInfo<K, V> info) {
        return this.operator.pipeline().xread(info.readArgs(), info.offset());
    }

    @SuppressWarnings("unchecked")
    private RedisFuture<List<StreamMessage<K, V>>> xreadgroup(StreamGroupInfo<K, V> info) {
        return this.operator.pipeline().xreadgroup(info.consumer(), info.readArgs(), info.offset());
    }

}
