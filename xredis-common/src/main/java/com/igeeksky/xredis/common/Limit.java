package com.igeeksky.xredis.common;

/**
 * 给定偏移量和最大条目数，用以获取指定数据集的一部分
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/24
 */
public class Limit {

    private static final Limit UNLIMITED = new Limit(null, null);

    private final Long offset;

    private final Long count;

    /**
     * 根据给定的 {@code offset} 和 {@code count} 创建 {@link Limit}
     *
     * @param offset 偏移量
     * @param count  最大条目数
     */
    private Limit(Long offset, Long count) {
        this.offset = offset;
        this.count = count;
    }

    /**
     * 偏移量
     *
     * @return 偏移量
     */
    public Long getOffset() {
        return offset;
    }

    /**
     * 最大条目数
     *
     * @return 最大条目数
     */
    public Long getCount() {
        return count;
    }

    /**
     * 是否无限制
     *
     * @return 是否无限制
     */
    public boolean isUnlimited() {
        return this == UNLIMITED;
    }

    /**
     * 无限制
     *
     * @return 无限制
     */
    public static Limit unlimited() {
        return UNLIMITED;
    }

    /**
     * 根据给定的 {@code offset} 和 {@code count} 创建 {@link Limit}
     *
     * @param offset 偏移量
     * @param count  最大条目数
     * @return {@link Limit}
     */
    public static Limit create(long offset, long count) {
        return new Limit(offset, count);
    }

    /**
     * 根据给定的 {@code count} 创建 {@link Limit}
     *
     * @param count 最大条目数
     * @return {@link Limit}
     */
    public static Limit from(long count) {
        return new Limit(0L, count);
    }

}
