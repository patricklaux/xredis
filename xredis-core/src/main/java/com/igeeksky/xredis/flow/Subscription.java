package com.igeeksky.xredis.flow;

import java.time.Duration;

/**
 * 订阅关系管理接口
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Subscription {

    /**
     * 取消订阅
     */
    void cancel();

    /**
     * 暂停订阅
     *
     * @param time 暂停时长
     */
    void pause(Duration time);

}