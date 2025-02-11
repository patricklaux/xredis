package com.igeeksky.xredis;

import com.igeeksky.xtool.core.lang.Assert;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class ExpiryKeyFieldValue {

    private final byte[] key;

    private final byte[] field;

    private final byte[] value;

    private final long ttl;

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

    public static ExpiryKeyFieldValue create(byte[] key, byte[] field, byte[] value, long ttl) {
        return new ExpiryKeyFieldValue(key, field, value, ttl);
    }

    public byte[] getKey() {
        return key;
    }

    public byte[] getField() {
        return field;
    }

    public byte[] getValue() {
        return value;
    }

    public long getTtl() {
        return ttl;
    }

}
