package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.RedisHelper;
import com.igeeksky.xredis.common.stream.container.ReadOptions;
import com.igeeksky.xredis.common.stream.container.StreamContainer;
import com.igeeksky.xredis.common.stream.container.StreamGenericContainer;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.config.LettuceGenericConfig;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.codec.RedisCodec;

import java.util.concurrent.*;

/**
 * LettuceOperator 抽象工厂类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public abstract sealed class AbstractLettuceFactory implements RedisOperatorFactory
        permits LettuceStandaloneFactory, LettuceSentinelFactory, LettuceClusterFactory {

    /**
     * 优雅关闭：超时时间
     */
    private final long timeout;
    /**
     * 优雅关闭：静默时间
     */
    private final long quietPeriod;
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
        this.timeout = config.getShutdownTimeout();
        this.quietPeriod = config.getShutdownQuietPeriod();
        this.executor = Executors.newThreadPerTaskExecutor(RedisHelper.getStreamVirtualFactory());
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
    public void shutdown(long quietPeriod, long timeout, TimeUnit unit) {
        AbstractRedisClient client = getClient();
        try {
            try {
                if (quietPeriod > 0) {
                    boolean ignored = executor.awaitTermination(quietPeriod, unit);
                }
                executor.shutdown();
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        } catch (Exception ignored) {
        }
        client.shutdown(quietPeriod, timeout, unit);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync() {
        return this.shutdownAsync(quietPeriod, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout, TimeUnit unit) {
        AbstractRedisClient client = getClient();
        return CompletableFuture.completedFuture(Boolean.TRUE)
                .thenApply(bool -> {
                    boolean terminated = false;
                    try {
                        try {
                            if (quietPeriod > 0) {
                                terminated = executor.awaitTermination(quietPeriod, unit);
                            }
                            executor.shutdown();
                        } catch (InterruptedException e) {
                            executor.shutdownNow();
                        }
                    } catch (Exception ignored) {
                    }
                    return terminated;
                }).thenCompose(ignored -> client.shutdownAsync(quietPeriod, timeout, unit));
    }

}
