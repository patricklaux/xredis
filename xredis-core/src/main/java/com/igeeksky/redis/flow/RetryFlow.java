package com.igeeksky.redis.flow;

import java.time.Duration;

/**
 * 可重试的无限流
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RetryFlow<E> implements Flow<E> {

    private final Sink<E> sink;

    /**
     * 构造函数
     */
    public RetryFlow(Sink<E> sink) {
        this.sink = sink;
    }

    @Override
    public Disposable subscribe(Subscriber<E> subscriber, int parallelism) {
        this.sink.subscribe(subscriber, parallelism);
        return new DefaultDisposable(sink);
    }

    /**
     * 释放接口的默认实现
     */
    private static class DefaultDisposable implements Disposable {

        private volatile Sink<?> sink;

        public DefaultDisposable(Sink<?> sink) {
            this.sink = sink;
        }

        @Override
        public void dispose() {
            sink.cancel();
            sink = null;
        }

        @Override
        public boolean isDisposed() {
            return sink == null;
        }

        @Override
        public void pause(Duration pauseTime) {
            if (sink == null) {
                return;
            }
            sink.pause(pauseTime);
        }

    }

}
