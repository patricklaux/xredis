package com.igeeksky.xredis.common;

import com.igeeksky.xtool.core.lang.Assert;

import java.util.Arrays;

/**
 * 键-字段-值-过期时间
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class ExpiryKeyFieldValue {

    private final byte[] key;

    private final byte[] field;

    private final byte[] value;

    private final long ttl;

    /**
     * 私有构造器
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     * @param ttl   过期时间
     */
    private ExpiryKeyFieldValue(byte[] key, byte[] field, byte[] value, long ttl) {
        Assert.notNull(key, "key must not be null");
        Assert.notNull(field, "field must not be null");
        Assert.notNull(value, "value must not be null");
        Assert.isTrue(ttl > 0, "ttl must be greater than 0");
        this.key = key;
        this.field = field;
        this.value = value;
        this.ttl = ttl;
    }

    /**
     * 创建对象
     *
     * @param key   键 （不能为空）
     * @param field 字段（不能为空）
     * @param value 值（不能为空）
     * @param ttl   过期时间（必须大于0）
     * @return ExpiryKeyFieldValue
     */
    public static ExpiryKeyFieldValue create(byte[] key, byte[] field, byte[] value, long ttl) {
        return new ExpiryKeyFieldValue(key, field, value, ttl);
    }

    /**
     * 获取键
     *
     * @return {@code byte[]} – 键
     */
    public byte[] getKey() {
        return key;
    }

    /**
     * 获取字段
     *
     * @return {@code byte[]} – 字段
     */
    public byte[] getField() {
        return field;
    }

    /**
     * 获取值
     *
     * @return {@code byte[]} – 值
     */
    public byte[] getValue() {
        return value;
    }

    /**
     * 获取过期时间
     *
     * @return {@code long} – 存活时间
     */
    public long getTtl() {
        return ttl;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExpiryKeyFieldValue that)) return false;

        return ttl == that.ttl && Arrays.equals(key, that.key) && Arrays.equals(field, that.field) && Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(key);
        result = 31 * result + Arrays.hashCode(field);
        result = 31 * result + Arrays.hashCode(value);
        result = 31 * result + Long.hashCode(ttl);
        return result;
    }

}
