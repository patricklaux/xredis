package com.igeeksky.xredis.common.stream;

import com.igeeksky.xredis.common.ConnectionMode;
import com.igeeksky.xredis.common.RedisFutureHelper;
import com.igeeksky.xredis.common.TimeConvertor;
import com.igeeksky.xtool.core.AsyncCloseable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stream 操作接口
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface StreamOperator<K, V> extends ConnectionMode, AsyncCloseable {

    /**
     * 异步转同步阻塞超时时间
     * <p>
     * 默认值：60000 单位：毫秒
     * <p>
     * 如果调用同步接口，会先调用异步接口获取 {@link CompletableFuture}，
     * 然后再调用 {@code future.get(timeout, TimeUnit.MILLISECONDS)} 方法等待数据处理完成。<p>
     * <b>注意：</b><p>
     * 1、当调用同步接口时，如果异步操作未在设定超时时间内完成或线程被中断，会抛出异常。<br>
     * 2、当调用同步接口时，请根据单次操作数据量、网络拥堵情况、RedisServer 处理能力等适当调整超时时间。
     *
     * @return {@link Long} – 异步转同步阻塞超时时间
     */
    long getSyncTimeout();

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @return {@code CompletableFuture<List<V>>} – 包含两个元素：1.unix time seconds；2.microseconds。
     */
    CompletableFuture<List<V>> time();

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @return {@code List<V>} – 包含两个元素：1.unix time seconds；2.microseconds。
     */
    default List<V> timeSync() {
        return RedisFutureHelper.get(time(), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @param convertor 时间格式转换器
     * @return {@code CompletableFuture<Long>} – 当前时间（秒）
     */
    CompletableFuture<Long> timeSeconds(TimeConvertor<V> convertor);

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @param convertor 时间格式转换器
     * @return {@link Long} – 当前时间（秒）
     */
    default Long timeSecondsSync(TimeConvertor<V> convertor) {
        return RedisFutureHelper.get(timeSeconds(convertor), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @param convertor 时间格式转换器
     * @return {@code CompletableFuture<Long>} – 当前时间（毫秒）
     */
    CompletableFuture<Long> timeMillis(TimeConvertor<V> convertor);

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @param convertor 时间格式转换器
     * @return {@link Long} – 当前时间（毫秒）
     */
    default Long timeMillisSync(TimeConvertor<V> convertor) {
        return RedisFutureHelper.get(timeMillis(convertor), getSyncTimeout());
    }

    /**
     * 获取 RedisServer 当前时间（异步）
     *
     * @param convertor 时间格式转换器
     * @return {@code CompletableFuture<Long>} – 当前时间（微秒）
     */
    CompletableFuture<Long> timeMicros(TimeConvertor<V> convertor);

    /**
     * 获取 RedisServer 当前时间（同步）
     *
     * @param convertor 时间格式转换器
     * @return {@link Long} – 当前时间（微秒）
     */
    default Long timeMicrosSync(TimeConvertor<V> convertor) {
        return RedisFutureHelper.get(timeMicros(convertor), getSyncTimeout());
    }

    /**
     * 确认消息（异步）
     *
     * @param key        流名称
     * @param group      消费组名称
     * @param messageIds 消息 ID 列表
     * @return {@code CompletableFuture<Long>} – 确认数量
     */
    CompletableFuture<Long> xack(K key, K group, String... messageIds);

    /**
     * 确认消息（同步）
     *
     * @param key        流名称
     * @param group      消费组名称
     * @param messageIds 消息 ID 列表
     * @return {@link Long} – 确认数量
     */
    default Long xackSync(K key, K group, String... messageIds) {
        return RedisFutureHelper.get(xack(key, group, messageIds), getSyncTimeout());
    }

    /**
     * 发布消息（异步）
     *
     * @param key  流名称
     * @param body 消息体
     * @return {@code CompletableFuture<String>} – 消息 ID
     */
    CompletableFuture<String> xadd(K key, Map<K, V> body);

    /**
     * 发布消息（同步）
     *
     * @param key  流名称
     * @param body 消息体
     * @return {@link String} – 消息 ID
     */
    default String xaddSync(K key, Map<K, V> body) {
        return RedisFutureHelper.get(xadd(key, body), getSyncTimeout());
    }

    /**
     * 发布消息（异步）
     *
     * @param key     流名称
     * @param options 消息发布选项
     * @param body    消息体
     * @return {@code CompletableFuture<String>} – 消息 ID
     */
    CompletableFuture<String> xadd(K key, XAddOptions options, Map<K, V> body);

    /**
     * 发布消息（同步）
     *
     * @param key     流名称
     * @param options 消息发布选项
     * @param body    消息体
     * @return {@link String} – 消息 ID
     */
    default String xaddSync(K key, XAddOptions options, Map<K, V> body) {
        return RedisFutureHelper.get(xadd(key, options, body), getSyncTimeout());
    }

    /**
     * 认领消息（异步）
     *
     * @param key           流名称
     * @param groupConsumer 消费组信息
     * @param minIdleTime   最小空闲时间
     * @param messageIds    消息 ID 列表
     * @return {@code CompletableFuture<List<XStreamMessage<K, V>>>} – 消息列表
     */
    CompletableFuture<List<XStreamMessage<K, V>>> xclaim(K key, XGroupConsumer<K> groupConsumer, long minIdleTime, String... messageIds);

    /**
     * 认领消息（同步）
     *
     * @param key           流名称
     * @param groupConsumer 消费组信息
     * @param minIdleTime   最小空闲时间
     * @param messageIds    消息 ID 列表
     * @return {@code List<XStreamMessage<K, V>>} – 消息列表
     */
    default List<XStreamMessage<K, V>> xclaimSync(K key, XGroupConsumer<K> groupConsumer, long minIdleTime, String... messageIds) {
        return RedisFutureHelper.get(xclaim(key, groupConsumer, minIdleTime, messageIds), getSyncTimeout());
    }

    /**
     * 删除消息（异步）
     *
     * @param key        流名称
     * @param messageIds 消息 ID 列表
     * @return {@code CompletableFuture<Long>} – 删除数量
     */
    CompletableFuture<Long> xdel(K key, String... messageIds);

    /**
     * 删除消息（同步）
     *
     * @param key        流名称
     * @param messageIds 消息 ID 列表
     * @return {@link Long} – 删除数量
     */
    default Long xdelSync(K key, String... messageIds) {
        return RedisFutureHelper.get(xdel(key, messageIds), getSyncTimeout());
    }

    /**
     * 创建消费组（异步）
     *
     * @param streamOffset 流偏移量
     * @param group        消费组名称
     * @return {@code CompletableFuture<String>} – "OK"：创建成功
     */
    CompletableFuture<String> xgroupCreate(XStreamOffset<K> streamOffset, K group);

    /**
     * 创建消费组（同步）
     *
     * @param streamOffset 流偏移量
     * @param group        消费组名称
     * @return {@link String} – "OK"：创建成功
     */
    default String xgroupCreateSync(XStreamOffset<K> streamOffset, K group) {
        return RedisFutureHelper.get(xgroupCreate(streamOffset, group), getSyncTimeout());
    }

    /**
     * 创建消费组（异步）
     *
     * @param streamOffset 流偏移量
     * @param group        消费组名称
     * @param options      消费组创建选项
     * @return {@code CompletableFuture<String>} – "OK"：创建成功
     */
    CompletableFuture<String> xgroupCreate(XStreamOffset<K> streamOffset, K group, XGroupCreateOptions options);

    /**
     * 创建消费组（同步）
     *
     * @param streamOffset 流偏移量
     * @param group        消费组名称
     * @param options      消费组创建选项
     * @return {@link String} – "OK"：创建成功
     */
    default String xgroupCreateSync(XStreamOffset<K> streamOffset, K group, XGroupCreateOptions options) {
        return RedisFutureHelper.get(xgroupCreate(streamOffset, group, options), getSyncTimeout());
    }

    /**
     * 创建消费者（异步）
     * <p>
     * 命令格式：{@code XGROUP CREATECONSUMER mystream mygroup myname}
     *
     * @param key           流名称
     * @param groupConsumer 消费组信息
     * @return {@code CompletableFuture<Boolean>} – true：创建成功
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-createconsumer/">XGROUP CREATECONSUMER</a>
     */
    CompletableFuture<Boolean> xgroupCreateconsumer(K key, XGroupConsumer<K> groupConsumer);

    /**
     * 创建消费者（同步）
     * <p>
     * 命令格式：{@code XGROUP CREATECONSUMER mystream mygroup myname}
     *
     * @param key           流名称
     * @param groupConsumer 消费组信息
     * @return {@link Boolean} – true：创建成功
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-createconsumer/">XGROUP CREATECONSUMER</a>
     */
    default Boolean xgroupCreateconsumerSync(K key, XGroupConsumer<K> groupConsumer) {
        return RedisFutureHelper.get(xgroupCreateconsumer(key, groupConsumer), getSyncTimeout());
    }

    /**
     * 删除消费者（异步）
     * <p>
     * 命令格式：{@code XGROUP DELCONSUMER mystream mygroup myname}
     *
     * @param key           流名称
     * @param groupConsumer 消费组信息
     * @return {@code CompletableFuture<Long>} – 截至删除之前，该消费者已读但未确认的消息数量
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-delconsumer/">XGROUP DELCONSUMER</a>
     */
    CompletableFuture<Long> xgroupDelconsumer(K key, XGroupConsumer<K> groupConsumer);

    /**
     * 删除消费者（同步）
     * <p>
     * 命令格式：{@code XGROUP DELCONSUMER mystream mygroup myname}
     *
     * @param key           流名称
     * @param groupConsumer 消费组信息
     * @return {@link Long} – 截至删除之前，该消费者已读但未确认的消息数量
     * @see <a href="https://redis.io/docs/latest/commands/xgroup-delconsumer/">XGROUP DELCONSUMER</a>
     */
    default Long xgroupDelconsumerSync(K key, XGroupConsumer<K> groupConsumer) {
        return RedisFutureHelper.get(xgroupDelconsumer(key, groupConsumer), getSyncTimeout());
    }

    /**
     * 删除消费组（异步）
     *
     * @param key   流名称
     * @param group 消费组名称
     * @return {@code CompletableFuture<Boolean>} – true：删除成功
     */
    CompletableFuture<Boolean> xgroupDestroy(K key, K group);

    /**
     * 删除消费组（同步）
     *
     * @param key   流名称
     * @param group 消费组名称
     * @return {@link Boolean} – true：删除成功
     */
    default Boolean xgroupDestroySync(K key, K group) {
        return RedisFutureHelper.get(xgroupDestroy(key, group), getSyncTimeout());
    }

    /**
     * 读取消息（异步）
     *
     * @param streams 流名称及其偏移量
     * @return {@code CompletableFuture<List<XStreamMessage<K, V>>>} – 消息列表
     */
    CompletableFuture<List<XStreamMessage<K, V>>> xread(XStreamOffset<K>... streams);

    /**
     * 读取消息（同步）
     *
     * @param streams 流名称及其偏移量
     * @return {@code List<XStreamMessage<K, V>>} – 消息列表
     */
    default List<XStreamMessage<K, V>> xreadSync(XStreamOffset<K>... streams) {
        return RedisFutureHelper.get(xread(streams), getSyncTimeout());
    }

    /**
     * 读取消息（异步）
     *
     * @param options 消息读取选项
     * @param streams 流名称及其偏移量
     * @return {@code CompletableFuture<List<XStreamMessage<K, V>>>} – 消息列表
     */
    CompletableFuture<List<XStreamMessage<K, V>>> xread(XReadOptions options, XStreamOffset<K>... streams);

    /**
     * 读取消息（同步）
     *
     * @param options 消息读取选项
     * @param streams 流名称及其偏移量
     * @return {@code List<XStreamMessage<K, V>>} – 消息列表
     */
    default List<XStreamMessage<K, V>> xreadSync(XReadOptions options, XStreamOffset<K>... streams) {
        return RedisFutureHelper.get(xread(options, streams), getSyncTimeout());
    }

    /**
     * 读取消息（消费组）（异步）
     *
     * @param groupConsumer 消费组信息
     * @param streams       流名称及其偏移量
     * @return {@code CompletableFuture<List<XStreamMessage<K, V>>>} – 消息列表
     */
    CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer, XStreamOffset<K>... streams);

    /**
     * 读取消息（消费组）（同步）
     *
     * @param groupConsumer 消费组信息
     * @param streams       流名称及其偏移量
     * @return {@code List<XStreamMessage<K, V>>} – 消息列表
     */
    default List<XStreamMessage<K, V>> xreadgroupSync(XGroupConsumer<K> groupConsumer, XStreamOffset<K>... streams) {
        return RedisFutureHelper.get(xreadgroup(groupConsumer, streams), getSyncTimeout());
    }

    /**
     * 读取消息（消费组）（异步）
     *
     * @param groupConsumer 消费组信息
     * @param options       消息读取选项
     * @param streams       流名称及其偏移量
     * @return {@code CompletableFuture<List<XStreamMessage<K, V>>>} – 消息列表
     */
    CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer, XReadOptions options, XStreamOffset<K>... streams);

    /**
     * 读取消息（消费组）（同步）
     *
     * @param groupConsumer 消费组信息
     * @param options       消息读取选项
     * @param streams       流名称及其偏移量
     * @return {@code List<XStreamMessage<K, V>>} – 消息列表
     */
    default List<XStreamMessage<K, V>> xreadgroupSync(XGroupConsumer<K> groupConsumer, XReadOptions options, XStreamOffset<K>... streams) {
        return RedisFutureHelper.get(xreadgroup(groupConsumer, options, streams), getSyncTimeout());
    }

}
