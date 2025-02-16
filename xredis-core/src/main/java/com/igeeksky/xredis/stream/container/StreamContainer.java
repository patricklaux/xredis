package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.api.Pipeline;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.TimeConvertor;
import com.igeeksky.xredis.common.flow.Flow;
import com.igeeksky.xredis.common.flow.RetryFlow;
import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.stream.StreamCodec;
import com.igeeksky.xredis.stream.StreamPublisher;
import com.igeeksky.xredis.stream.XAddOptions;
import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.Consumer;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.AsyncCloseable;

import java.util.List;
import java.util.concurrent.*;

/**
 * 循环监听 Stream 消息
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamContainer<K, V> implements AsyncCloseable {

    private final StreamBlockingTask<K, V> blockingTask;
    private final StreamNonBlockingTask<K, V> nonBlockingTask;

    private final long interval;
    private final RedisOperator<K, V> operator;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private volatile Future<?> blockingPullFuture;
    private volatile Future<?> nonBlockingPullFuture;
    private volatile ScheduledFuture<?> pullFuture;
    private volatile ScheduledFuture<?> blockingFuture;
    private volatile ScheduledFuture<?> nonBlockingFuture;

    /**
     * 创建一个新的 StreamContainer 实例
     *
     * @param operator  RedisOperator
     * @param executor  虚拟线程执行器
     * @param scheduler 调度器
     * @param interval  两次拉取消息任务的间隔时间，单位毫秒（必须大于 0）
     */
    public StreamContainer(RedisOperator<K, V> operator, long interval, ExecutorService executor, ScheduledExecutorService scheduler) {
        Assert.notNull(operator, "operator must not be null");
        Assert.notNull(executor, "executor must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.isTrue(interval > 0, "interval must be greater than 0");
        this.interval = interval;
        this.operator = operator;
        this.executor = executor;
        this.scheduler = scheduler;
        this.blockingTask = new StreamBlockingTask<>(operator);
        this.nonBlockingTask = new StreamNonBlockingTask<>(operator);
        this.start();
    }

    private void start() {
        this.pullFuture = this.scheduler.scheduleWithFixedDelay(() -> {
            if (blockingPullFuture == null || blockingPullFuture.isDone()) {
                blockingPullFuture = this.executor.submit(this.blockingTask::pull);
            }
            if (nonBlockingPullFuture == null || nonBlockingPullFuture.isDone()) {
                nonBlockingPullFuture = this.executor.submit(this.nonBlockingTask::pull);
            }
        }, this.interval, this.interval, TimeUnit.MILLISECONDS);

        long delay = Math.max(1, this.interval / 2);
        this.blockingFuture = this.scheduler.scheduleWithFixedDelay(this.blockingTask, delay, delay, TimeUnit.MILLISECONDS);
        this.nonBlockingFuture = this.scheduler.scheduleWithFixedDelay(this.nonBlockingTask, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建一个 StreamMessagePublisher 对象，用于发布消息到指定的流中。
     *
     * @param stream  流的名称
     * @param options 发布消息的选项
     * @param codec   消息编解码器
     * @param <T>     消息类型
     * @return {@link StreamPublisher}
     */
    public <T> StreamPublisher<K, V, T> publisher(K stream, XAddOptions options, StreamCodec<K, V, T> codec) {
        return new StreamPublisher<>(stream, options, codec, operator.async());
    }

    /**
     * 获取 Redis 服务器的当前时间戳，单位为毫秒。
     *
     * @param convertor 时间戳转换器
     * @return 服务器的当前时间戳
     */
    public CompletableFuture<Long> serverTimeMillis(TimeConvertor<V> convertor) {
        Pipeline<K, V> pipeline = this.operator.pipeline();
        RedisFuture<List<V>> time = pipeline.time();
        pipeline.flushCommands();
        return time.toCompletableFuture().thenApply(convertor::milliseconds);
    }

    /**
     * 订阅流（非 group）
     *
     * @param offset  偏移量
     * @param options 读选项
     * @return {@link Flow} 数据流
     */
    public Flow<StreamMessage<K, V>> subscribe(StreamOffset<K> offset, XReadOptions options) {
        Assert.notNull(offset, "offset must not be null");
        Assert.notNull(options, "options must not be null");
        RetrySink<StreamMessage<K, V>> sink = new RetrySink<>(executor, options.count());
        if (options.block() >= 0) {
            this.blockingTask.add(new StreamInfo<>(options.to(), offset, sink));
        } else {
            this.nonBlockingTask.add(new StreamInfo<>(options.to(), offset, sink));
        }
        return new RetryFlow<>(sink);
    }

    /**
     * 订阅流（group）
     *
     * @param offset   偏移量
     * @param options  读选项
     * @param consumer 消费者
     * @return {@link Flow} 数据流
     */
    public Flow<StreamMessage<K, V>> subscribe(StreamOffset<K> offset, XReadOptions options, Consumer<K> consumer) {
        Assert.notNull(offset, "offset must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(consumer, "consumer must not be null");
        RetrySink<StreamMessage<K, V>> sink = new RetrySink<>(executor, options.count());
        if (options.block() >= 0) {
            this.blockingTask.add(new StreamGroupInfo<>(options.to(), offset, sink, consumer));
        } else {
            this.nonBlockingTask.add(new StreamGroupInfo<>(options.to(), offset, sink, consumer));
        }
        return new RetryFlow<>(sink);
    }

    /**
     * 关闭 StreamContainer 对象
     * <p>
     * 1. 停止拉取消息和消费消息（如有 Listener 已在处理消息，则该 Listener 将继续处理直到完成）；<br>
     * 2. 清空流列表和消费者列表；<br>
     * 3. 关闭 Redis 连接。<br>
     *
     * @return {@link CompletableFuture} – 关闭操作的异步通知。
     */
    @Override
    public CompletableFuture<Void> closeAsync() {
        if (this.pullFuture != null) {
            this.pullFuture.cancel(false);
        }
        if (this.blockingPullFuture != null) {
            this.blockingPullFuture.cancel(false);
            this.blockingPullFuture = null;
        }
        if (this.nonBlockingPullFuture != null) {
            this.nonBlockingPullFuture.cancel(false);
        }
        if (this.blockingFuture != null) {
            this.blockingFuture.cancel(false);
        }
        if (this.nonBlockingFuture != null) {
            this.nonBlockingFuture.cancel(false);
        }
        return this.operator.closeAsync();
    }

}
