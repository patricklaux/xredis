package com.igeeksky.xredis.common.stream;

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
     * 私有构造方法
     *
     * @param block 阻塞时长（毫秒）
     * @param count 读取数量
     * @param noack 是否无需确认
     */
    private XReadOptions(Long block, Long count, boolean noack) {
        this.block = block;
        this.count = count;
        this.noack = noack;
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
     * 获取：阻塞时长（毫秒）
     *
     * @return {@code Long} – 阻塞时长（毫秒）
     */
    public Long getBlock() {
        return block;
    }

    /**
     * 获取：读取数量
     *
     * @return {@code Long} – 读取数量
     */
    public Long getCount() {
        return count;
    }

    /**
     * 获取：是否无需确认
     *
     * @return {@code boolean} – {@code true}，无需确认；{@code false}，需要确认
     */
    public boolean isNoack() {
        return noack;
    }

    /**
     * 设置：阻塞时长（毫秒）
     * <p>
     * Stream 读选项
     *
     * @param block 拉取消息的阻塞时长，单位毫秒 <br>
     *              1. 小于 0 时不阻塞；<br>
     *              2. 等于 0 时无限阻塞，直到有新消息到达；<br>
     *              3. 大于 0 时为最大阻塞时长，直到有新消息到达或超过此设定的时长。
     * @return {@link XReadOptions}
     */
    public static XReadOptions block(Long block) {
        return new XReadOptions(block, null, false);
    }

    /**
     * 设置：读取数量
     *
     * @param count 读取数量
     * @return {@link XReadOptions}
     */
    public static XReadOptions count(Long count) {
        return new XReadOptions(null, count, false);
    }

    /**
     * 设置为无需确认
     *
     * @return {@link XReadOptions}
     */
    public static XReadOptions noack() {
        return new XReadOptions(null, null, true);
    }

    /**
     * 设置：阻塞时长（毫秒） 和 读取数量
     *
     * @param block 阻塞时长（毫秒）
     * @param count 读取数量
     * @return {@link XReadOptions}
     */
    public static XReadOptions of(Long block, Long count) {
        return new XReadOptions(block, count, false);
    }

    /**
     * 设置：阻塞时长（毫秒）、读取数量、是否无需确认
     *
     * @param block 阻塞时长（毫秒）
     * @param count 读取数量
     * @param noack 是否无需确认
     * @return {@link XReadOptions}
     */
    public static XReadOptions of(Long block, Long count, boolean noack) {
        return new XReadOptions(block, count, noack);
    }

}