package com.igeeksky.xredis.common.stream.container;

import com.igeeksky.xredis.common.stream.XReadOptions;
import com.igeeksky.xtool.core.lang.Assert;

/**
 * StreamContainer 特殊的流读取选项
 *
 * @param block 最大阻塞时长（单位：毫秒） <br>
 *              1. {@code block == null} – 不阻塞；<br>
 *              2. {@code block == 0} – 无限阻塞，直到有新消息到达；<br>
 *              3. {@code block > 0} – 直到有新消息到达或超过此设定时长。
 * @param count 最大读取数量 {@code 0 < count <= (1 << 29)}
 * @param noack 是否自动确认（仅用于消费者组）。 <br>
 *              1. {@code true} – 自动确认； <br>
 *              2. {@code false} – 手动确认。
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record ReadOptions(Long block, int count, boolean noack) {

    /**
     * 最大读取数量(536870912)
     */
    public static final int MAX_COUNT = 536870912;

    /**
     * 参数校验
     */
    public ReadOptions {
        Assert.isTrue(count > 0, "count must be greater than 0");
        Assert.isTrue(count <= 536870912, "count must be less than or equal to " + MAX_COUNT);
        Assert.isTrue(block == null || block >= 0, "block must be greater than or equal to 0");
    }

    /**
     * 转换为 {@link XReadOptions}
     *
     * @return {@link XReadOptions}
     */
    public XReadOptions to() {
        return XReadOptions.builder().block(block).count((long) count).noack(noack).build();
    }

}
