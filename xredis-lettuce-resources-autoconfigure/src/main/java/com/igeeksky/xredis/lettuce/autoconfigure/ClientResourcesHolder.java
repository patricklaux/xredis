package com.igeeksky.xredis.lettuce.autoconfigure;

import com.igeeksky.xtool.core.GracefulShutdown;
import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.resource.ClientResources;
import io.netty.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ClientResources 持有者
 * 支持优雅关机
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class ClientResourcesHolder implements GracefulShutdown {

    private static final Logger log = LoggerFactory.getLogger(ClientResourcesHolder.class);

    private final long timeout;
    private final long quietPeriod;
    private final TimeUnit timeUnit;
    private final ClientResources clientResources;

    /**
     * 采用默认参数创建 ClientResourcesHolder
     * <p>
     * quietPeriod：默认为 100ms；timeout：默认为 2000ms。
     *
     * @param clientResources ClientResources
     */
    public ClientResourcesHolder(ClientResources clientResources) {
        this(clientResources, 100, 2000, TimeUnit.MILLISECONDS);
    }

    /**
     * 根据传入参数创建 ClientResourcesHolder
     *
     * @param clientResources ClientResources
     * @param timeout         最大超时
     * @param quietPeriod     静默时长
     * @param timeUnit        时间单位
     */
    public ClientResourcesHolder(ClientResources clientResources, long quietPeriod, long timeout, TimeUnit timeUnit) {
        Assert.notNull(clientResources, "clientResources must not be null.");
        Assert.notNull(timeUnit, "timeUnit must not be null.");
        Assert.isTrue(timeout > 0, "timeout must be greater than 0.");
        Assert.isTrue(quietPeriod >= 0, "quietPeriod must be greater or equal to 0.");
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.timeUnit = timeUnit;
        this.clientResources = clientResources;
    }

    /**
     * 获取 ClientResources
     *
     * @return {@link ClientResources}
     */
    public ClientResources get() {
        return clientResources;
    }

    /**
     * 根据预设参数，优雅关闭 ClientResources
     *
     * @see ClientResources#shutdown(long, long, TimeUnit)
     */
    @Override
    public void shutdown() {
        this.shutdown(quietPeriod, timeout, timeUnit);
    }

    /**
     * 根据传入参数，优雅关闭 ClientResources（同步）
     *
     * @param quietPeriod 静默时长
     * @param timeout     最大超时
     * @param timeUnit    时间单位
     * @see ClientResources#shutdown(long, long, TimeUnit)
     */
    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit timeUnit) {
        try {
            this.shutdownAsync(quietPeriod, timeout, timeUnit).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException ignored) {
        } catch (TimeoutException e) {
            log.error("Graceful shutdown timeout. wait: {} {}", timeout, timeUnit.name(), e);
        }
    }

    /**
     * 根据预设参数，优雅关闭 ClientResources（异步）
     *
     * @return {@code CompletableFuture<Void>} – 关闭结果（可能的异常信息）
     * @see ClientResources#shutdown(long, long, TimeUnit)
     */
    @Override
    public CompletableFuture<Void> shutdownAsync() {
        return this.shutdownAsync(quietPeriod, timeout, timeUnit);
    }

    /**
     * 根据传入参数，优雅关闭 ClientResources（异步）
     * <p>
     * 1. 调用 {@link ClientResources#shutdown(long, long, TimeUnit)} 方法，关闭 ClientResources。
     * 2. 打印关闭日志。
     * 3. 返回 {@code CompletableFuture<Void>}，用于接收关闭结果。
     *
     * @param quietPeriod 静默时长
     * @param timeout     最大超时
     * @param timeUnit    时间单位
     * @return {@code CompletableFuture<Void>} – 关闭结果（可能的异常信息）
     * @see ClientResources#shutdown(long, long, TimeUnit)
     */
    @Override
    public CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit timeUnit) {
        log.info("Commencing graceful shutdown. Waiting for active channel to complete.");
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        Future<Boolean> shutdown = clientResources.shutdown(quietPeriod, timeout, timeUnit);
        shutdown.addListener(f -> {
            if (f.isSuccess()) {
                future.complete((Boolean) f.get());
            } else {
                future.completeExceptionally(f.cause());
            }
        });
        return future.whenComplete((bool, t) -> {
            if (t != null) {
                log.error("Graceful shutdown has error. {}", t.getMessage(), t);
                return;
            }
            if (bool != null && bool) {
                log.info("Graceful shutdown complete.");
            } else {
                log.warn("Graceful shutdown has error.");
            }
        }).thenApply(b -> null);
    }

}
