package com.igeeksky.xredis.lettuce.autoconfigure;

import com.igeeksky.xredis.lettuce.cluster.LettuceClusterFactory;
import com.igeeksky.xredis.lettuce.LettuceSentinelFactory;
import com.igeeksky.xredis.lettuce.LettuceStandaloneFactory;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
import com.igeeksky.xredis.common.RedisConfigException;
import com.igeeksky.xredis.lettuce.config.ClientOptionsBuilderCustomizer;
import com.igeeksky.xredis.lettuce.config.LettuceClusterConfig;
import com.igeeksky.xredis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.xredis.lettuce.config.LettuceStandaloneConfig;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceConfigHelper;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Xredis 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LettuceClientResourcesConfiguration.class)
@EnableConfigurationProperties(XredisProperties.class)
public class XredisAutoConfiguration {

    private final XredisProperties xredisProperties;

    /**
     * Xredis 配置
     *
     * @param xredisProperties 配置项
     */
    public XredisAutoConfiguration(XredisProperties xredisProperties) {
        this.xredisProperties = xredisProperties;
    }

    @Bean(destroyMethod = "shutdown")
    RedisOperatorFactory redisOperatorFactory(ClientResources clientResources,
                                              ObjectProvider<ClientOptionsBuilderCustomizer> customizers) {

        String id = xredisProperties.getId();

        LettuceSentinel sentinel = xredisProperties.getSentinel();
        if (sentinel != null) {
            LettuceSentinelConfig config = LettuceConfigHelper.createConfig(id, sentinel);
            ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(), sentinel.getClientOptions(), customizers);
            return new LettuceSentinelFactory(config, options, clientResources);
        }
        LettuceCluster cluster = xredisProperties.getCluster();
        if (cluster != null) {
            LettuceClusterConfig config = LettuceConfigHelper.createConfig(id, cluster);
            ClusterClientOptions options = ClientOptionsHelper.clusterClientOptions(config.getId(), cluster.getClientOptions(), customizers);
            return new LettuceClusterFactory(config, options, clientResources);
        }
        LettuceStandalone standalone = xredisProperties.getStandalone();
        if (standalone != null) {
            LettuceStandaloneConfig config = LettuceConfigHelper.createConfig(id, standalone);
            ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(), standalone.getClientOptions(), customizers);
            return new LettuceStandaloneFactory(config, options, clientResources);
        }
        throw new RedisConfigException("xredis.lettuce:[" + id + "] init error." + xredisProperties);
    }

    @Bean(name = "stringRedisOperator")
    RedisOperator<String, String> stringRedisOperator(RedisOperatorFactory redisOperatorFactory) {
        return redisOperatorFactory.redisOperator(StringCodec.UTF8);
    }

}