package com.igeeksky.xredis;

import com.igeeksky.xredis.api.RedisOperator;
import com.igeeksky.xredis.common.stream.*;
import io.lettuce.core.Consumer;
import io.lettuce.core.XReadArgs;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Lettuce Stream 操作实现
 *
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
    public CompletableFuture<List<XStreamMessage<K, V>>> xread(XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> operator.async().xread(offsets))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @Override
    public CompletableFuture<List<XStreamMessage<K, V>>> xread(XReadOptions options, XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> operator.async().xread(LettuceConvertor.toXReadArgs(options), offsets))
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @Override
    public CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer,
                                                                    XStreamOffset<K>... streams) {
        return CompletableFuture.completedFuture(streams)
                .thenApply(LettuceConvertor::toXStreamOffsets)
                .thenCompose(offsets -> {
                    Consumer<K> consumer = LettuceConvertor.toXGroupConsumer(groupConsumer);
                    return operator.async().xreadgroup(consumer, offsets);
                })
                .thenApply(LettuceConvertor::fromStreamMessages);
    }

    @Override
    public CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer,
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
    public CompletableFuture<Void> closeAsync() {
        return operator.closeAsync();
    }

}
