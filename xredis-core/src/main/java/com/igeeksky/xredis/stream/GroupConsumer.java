package com.igeeksky.xredis.stream;

/**
 * 消费者组
 *
 * @param group 消费者组
 * @param name  消费者名称
 * @author Patrick.Lau
 * @since 1.0.0
 */
public record GroupConsumer(String group, String name) {

}
