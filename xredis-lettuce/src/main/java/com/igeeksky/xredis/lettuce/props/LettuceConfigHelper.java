package com.igeeksky.xredis.lettuce.props;


import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.lettuce.config.*;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.ReadFrom;
import io.lettuce.core.SslVerifyMode;

import java.util.ArrayList;
import java.util.List;

/**
 * lettuce 配置辅助类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public final class LettuceConfigHelper {

    /**
     * 私有构造函数
     */
    private LettuceConfigHelper() {
    }

    /**
     * 创建 lettuce 单机模式 或 副本集模式 配置
     *
     * @param id         {@link RedisOperatorFactory} 唯一标识
     * @param standalone 配置项
     * @return {@link LettuceStandaloneConfig} – lettuce 单机模式 或 副本集模式 配置
     */
    public static LettuceStandaloneConfig createConfig(String id, LettuceStandalone standalone) {
        LettuceStandaloneConfig config = new LettuceStandaloneConfig();
        setGeneric(id, standalone, config);

        String node = StringUtils.trimToNull(standalone.getNode());
        if (node != null) {
            config.setNode(new RedisNode(node));
        }

        String readFrom = StringUtils.trimToNull(standalone.getReadFrom());
        if (readFrom != null) {
            config.setReadFrom(ReadFrom.valueOf(readFrom));
        }

        config.setNodes(convertNodes(standalone.getNodes()));

        return config;
    }


    /**
     * 创建 lettuce 哨兵模式配置
     *
     * @param id       {@link RedisOperatorFactory} 唯一标识
     * @param sentinel 哨兵模式配置
     * @return {@link LettuceSentinelConfig} – lettuce 哨兵模式配置
     */
    public static LettuceSentinelConfig createConfig(String id, LettuceSentinel sentinel) {
        LettuceSentinelConfig config = new LettuceSentinelConfig();
        setGeneric(id, sentinel, config);

        String masterId = StringUtils.trimToNull(sentinel.getMasterId());
        Assert.notNull(masterId, () -> "Id:[" + id + "] sentinel:master-id must not be null or empty");
        config.setMasterId(masterId);

        List<RedisNode> sentinels = convertNodes(sentinel.getNodes());
        Assert.notEmpty(sentinels, () -> "Id:[" + id + "] sentinel:nodes must not be empty");
        config.setNodes(sentinels);

        String readFrom = StringUtils.trimToNull(sentinel.getReadFrom());
        if (readFrom != null) {
            config.setReadFrom(ReadFrom.valueOf(readFrom));
        }

        String sentinelUsername = StringUtils.trimToNull(sentinel.getUsername());
        if (sentinelUsername != null) {
            config.setSentinelUsername(sentinelUsername);
        }

        String sentinelPassword = StringUtils.trimToNull(sentinel.getPassword());
        if (sentinelPassword != null) {
            config.setSentinelPassword(sentinelPassword);
        }

        return config;
    }


    /**
     * 创建 lettuce 集群模式配置
     *
     * @param id      {@link RedisOperatorFactory} 唯一标识
     * @param cluster 集群模式配置
     * @return {@link LettuceClusterConfig} – lettuce 集群模式配置
     */
    public static LettuceClusterConfig createConfig(String id, LettuceCluster cluster) {
        LettuceClusterConfig config = new LettuceClusterConfig();
        setGeneric(id, cluster, config);

        List<RedisNode> nodes = convertNodes(cluster.getNodes());
        Assert.notEmpty(nodes, () -> "Id:[" + id + "] cluster:nodes must not be empty");
        config.setNodes(nodes);

        String readFrom = StringUtils.trimToNull(cluster.getReadFrom());
        if (readFrom != null) {
            config.setReadFrom(ReadFrom.valueOf(readFrom));
        }

        return config;
    }

    /**
     * 公共配置赋值
     *
     * @param id       {@link RedisOperatorFactory} 唯一标识
     * @param original 用户输入配置
     * @param config   最终 RedisOperatorFactory 所需配置
     */
    private static void setGeneric(String id, LettuceGeneric original, LettuceGenericConfig config) {
        id = StringUtils.trimToNull(id);
        Assert.notNull(id, "lettuce:id must not be null or empty");

        config.setId(id);

        String username = StringUtils.trimToNull(original.getUsername());
        if (username != null) {
            config.setUsername(username);
        }

        String password = StringUtils.trimToNull(original.getPassword());
        if (password != null) {
            config.setPassword(password);
        }

        config.setDatabase(original.getDatabase());

        String clientName = StringUtils.trimToNull(original.getClientName());
        if (clientName != null) {
            config.setClientName(clientName);
        }

        Boolean ssl = original.getSsl();
        if (ssl != null) {
            config.setSsl(ssl);
        }

        Boolean startTls = original.getStartTls();
        if (startTls != null) {
            config.setStartTls(startTls);
        }

        String sslVerifyMode = StringUtils.toUpperCase(original.getSslVerifyMode());
        if (StringUtils.hasLength(sslVerifyMode)) {
            config.setSslVerifyMode(SslVerifyMode.valueOf(sslVerifyMode));
        }

        Long timeout = original.getTimeout();
        if (timeout != null) {
            config.setTimeout(timeout);
        }

        Long shutdownTimeout = original.getShutdownTimeout();
        if (shutdownTimeout != null) {
            config.setShutdownTimeout(shutdownTimeout);
        }

        Long shutdownQuietPeriod = original.getShutdownQuietPeriod();
        if (shutdownQuietPeriod != null) {
            config.setShutdownQuietPeriod(shutdownQuietPeriod);
        }
    }

    /**
     * 节点字符串转 RedisNode 列表
     *
     * @param sources 节点字符串集合
     * @return {@code List<RedisNode>} – RedisNode 列表
     */
    private static List<RedisNode> convertNodes(List<String> sources) {
        List<RedisNode> nodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(sources)) {
            sources.forEach(node -> nodes.add(new RedisNode(node)));
        }
        return nodes;
    }

}
