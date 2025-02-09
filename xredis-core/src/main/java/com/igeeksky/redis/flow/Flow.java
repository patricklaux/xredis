package com.igeeksky.redis.flow;

/**
 * 数据流
 *
 * @param <E> 数据类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface Flow<E> {

    /**
     * 订阅
     *
     * @param subscriber  订阅者（不能为空）
     * @param parallelism 并行度（需大于 0）
     * @return {@link Disposable}
     */
    Disposable subscribe(Subscriber<E> subscriber, int parallelism);

}
