package com.igeeksky.xredis.common.stream;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * 流读偏移
 *
 * @author Patrick.Lau
 * @see <a href="https://redis.io/commands/xread">XREAD</a>
 * @see <a href="https://redis.io/docs/latest/commands/xreadgroup/">XREADGROUP</a>
 * @since 1.0.0 2024/7/20
 */
public class XStreamOffset<K> {

    /**
     * 从第一个消息开始读取
     */
    public static final String first = "0-0";

    /**
     * 读取新到达的消息（适用于阻塞模式）
     */
    public static final String latest = "$";

    /**
     * 读取大于最后消费位置的消息（适用于消费者组）
     */
    public static final String lastConsumed = ">";

    private final K key;

    private final String offset;

    /**
     * 构造函数
     *
     * @param key    流名称
     * @param offset 偏移量
     */
    private XStreamOffset(K key, String offset) {
        Assert.notNull(key, "key must not be null");
        Assert.hasText(offset, "offset must not be null or empty");
        this.key = key;
        this.offset = offset;
    }

    /**
     * 获取流名称
     *
     * @return {@code K} – 流名称
     */
    public K getKey() {
        return key;
    }

    /**
     * 获取偏移量
     *
     * @return {@link String} – 偏移量
     */
    public String getOffset() {
        return offset;
    }

    /**
     * 指定读取偏移量
     *
     * @param key    流名称
     * @param offset 偏移量
     * @return {@code ReadOffset}
     */
    public static <K> XStreamOffset<K> from(K key, String offset) {
        return new XStreamOffset<>(key, offset);
    }

    /**
     * 从第一个消息开始读取
     *
     * @param key 流名称
     * @return {@code ReadOffset}
     */
    public static <K> XStreamOffset<K> first(K key) {
        return new XStreamOffset<>(key, first);
    }

    /**
     * 读取新到达的消息（适用于阻塞模式）
     *
     * @param key 流名称
     * @return {@code ReadOffset}
     */
    public static <K> XStreamOffset<K> latest(K key) {
        return new XStreamOffset<>(key, latest);
    }

    /**
     * 读取大于最后消费位置的消息（适用于消费者组）
     *
     * @param key 流名称
     * @return {@code ReadOffset}
     */
    public static <K> XStreamOffset<K> lastConsumed(K key) {
        return new XStreamOffset<>(key, lastConsumed);
    }

}
