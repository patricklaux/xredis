package com.igeeksky.xredis.autoconfigure;


import com.igeeksky.xredis.api.RedisOperatorFactory;
import com.igeeksky.xredis.props.LettuceCluster;
import com.igeeksky.xredis.props.LettuceSentinel;
import com.igeeksky.xredis.props.LettuceStandalone;
import com.igeeksky.xtool.core.json.SimpleJSON;

/**
 * Lettuce 配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-27
 */
public class LettuceConfig {

    private String id;

    private LettuceCluster cluster;

    private LettuceSentinel sentinel;

    private LettuceStandalone standalone;

    /**
     * 默认构造函数
     */
    public LettuceConfig() {
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
     * 集群模式配置
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 初始化 Lettuce 客户端时，优先读取 sentinel 配置，其次是 cluster 配置，最后才是 standalone 配置。
     * <p>
     * 1、首先，如 sentinel 配置不为空，使用 sentinel 配置，忽略 cluster 配置 和 standalone 配置。<br>
     * 2、其次，如 sentinel 配置为空，读取 cluster 配置。如 cluster 配置不为空，使用 cluster 配置，忽略 standalone 配置。<br>
     * 3、最后，如 cluster 配置为空，则读取 standalone 配置。
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

    /**
     * 哨兵模式配置
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 初始化 Lettuce 客户端时，优先读取 sentinel 配置，其次是 cluster 配置，最后才是 standalone 配置。
     * <p>
     * 1、首先，如 sentinel 配置不为空，使用 sentinel 配置，忽略 cluster 配置 和 standalone 配置。<br>
     * 2、其次，如 sentinel 配置为空，读取 cluster 配置。如 cluster 配置不为空，使用 cluster 配置，忽略 standalone 配置。<br>
     * 3、最后，如 cluster 配置为空，则读取 standalone 配置。
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
     * 单机模式 或 副本集模式 配置
     * <p>
     * <b>注意事项：</b>
     * <p>
     * 初始化 Lettuce 客户端时，优先读取 sentinel 配置，其次是 cluster 配置，最后才是 standalone 配置。
     * <p>
     * 1、首先，如 sentinel 配置不为空，使用 sentinel 配置，忽略 cluster 配置 和 standalone 配置。<br>
     * 2、其次，如 sentinel 配置为空，读取 cluster 配置。如 cluster 配置不为空，使用 cluster 配置，忽略 standalone 配置。<br>
     * 3、最后，如 cluster 配置为空，则读取 standalone 配置。
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

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}
