package com.igeeksky.xredis.common.stream;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * 消费者组
 *
 * @param <K>   键类型
 * @param group 消费者组名称
 * @param name  消费者名称
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record XGroupConsumer<K>(K group, K name) {

    public XGroupConsumer {
        Assert.notNull(group, "group must not be null.");
        Assert.notNull(name, "name must not be null.");
    }

}
