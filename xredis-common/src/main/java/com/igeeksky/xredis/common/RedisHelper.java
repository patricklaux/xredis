package com.igeeksky.xredis.common;

import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * RedisFuture 辅助类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public final class RedisHelper {

    private static final VirtualThreadFactory STREAM_VIRTUAL_FACTORY = new VirtualThreadFactory("virtual-stream-");

    /**
     * 私有构造方法
     */
    private RedisHelper() {
    }

    /**
     * 获取虚拟线程工厂
     *
     * @return 虚拟线程工厂
     */
    public static VirtualThreadFactory getStreamVirtualFactory() {
        return STREAM_VIRTUAL_FACTORY;
    }

    /**
     * 获取 Future 结果（无限期阻塞，直到获取结果或发生异常）
     *
     * @param future Future
     * @param <T>    泛型
     * @return 结果
     */
    public static <T> T get(Future<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw convert(e);
        } catch (ExecutionException e) {
            throw convert(e);
        }
    }

    /**
     * 获取 Future 结果（指定超时时间，直到获取结果或发生异常）
     *
     * @param future        Future
     * @param timeoutMillis 超时时间（毫秒）
     * @param <T>           泛型
     * @return 结果
     */
    public static <T> T get(Future<T> future, long timeoutMillis) {
        return get(future, timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * 获取 Future 结果（指定超时时间，直到获取结果或发生异常）
     *
     * @param future  Future
     * @param timeout 超时时间
     * @param unit    时间单位
     * @param <T>     泛型
     * @return 结果
     */
    public static <T> T get(Future<T> future, long timeout, TimeUnit unit) {
        try {
            return future.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw convert(e);
        } catch (ExecutionException e) {
            throw convert(e);
        } catch (TimeoutException e) {
            throw convert(e, timeout, unit);
        }
    }

    private static RuntimeException convert(InterruptedException e) {
        return new RedisOperationException("Redis:Interrupted", e);
    }

    private static RuntimeException convert(ExecutionException e) {
        return new RedisOperationException("Redis:Execution:" + e.getMessage(), e.getCause());
    }

    private static RuntimeException convert(TimeoutException e, long timeout, TimeUnit unit) {
        return new RedisOperationException("Redis:Timeout:wait:[" + timeout + unit.name() + "]. " + e.getMessage(), e);
    }

}
