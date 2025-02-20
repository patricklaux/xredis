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
     * Creates a {@link Limit} given {@code offset} and {@code count}.
     *
     * @param offset 偏移量
     * @param count  最大条目数
     */
    private Limit(Long offset, Long count) {
        this.offset = offset;
        this.count = count;
    }

    public Long getOffset() {
        return offset;
    }

    public Long getCount() {
        return count;
    }

    public boolean isUnlimited() {
        return this == UNLIMITED;
    }

    /**
     * @return an unlimited limit.
     */
    public static Limit unlimited() {
        return UNLIMITED;
    }

    /**
     * Creates a {@link Limit} given {@code offset} and {@code count}.
     *
     * @param offset the offset.
     * @param count  the limit count.
     * @return the {@link Limit}
     */
    public static Limit create(long offset, long count) {
        return new Limit(offset, count);
    }

    public static Limit from(long count) {
        return new Limit(0L, count);
    }

}
