package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xtool.core.concurrent.Futures;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 流任务抽象类
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGenericTask<K, V> implements StreamTask<K, V> {

    private static final Logger log = LoggerFactory.getLogger(StreamGenericTask.class);

    private final StreamOperator<K, V> operator;

    private final Lock pullLock = new ReentrantLock();

    private final ArrayList<Future<?>> dispatchFutures = new ArrayList<>();

    /**
     * 流信息列表
     */
    protected final Queue<StreamInfo<K, V>> streams = new ConcurrentLinkedQueue<>();

    /**
     * 构造函数
     *
     * @param operator 流操作
     */
    public StreamGenericTask(StreamOperator<K, V> operator) {
        this.operator = operator;
    }

    @Override
    public void add(StreamInfo<K, V> info) {
        streams.offer(info);
    }

    @Override
    public void pull() {
        try {
            boolean locked = pullLock.tryLock();
            if (locked) {
                try {
                    this.doPull();
                } catch (Throwable e) {
                    log.error("Error occurred during doPull: {}", e.getMessage(), e);
                } finally {
                    pullLock.unlock();
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Failed to acquire lock for pull operation");
                }
            }
        } catch (Throwable ignore) {
        }
    }

    private void doPull() {
        // 需等待 StreamInfo 更新 offset：如果上次任务未完成，那么 offset 可能还未更新，会拉取到重复的消息
        int size = dispatchFutures.size();
        int last = Futures.checkAll(0, dispatchFutures);
        if (last < size) {
            return;
        }
        dispatchFutures.clear();
        if (streams.isEmpty()) {
            return;
        }
        Iterator<? extends StreamInfo<K, V>> iterator = streams.iterator();
        while (iterator.hasNext()) {
            StreamInfo<K, V> info = iterator.next();
            RetrySink<XStreamMessage<K, V>> sink = info.getSink();
            if (sink.isCancelled()) {
                iterator.remove();
                continue;
            }
            if (sink.isNotReady() || sink.isNotEnoughSpace() || sink.isPullPaused()) {
                continue;
            }
            // 对于无阻塞选项的情况，可以一次性提交所有拉取命令，然后再统一分发数据
            if (info instanceof StreamGroupInfo<K, V> groupInfo) {
                dispatchFutures.add(this.dispatch(info, this.xreadgroup(groupInfo)));
            } else {
                dispatchFutures.add(this.dispatch(info, this.xread(info)));
            }
        }
    }

    @Override
    public void consume() {
        try {
            Iterator<? extends StreamInfo<K, V>> iterator = streams.iterator();
            while (iterator.hasNext()) {
                StreamInfo<K, V> info = iterator.next();
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
    private CompletableFuture<List<XStreamMessage<K, V>>> xread(StreamInfo<K, V> info) {
        return this.operator.xreadAsync(info.getOptions(), info.getOffset());
    }

    @SuppressWarnings("unchecked")
    private CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(StreamGroupInfo<K, V> info) {
        return this.operator.xreadgroupAsync(info.getConsumer(), info.getOptions(), info.getOffset());
    }

    /**
     * 分发数据
     *
     * @param info   流相关信息
     * @param future 拉取的消息
     * @return {@code CompletableFuture<Void>}
     */
    private CompletableFuture<Void> dispatch(StreamInfo<K, V> info,
                                             CompletableFuture<List<XStreamMessage<K, V>>> future) {
        return future.thenAccept(info::receive)
                .exceptionally(t -> {
                    info.getSink().error(t);
                    return null;
                });
    }

}
