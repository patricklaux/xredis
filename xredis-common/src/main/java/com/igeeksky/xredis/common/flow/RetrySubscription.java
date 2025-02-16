package com.igeeksky.xredis.common.flow;

import java.time.Duration;

/**
 * 可重试的订阅关系管理接口
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RetrySubscription extends Subscription {

    /**
     * 立即重试
     */
    void retry();

    /**
     * 延迟重试
     *
     * @param delay 延迟时间
     */
    void retry(Duration delay);

}
