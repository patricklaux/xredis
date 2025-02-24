package com.igeeksky.xredis.common;

import com.igeeksky.xtool.core.lang.Assert;

import java.util.Objects;

/**
 * 数据范围
 *
 * @param <T> 限定值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class Range<T> {

    private final Boundary<T> lower;
    private final Boundary<T> upper;

    /**
     * 根据给定的上限和下限，创建 {@link Range} 对象
     *
     * @param lower 下限（不能为空）
     * @param upper 上限（不能为空）
     */
    private Range(Boundary<T> lower, Boundary<T> upper) {
        Assert.notNull(lower, "Lower bound must not be null");
        Assert.notNull(upper, "Upper bound must not be null");
        this.lower = lower;
        this.upper = upper;
    }

    /**
     * 获取下限
     *
     * @return 下限
     */
    public Boundary<T> getLower() {
        return lower;
    }

    /**
     * 获取上限
     *
     * @return 上限
     */
    public Boundary<T> getUpper() {
        return upper;
    }

    /**
     * 判断是否无限制
     *
     * @return {@code true} 无限制，其它情况则返回 {@code false}
     */
    public boolean isUnbounded() {
        return lower.isUnbounded() && upper.isUnbounded();
    }

    /**
     * 创建一个无上下限的 {@link Range}
     *
     * @param <T> 限定值类型
     * @return 无上下限的 {@link Range} 对象
     */
    public static <T> Range<T> unbounded() {
        return new Range<>(Boundary.unbounded(), Boundary.unbounded());
    }

    /**
     * 开区间（不包含给定的上限和下限）
     *
     * @param lower 下限（不能为空）
     * @param upper 上限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> open(T lower, T upper) {
        return new Range<>(Boundary.excluding(lower), Boundary.excluding(upper));
    }

    /**
     * 闭区间（包含给定的上限和下限）
     *
     * @param lower 下限（不能为空）
     * @param upper 上限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> closed(T lower, T upper) {
        return new Range<>(Boundary.including(lower), Boundary.including(upper));
    }

    /**
     * 根据给定的上限和下限，创建 {@link Range} 对象
     *
     * @param lower 下限（不能为空）
     * @param upper 上限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> from(Boundary<T> lower, Boundary<T> upper) {
        return new Range<>(lower, upper);
    }

    /**
     * 无上限 且 大于下限
     *
     * @param lower 下限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> gt(T lower) {
        return new Range<>(Boundary.excluding(lower), Boundary.unbounded());
    }

    /**
     * 无上限 且 大于等于下限
     *
     * @param lower 下限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> gte(T lower) {
        return new Range<>(Boundary.including(lower), Boundary.unbounded());
    }

    /**
     * 无下限 且 小于上限
     *
     * @param upper 上限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> lt(T upper) {
        return new Range<>(Boundary.unbounded(), Boundary.excluding(upper));
    }

    /**
     * 无下限 且 小于等于上限
     *
     * @param upper 上限（不能为空）
     * @param <T>   限定值类型
     * @return {@link Range} 对象
     */
    public static <T> Range<T> lte(T upper) {
        return new Range<>(Boundary.unbounded(), Boundary.including(upper));
    }

    /**
     * 边界
     *
     * @param <T> 限定值类型
     */
    public static class Boundary<T> {

        private static final Boundary<?> UNBOUNDED = new Boundary<>(null, true);

        private final T value;

        private final boolean including;

        private Boundary(T value, boolean including) {
            this.value = value;
            this.including = including;
        }

        /**
         * 创建一个无限定值的边界
         *
         * @param <T> 限定值类型
         * @return {@link Boundary} 对象
         */
        @SuppressWarnings("unchecked")
        public static <T> Boundary<T> unbounded() {
            return (Boundary<T>) UNBOUNDED;
        }

        /**
         * 创建不包含限定值的边界
         *
         * @param value 限定值（不能为空）
         * @param <T>   限定值类型
         * @return {@link Boundary} 对象
         */
        public static <T> Boundary<T> including(T value) {
            Assert.notNull(value, "Value must not be null");
            return new Boundary<>(value, true);
        }

        /**
         * 创建一个不包含限定值的边界
         *
         * @param value 限定值（不能为空）
         * @param <T>   限定值类型
         * @return {@link Boundary} 对象
         */
        public static <T> Boundary<T> excluding(T value) {
            Assert.notNull(value, "Value must not be null");
            return new Boundary<>(value, false);
        }

        /**
         * 获取限定值
         *
         * @return 限定值
         */
        public T getValue() {
            return value;
        }

        /**
         * 是否包含限定值
         *
         * @return {@code true} 包含限定值，否则返回 {@code false}
         */
        public boolean isIncluding() {
            return including;
        }

        /**
         * 是否无限制
         *
         * @return {@code true} 无限制，否则返回 {@code false}
         */
        public boolean isUnbounded() {
            return this == UNBOUNDED;
        }

        @Override
        public final boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Boundary<?> boundary)) return false;

            return including == boundary.including && Objects.equals(value, boundary.value);
        }

        @Override
        public int hashCode() {
            int result = Objects.hashCode(value);
            result = 31 * result + Boolean.hashCode(including);
            return result;
        }

    }

}
