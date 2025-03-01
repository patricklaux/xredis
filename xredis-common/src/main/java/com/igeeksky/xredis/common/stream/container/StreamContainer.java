package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.*;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 循环监听 Stream 消息（仅适用于非消费者组）
 * <p>
 * 使用公共的 {@link ReadOptions} 参数，将所有流合并到一个 {@code xread} 命令读取。
 * <p>
 * 对于非消费者组的流消息读取，{@code xread} 支持一次命令读取多个流的消息，
 * 因此特别创建此类，以最大限度地减少命令阻塞和多次发送命令带来地网络时延。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamContainer<K, V> extends AbstractStreamContainer<K, V> {

    private final StreamContainerTask<K, V> streamTask;

    private final int count;
    private final long interval;
    private final XReadOptions options;

    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    /**
     * 创建一个新的 StreamContainer 实例
     *
     * @param operator    RedisOperator
     * @param executor    虚拟线程执行器（不能为空）
     * @param scheduler   定时任务调度器（不能为空）
     * @param quietPeriod quietPeriod 优雅关闭（等待正在运行的任务完成，单位毫秒）
     * @param timeout     timeout 优雅关闭（最大等待时间，单位毫秒）
     * @param interval    两次拉取消息的时间间隔，单位毫秒 {@code interval > 0}
     * @param options     读选项（不能为空）
     */
    public StreamContainer(StreamOperator<K, V> operator, ExecutorService executor,
                           ScheduledExecutorService scheduler, long quietPeriod, long timeout,
                           long interval, ReadOptions options) {
        super(operator, quietPeriod, timeout);
        Assert.notNull(operator, "operator must not be null");
        Assert.notNull(executor, "executor must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.isTrue(interval > 0, "interval must be greater than 0");

        this.count = options.count();
        this.options = options.to();
        this.interval = interval;
        this.executor = executor;
        this.scheduler = scheduler;
        this.streamTask = new StreamContainerTask<>(operator, this.options);
        this.start();
    }

    private void start() {
        this.schedulePullFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::pull,
                this.interval, this.interval, TimeUnit.MILLISECONDS);
        this.scheduleConsumeFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::consume,
                this.interval, Math.max(1, this.interval / 2), TimeUnit.MILLISECONDS);
    }

    /**
     * 订阅流（非 group，所有流读取采用公共的相同的 {@link ReadOptions}）
     * <p>
     * 注意：<br>
     * StreamContainer 拉取消息是将多个 Stream 合并为一个 xread 命令处理，不支持重复订阅同一个流。<br>
     * 如果已经订阅，再次订阅之前需先调用 {@link Flow#cancel()}（或调用 {@link Disposable#dispose()}}）
     * 取消原订阅，否则会抛出异常。
     *
     * @param offset 读偏移（不能为空）
     * @return {@link Flow} – 无限数据流（订阅之后除非取消，否则将一直拉取流消息并推送给 {@link Subscriber}）
     * @since 1.0.0
     */
    public Flow<XStreamMessage<K, V>> subscribe(XStreamOffset<K> offset) {
        Assert.notNull(offset, "offset must not be null");
        RetrySink<XStreamMessage<K, V>> sink = new RetrySink<>(executor, count);
        this.streamTask.add(new StreamInfo<>(options, offset, sink));
        return new RetryFlow<>(sink);
    }

}
