package com.igeeksky.xredis.common;

import java.util.concurrent.CompletableFuture;

/**
 * 异步关闭接口
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface AsyncCloseable {

    /**
     * 异步关闭
     *
     * @return 异步关闭
     */
    CompletableFuture<Void> closeAsync();

}
