package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.RedisHelper;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * LettuceClusterOperator
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterOperator<K, V> implements RedisOperator<K, V> {

    private static final Logger log = LoggerFactory.getLogger(LettuceClusterOperator.class);

    private final long timeout;
    private final long quietPeriod;
    private final LettuceClusterSyncOperator<K, V> sync;
    private final LettuceClusterAsyncOperator<K, V> async;
    private final LettuceClusterReactiveOperator<K, V> reactive;
    private final StatefulRedisClusterConnection<K, V> connection;

    /**
     * cluster operator constructor
     *
     * @param connection  立即提交命令连接（autoFlush = true）
     * @param codec       编解码器
     * @param quietPeriod 优雅关闭 quietPeriod
     * @param timeout     优雅关闭 timeout
     */
    public LettuceClusterOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec,
                                  long quietPeriod, long timeout) {
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.connection = connection;
        this.sync = new LettuceClusterSyncOperator<>(connection);
        this.async = new LettuceClusterAsyncOperator<>(connection, codec);
        this.reactive = new LettuceClusterReactiveOperator<>(connection, codec);
    }

    /**
     * cluster operator constructor
     *
     * @param connection  立即提交命令连接（autoFlush = true）
     * @param codec       编解码器
     * @param quietPeriod 优雅关闭 quietPeriod
     * @param timeout     优雅关闭 timeout
     * @param parser      json 解析器
     */
    public LettuceClusterOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec,
                                  long quietPeriod, long timeout, Supplier<JsonParser> parser) {
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.connection = connection;
        this.sync = new LettuceClusterSyncOperator<>(connection);
        this.async = new LettuceClusterAsyncOperator<>(connection, codec, parser);
        this.reactive = new LettuceClusterReactiveOperator<>(connection, codec, parser);
    }

    @Override
    public LettuceClusterSyncOperator<K, V> sync() {
        return sync;
    }

    @Override
    public LettuceClusterAsyncOperator<K, V> async() {
        return async;
    }

    @Override
    public LettuceClusterReactiveOperator<K, V> reactive() {
        return reactive;
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public void close() {
        try {
            RedisHelper.get(closeAsync(), timeout);
        } catch (Exception e) {
            log.error("LettuceClusterOperator: Close has error. {}", e.getMessage(), e);
        }
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        if (quietPeriod > 0) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(quietPeriod));
        }
        return connection.closeAsync();
    }

}
