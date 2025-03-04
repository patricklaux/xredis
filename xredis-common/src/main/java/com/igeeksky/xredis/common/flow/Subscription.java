package com.igeeksky.xredis.common.flow;

import java.time.Duration;

/**
 * 订阅关系维护接口
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Subscription {

    /**
     * 完全取消订阅
     * <p>
     * 调用此方法后不可恢复，需重新订阅
     */
    void cancel();

    /**
     * 暂停拉取消息
     * <p>
     * {@code Datasource ---X--> Flow -----> Subscriber}
     * <p>
     * 仅暂停拉取消息，已拉取消息仍将继续推送给订阅者，直到数据池为空。
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

}