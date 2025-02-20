package com.igeeksky.xredis.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Shutdown {

    void shutdown();

    void shutdown(long quietPeriod, long timeout, TimeUnit unit);

    CompletableFuture<Void> shutdownAsync();

    CompletableFuture<Void> shutdownAsync(long quietPeriod);

}
