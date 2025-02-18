package com.igeeksky.xredis.common.stream;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * 消费组信息
 *
 * @param <K>   键类型
 * @param group 消费组名称（不能为空）
 * @param name  消费者名称（不能为空）
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record XGroupConsumer<K>(K group, K name) {

    /**
     * 消费组信息
     *
     * @param group 消费组名称（不能为空）
     * @param name  消费者名称（不能为空）
     */
    public XGroupConsumer {
        Assert.notNull(group, "group must not be null.");
        Assert.notNull(name, "name must not be null.");
    }

}
