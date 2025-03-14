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
 * 使用公共读取参数的流容器
 * <p>
 * 使用定时任务拉取 Stream 消息并推送给消费者，
 * 拉取消息时每个 Stream 都使用独立的 {@link ReadOptions} 参数，
 * 如果有多个 Stream，那么会分多次发送命令并接收结果，因为底层实现只有一条连接，所以不是并行操作，而是串行操作。
 * <p>
 * 因此，如果有多个 Stream：<br>
 * 1. 要么无 block 选项，要么 block 选项值很小且非 0；<br>
 * 2. 或者使用多个 {@link StreamGenericContainer} 实例，每个 Stream 使用一个独占实例。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamGenericContainer<K, V> extends AbstractStreamContainer<K, V> {

    private final StreamGenericTask<K, V> streamTask;

    private final long period;
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
     * @param period      拉取消息任务的间隔时间，单位毫秒（必须大于 0）
     */
    public StreamGenericContainer(StreamOperator<K, V> operator, ExecutorService executor,
                                  ScheduledExecutorService scheduler, long quietPeriod, long timeout, long period) {
        super(operator, quietPeriod, timeout);
        Assert.notNull(executor, "executor must not be null");
        Assert.notNull(scheduler, "scheduler must not be null");
        Assert.isTrue(period > 0, "interval must be greater than 0");

        this.period = period;
        this.executor = executor;
        this.scheduler = scheduler;
        this.streamTask = new StreamGenericTask<>(operator);
        this.start();
    }

    /**
     * 启动 pull 和 consume 任务
     */
    private void start() {
        this.schedulePullFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::pull,
                this.period, this.period, TimeUnit.MILLISECONDS);
        this.scheduleConsumeFuture = this.scheduler.scheduleWithFixedDelay(this.streamTask::consume,
                this.period, Math.max(1, this.period / 2), TimeUnit.MILLISECONDS);
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
