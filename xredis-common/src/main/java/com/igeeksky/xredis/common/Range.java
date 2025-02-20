package com.igeeksky.xredis.common;

import com.igeeksky.xtool.core.lang.Assert;

import java.util.Objects;

/**
 * 数据的上限和下限
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class Range<T> {

    private final Boundary<T> lower;
    private final Boundary<T> upper;

    private Range(Boundary<T> lower, Boundary<T> upper) {
        Assert.notNull(lower, "Lower bound must not be null");
        Assert.notNull(upper, "Upper bound must not be null");
        this.lower = lower;
        this.upper = upper;
    }

    public Boundary<T> getLower() {
        return lower;
    }

    public Boundary<T> getUpper() {
        return upper;
    }

    public boolean isUnbounded() {
        return lower.isUnbounded() && upper.isUnbounded();
    }

    /**
     * Create an unbounded range.
     *
     * @param <T> type of the range.
     * @return the range.
     */
    public static <T> Range<T> unbounded() {
        return new Range<>(Boundary.unbounded(), Boundary.unbounded());
    }

    /**
     * Create a range that excludes the lower and upper bounds.
     *
     * @param lower must not be {@code null}.
     * @param upper must not be {@code null}.
     * @param <T>   type of the range.
     * @return the range.
     */
    public static <T> Range<T> open(T lower, T upper) {
        return new Range<>(Boundary.excluding(lower), Boundary.excluding(upper));
    }

    /**
     * Create a range that includes the lower and upper bounds.
     *
     * @param lower must not be {@code null}.
     * @param upper must not be {@code null}.
     * @param <T>   type of the range.
     * @return the range.
     */
    public static <T> Range<T> closed(T lower, T upper) {
        return new Range<>(Boundary.including(lower), Boundary.including(upper));
    }

    public static <T> Range<T> from(Boundary<T> lower, Boundary<T> upper) {
        return new Range<>(lower, upper);
    }

    public static <T> Range<T> gt(T lower) {
        return new Range<>(Boundary.excluding(lower), Boundary.unbounded());
    }

    public static <T> Range<T> gte(T lower) {
        return new Range<>(Boundary.including(lower), Boundary.unbounded());
    }

    public static <T> Range<T> lt(T upper) {
        return new Range<>(Boundary.unbounded(), Boundary.excluding(upper));
    }

    public static <T> Range<T> lte(T upper) {
        return new Range<>(Boundary.unbounded(), Boundary.including(upper));
    }

    public static class Boundary<T> {

        private static final Boundary<?> UNBOUNDED = new Boundary<>(null, true);

        private final T value;

        private final boolean including;

        private Boundary(T value, boolean including) {
            this.value = value;
            this.including = including;
        }

        /**
         * Creates an unbounded (infinite) boundary that marks the beginning/end of the range.
         *
         * @param <T> inferred type.
         * @return the unbounded boundary.
         */
        @SuppressWarnings("unchecked")
        public static <T> Boundary<T> unbounded() {
            return (Boundary<T>) UNBOUNDED;
        }

        /**
         * Create a {@link Boundary} based on the {@code value} that includes the value when comparing ranges. Greater or
         * equals, less or equals. but not Greater or equal, less or equal to {@code value}.
         *
         * @param value must not be {@code null}.
         * @param <T>   value type.
         * @return the {@link Boundary}.
         */
        public static <T> Boundary<T> including(T value) {

            Assert.notNull(value, "Value must not be null");

            return new Boundary<>(value, true);
        }

        /**
         * Create a {@link Boundary} based on the {@code value} that excludes the value when comparing ranges. Greater or less
         * to {@code value} but not greater or equal, less or equal.
         *
         * @param value must not be {@code null}.
         * @param <T>   value type.
         * @return the {@link Boundary}.
         */
        public static <T> Boundary<T> excluding(T value) {

            Assert.notNull(value, "Value must not be null");

            return new Boundary<>(value, false);
        }

        /**
         * @return the value
         */
        public T getValue() {
            return value;
        }

        /**
         * @return {@code true} if the boundary includes the value.
         */
        public boolean isIncluding() {
            return including;
        }

        /**
         * @return {@code true} if the bound is unbounded.
         * @since 6.0
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
