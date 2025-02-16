package com.igeeksky.xredis.common.flow;

import java.time.Duration;

/**
 * 数据池
 *
 * @param <E> 数据类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Sink<E> {

    /**
     * 订阅
     * <p>
     * 由 Flow 调用
     *
     * @param subscriber  订阅者
     * @param parallelism 并行度
     */
    void subscribe(Subscriber<E> subscriber, int parallelism);

    /**
     * 传递上游异常
     * <p>
     * 由上游发布者调用
     *
     * @param t Throwable
     */
    void error(Throwable t);

    /**
     * 接收上游数据
     * <p>
     * 由上游发布者调用
     *
     * @param element 下游接收数据
     * @return 下游是否接收成功
     */
    boolean next(E element);

    /**
     * 暂停订阅
     * <p>
     * 由 Subscription（或 Disposable） 调用
     *
     * @param pauseTime 暂停时间
     */
    void pause(Duration pauseTime);

    /**
     * 取消订阅
     * <p>
     * 由 Subscription（或 Disposable，或 Flow） 调用
     */
    void cancel();

}
