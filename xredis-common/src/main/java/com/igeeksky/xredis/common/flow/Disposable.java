package com.igeeksky.xredis.common.flow;

import java.time.Duration;

/**
 * 释放资源接口
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Disposable {

    /**
     * 放弃订阅：关闭流并释放资源
     */
    void dispose();

    /**
     * 是否已释放
     *
     * @return true 已释放
     */
    boolean isDisposed();

}
