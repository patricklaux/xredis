package com.igeeksky.xredis;

import java.util.List;

/**
 * 用于将 RedisServer 返回的时间转换为 {@code long} 类型时间
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface TimeConvertor<V> {

    /**
     * 转换为毫秒
     * <p>
     * {@code (serverTime.get(0) * 1000) + (serverTime.get(1) / 1000)}
     *
     * @param serverTime Redis 返回的时间
     * @return long 类型时间
     */
    long milliseconds(List<V> serverTime);

    /**
     * 转换为微秒
     * <p>
     * {@code serverTime.get(0) * 1000000 + serverTime.get(1)}
     *
     * @param serverTime Redis 返回的时间
     * @return long 类型时间
     */
    long microseconds(List<V> serverTime);

    /**
     * 转换为秒
     * <p>
     * {@code serverTime.get(0))}
     *
     * @param serverTime Redis 返回的时间
     * @return long 类型时间
     */
    long seconds(List<V> serverTime);

}
