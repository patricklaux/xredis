package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.flow.Flow;
import com.igeeksky.xredis.common.flow.RetryFlow;
import com.igeeksky.xredis.common.flow.RetrySink;
import com.igeeksky.xredis.common.flow.Subscriber;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xredis.common.stream.XGroupConsumer;
import com.igeeksky.xredis.common.stream.XStreamMessage;
import com.igeeksky.xredis.common.stream.XStreamOffset;
import com.igeeksky.xtool.core.lang.Assert;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 循环监听 Stream 消息
 * <p>
 * 当有多个流有 block 选项，
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGenericContainer<K, V> extends AbstractStreamContainer<K, V> {

    private final StreamTask<K, V> streamTask;

    private final long interval;
    private final ExecutorService executor;
    private final ScheduledExecutorService scheduler;

    /**
     * 创建一个新的 StreamGenericContainer 实例
     *
     * @param operator    StreamOperator
     * @param executor    虚拟线程执行器
     * @param scheduler   调度器
     * @param quietPeriod quietPeriod 优雅关闭（等待正在运行的任务完成，单位毫秒）
     * @param timeout     timeout 优雅关闭（最大等待时间，单位毫秒）
     * @param interval    拉取消息任务的间隔时间，单位毫秒（必须大于 0）
     */
    public StreamGenericContainer(StreamOperator<K, V> operator, ExecutorService executor,
                                  ScheduledExecutorService scheduler, long quietPeriod, long timeout, long interval) {
        super(operator, quietPeriod, timeout);
        Assert.notNull(executor, "executor must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.isTrue(interval > 0, "interval must be greater than 0");

        this.interval = interval;
        this.executor = executor;
        this.scheduler = scheduler;
        this.streamTask = new StreamGenericTask<>(operator);
        this.start();
    }

    /**
     * 启动 pull 和 consume 任务
     */
    private void start() {
        this.schedulePullFuture = this.scheduler.scheduleWithFixedDelay(() -> {
            if (vitrualPullFuture == null || vitrualPullFuture.isDone()) {
                vitrualPullFuture = this.executor.submit(this.streamTask::pull);
            }
        }, this.interval, this.interval, TimeUnit.MILLISECONDS);
        this.scheduleConsumeFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::consume,
                this.interval, Math.max(1, this.interval / 2), TimeUnit.MILLISECONDS);
    }

    /**
     * 订阅流（非 group）
     *
     * @param offset  偏移量（不能为空）
     * @param options 读选项（不能为空） {@code 0 < count <= (1 << 29)}
     * @return {@link Flow} – 无限数据流（订阅之后除非取消，否则将一直拉取流消息并推送给 {@link Subscriber}）
     */
    public Flow<XStreamMessage<K, V>> subscribe(XStreamOffset<K> offset, ReadOptions options) {
        Assert.notNull(offset, "offset must not be null");
        Assert.notNull(options, "options must not be null");

        RetrySink<XStreamMessage<K, V>> sink = new RetrySink<>(executor, options.count());
        this.streamTask.add(new StreamInfo<>(options.to(), offset, sink));
        return new RetryFlow<>(sink);
    }

    /**
     * 订阅流（group）
     *
     * @param offset   读偏移（不能为空）
     * @param options  读选项（不能为空） {@code 0 < count <= (1 << 29)} 且 {@code block > 0}
     * @param consumer 消费组名及消费者名（不能为空）
     * @return {@link Flow} – 无限数据流（订阅之后除非取消，否则将一直拉取流消息并推送给 {@link Subscriber}）
     */
    public Flow<XStreamMessage<K, V>> subscribe(XStreamOffset<K> offset, ReadOptions options,
                                                XGroupConsumer<K> consumer) {
        Assert.notNull(offset, "offset must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(consumer, "consumer must not be null");

        RetrySink<XStreamMessage<K, V>> sink = new RetrySink<>(executor, options.count());
        this.streamTask.add(new StreamGroupInfo<>(options.to(), offset, sink, consumer));
        return new RetryFlow<>(sink);
    }

}
