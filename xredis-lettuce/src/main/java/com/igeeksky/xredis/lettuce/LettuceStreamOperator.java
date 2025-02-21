package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.TimeConvertor;
import com.igeeksky.xredis.common.stream.*;
import com.igeeksky.xredis.lettuce.api.RedisOperator;
import io.lettuce.core.Consumer;
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
     * 构造器
     *
     * @param operator RedisOperator
     */
    public LettuceStreamOperator(RedisOperator<K, V> operator) {
        this.operator = operator;
    }

    @Override
    public CompletableFuture<List<V>> time() {
        return operator.async().time().toCompletableFuture();
    }

    @Override
    public CompletableFuture<Long> timeSeconds(TimeConvertor<V> convertor) {
        return operator.async().timeSeconds(convertor);
    }

    @Override
    public CompletableFuture<Long> timeMillis(TimeConvertor<V> convertor) {
        return operator.async().timeMillis(convertor);
    }

    @Override
    public CompletableFuture<Long> timeMicros(TimeConvertor<V> convertor) {
        return operator.async().timeMicros(convertor);
    }

    @Override
    public CompletableFuture<Long> xack(K key, K group, String... messageIds) {
        return operator.async().xack(key, group, messageIds).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> xadd(K key, Map<K, V> body) {
        return operator.async().xadd(key, body).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> xadd(K key, XAddOptions options, Map<K, V> body) {
        return operator.async().xadd(key, LettuceConvertor.toXAddArgs(options), body).toCompletableFuture();
    }

    @Override
    public CompletableFuture<List<XStreamMessage<K, V>>> xclaim(K key, XGroupConsumer<K> groupConsumer,
                                                                long minIdleTime, String... messageIds) {
        return CompletableFuture.completedFuture(groupConsumer)
                .thenApply(LettuceConvertor::toXGroupConsumer)
                .thenCompose(consumer -> operator.async().xclaim(key, consumer, minIdleTime, messageIds))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @Override
    public CompletableFuture<Long> xdel(K key, String... messageIds) {
        return operator.async().xdel(key, messageIds).toCompletableFuture();
    }

    @Override
    public CompletableFuture<String> xgroupCreate(XStreamOffset<K> streamOffset, K group) {
        return CompletableFuture.completedFuture(streamOffset)
                .thenApply(LettuceConvertor::toXStreamOffset)
                .thenCompose(offset -> operator.async().xgroupCreate(offset, group));
    }

    @Override
    public CompletableFuture<String> xgroupCreate(XStreamOffset<K> streamOffset, K group, XGroupCreateOptions options) {
        return CompletableFuture.completedFuture(streamOffset)
                .thenApply(LettuceConvertor::toXStreamOffset)
                .thenCompose(offset -> {
                    XGroupCreateArgs args = LettuceConvertor.toXGroupCreateOptions(options);
                    return operator.async().xgroupCreate(offset, group, args);
                });
    }

    @Override
    public CompletableFuture<Boolean> xgroupCreateconsumer(K key, XGroupConsumer<K> groupConsumer) {
        return CompletableFuture.completedFuture(groupConsumer)
                .thenApply(LettuceConvertor::toXGroupConsumer)
                .thenCompose(consumer -> operator.async().xgroupCreateconsumer(key, consumer));
    }

    @Override
    public CompletableFuture<Long> xgroupDelconsumer(K key, XGroupConsumer<K> groupConsumer) {
        return CompletableFuture.completedFuture(groupConsumer)
                .thenApply(LettuceConvertor::toXGroupConsumer)
                .thenCompose(consumer -> operator.async().xgroupDelconsumer(key, consumer));
    }

    @Override
    public CompletableFuture<Boolean> xgroupDestroy(K key, K group) {
        return operator.async().xgroupDestroy(key, group).toCompletableFuture();
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xread(XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> operator.async().xread(offsets))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xread(XReadOptions options, XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> operator.async().xread(LettuceConvertor.toXReadArgs(options), offsets))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer,
                                                                          XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> {
                    Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
                    return operator.async().xreadgroup(consumer, offsets);
                })
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @SafeVarargs
    @Override
    public final CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer,
                                                                          XReadOptions options, XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> {
                    Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
                    XReadArgs readArgs = LettuceConvertor.toXReadArgs(options);
                    return operator.async().xreadgroup(consumer, readArgs, offsets);
                })
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @Override
    public boolean isCluster() {
        return operator.isCluster();
    }

    @Override
    public CompletableFuture<Void> closeAsync() {
        return operator.closeAsync();
    }

}
