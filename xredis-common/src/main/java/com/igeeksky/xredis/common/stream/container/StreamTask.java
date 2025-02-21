package com.igeeksky.xredis.common.stream.container;

/**
 * 流消息拉取及消费任务
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface StreamTask<K, V> {

    /**
     * 添加流
     *
     * @param info 流相关信息
     */
    void add(StreamInfo<K, V> info);

    /**
     * 拉取消息
     */
    void pull();

    /**
     * 消费消息
     */
    void consume();

}
