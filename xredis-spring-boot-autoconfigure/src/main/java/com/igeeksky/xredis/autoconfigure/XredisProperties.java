package com.igeeksky.xredis.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Lettuce 自动配置属性
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-26
 */
@Configuration
@ConfigurationProperties(prefix = "xredis")
public class XredisProperties {

    /**
     * lettuce 配置
     * <p>
     * 如不使用 Lettuce，可删除此配置
     */
    private LettuceConfig lettuce;

    public LettuceConfig getLettuce() {
        return lettuce;
    }

    public void setLettuce(LettuceConfig lettuce) {
        this.lettuce = lettuce;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("{");
        builder.append("\"lettuce:\"");
        if (lettuce != null) {
            builder.append(lettuce);
        } else {
            builder.append("[]");
        }
        return builder.append("}").toString();
    }

}
