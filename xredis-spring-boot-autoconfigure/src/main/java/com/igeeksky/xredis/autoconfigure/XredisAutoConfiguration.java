package com.igeeksky.xredis.autoconfigure;

import com.igeeksky.redis.LettuceClusterFactory;
import com.igeeksky.redis.LettuceSentinelFactory;
import com.igeeksky.redis.LettuceStandaloneFactory;
import com.igeeksky.redis.RedisConfigException;
import com.igeeksky.redis.api.RedisOperator;
import com.igeeksky.redis.api.RedisOperatorFactory;
import com.igeeksky.redis.config.ClientOptionsBuilderCustomizer;
import com.igeeksky.redis.config.LettuceClusterConfig;
import com.igeeksky.redis.config.LettuceSentinelConfig;
import com.igeeksky.redis.config.LettuceStandaloneConfig;
import com.igeeksky.redis.props.LettuceCluster;
import com.igeeksky.redis.props.LettuceConfigHelper;
import com.igeeksky.redis.props.LettuceSentinel;
import com.igeeksky.redis.props.LettuceStandalone;
import com.igeeksky.xredis.configure.ClientOptionsHelper;
import com.igeeksky.xredis.configure.LettuceClientResourcesConfiguration;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.resource.ClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Xredis 自动配置
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-18
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureAfter(LettuceClientResourcesConfiguration.class)
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

        LettuceConfig lettuce = xredisProperties.getLettuce();
        String id = lettuce.getId();
        List<ClientOptionsBuilderCustomizer> clientOptionsCustomizers = customizers.orderedStream().toList();

        LettuceSentinel sentinel = lettuce.getSentinel();
        if (sentinel != null) {
            LettuceSentinelConfig config = LettuceConfigHelper.createConfig(id, sentinel);
            ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(), sentinel.getClientOptions(), clientOptionsCustomizers);
            return new LettuceSentinelFactory(config, options, clientResources);
        }
        LettuceCluster cluster = lettuce.getCluster();
        if (cluster != null) {
            LettuceClusterConfig config = LettuceConfigHelper.createConfig(id, cluster);
            ClusterClientOptions options = ClientOptionsHelper.clusterClientOptions(config.getId(), cluster.getClientOptions(), clientOptionsCustomizers);
            return new LettuceClusterFactory(config, options, clientResources);
        }
        LettuceStandalone standalone = lettuce.getStandalone();
        if (standalone != null) {
            LettuceStandaloneConfig config = LettuceConfigHelper.createConfig(id, standalone);
            ClientOptions options = ClientOptionsHelper.clientOptions(config.getId(), standalone.getClientOptions(), clientOptionsCustomizers);
            return new LettuceStandaloneFactory(config, options, clientResources);
        }
        throw new RedisConfigException("xredis.lettuce:[" + id + "] init error." + lettuce);
    }

    @Bean(name = "stringRedisOperator")
    RedisOperator<String, String> stringRedisOperator(RedisOperatorFactory redisOperatorFactory) {
        return redisOperatorFactory.redisOperator(StringCodec.UTF8);
    }

}