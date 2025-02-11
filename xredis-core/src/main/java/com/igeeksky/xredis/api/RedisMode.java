package com.igeeksky.xredis.api;

/**
 * Redis 连接模式
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface RedisMode {

    /**
     * 是否集群链接
     *
     * @return true if this connection is a cluster connection.
     */
    default boolean isCluster() {
        return false;
    }

}
