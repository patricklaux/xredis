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
     * 接收上游消息
     * <p>
     * 由上游发布者调用
     *
     * @param element 消息元素
     * @return 是否成功接收消息
     */
    boolean next(E element);

    /**
     * 暂停拉取消息
     * <p>
     * {@code Datasource ---X--> Flow -----> Subscriber}
     * <p>
     * 仅暂停拉取消息，已拉取的消息将继续推送给订阅者，直到数据池为空。
     *
     * @param pauseTime 暂停时长（达到此时长后，自动恢复拉取消息）
     */
    void pausePull(Duration pauseTime);

    /**
     * 暂停推送消息
     * <p>
     * {@code Datasource -----> Flow ---X--> Subscriber}
     * <p>
     * 仅暂停推送消息，拉取消息任务仍将继续，直到数据池已满。
     *
     * @param pauseTime 暂停时长（达到此时长后，自动恢复推送消息）
     */
    void pausePush(Duration pauseTime);

    /**
     * 完全取消订阅
     * <p>
     * 调用此方法后不可恢复，需重新订阅
     */
    void cancel();

}
