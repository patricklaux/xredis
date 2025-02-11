package com.igeeksky.xredis;

/**
 * Redis 操作异常
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-09-17
 */
public class RedisOperationException extends RuntimeException {

    /**
     * 无参构造
     */
    public RedisOperationException() {
        super();
    }

    /**
     * 带参构造
     *
     * @param message 异常信息
     */
    public RedisOperationException(String message) {
        super(message);
    }

    /**
     * 带参构造
     *
     * @param message 异常信息
     * @param cause   异常
     */
    public RedisOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
