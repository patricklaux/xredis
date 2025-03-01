package com.igeeksky.xredis.common.stream;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * 消费者组读取消息选项
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/20
 */
public class XReadOptions {

    private final Long block;

    private final Long count;

    private final boolean noack;

    /**
     * 私有构造器
     *
     * @param builder {@link Builder}
     */
    private XReadOptions(Builder builder) {
        this.block = builder.block;
        this.count = builder.count;
        this.noack = builder.noack;
    }

    /**
     * 是否包含有效参数
     *
     * @return {@code boolean} – {@code false}，无需附加可选参数；{@code true}，需要附加可选参数
     */
    public boolean valid() {
        return block != null || count != null || noack;
    }

    /**
     * 最大阻塞时长（毫秒）
     *
     * @return {@code Long} – 阻塞时长（毫秒）
     */
    public Long getBlock() {
        return block;
    }

    /**
     * 最大读取数量
     *
     * @return {@code Long} – 读取数量
     */
    public Long getCount() {
        return count;
    }

    /**
     * 是否自动确认
     *
     * @return {@code boolean} – 是否自动确认。 {@code true} – 自动确认；{@code false} – 手动确认
     */
    public boolean isNoack() {
        return noack;
    }

    /**
     * 创建 XReadOptions-builder
     *
     * @return {@link XReadOptions.Builder}
     */
    public static XReadOptions.Builder builder() {
        return new Builder();
    }

    /**
     * XReadOptions-builder
     */
    public static class Builder {
        private Long block;
        private Long count;
        private boolean noack;

        /**
         * 私有构造器
         */
        private Builder() {
        }

        /**
         * 设置：最大阻塞时长（单位：毫秒）
         *
         * @param block 最大阻塞时长 <br>
         *              1. {@code block == null} – 不阻塞；<br>
         *              2. {@code block == 0} – 无限阻塞，直到有新消息到达；<br>
         *              3. {@code block > 0} – 最大阻塞时长，直到有新消息到达或超过此设定时长。
         * @return {@link XReadOptions}
         */
        public Builder block(Long block) {
            Assert.isTrue(block == null || block >= 0, "block must be greater or equal 0");
            this.block = block;
            return this;
        }

        /**
         * 设置：最大读取数量
         *
         * @param count 最大读取数量 <br>
         *              1. {@code count == null} – 无数量限制；<br>
         *              2. {@code count > 0} – 最大读取数量。
         * @return {@link XReadOptions}
         */
        public Builder count(Long count) {
            Assert.isTrue(count == null || count > 0, "count must be greater than 0");
            this.count = count;
            return this;
        }

        /**
         * 设置：是否自动确认（仅用于消费者组）
         * <p>
         * 默认 {@code false}
         * <p>
         * 当值为 {@code false} 时，消息消费完成后需手动调用 {@code xack} 命令确认消息。
         *
         * @param noack 是否自动确认 {@code true} – 自动确认；{@code false} – 手动确认
         * @return {@link XReadOptions.Builder}
         * @see <a href="https://redis.io/docs/latest/commands/xreadgroup/">XREADGROUP</a>
         * @see <a href="https://redis.io/docs/latest/commands/xack/">XACK</a>
         */
        public Builder noack(boolean noack) {
            this.noack = noack;
            return this;
        }

        /**
         * 根据已设置参数创建 {@link XReadOptions}
         *
         * @return {@link XReadOptions}
         */
        public XReadOptions build() {
            return new XReadOptions(this);
        }

    }

}