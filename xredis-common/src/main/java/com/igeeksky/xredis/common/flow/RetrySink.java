package com.igeeksky.xredis.common.flow;

import com.igeeksky.xtool.core.concurrent.Futures;
import com.igeeksky.xtool.core.lang.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 可重试的 Sink
 *
 * @param <E> 数据类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RetrySink<E> implements Sink<E>, Runnable {

    private static final Logger log = LoggerFactory.getLogger(RetrySink.class);

    private final Lock lock = new ReentrantLock();

    private final int count;
    private final ExecutorService executor;
    private final Subscription subscription;

    private volatile long pauseTime;
    private volatile boolean paused;
    private volatile boolean cancelled;
    private volatile Subscriber<E> subscriber;

    private volatile Future<?>[] futures;
    private volatile ConsumeTask<?>[] tasks;
    private volatile ArrayBlockingQueue<E> buffer;

    /**
     * 构造函数
     *
     * @param executor 虚拟线程池
     * @param count    单次拉取消息的最大数量，缓冲区大小为 {@code count * 2}
     */
    public RetrySink(ExecutorService executor, int count) {
        this.count = count;
        this.executor = executor;
        this.subscription = new DefaultSubscription<>(this);
    }

    @Override
    public void subscribe(Subscriber<E> subscriber, int parallelism) {
        Assert.notNull(subscriber, "subscriber must not be null.");
        Assert.isTrue(parallelism > 0, "parallelism must be greater than 0.");
        if (this.isCancelled()) {
            return;
        }
        if (this.subscriber != null) {
            throw new IllegalStateException("subscriber already set.");
        }
        lock.lock();
        try {
            if (this.isCancelled()) {
                return;
            }
            if (this.subscriber != null) {
                throw new IllegalStateException("subscriber already set.");
            }
            this.subscriber = subscriber;
            this.buffer = new ArrayBlockingQueue<>(this.count << 1);
            this.tasks = new ConsumeTask[parallelism];
            for (int i = 0; i < parallelism; i++) {
                this.tasks[i] = new ConsumeTask<>(this);
            }
            this.futures = new Future[parallelism];
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void pause(Duration pauseTime) {
        Assert.notNull(pauseTime, "pauseTime must not be null");
        long nanos = pauseTime.toNanos();
        Assert.isTrue(nanos > 0, "pauseTime must be greater than 0");
        this.pauseTime = nanos;
        this.paused = true;
    }

    @Override
    public void cancel() {
        if (this.cancelled) {
            return;
        }
        lock.lock();
        try {
            if (this.cancelled) {
                return;
            }
            this.cancelled = true;
            Future<?>[] futures1 = this.futures;
            if (futures1 != null) {
                Futures.cancelAll(0, futures1, false);
                this.futures = null;
            }
            this.tasks = null;
            this.buffer = null;
            this.subscriber = null;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 是否已经取消订阅
     *
     * @return {@code true} 已取消； {@code false} 未取消
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * 是否就绪（即订阅者不为空）
     *
     * @return {@code true} 已就绪； {@code false} 未就绪
     */
    public boolean isNotReady() {
        return (this.subscriber == null);
    }

    /**
     * 是否有足够空间容纳新拉取的元素
     *
     * @return {@code true} 空间充足； {@code false} 空间不足
     */
    public boolean isNotEnoughSpace() {
        return this.buffer.size() > this.count;
    }

    @Override
    public boolean next(E element) {
        return this.buffer.offer(element);
    }

    @Override
    public void error(Throwable t) {
        Subscriber<E> s = this.subscriber;
        if (this.cancelled || s == null) {
            return;
        }
        s.onError(t, this.subscription);
    }

    /**
     * 启动消费任务
     */
    @Override
    public void run() {
        if (this.cancelled || this.subscriber == null) {
            return;
        }
        lock.lock();
        try {
            if (this.cancelled || this.subscriber == null) {
                return;
            }
            for (int i = 0; i < this.futures.length; i++) {
                Future<?> future = this.futures[i];
                if (future == null || future.isDone()) {
                    this.futures[i] = this.executor.submit(this.tasks[i]);
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    private static class ConsumeTask<E> implements RetrySubscription, Runnable {

        private final RetrySink<E> sink;
        private final AtomicInteger attempts = new AtomicInteger(0);

        private volatile E element;
        private volatile long delay;
        private volatile boolean retry;

        public ConsumeTask(RetrySink<E> sink) {
            this.sink = sink;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    if (this.sink.cancelled) {
                        return;
                    }
                    ArrayBlockingQueue<E> buf = this.sink.buffer;
                    Subscriber<E> s = this.sink.subscriber;

                    if (this.sink.paused) {
                        if (buf.isEmpty() && !this.retry) {
                            return;
                        }
                        LockSupport.parkNanos(this.sink.pauseTime);
                    }
                    if (this.sink.cancelled) {
                        return;
                    }
                    if (this.retry) {
                        this.processFailed(s);
                        continue;
                    }
                    E element = buf.poll();
                    if (element == null) {
                        return;
                    }
                    try {
                        s.onNext(element);
                    } catch (Throwable t) {
                        s.onError(t, this.element = element, attempts.incrementAndGet(), this);
                    }
                }
            } catch (Throwable t) {
                log.error(t.getMessage(), t);
            }
        }

        /**
         * 失败重试
         */
        private void processFailed(Subscriber<E> s) {
            if (this.delay > 0) {
                LockSupport.parkNanos(this.delay);
                if (this.sink.cancelled) {
                    return;
                }
            }
            try {
                s.onNext(element);
                this.reset();
            } catch (Throwable t) {
                this.retry = false;
                s.onError(t, element, attempts.incrementAndGet(), this);
            }
        }

        private void reset() {
            this.retry = false;
            this.attempts.set(0);
        }

        @Override
        public void retry() {
            this.delay = 0;
            this.retry = true;
        }

        @Override
        public void retry(Duration delay) {
            Assert.notNull(delay, "delay must not be null.");
            long nanos = delay.toNanos();
            Assert.isTrue(nanos > 0, "delay must be greater than 0.");
            this.delay = nanos;
            this.retry = true;
        }

        @Override
        public void cancel() {
            this.sink.cancel();
        }

        @Override
        public void pause(Duration time) {
            this.sink.pause(time);
        }

    }

}
