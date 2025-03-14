package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.RedisHelper;
import com.igeeksky.xredis.lettuce.api.Pipeline;
import io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl;
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
 * 管道接口实现（集群模式）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterPipeline<K, V> extends RedisAdvancedClusterAsyncCommandsImpl<K, V> implements Pipeline<K, V> {

    private static final Logger log = LoggerFactory.getLogger(LettuceClusterPipeline.class);
    private final long timeout;
    private final long quietPeriod;
    private final StatefulRedisClusterConnection<K, V> connection;

    /**
     * cluster operator constructor
     *
     * @param connection  批量提交命令连接（autoFlush = false）
     * @param codec       编解码器
     * @param quietPeriod 优雅关闭 quietPeriod
     * @param timeout     优雅关闭 timeout
     */
    public LettuceClusterPipeline(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec,
                                  long quietPeriod, long timeout) {
        super(connection, codec);
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.connection = connection;
    }

    /**
     * cluster operator constructor
     *
     * @param connection  批量提交命令连接（autoFlush = false）
     * @param codec       编解码器
     * @param quietPeriod 优雅关闭 quietPeriod
     * @param timeout     优雅关闭 timeout
     * @param parser      json 解析器
     */
    public LettuceClusterPipeline(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec,
                                  long quietPeriod, long timeout, Supplier<JsonParser> parser) {
        super(connection, codec, parser);
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.connection = connection;
    }

    @Override
    public void flushCommands() {
        connection.flushCommands();
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException(
                "Pipeline doesn't support change auto flush mode, it must be false." +
                        "If you want to flush commands immediately, please use redis-*-operator."
        );
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public void close() {
        try {
            RedisHelper.get(closeAsync(), timeout, TimeUnit.MILLISECONDS, false, false);
        } catch (Exception e) {
            log.error("LettuceClusterPipeline: Close has error. {}", e.getMessage(), e);
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