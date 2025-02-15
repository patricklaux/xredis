package com.igeeksky.xredis.common;

/**
 * 脚本执行结果类别
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/7/13
 */
public enum ResultType {

    /**
     * script 返回值为 0 或 1，转换成 {@link Boolean}
     */
    BOOLEAN,

    /**
     * 数值转换成 {@link Long}.
     */
    INTEGER,

    /**
     * List of flat arrays.
     */
    MULTI,

    /**
     * Simple status value such as {@code OK}. The Redis response is parsed as ASCII.
     */
    STATUS,

    /**
     * return as value type
     */
    VALUE

}