package com.igeeksky.xredis.stream;

import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.XReadArgs;

/**
 * Stream 读选项
 *
 * @param block 拉取消息的最大阻塞时长，单位毫秒 <br>
 *              1. 小于 0 时不阻塞；
 *              2. 等于 0 时无限阻塞，直到有新消息到达；
 *              3. 大于 0 时为最大阻塞时长，直到有新消息到达或已达设定的最大阻塞时长。
 * @param count 单次任务拉取消息的最大数量 {@code 0 < count <= 2^29}
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record XReadOptions(long block, int count) {

    private static final int MAX_COUNT = 1 << 29;

    /**
     * 执行数据校验
     *
     * @param block 拉取消息的最大阻塞时长，单位毫秒 <br>
     *              1. 小于 0 时不阻塞；
     *              2. 等于 0 时无限阻塞，直到有新消息到达；
     *              3. 大于 0 时为最大阻塞时长，直到有新消息到达或已达设定的最大阻塞时长。
     * @param count 单次任务拉取消息的最大数量 {@code 0 < count <= 2^29}
     */
    public XReadOptions {
        Assert.isTrue(count > 0, "count must be greater than 0");
        Assert.isTrue(count <= MAX_COUNT, "count must be less than or equal to " + MAX_COUNT);
    }

    /**
     * 转换为 Lettuce 流读取参数
     *
     * @return {@link XReadArgs} 流读取参数
     */
    public XReadArgs to() {
        if (block >= 0) {
            return XReadArgs.Builder.block(block).count(count);
        } else {
            return XReadArgs.Builder.count(count);
        }
    }

}
