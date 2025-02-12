package com.igeeksky.xredis.cluster;

import com.igeeksky.xredis.api.RedisReactiveOperator;
import io.lettuce.core.cluster.RedisAdvancedClusterReactiveCommandsImpl;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.json.JsonParser;
import reactor.core.publisher.Mono;

/**
 * 响应式操作接口实现（集群模式）
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceClusterReactiveOperator<K, V> extends RedisAdvancedClusterReactiveCommandsImpl<K, V> implements RedisReactiveOperator<K, V> {

    /**
     * cluster operator constructor
     *
     * @param connection 批量提交命令连接（autoFlush = true）
     * @param codec      编解码器
     */
    public LettuceClusterReactiveOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec) {
        super(connection, codec);
    }

    /**
     * cluster operator constructor
     *
     * @param connection 批量提交命令连接（autoFlush = true）
     * @param codec      编解码器
     * @param parser     json 解析器
     */
    public LettuceClusterReactiveOperator(StatefulRedisClusterConnection<K, V> connection, RedisCodec<K, V> codec, Mono<JsonParser> parser) {
        super(connection, codec, parser);
    }

    @Override
    public boolean isCluster() {
        return true;
    }

    /**
     * 禁止修改 autoFlush
     * <p>
     * 调用此方法将抛出异常。
     *
     * @param autoFlush state of autoFlush.
     */
    @Override
    public void setAutoFlushCommands(boolean autoFlush) {
        throw new UnsupportedOperationException("RedisOperator doesn't support change auto flush mode, it must be true." +
                "If you want to batch submit commands, please use pipeline.");
    }

}
