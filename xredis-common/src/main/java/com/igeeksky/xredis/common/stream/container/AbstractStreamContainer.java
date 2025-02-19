package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.AsyncCloseable;
import com.igeeksky.xredis.common.stream.StreamOperator;
import com.igeeksky.xtool.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * 流容器（抽象类）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public abstract class AbstractStreamContainer<K, V> implements AsyncCloseable {

    private static final Logger log = LoggerFactory.getLogger(AbstractStreamContainer.class);

    private final long quietPeriod;

    private final long timeout;

    /**
     * Redis 流操作对象
     */
    protected final StreamOperator<K, V> operator;

    /**
     * 拉取消息的 Future（虚拟线程执行）
     */
    protected volatile Future<?> vitrualPullFuture;

    /**
     * 拉取消息的 Future（调度器执行）
     */
    protected volatile ScheduledFuture<?> schedulePullFuture;

    /**
     * 消费消息的 Future（调度器执行）
     */
    protected volatile ScheduledFuture<?> scheduleConsumeFuture;

    /**
     * 构造函数
     *
     * @param operator    Redis 流操作对象
     * @param quietPeriod quietPeriod 优雅关闭（等待正在运行的任务完成，单位毫秒）
     * @param timeout     timeout 优雅关闭（最大等待时间，单位毫秒）
     */
    public AbstractStreamContainer(StreamOperator<K, V> operator, long quietPeriod, long timeout) {
        Assert.notNull(operator, "operator must not be null");
        this.operator = operator;
        this.quietPeriod = quietPeriod;
        this.timeout = timeout;
    }

    /**
     * 关闭 StreamContainer 对象
     * <p>
     * 1. 停止拉取任务（等待正在运行的任务完成）<br>
     * 2. 停止消费任务（等待正在运行的任务完成）<br>
     * 3. 关闭 Redis 连接。
     *
     * @since 1.0.0
     */
    public void shutdown() {
        try {
            this.closeAsync().get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 关闭 StreamContainer 对象
     * <p>
     * 1. 停止拉取任务（等待正在运行的任务完成）<br>
     * 2. 停止消费任务（等待正在运行的任务完成）<br>
     * 3. 关闭 Redis 连接。
     *
     * @return {@link CompletableFuture} – 关闭操作的异步通知。
     * @since 1.0.0
     */
    @Override
    public CompletableFuture<Void> closeAsync() {
        if (this.schedulePullFuture != null) {
            this.schedulePullFuture.cancel(false);
        }
        if (this.vitrualPullFuture != null) {
            this.vitrualPullFuture.cancel(false);
        }
        if (this.scheduleConsumeFuture != null) {
            this.scheduleConsumeFuture.cancel(false);
        }
        if (this.quietPeriod > 0) {
            try {
                Thread.sleep(this.quietPeriod);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return this.operator.closeAsync();
    }

}
