package com.igeeksky.xredis.lettuce.autoconfigure;

import io.lettuce.core.resource.DefaultClientResources;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.data.redis.ClientResourcesBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * Lettuce 客户端资源自动配置
 *
 * @author Patrick.Lau
 * @since 1.0.0 2025-02-10
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class LettuceClientResourcesConfiguration {

    /**
     * 无参构造器
     */
    public LettuceClientResourcesConfiguration() {
    }

    @ConditionalOnMissingBean(ClientResourcesHolder.class)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean(name = "clientResourcesHolder", destroyMethod = "shutdown")
    ClientResourcesHolder clientResourcesHolder(ObjectProvider<ClientResourcesBuilderCustomizer> customizers) {
        DefaultClientResources.Builder builder = DefaultClientResources.builder();
        customizers.orderedStream().forEach((customizer) -> customizer.customize(builder));
        return new ClientResourcesHolder(builder.build());
    }

}