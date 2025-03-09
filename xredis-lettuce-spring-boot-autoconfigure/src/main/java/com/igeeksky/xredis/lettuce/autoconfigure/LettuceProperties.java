package com.igeeksky.xredis.lettuce.autoconfigure;

import com.igeeksky.xredis.common.RedisConfigException;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;
import org.springframework.context.annotation.Role;

/**
 * Lettuce 自动配置属性
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConfigurationProperties(prefix = "xredis.lettuce")
public class LettuceProperties {

    private String id = "lettuce";

    @NestedConfigurationProperty
    private LettuceStandalone standalone;

    @NestedConfigurationProperty
    private LettuceSentinel sentinel;

    @NestedConfigurationProperty
    private LettuceCluster cluster;

    /**
     * 默认构造器
     */
    public LettuceProperties() {
    }

    /**
     * {@link RedisOperatorFactory} 唯一标识
     *
     * @return {@code String} – {@link RedisOperatorFactory} 唯一标识
     */
    public String getId() {
        return id;
    }

    /**
     * {@link RedisOperatorFactory} 唯一标识
     *
     * @param id {@link RedisOperatorFactory} 唯一标识
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 单机模式 或 副本集模式 配置
     * <p>
     * <b>注意：</b><br>
     * 初始化客户端时，按（sentinel → cluster  → standalone）顺序读取配置，
     * 任一配置不为空，则自动使用该配置，忽略后续其它配置。<br>
     * 如果所有配置均为空，则抛出 {@link RedisConfigException} 异常。
     *
     * @return {@link LettuceStandalone} – 单机模式 或 副本集模式 配置
     */
    public LettuceStandalone getStandalone() {
        return standalone;
    }

    /**
     * 单机模式 或 副本集模式 配置
     *
     * @param standalone 单机模式 或 副本集模式 配置
     */
    public void setStandalone(LettuceStandalone standalone) {
        this.standalone = standalone;
    }

    /**
     * 哨兵模式配置
     * <p>
     * <b>注意：</b><br>
     * 初始化客户端时，按（sentinel → cluster  → standalone）顺序读取配置，
     * 任一配置不为空，则自动使用该配置，忽略后续其它配置。<br>
     * 如果所有配置均为空，则抛出 {@link RedisConfigException} 异常。
     *
     * @return {@link LettuceSentinel} – 哨兵模式配置
     */
    public LettuceSentinel getSentinel() {
        return sentinel;
    }

    /**
     * 哨兵模式配置
     *
     * @param sentinel 哨兵模式配置
     */
    public void setSentinel(LettuceSentinel sentinel) {
        this.sentinel = sentinel;
    }

    /**
     * 集群模式配置
     * <p>
     * <b>注意：</b><br>
     * 初始化客户端时，按（sentinel → cluster  → standalone）顺序读取配置，
     * 任一配置不为空，则自动使用该配置，忽略后续其它配置。<br>
     * 如果所有配置均为空，则抛出 {@link RedisConfigException} 异常。
     *
     * @return {@link LettuceCluster} – 集群模式配置
     */
    public LettuceCluster getCluster() {
        return cluster;
    }

    /**
     * 集群模式配置
     *
     * @param cluster 集群模式配置
     */
    public void setCluster(LettuceCluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"id\":\"").append((id != null) ? id : "").append("\", ")
                .append("\"standalone\":").append((standalone != null) ? standalone : "{}").append(", ")
                .append("\"sentinel\":").append((sentinel != null) ? sentinel : "{}").append(", ")
                .append("\"cluster\":").append((cluster != null) ? cluster : "{}")
                .append("}");
        return builder.toString();
    }

}
