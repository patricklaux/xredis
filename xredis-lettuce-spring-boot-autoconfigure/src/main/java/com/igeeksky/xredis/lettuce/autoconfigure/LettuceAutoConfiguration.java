package com.igeeksky.xredis.lettuce.autoconfigure;

import com.igeeksky.xredis.common.RedisConfigException;
import com.igeeksky.xredis.lettuce.LettuceClusterFactory;
import com.igeeksky.xredis.lettuce.LettuceSentinelFactory;
import com.igeeksky.xredis.lettuce.LettuceStandaloneFactory;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import com.igeeksky.xredis.lettuce.api.RedisOperatorFactory;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Lettuce 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@AutoConfigureAfter(LettuceClientResourcesConfiguration.class)
@EnableConfigurationProperties(LettuceProperties.class)
public class LettuceAutoConfiguration {

    private final LettuceProperties lettuceProperties;

    /**
     * Lettuce 自动配置
     *
     * @param lettuceProperties 配置项
     */
    public LettuceAutoConfiguration(LettuceProperties lettuceProperties) {
        this.lettuceProperties = lettuceProperties;
    }

    /**
     * 根据配置创建 {@link RedisOperatorFactory}
     *
     * @param clientResources Lettuce 客户端资源
     * @param customizers     自定义的客户端选项（部分选项需通过编程实现）
     * @return {@link RedisOperatorFactory} – Redis 客户端工厂
     */
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(name = "lettuceOperatorFactory", destroyMethod = "shutdown")
    RedisOperatorFactory lettuceOperatorFactory(ClientResourcesHolder clientResources,
                                                ObjectProvider<ClientOptionsBuilderCustomizer> customizers) {

        String id = lettuceProperties.getId();
        LettuceSentinel sentinel = lettuceProperties.getSentinel();
        if (sentinel != null) {
            LettuceSentinelConfig config = LettuceConfigHelper.createConfig(id, sentinel);
            ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(), sentinel.getClientOptions(), customizers);
            return new LettuceSentinelFactory(config, options, clientResources.get());
        }
        LettuceCluster cluster = lettuceProperties.getCluster();
        if (cluster != null) {
            LettuceClusterConfig config = LettuceConfigHelper.createConfig(id, cluster);
            ClusterClientOptions options = ClientOptionsHelper.clusterClientOptions(config.getId(), cluster.getClientOptions(), customizers);
            return new LettuceClusterFactory(config, options, clientResources.get());
        }
        LettuceStandalone standalone = lettuceProperties.getStandalone();
        if (standalone != null) {
            LettuceStandaloneConfig config = LettuceConfigHelper.createConfig(id, standalone);
            ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(), standalone.getClientOptions(), customizers);
            return new LettuceStandaloneFactory(config, options, clientResources.get());
        }
        throw new RedisConfigException("xredis.lettuce:[" + id + "] init error." + lettuceProperties);
    }

    /**
     * 创建支持操作 String 类型的 {@link RedisOperator}
     *
     * @param redisOperatorFactory RedisOperatorFactory
     * @return {@link RedisOperator} – 支持操作 String 类型
     */
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(name = "stringRedisOperator", destroyMethod = "closeAsync")
    RedisOperator<String, String> stringRedisOperator(RedisOperatorFactory redisOperatorFactory) {
        return redisOperatorFactory.redisOperator(StringCodec.UTF8);
    }

}