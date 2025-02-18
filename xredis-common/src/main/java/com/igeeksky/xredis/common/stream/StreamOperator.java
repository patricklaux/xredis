package com.igeeksky.xredis.common.stream;

import com.igeeksky.xredis.common.AsyncCloseable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Stream 操作接口
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public interface StreamOperator<K, V> extends AsyncCloseable {

    CompletableFuture<Long> xack(K key, K group, String... messageIds);

    CompletableFuture<String> xadd(K key, Map<K, V> body);

    CompletableFuture<String> xadd(K key, XAddOptions options, Map<K, V> body);

    CompletableFuture<List<XStreamMessage<K, V>>> xclaim(K key, XGroupConsumer<K> consumer, long minIdleTime, String... messageIds);

    CompletableFuture<Long> xdel(K key, String... messageIds);

    CompletableFuture<String> xgroupCreate(XStreamOffset<K> streamOffset, K group);

    CompletableFuture<List<XStreamMessage<K, V>>> xread(XStreamOffset<K>... streams);

    CompletableFuture<List<XStreamMessage<K, V>>> xread(XReadOptions options, XStreamOffset<K>... streams);

    CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer, XStreamOffset<K>... streams);

    CompletableFuture<List<XStreamMessage<K, V>>> xreadgroup(XGroupConsumer<K> groupConsumer, XReadOptions options, XStreamOffset<K>... streams);

}
