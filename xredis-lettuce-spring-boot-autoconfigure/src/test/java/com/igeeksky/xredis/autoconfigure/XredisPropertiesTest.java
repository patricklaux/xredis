package com.igeeksky.xredis.autoconfigure;

import com.igeeksky.xredis.lettuce.autoconfigure.XredisProperties;
import com.igeeksky.xredis.lettuce.props.LettuceCluster;
import com.igeeksky.xredis.lettuce.props.LettuceSentinel;
import com.igeeksky.xredis.lettuce.props.LettuceStandalone;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 测试 {@link XredisProperties}
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
class XredisPropertiesTest {

    /**
     * 测试 {@link XredisProperties#toString()} 是否为 JSON 格式
     */
    @Test
    void testToString() {
        XredisProperties properties = new XredisProperties();
        properties.setId("id");
        properties.setStandalone(new LettuceStandalone());
        properties.setCluster(new LettuceCluster());
        properties.setSentinel(new LettuceSentinel());
        System.out.println(properties);
        String expected = "{\"id\":\"id\", \"standalone\":{\"database\":0}, \"sentinel\":{\"database\":0}, \"cluster\":{\"database\":0}}";
        Assertions.assertEquals(expected, properties.toString());
    }

}