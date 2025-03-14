package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.RedisHelper;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * RedisOperator 实现类（非集群）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceOperator<K, V> implements RedisOperator<K, V> {

    private static final Logger log = LoggerFactory.getLogger(LettuceOperator.class);
    private final long timeout;
    private final long quietPeriod;
    private final LettuceSyncOperator<K, V> sync;
    private final LettuceAsyncOperator<K, V> async;
    private final LettuceReactiveOperator<K, V> reactive;
    private final StatefulRedisConnection<K, V> connection;

    /**
     * Standalone or Sentinel
     *
     * @param connection  立即提交命令连接（autoFlush = true）
     * @param codec       编解码器
     * @param quietPeriod 优雅关闭 quietPeriod
     * @param timeout     优雅关闭 timeout
     */
    public LettuceOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec,
                           long quietPeriod, long timeout) {
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.connection = connection;
        this.sync = new LettuceSyncOperator<>(connection);
        this.async = new LettuceAsyncOperator<>(connection, codec);
        this.reactive = new LettuceReactiveOperator<>(connection, codec);
    }

    /**
     * Standalone or Sentinel
     *
     * @param connection  立即提交命令连接（autoFlush = true）
     * @param codec       编解码器
     * @param quietPeriod 优雅关闭 quietPeriod
     * @param timeout     优雅关闭 timeout
     * @param parser      json 解析器
     */
    public LettuceOperator(StatefulRedisConnection<K, V> connection, RedisCodec<K, V> codec,
                           long quietPeriod, long timeout, Supplier<JsonParser> parser) {
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.connection = connection;
        this.sync = new LettuceSyncOperator<>(connection);
        this.async = new LettuceAsyncOperator<>(connection, codec, parser);
        this.reactive = new LettuceReactiveOperator<>(connection, codec, parser);
    }

    @Override
    public LettuceSyncOperator<K, V> sync() {
        return sync;
    }

    @Override
    public LettuceAsyncOperator<K, V> async() {
        return async;
    }

    @Override
    public LettuceReactiveOperator<K, V> reactive() {
        return reactive;
    }

    @Override
    public void close() {
        try {
            RedisHelper.get(closeAsync(), timeout, TimeUnit.MILLISECONDS, false, false);
        } catch (Exception e) {
            log.error("LettuceOperator: Close has error. {}", e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        if (quietPeriod > 0) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(quietPeriod));
        }
        return connection.closeAsync();
    }

    @Override
    public boolean isCluster() {
        return false;
    }

}
