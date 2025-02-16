package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.api.Pipeline;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.lang.ArrayUtils;
import com.igeeksky.xtool.core.tuple.Tuple1;
import com.igeeksky.xtool.core.tuple.Tuple2;
import com.igeeksky.xtool.core.tuple.Tuples;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

    private final boolean block;

    private final XReadArgs readArgs;

    private final RedisOperator<K, V> operator;

    private final ConcurrentHashMap<Tuple1<K>, StreamInfo<K, V>> streams = new ConcurrentHashMap<>();

    /**
     * 构造器
     *
     * @param operator RedisOperator
     * @param options  流读取选项
     */
    public StreamGenericTask(RedisOperator<K, V> operator, XReadOptions options) {
        this.operator = operator;
        this.readArgs = options.to();
        this.block = options.block() >= 0;
    }

    @Override
    public void run() {
        try {
            Iterator<Map.Entry<Tuple1<K>, StreamInfo<K, V>>> iterator = streams.entrySet().iterator();
            while (iterator.hasNext()) {
                StreamInfo<K, V> info = iterator.next().getValue();

                RetrySink<StreamMessage<K, V>> sink = info.sink();
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
        Tuple1<K> key = Tuples.of(info.offset().getName());
        StreamInfo<K, V> current = streams.compute(key, (k, old) -> {
            if (old != null) {
                RetrySink<StreamMessage<K, V>> sink = old.sink();
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
            Tuple2<StreamOffset<K>[], List<RetrySink<StreamMessage<K, V>>>> tuple2 = getStreamOffsets();
            StreamOffset<K>[] offsets = tuple2.getT1();
            List<RetrySink<StreamMessage<K, V>>> sinks = tuple2.getT2();
            // 无可用流
            if (ArrayUtils.isEmpty(offsets)) {
                return;
            }
            // 拉取消息 & 分发消息
            if (block) {
                dispatch(sinks, this.xreadBlock(offsets));
            } else {
                dispatch(sinks, this.xread(offsets));
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private void dispatch(List<RetrySink<StreamMessage<K, V>>> sinks, RedisFuture<List<StreamMessage<K, V>>> future) {
        future.toCompletableFuture()
                .thenApply(messages -> {
                    // 根据流名称将消息归并
                    Map<Tuple1<K>, List<StreamMessage<K, V>>> map = Maps.newHashMap(streams.size());
                    if (CollectionUtils.isNotEmpty(messages)) {
                        for (StreamMessage<K, V> message : messages) {
                            if (message == null) {
                                continue;
                            }
                            map.computeIfAbsent(Tuples.of(message.getStream()), k -> new ArrayList<>()).add(message);
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
    private void sendToSink(Tuple1<K> key, List<StreamMessage<K, V>> messages) {
        try {
            StreamInfo<K, V> info = streams.get(key);
            if (info == null) {
                return;
            }
            RetrySink<StreamMessage<K, V>> sink = info.sink();
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
    @SuppressWarnings("unchecked")
    private Tuple2<StreamOffset<K>[], List<RetrySink<StreamMessage<K, V>>>> getStreamOffsets() {
        List<StreamOffset<K>> offsets = new ArrayList<>(streams.size());
        List<RetrySink<StreamMessage<K, V>>> sinks = new ArrayList<>(streams.size());

        Iterator<Map.Entry<Tuple1<K>, StreamInfo<K, V>>> iterator = streams.entrySet().iterator();
        while (iterator.hasNext()) {
            StreamInfo<K, V> info = iterator.next().getValue();
            RetrySink<StreamMessage<K, V>> sink = info.sink();
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

        return Tuples.of(offsets.toArray(new StreamOffset[0]), sinks);
    }

    private RedisFuture<List<StreamMessage<K, V>>> xread(StreamOffset<K>[] offsets) {
        Pipeline<K, V> pipeline = this.operator.pipeline();
        RedisFuture<List<StreamMessage<K, V>>> future = pipeline.xread(readArgs, offsets);
        pipeline.flushCommands();
        return future;
    }

    private RedisFuture<List<StreamMessage<K, V>>> xreadBlock(StreamOffset<K>[] offsets) {
        return this.operator.async().xread(readArgs, offsets);
    }

}
