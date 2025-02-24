package com.igeeksky.xredis.common.flow;

import java.time.Duration;

/**
 * 可重试的订阅关系管理接口
 *
 * @param <E> 元素类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RetrySubscription<E> extends Subscription {

    /**
     * 立即重试
     *
     * @param element 需重试的元素
     */
    void retry(E element);

    /**
     * 延迟重试
     *
     * @param element 需重试的元素
     * @param delay   延迟时间
     */
    void retry(E element, Duration delay);

}
