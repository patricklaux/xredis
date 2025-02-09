package com.igeeksky.redis.config;


import io.lettuce.core.ReadFrom;

import java.util.List;

/**
 * Lettuce Sentinel 配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
public final class LettuceSentinelConfig extends LettuceGenericConfig {

    private ReadFrom readFrom = ReadFrom.UPSTREAM;

    private String masterId;

    private String sentinelUsername;

    private String sentinelPassword;

    private List<RedisNode> nodes;

    /**
     * 默认构造函数
     */
    public LettuceSentinelConfig() {
    }

    /**
     * 获取：读节点选择策略
     * <p>
     * 默认：UPSTREAM
     *
     * @return {@link ReadFrom} – 读节点选择策略
     */
    public ReadFrom getReadFrom() {
        return readFrom;
    }

    /**
     * 设置：读节点选择策略
     *
     * @param readFrom 读节点选择策略
     */
    public void setReadFrom(ReadFrom readFrom) {
        this.readFrom = readFrom;
    }

    /**
     * 获取：主节点名称
     *
     * @return {@link String} – 主节点名称
     */
    public String getMasterId() {
        return masterId;
    }

    /**
     * 设置：主节点名称
     *
     * @param masterId 主节点名称
     */
    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    /**
     * 获取：哨兵用户名
     *
     * @return {@link String} – 哨兵用户名
     */
    public String getSentinelUsername() {
        return sentinelUsername;
    }

    /**
     * 设置：哨兵用户名
     *
     * @param sentinelUsername 哨兵用户名
     */
    public void setSentinelUsername(String sentinelUsername) {
        this.sentinelUsername = sentinelUsername;
    }

    /**
     * 获取：哨兵密码
     *
     * @return {@link String} – 哨兵密码
     */
    public String getSentinelPassword() {
        return sentinelPassword;
    }

    /**
     * 设置：哨兵密码
     *
     * @param sentinelPassword 哨兵密码
     */
    public void setSentinelPassword(String sentinelPassword) {
        this.sentinelPassword = sentinelPassword;
    }

    /**
     * 获取：哨兵节点列表
     *
     * @return {@link List} – 哨兵节点列表
     */
    public List<RedisNode> getNodes() {
        return nodes;
    }

    /**
     * 设置：哨兵节点列表
     *
     * @param nodes 哨兵节点列表
     */
    public void setNodes(List<RedisNode> nodes) {
        this.nodes = nodes;
    }

}
