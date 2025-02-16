package com.igeeksky.xredis.common.flow;

import java.time.Duration;

/**
 * Subscription 默认实现
 *
 * @param <E>  数据类型
 * @param sink 数据池
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record DefaultSubscription<E>(Sink<E> sink) implements Subscription {

    @Override
    public void cancel() {
        sink.cancel();
    }

    @Override
    public void pause(Duration time) {
        sink.pause(time);
    }

}