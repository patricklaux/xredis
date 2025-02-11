package com.igeeksky.xredis.stream.container;

import com.igeeksky.xredis.TimeConvertor;
import com.igeeksky.xredis.api.Pipeline;
import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.flow.Disposable;
import com.igeeksky.xredis.flow.Flow;
import com.igeeksky.xredis.flow.RetryFlow;
import com.igeeksky.xredis.flow.RetrySink;
import com.igeeksky.xredis.stream.StreamCodec;
import com.igeeksky.xredis.stream.StreamPublisher;
import com.igeeksky.xredis.stream.XAddOptions;
import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.XReadArgs.StreamOffset;
import io.lettuce.core.api.AsyncCloseable;

import java.util.List;
import java.util.concurrent.*;

/**
 * 循环监听 Stream 消息（仅适用于非消费者组）
 * <p>
 * 使用公共的 XReadArgs 参数，将所有流合并到一个 xread 命令进行读取，以减少命令阻塞。
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGenericContainer<K, V> implements AsyncCloseable {

    private final StreamGenericTask<K, V> genericTask;

    private final int count;
    private final long interval;
    private final XReadArgs args;
    private final RedisOperator<K, V> operator;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    private volatile Future<?> genericPullFuture;
    private volatile ScheduledFuture<?> pullFuture;
    private volatile ScheduledFuture<?> genericFuture;

    /**
     * 创建一个新的 StreamContainer 实例
     *
     * @param operator  RedisOperator
     * @param options   拉取消息选项
     * @param interval  两次拉取消息任务的间隔时间，单位毫秒（必须大于 0）
     * @param executor  虚拟线程执行器
     * @param scheduler 定时任务调度器
     */
    public StreamGenericContainer(RedisOperator<K, V> operator, long interval, XReadOptions options,
                                  ExecutorService executor, ScheduledExecutorService scheduler) {
        Assert.notNull(operator, "operator must not be null");
        Assert.notNull(executor, "executor must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.isTrue(interval > 0, "interval must be greater than 0");
        this.args = options.to();
        this.count = options.count();
        this.interval = interval;
        this.operator = operator;
        this.executor = executor;
        this.scheduler = scheduler;
        this.genericTask = new StreamGenericTask<>(operator, options);
        this.start();
    }

    private void start() {
        this.pullFuture = this.scheduler.scheduleWithFixedDelay(() -> {
            if (genericPullFuture == null || genericPullFuture.isDone()) {
                genericPullFuture = this.executor.submit(this.genericTask::pull);
            }
        }, this.interval, this.interval, TimeUnit.MILLISECONDS);
        long delay = Math.max(1, this.interval / 2);
        this.genericFuture = this.scheduler.scheduleWithFixedDelay(this.genericTask, delay, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * 创建一个 StreamMessagePublisher 对象，用于发布消息到指定的流。
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
     * <p>
     * 注意：<br>
     * StreamGenericContainer 读取消息时是将多个 Stream 合并为一个 xread 命令进行拉取消息，
     * 返回的消息列表无法分辨同一个流的不同订阅者，因此不支持多次订阅同一个流。<br>
     * 如果已经订阅，再次订阅之前需先调用 {@link Flow#cancel()}（或调用 {@link Disposable#dispose()}}），否则会抛出异常。
     *
     * @param offset 偏移量
     */
    public Flow<StreamMessage<K, V>> subscribe(StreamOffset<K> offset) {
        Assert.notNull(offset, "offset must not be null");
        RetrySink<StreamMessage<K, V>> sink = new RetrySink<>(executor, count);
        this.genericTask.add(new StreamInfo<>(args, offset, sink));
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
        if (this.genericPullFuture != null) {
            this.genericPullFuture.cancel(false);
            this.genericPullFuture = null;
        }
        if (this.genericFuture != null) {
            this.genericFuture.cancel(false);
        }
        return this.operator.closeAsync();
    }

}