package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.collection.Maps;
import com.igeeksky.xtool.core.tuple.Tuple1;
import com.igeeksky.xtool.core.tuple.Tuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
public class StreamContainerTask<K, V> implements StreamTask<K, V> {

    private static final Logger log = LoggerFactory.getLogger(StreamContainerTask.class);

    private final XReadOptions options;

    private final StreamOperator<K, V> operator;

    private final ConcurrentHashMap<Tuple1<K>, StreamInfo<K, V>> streams = new ConcurrentHashMap<>();

    /**
     * 构造器
     *
     * @param operator RedisOperator
     * @param options  流读取选项
     */
    public StreamContainerTask(StreamOperator<K, V> operator, XReadOptions options) {
        this.options = options;
        this.operator = operator;
    }

    @Override
    public void add(StreamInfo<K, V> info) {
        Tuple1<K> key = Tuples.of(info.getOffset().getKey());
        StreamInfo<K, V> current = streams.compute(key, (k, old) -> {
            if (old != null) {
                RetrySink<XStreamMessage<K, V>> sink = old.getSink();
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
        try {
            this.doPull();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    private void doPull() {
        if (streams.isEmpty()) {
            return;
        }
        List<XStreamOffset<K>> offsets = new ArrayList<>(streams.size());
        List<RetrySink<XStreamMessage<K, V>>> sinks = new ArrayList<>(streams.size());
        Iterator<Map.Entry<Tuple1<K>, StreamInfo<K, V>>> iterator = streams.entrySet().iterator();
        while (iterator.hasNext()) {
            StreamInfo<K, V> info = iterator.next().getValue();
            RetrySink<XStreamMessage<K, V>> sink = info.getSink();
            if (sink.isCancelled()) {
                iterator.remove();
                continue;
            }
            if (sink.isNotReady() || sink.isNotEnoughSpace()) {
                continue;
            }
            sinks.add(sink);
            offsets.add(info.getOffset());
        }
        // 拉取消息 & 分发消息
        this.dispatch(sinks, this.xread(offsets));
    }

    /**
     * 分发消息
     *
     * @param sinks  数据池列表
     * @param future 消息
     */
    private void dispatch(List<RetrySink<XStreamMessage<K, V>>> sinks,
                          CompletableFuture<List<XStreamMessage<K, V>>> future) {
        future.thenApply(this::merge)
                .thenAccept(map -> map.forEach(this::push))
                .exceptionally(t -> {
                    sinks.forEach(sink -> {
                        if (!sink.isCancelled()) {
                            sink.error(t);
                        }
                    });
                    return null;
                })
                .join();
    }

    /**
     * 合并消息
     *
     * @param messages 消息
     * @return {@code Map<Tuple1<K>, List<StreamMessage<K, V>>>} – key: 流 value: 消息列表
     */
    private Map<Tuple1<K>, List<XStreamMessage<K, V>>> merge(List<XStreamMessage<K, V>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyMap();
        }
        Map<Tuple1<K>, List<XStreamMessage<K, V>>> map = Maps.newHashMap(streams.size());
        for (XStreamMessage<K, V> message : messages) {
            if (message != null) {
                Tuple1<K> key = Tuples.of(message.stream());
                map.computeIfAbsent(key, k -> new ArrayList<>()).add(message);
            }
        }
        return map;
    }

    /**
     * 推送消息
     *
     * @param key      流
     * @param messages 消息
     */
    private void push(Tuple1<K> key, List<XStreamMessage<K, V>> messages) {
        try {
            // 获取流信息
            StreamInfo<K, V> info = streams.get(key);
            if (info == null) {
                return;
            }
            // 获取数据池，如果已取消，则从订阅列表中移除
            RetrySink<XStreamMessage<K, V>> sink = info.getSink();
            if (sink.isCancelled()) {
                streams.remove(key);
                return;
            }
            // 分发数据
            info.receive(messages);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void consume() {
        try {
            Iterator<Map.Entry<Tuple1<K>, StreamInfo<K, V>>> iterator = streams.entrySet().iterator();
            while (iterator.hasNext()) {
                StreamInfo<K, V> info = iterator.next().getValue();
                RetrySink<XStreamMessage<K, V>> sink = info.getSink();
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

    @SuppressWarnings("unchecked")
    private CompletableFuture<List<XStreamMessage<K, V>>> xread(List<XStreamOffset<K>> offsets) {
        // 无可用流
        if (CollectionUtils.isEmpty(offsets)) {
            return CompletableFuture.completedFuture(null);
        }
        return operator.xread(options, offsets.toArray(new XStreamOffset[0]));
    }

}
