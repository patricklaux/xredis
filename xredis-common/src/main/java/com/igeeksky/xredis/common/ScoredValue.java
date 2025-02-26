package com.igeeksky.xredis.common;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * (分-值)对
 *
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class ScoredValue<V> {

    private final V value;
    private final double score;

    /**
     * 创建一个 (分-值)对
     *
     * @param score 分数
     * @param value 值（不能为空）
     */
    private ScoredValue(double score, V value) {
        this.score = score;
        this.value = value;
    }

    /**
     * 值对应的分数
     *
     * @return {@code double} 值对应的分数
     */
    public double getScore() {
        return score;
    }

    /**
     * 值
     *
     * @return {@code V} 值
     */
    public V getValue() {
        return value;
    }

    /**
     * 创建一个 (分-值)对
     *
     * @param score 分数
     * @param value 值（不能为空）
     * @param <V>   值类型
     * @return {@code ScoredValue<V>} (分-值)对
     */
    public static <V> ScoredValue<V> just(double score, V value) {
        Assert.notNull(value, "value must not be null");
        return new ScoredValue<>(score, value);
    }

}