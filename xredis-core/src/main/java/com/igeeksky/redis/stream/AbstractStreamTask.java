package com.igeeksky.redis.stream;

import com.igeeksky.redis.flow.RetrySink;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 流任务抽象类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public abstract class AbstractStreamTask<K, V> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AbstractStreamTask.class);

    protected final Queue<StreamInfo<K, V>> streams = new ConcurrentLinkedQueue<>();

    /**
     * 消费信息
     */
    @Override
    public void run() {
        try {
            Iterator<? extends StreamInfo<K, V>> iterator = streams.iterator();
            while (iterator.hasNext()) {
                StreamInfo<K, V> info = iterator.next();
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

    /**
     * 添加流
     *
     * @param info 流相关信息
     */
    public void add(StreamInfo<K, V> info) {
        streams.offer(info);
    }

    /**
     * 拉取流消息并分发给 {@link RetrySink}
     */
    public void pull() {
        if (streams.isEmpty()) {
            return;
        }

        try {
            this.doPull();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 拉取数据
     */
    protected abstract void doPull();

    /**
     * 分发数据
     *
     * @param future 拉取数据结果
     * @param info   流相关信息
     */
    protected void dispatch(StreamInfo<K, V> info, RedisFuture<List<StreamMessage<K, V>>> future) {
        future.toCompletableFuture()
                .thenAccept(messages -> {
                    RetrySink<StreamMessage<K, V>> sink = info.sink();
                    if (sink.isCancelled() || CollectionUtils.isEmpty(messages)) {
                        return;
                    }
                    String id = null;
                    for (StreamMessage<K, V> message : messages) {
                        if (sink.next(message)) {
                            id = message.getId();
                        } else {
                            break;
                        }
                    }
                    // 更新读偏移
                    if (id != null) {
                        info.offset(XReadArgs.StreamOffset.from(info.offset().getName(), id));
                    }
                })
                .exceptionally(t -> {
                    info.sink().error(t);
                    return null;
                })
                .join();
    }

}
