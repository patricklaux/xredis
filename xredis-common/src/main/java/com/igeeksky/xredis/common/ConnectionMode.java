package com.igeeksky.xredis.common;

/**
 * Redis 连接模式
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface ConnectionMode {

    /**
     * 是否集群链接
     * <p>
     * 这里是指创建连接的方式，并不是指 RedisServer 的模式。
     * 譬如 RedisServer 模式为集群模式，但创建连接的方式为单机模式，返回 false。
     *
     * @return true if this connection is a cluster connection.
     */
    boolean isCluster();

}
