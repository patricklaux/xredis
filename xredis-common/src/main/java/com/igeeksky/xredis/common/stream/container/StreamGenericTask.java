package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.tuple.Tuple1;
import com.igeeksky.xtool.core.tuple.Tuple2;
import com.igeeksky.xtool.core.tuple.Tuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 流处理任务
 * <p>
 * 仅适用于非消费者组的情形，使用公共的 XReadArgs 参数：<br>
 * xread 命令带有 block 选项，当 block 大于等于 0 时，xread 命令会阻塞，直到有新消息或超过堵塞时长。<br>
 * 为了避免读取多个流时频繁堵塞，导致无法及时读取消息，
 * 这里采用公共参数，将多个流合并到一个 xread 命令进行读取，以减少网络请求次数及可能的堵塞时长。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGenericTask<K, V> implements StreamTask<K, V> {

    private static final Logger log = LoggerFactory.getLogger(StreamGenericTask.class);

    private final XReadOptions options;

    private final StreamOperator<K, V> operator;

    private final ConcurrentHashMap<Tuple1<K>, StreamInfo<K, V>> streams = new ConcurrentHashMap<>();

    /**
     * 构造器
     *
     * @param operator RedisOperator
     * @param options  流读取选项
     */
    public StreamGenericTask(StreamOperator<K, V> operator, XReadOptions options) {
        this.options = options;
        this.operator = operator;
    }

    @Override
    public void run() {
        try {
            Iterator<Map.Entry<Tuple1<K>, StreamInfo<K, V>>> iterator = streams.entrySet().iterator();
            while (iterator.hasNext()) {
                StreamInfo<K, V> info = iterator.next().getValue();

                RetrySink<XStreamMessage<K, V>> sink = info.sink();
                if (sink.isCancelled()) {
                    iterator.remove();
                    continue;
                }

                sink.run();
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void add(StreamInfo<K, V> info) {
        Tuple1<K> key = Tuples.of(info.offset().getKey());
        StreamInfo<K, V> current = streams.compute(key, (k, old) -> {
            if (old != null) {
                RetrySink<XStreamMessage<K, V>> sink = old.sink();
                if (!sink.isCancelled()) {
                    return old;
                }
            }
            return info;
        });
        if (info != current) {
            throw new IllegalStateException("The stream already exists");
        }
    }

    @Override
    public void pull() {
        if (streams.isEmpty()) {
            return;
        }
        try {
            Tuple2<XStreamOffset<K>[], List<RetrySink<XStreamMessage<K, V>>>> tuple2 = getStreamOffsets();
            XStreamOffset<K>[] offsets = tuple2.getT1();
            List<RetrySink<XStreamMessage<K, V>>> sinks = tuple2.getT2();
            // 无可用流
            if (ArrayUtils.isEmpty(offsets)) {
                return;
            }
            // 拉取消息 & 分发消息
            dispatch(sinks, this.xread(offsets));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private void dispatch(List<RetrySink<XStreamMessage<K, V>>> sinks, CompletableFuture<List<XStreamMessage<K, V>>> future) {
        future.toCompletableFuture()
                .thenApply(messages -> {
                    // 根据流名称将消息归并
                    Map<Tuple1<K>, List<XStreamMessage<K, V>>> map = Maps.newHashMap(streams.size());
                    if (CollectionUtils.isNotEmpty(messages)) {
                        for (XStreamMessage<K, V> message : messages) {
                            if (message == null) {
                                continue;
                            }
                            map.computeIfAbsent(Tuples.of(message.stream()), k -> new ArrayList<>()).add(message);
                        }
                    }
                    return map;
                })
                .thenAccept(map -> map.forEach(this::sendToSink))
                .exceptionally(t -> {
                    sinks.forEach(sink -> sink.error(t));
                    return null;
                })
                .join();
    }

    /**
     * 根据流名称分发消息
     *
     * @param key      流名称
     * @param messages 消息列表
     */
    private void sendToSink(Tuple1<K> key, List<XStreamMessage<K, V>> messages) {
        try {
            StreamInfo<K, V> info = streams.get(key);
            if (info == null) {
                return;
            }
            RetrySink<XStreamMessage<K, V>> sink = info.sink();
            if (sink.isCancelled()) {
                streams.remove(key);
                return;
            }
            sendToSink(info, messages);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 获取流读偏移信息和消费者
     *
     * @return {@code Tuple2<StreamOffset<K>[], List<RetrySink<StreamMessage<K, V>>>>}
     * key: 流信息 value: 消费者
     */
    private Tuple2<XStreamOffset<K>[], List<RetrySink<XStreamMessage<K, V>>>> getStreamOffsets() {
        List<XStreamOffset<K>> offsets = new ArrayList<>(streams.size());
        List<RetrySink<XStreamMessage<K, V>>> sinks = new ArrayList<>(streams.size());

        Iterator<Map.Entry<Tuple1<K>, StreamInfo<K, V>>> iterator = streams.entrySet().iterator();
        while (iterator.hasNext()) {
            StreamInfo<K, V> info = iterator.next().getValue();
            RetrySink<XStreamMessage<K, V>> sink = info.sink();
            if (sink.isCancelled()) {
                iterator.remove();
                continue;
            }
            if (sink.isNotReady() || sink.isNotEnoughSpace()) {
                continue;
            }
            sinks.add(sink);
            offsets.add(info.offset());
        }

        @SuppressWarnings("unchecked")
        XStreamOffset<K>[] offsetsArray = offsets.toArray(new XStreamOffset[0]);
        return Tuples.of(offsetsArray, sinks);
    }

    private CompletableFuture<List<XStreamMessage<K, V>>> xread(XStreamOffset<K>[] offsets) {
        return operator.xread(options, offsets);
    }

}
