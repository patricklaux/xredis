package com.igeeksky.xredis.flow;

import java.time.Duration;

/**
 * Subscription 默认实现
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record DefaultSubscription(Sink<?> sink) implements Subscription {

    @Override
    public void cancel() {
        sink.cancel();
    }

    @Override
    public void pause(Duration time) {
        sink.pause(time);
    }

}