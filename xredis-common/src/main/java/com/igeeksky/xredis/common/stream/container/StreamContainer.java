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
 * 使用公共读取参数的流容器（仅适用于非消费者组）
 * <p>
 * 使用定时任务拉取 Stream 消息并推送给消费者，
 * 拉取消息时所有 Stream 都使用公共的 {@link ReadOptions} 参数，
 * 并且将所有 Stream 合并到一个 {@code xread} 命令进行读取，以减少命令阻塞带来的时延。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamContainer<K, V> extends AbstractStreamContainer<K, V> {

    private final StreamContainerTask<K, V> streamTask;

    private final int count;
    private final long period;
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
     * @param period      两次拉取消息的时间间隔，单位毫秒 {@code interval > 0}
     * @param options     读选项（不能为空）
     */
    public StreamContainer(StreamOperator<K, V> operator, ExecutorService executor,
                           ScheduledExecutorService scheduler, long quietPeriod, long timeout,
                           long period, ReadOptions options) {
        super(operator, quietPeriod, timeout);
        Assert.notNull(executor, "executor must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.isTrue(period > 0, "interval must be greater than 0");

        this.count = options.count();
        this.options = options.to();
        this.period = period;
        this.executor = executor;
        this.scheduler = scheduler;
        this.streamTask = new StreamContainerTask<>(operator, this.options);
        this.start();
    }

    private void start() {
        this.schedulePullFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::pull,
                this.period, this.period, TimeUnit.MILLISECONDS);
        this.scheduleConsumeFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::consume,
                this.period, Math.max(1, this.period / 2), TimeUnit.MILLISECONDS);
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
