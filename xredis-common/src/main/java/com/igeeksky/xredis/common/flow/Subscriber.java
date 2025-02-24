package com.igeeksky.xredis.common.flow;

/**
 * 订阅者
 *
 * @param <E> 数据类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Subscriber<E> {

    /**
     * 处理接收到消息
     *
     * @param element 消息
     */
    void onNext(E element);

    /**
     * 上游异常处理
     * <p>
     * 譬如：RedisServer 连接异常，数据序列化异常……等.
     *
     * @param t 错误
     * @param s 订阅
     */
    void onError(Throwable t, Subscription s);

    /**
     * 消费异常处理
     * <p>
     * 如果调用 {@link RetrySubscription#retry}，
     *
     * @param t        错误
     * @param element  消息
     * @param attempts 已重试次数
     * @param s        订阅
     */
    void onError(Throwable t, E element, int attempts, RetrySubscription<E> s);

}