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

    private volatile boolean cancelled;
    private volatile long restartPullTime;
    private volatile long restartPushTime;
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
            this.buffer = new ArrayBlockingQueue<>(this.count << 1);
            this.subscriber = subscriber;
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
    public void pausePull(Duration pauseTime) {
        Assert.notNull(pauseTime, "pauseTime must not be null");
        long millis = pauseTime.toMillis();
        Assert.isTrue(millis > 0, "pauseTime must be greater than 0");
        this.restartPullTime = System.currentTimeMillis() + millis;
    }

    @Override
    public void pausePush(Duration pauseTime) {
        Assert.notNull(pauseTime, "pauseTime must not be null");
        long millis = pauseTime.toMillis();
        Assert.isTrue(millis > 0, "pauseTime must be greater than 0");
        this.restartPushTime = System.currentTimeMillis() + millis;
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
            Future<?>[] futures1 = this.futures;
            if (futures1 != null) {
                Futures.cancelAll(0, futures1, false);
                this.futures = null;
            }
            this.tasks = null;
            this.buffer = null;
            this.subscriber = null;
            this.cancelled = true;
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
     * 是否处于暂停状态
     *
     * @return {@code true} 处于暂停状态； {@code false} 未处于暂停状态
     */
    public boolean isPullPaused() {
        return this.restartPullTime > System.currentTimeMillis();
    }

    /**
     * 是否有足够空间容纳新拉取的元素
     *
     * @return {@code true} 空间不足； {@code false} 空间充足
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

    private static class ConsumeTask<E> implements RetrySubscription<E>, Runnable {

        private final RetrySink<E> sink;
        private final AtomicInteger attempts = new AtomicInteger(0);

        private volatile E element;
        private volatile long delayNanos;
        private volatile boolean retry;

        public ConsumeTask(RetrySink<E> sink) {
            this.sink = sink;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    Subscriber<E> s = this.sink.subscriber;
                    if (this.sink.cancelled || s == null) {
                        return;
                    }
                    long sleepMillis = sink.restartPushTime - System.currentTimeMillis();
                    if (sleepMillis > 0) {
                        long sleepNanos = sleepMillis * 1000000;
                        long delay = this.delayNanos;
                        this.delayNanos = delay - sleepNanos;
                        LockSupport.parkNanos(sleepNanos);
                        continue;
                    }
                    if (this.retry) {
                        this.processFailed(s);
                        continue;
                    }
                    ArrayBlockingQueue<E> buf = this.sink.buffer;
                    if (buf == null) {
                        return;
                    }
                    E element = buf.poll();
                    if (element == null) {
                        return;
                    }
                    try {
                        s.onNext(element);
                    } catch (Throwable t) {
                        s.onError(t, element, attempts.incrementAndGet(), this);
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
            E e1 = this.element;
            long delay = this.delayNanos;
            if (delay > 0) {
                LockSupport.parkNanos(delay);
                if (this.sink.cancelled) {
                    return;
                }
            }
            try {
                s.onNext(e1);
                this.reset();
            } catch (Throwable t) {
                this.retry = false;
                this.element = null;
                s.onError(t, e1, attempts.incrementAndGet(), this);
            }
        }

        private void reset() {
            this.retry = false;
            this.element = null;
            this.attempts.set(0);
        }

        @Override
        public void retry(E element) {
            Assert.notNull(element, "retry element must not be null.");
            this.element = element;
            this.delayNanos = 0;
            this.retry = true;
        }

        @Override
        public void retry(E element, Duration delay) {
            Assert.notNull(element, "retry element must not be null.");
            Assert.notNull(delay, "delay must not be null.");
            long nanos = delay.toNanos();
            Assert.isTrue(nanos > 0, "delay must be greater than 0.");
            this.element = element;
            this.delayNanos = nanos;
            this.retry = true;
        }

        @Override
        public void cancel() {
            this.sink.cancel();
        }

        @Override
        public void pausePull(Duration pauseTime) {
            this.sink.pausePull(pauseTime);
        }

        @Override
        public void pausePush(Duration pauseTime) {
            this.sink.pausePush(pauseTime);
        }

    }

}
