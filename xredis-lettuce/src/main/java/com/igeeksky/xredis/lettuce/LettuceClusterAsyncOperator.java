package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.lettuce.api.RedisAsyncOperator;
import io.lettuce.core.cluster.RedisAdvancedClusterAsyncCommandsImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;

import java.util.function.Supplier;

/**
 * 异步操作接口实现（集群模式）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterAsyncOperator<K, V> extends RedisAdvancedClusterAsyncCommandsImpl<K, V>
        implements RedisAsyncOperator<K, V> {

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     */
    public LettuceClusterAsyncOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    /**
     * Standalone or Sentinel
     *
     * @param connection 立即提交命令连接（autoFlush = true）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceClusterAsyncOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec,
                                       Supplier<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

}
