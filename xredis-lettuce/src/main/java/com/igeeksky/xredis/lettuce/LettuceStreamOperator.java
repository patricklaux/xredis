package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.TimeConvertor;
import com.igeeksky.xredis.common.stream.*;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.Consumer;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XGroupCreateArgs;
import io.lettuce.core.XReadArgs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Lettuce Stream 操作实现
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceStreamOperator<K, V> implements StreamOperator<K, V> {

    private final RedisOperator<K, V> operator;

    /**
     * 使用 {@code RedisOperator} 创建 {@link LettuceStreamOperator}
     *
     * @param operator RedisOperator
     */
    public LettuceStreamOperator(RedisOperator<K, V> operator) {
        this.operator = operator;
    }

    @Override
    public CompletableFuture<List<V>> timeAsync() {
        return this.operator.async().time().toCompletableFuture();
    }

    @Override
    public List<V> time() {
        return this.operator.sync().time();
    }

    @Override
    public CompletableFuture<Long> timeSecondsAsync(TimeConvertor<V> convertor) {
        return this.operator.async().timeSeconds(convertor);
    }

    @Override
    public Long timeSeconds(TimeConvertor<V> convertor) {
        return this.operator.sync().timeSeconds(convertor);
    }

    @Override
    public CompletableFuture<Long> timeMillisAsync(TimeConvertor<V> convertor) {
        return this.operator.async().timeMillis(convertor);
    }

    @Override
    public Long timeMillis(TimeConvertor<V> convertor) {
        return this.operator.sync().timeMillis(convertor);
    }

    @Override
    public CompletableFuture<Long> timeMicrosAsync(TimeConvertor<V> convertor) {
        return this.operator.async().timeMicros(convertor);
    }

    @Override
    public Long timeMicros(TimeConvertor<V> convertor) {
        return this.operator.sync().timeMicros(convertor);
    }

    @Override
    public CompletableFuture<Long> xackAsync(K key, K group, String... messageIds) {
        return this.operator.async().xack(key, group, messageIds).toCompletableFuture();
    }

    @Override
    public Long xack(K key, K group, String... messageIds) {
        return this.operator.sync().xack(key, group, messageIds);
    }

    @Override
    public CompletableFuture<String> xaddAsync(K key, Map<K, V> body) {
        return this.operator.async().xadd(key, body).toCompletableFuture();
    }

    @Override
    public String xadd(K key, Map<K, V> body) {
        return this.operator.sync().xadd(key, body);
    }

    @Override
    public CompletableFuture<String> xaddAsync(K key, XAddOptions options, Map<K, V> body) {
        return this.operator.async().xadd(key, LettuceConvertor.toXAddArgs(options), body).toCompletableFuture();
    }

    @Override
    public String xadd(K key, XAddOptions options, Map<K, V> body) {
        return this.operator.sync().xadd(key, LettuceConvertor.toXAddArgs(options), body);
    }

    @Override
    public CompletableFuture<List<XStreamMessage<K, V>>> xclaimAsync(K key, XGroupConsumer<K> groupConsumer,
                                                                     long minIdleTime, String... messageIds) {
        return CompletableFuture.completedFuture(groupConsumer)
                .thenApply(LettuceConvertor::toXGroupConsumer)
                .thenCompose(consumer -> this.operator.async().xclaim(key, consumer, minIdleTime, messageIds))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @Override
    public List<XStreamMessage<K, V>> xclaim(K key, XGroupConsumer<K> groupConsumer, long minIdleTime, String... messageIds) {
        Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
        List<StreamMessage<K, V>> messages = this.operator.sync().xclaim(key, consumer, minIdleTime, messageIds);
        return LettuceConvertor.fromStreamMessages(messages);
    }

    @Override
    public CompletableFuture<Long> xdelAsync(K key, String... messageIds) {
        return this.operator.async().xdel(key, messageIds).toCompletableFuture();
    }

    @Override
    public Long xdel(K key, String... messageIds) {
        return this.operator.sync().xdel(key, messageIds);
    }

    @Override
    public CompletableFuture<String> xgroupCreateAsync(XStreamOffset<K> streamOffset, K group) {
        return CompletableFuture.completedFuture(streamOffset)
                .thenApply(LettuceConvertor::toXStreamOffset)
                .thenCompose(offset -> this.operator.async().xgroupCreate(offset, group));
    }

    @Override
    public String xgroupCreate(XStreamOffset<K> streamOffset, K group) {
        return this.operator.sync().xgroupCreate(LettuceConvertor.toXStreamOffset(streamOffset), group);
    }

    @Override
    public CompletableFuture<String> xgroupCreateAsync(XStreamOffset<K> streamOffset, K group, XGroupCreateOptions options) {
        return CompletableFuture.completedFuture(streamOffset)
                .thenApply(LettuceConvertor::toXStreamOffset)
                .thenCompose(offset -> {
                    XGroupCreateArgs args = LettuceConvertor.toXGroupCreateOptions(options);
                    return this.operator.async().xgroupCreate(offset, group, args);
                });
    }

    @Override
    public String xgroupCreate(XStreamOffset<K> streamOffset, K group, XGroupCreateOptions options) {
        XGroupCreateArgs args = LettuceConvertor.toXGroupCreateOptions(options);
        XReadArgs.StreamOffset<K> offset = LettuceConvertor.toXStreamOffset(streamOffset);
        return this.operator.sync().xgroupCreate(offset, group, args);
    }

    @Override
    public CompletableFuture<Boolean> xgroupCreateconsumerAsync(K key, XGroupConsumer<K> groupConsumer) {
        return CompletableFuture.completedFuture(groupConsumer)
                .thenApply(LettuceConvertor::toXGroupConsumer)
                .thenCompose(consumer -> this.operator.async().xgroupCreateconsumer(key, consumer));
    }

    @Override
    public Boolean xgroupCreateconsumer(K key, XGroupConsumer<K> groupConsumer) {
        Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
        return this.operator.sync().xgroupCreateconsumer(key, consumer);
    }

    @Override
    public CompletableFuture<Long> xgroupDelconsumerAsync(K key, XGroupConsumer<K> groupConsumer) {
        return CompletableFuture.completedFuture(groupConsumer)
                .thenApply(LettuceConvertor::toXGroupConsumer)
                .thenCompose(consumer -> this.operator.async().xgroupDelconsumer(key, consumer));
    }

    @Override
    public Long xgroupDelconsumer(K key, XGroupConsumer<K> groupConsumer) {
        Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
        return this.operator.sync().xgroupDelconsumer(key, consumer);
    }

    @Override
    public CompletableFuture<Boolean> xgroupDestroyAsync(K key, K group) {
        return this.operator.async().xgroupDestroy(key, group).toCompletableFuture();
    }

    @Override
    public Boolean xgroupDestroy(K key, K group) {
        return this.operator.sync().xgroupDestroy(key, group);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xreadAsync(XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> this.operator.async().xread(offsets))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final List<XStreamMessage<K, V>> xread(XStreamOffset<K>... streams) {
        XReadArgs.StreamOffset<K>[] offsets = LettuceConvertor.toXStreamOffsets(streams);
        List<StreamMessage<K, V>> messages = this.operator.sync().xread(offsets);
        return LettuceConvertor.fromStreamMessages(messages);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xreadAsync(XReadOptions options, XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> this.operator.async().xread(LettuceConvertor.toXReadArgs(options), offsets))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final List<XStreamMessage<K, V>> xread(XReadOptions options, XStreamOffset<K>... streams) {
        XReadArgs.StreamOffset<K>[] offsets = LettuceConvertor.toXStreamOffsets(streams);
        List<StreamMessage<K, V>> messages = this.operator.sync().xread(LettuceConvertor.toXReadArgs(options), offsets);
        return LettuceConvertor.fromStreamMessages(messages);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xreadgroupAsync(XGroupConsumer<K> groupConsumer,
                                                                               XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> {
                    Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
                    return this.operator.async().xreadgroup(consumer, offsets);
                })
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final List<XStreamMessage<K, V>> xreadgroup(XGroupConsumer<K> groupConsumer, XStreamOffset<K>... streams) {
        XReadArgs.StreamOffset<K>[] offsets = LettuceConvertor.toXStreamOffsets(streams);
        Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
        List<StreamMessage<K, V>> messages = this.operator.sync().xreadgroup(consumer, offsets);
        return LettuceConvertor.fromStreamMessages(messages);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xreadgroupAsync(XGroupConsumer<K> groupConsumer,
                                                                               XReadOptions options, XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> {
                    Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
                    XReadArgs readArgs = LettuceConvertor.toXReadArgs(options);
                    return this.operator.async().xreadgroup(consumer, readArgs, offsets);
                })
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final List<XStreamMessage<K, V>> xreadgroup(XGroupConsumer<K> groupConsumer, XReadOptions options, XStreamOffset<K>... streams) {
        XReadArgs.StreamOffset<K>[] offsets = LettuceConvertor.toXStreamOffsets(streams);
        Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
        XReadArgs readArgs = LettuceConvertor.toXReadArgs(options);
        List<StreamMessage<K, V>> messages = this.operator.sync().xreadgroup(consumer, readArgs, offsets);
        return LettuceConvertor.fromStreamMessages(messages);
    }

    @Override
    public boolean isCluster() {
        return this.operator.isCluster();
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return this.operator.closeAsync();
    }

}
