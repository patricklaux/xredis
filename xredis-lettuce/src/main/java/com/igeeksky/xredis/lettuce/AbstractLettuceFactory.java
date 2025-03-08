package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.RedisHelper;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.common.stream.container.StreamGenericContainer;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.config.LettuceGenericConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.codec.RedisCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * LettuceOperator 抽象工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public abstract sealed class AbstractLettuceFactory implements RedisOperatorFactory
        permits LettuceStandaloneFactory, LettuceSentinelFactory, LettuceClusterFactory {

    private static final Logger log = LoggerFactory.getLogger(AbstractLettuceFactory.class);

    /**
     * RedisOperatorFactory 唯一标识
     */
    private final String id;
    /**
     * 优雅关闭：超时时间
     */
    protected final long timeout;
    /**
     * 优雅关闭：静默时间
     */
    protected final long quietPeriod;
    /**
     * 虚拟线程池
     */
    private final ExecutorService executor;

    /**
     * 构造函数
     *
     * @param config Lettuce 配置
     */
    public AbstractLettuceFactory(LettuceGenericConfig config) {
        this.id = config.getId();
        this.timeout = config.getShutdownTimeout();
        this.quietPeriod = config.getShutdownQuietPeriod();
        this.executor = Executors.newThreadPerTaskExecutor(RedisHelper.getStreamVirtualFactory());
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public <K, V> LettuceStreamOperator<K, V> streamOperator(RedisCodec<K, V> codec) {
        return new LettuceStreamOperator<>(this.redisOperator(codec));
    }

    @Override
    public <K, V> StreamContainer<K, V> streamContainer(RedisCodec<K, V> codec, ScheduledExecutorService scheduler,
                                                        long interval, ReadOptions options) {
        return new StreamContainer<>(this.streamOperator(codec), executor, scheduler,
                quietPeriod, timeout, interval, options);
    }

    @Override
    public <K, V> StreamGenericContainer<K, V> streamGenericContainer(RedisCodec<K, V> codec,
                                                                      ScheduledExecutorService scheduler,
                                                                      long interval) {
        return new StreamGenericContainer<>(this.streamOperator(codec), executor, scheduler,
                quietPeriod, timeout, interval);
    }

    /**
     * 获取 RedisClient
     *
     * @return AbstractRedisClient
     */
    protected abstract AbstractRedisClient getClient();

    @Override
    public void shutdown() {
        this.shutdown(quietPeriod, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void shutdown(long quietPeriod, long timeout, TimeUnit timeUnit) {
        try {
            this.shutdownAsync(quietPeriod, timeout, timeUnit).get(timeout, timeUnit);
        } catch (InterruptedException | ExecutionException ignored) {
        } catch (TimeoutException e) {
            log.error("Graceful shutdown timeout. wait: {} {}", timeout, timeUnit.name(), e);
        }
    }

    @Override
    public CompletableFuture<Void> shutdownAsync() {
        return this.shutdownAsync(quietPeriod, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit timeUnit) {
        log.info("Commencing graceful shutdown. Waiting for active connection to complete.");
        return CompletableFuture.supplyAsync(() -> {
            boolean terminated = false;
            try {
                try {
                    if (quietPeriod > 0) {
                        terminated = executor.awaitTermination(quietPeriod, timeUnit);
                    }
                    executor.shutdown();
                } catch (InterruptedException e) {
                    executor.shutdownNow();
                }
            } catch (Throwable t) {
                log.error("[Executor] Graceful shutdown has error. {}", t.getMessage(), t);
            }
            return terminated;
        }).thenCompose(ignored -> {
            AbstractRedisClient client = this.getClient();
            return client.shutdownAsync(quietPeriod, timeout, timeUnit)
                    .whenComplete((v, t) -> {
                        if (t != null) {
                            log.error("[RedisClient] Graceful shutdown has error.{}", t.getMessage(), t);
                        } else {
                            log.info("[RedisClient] Graceful shutdown completed.");
                        }
                    });
        });
    }

}
