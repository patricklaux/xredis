package com.igeeksky.xredis.lettuce;

import com.igeeksky.xredis.common.Limit;
import com.igeeksky.xredis.common.Range;
import com.igeeksky.xredis.common.stream.*;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lettuce 对象转换
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public abstract class LettuceConvertor {

    /**
     * 私有构造器
     */
    private LettuceConvertor() {
    }

    /**
     * {@link XReadOptions} 转换为 Lettuce {@link XReadArgs}
     *
     * @param <K>      键类型
     * @param consumer 消费者组名及消费者名称
     * @return Lettuce {@link XReadArgs} 对象
     */
    public static <K> Consumer<K> toXGroupConsumer(XGroupConsumer<K> consumer) {
        return Consumer.from(consumer.group(), consumer.name());
    }

    /**
     * {@link XReadOptions} 转换为 Lettuce {@link XReadArgs}
     *
     * @param options 流消息读取选项
     * @return Lettuce {@link XReadArgs} 对象
     */
    public static XReadArgs toXReadArgs(XReadOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }
        XReadArgs args = new XReadArgs();
        Long block = options.getBlock();
        if (block != null && block >= 0) {
            args.block(block);
        }
        Long count = options.getCount();
        if (count != null) {
            args.count(count);
        }
        args.noack(options.isNoack());
        return args;
    }

    /**
     * {@link XAddOptions} 转换为 Lettuce {@link XAddArgs}
     *
     * @param options 流消息发布选项
     * @return Lettuce {@link XAddArgs} 对象
     */
    public static XAddArgs toXAddArgs(XAddOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }
        XAddArgs args = new XAddArgs();
        Long maxLen = options.getMaxLen();
        if (maxLen != null) {
            args.maxlen(maxLen);
        }
        Long limit = options.getLimit();
        if (limit != null) {
            args.limit(limit);
        }
        if (options.isApproximateTrimming()) {
            args.approximateTrimming();
        }
        if (options.isExactTrimming()) {
            args.exactTrimming();
        }
        if (options.isNomkstream()) {
            args.nomkstream();
        }
        return args;
    }

    /**
     * Lettuce {@link io.lettuce.core.KeyValue} 转换为 {@link KeyValue}
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param keyValues Lettuce {@link io.lettuce.core.KeyValue} 对象列表
     * @return {@link KeyValue} 对象列表
     */
    public static <K, V> List<KeyValue<K, V>> fromKeyValues(List<io.lettuce.core.KeyValue<K, V>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyList();
        }
        List<KeyValue<K, V>> results = new ArrayList<>(keyValues.size());
        keyValues.forEach(keyValue -> {
            if (keyValue != null && keyValue.hasValue()) {
                results.add(KeyValue.create(keyValue.getKey(), keyValue.getValue()));
            }
        });
        return results;
    }

    /**
     * {@link XStreamOffset} 转换为 Lettuce {@link XReadArgs.StreamOffset}
     *
     * @param <K>     键类型
     * @param offsets Lettuce {@link XReadArgs.StreamOffset} 对象列表
     * @return {@link XStreamOffset} 对象列表
     */
    public static <K> XReadArgs.StreamOffset<K>[] toXStreamOffsets(XStreamOffset<K>[] offsets) {
        if (offsets == null || offsets.length == 0) {
            return null;
        }
        @SuppressWarnings("unchecked")
        XReadArgs.StreamOffset<K>[] result = new XReadArgs.StreamOffset[offsets.length];
        for (int i = 0; i < offsets.length; i++) {
            result[i] = toXStreamOffset(offsets[i]);
        }
        return result;
    }

    /**
     * {@link XStreamOffset} 转换为 Lettuce {@link XReadArgs.StreamOffset}
     *
     * @param <K>    键类型
     * @param offset Lettuce {@link XReadArgs.StreamOffset} 对象
     * @return {@link XStreamOffset} 对象
     */
    public static <K> XReadArgs.StreamOffset<K> toXStreamOffset(XStreamOffset<K> offset) {
        return XReadArgs.StreamOffset.from(offset.getKey(), offset.getOffset());
    }

    /**
     * Lettuce {@link StreamMessage} 转换为 {@link XStreamMessage}
     *
     * @param <K>      键类型
     * @param <V>      值类型
     * @param messages Lettuce {@link StreamMessage} 对象列表
     * @return {@link XStreamMessage} 对象列表
     */
    public static <K, V> List<XStreamMessage<K, V>> fromStreamMessages(List<StreamMessage<K, V>> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }
        List<XStreamMessage<K, V>> list = new ArrayList<>(messages.size());
        for (StreamMessage<K, V> message : messages) {
            if (message != null) {
                list.add(new XStreamMessage<>(message.getStream(), message.getId(), message.getBody()));
            }
        }
        return list;
    }

    /**
     * {@link XGroupCreateOptions} 转换为 Lettuce {@link XGroupCreateArgs}
     *
     * @param options 流消息组创建选项
     * @return Lettuce {@link XGroupCreateArgs} 对象
     */
    public static XGroupCreateArgs toXGroupCreateOptions(XGroupCreateOptions options) {
        if (options == null || !options.valid()) {
            return null;
        }
        XGroupCreateArgs args = new XGroupCreateArgs();
        args.mkstream(options.isMkstream());
        if (options.getEntriesRead() != null) {
            args.entriesRead(options.getEntriesRead());
        }
        return args;
    }

    /**
     * {@link Range} 转换为 Lettuce {@link io.lettuce.core.Range}
     *
     * @param range {@link Range} 对象
     * @param <T>   范围类型
     * @return Lettuce {@link io.lettuce.core.Range} 对象
     */
    public static <T> io.lettuce.core.Range<T> toRange(Range<T> range) {
        if (range == null) {
            return null;
        }

        if (range.isUnbounded()) {
            return io.lettuce.core.Range.unbounded();
        }

        return io.lettuce.core.Range.from(toBoundary(range.getLower()), toBoundary(range.getLower()));
    }

    /**
     * {@link Range.Boundary} 转换为 Lettuce {@link io.lettuce.core.Range.Boundary}
     *
     * @param boundary {@link Range.Boundary} 对象
     * @param <T>      边界类型
     * @return Lettuce {@link io.lettuce.core.Range.Boundary} 对象
     */
    public static <T> io.lettuce.core.Range.Boundary<T> toBoundary(Range.Boundary<T> boundary) {
        if (boundary.isUnbounded()) {
            return io.lettuce.core.Range.Boundary.unbounded();
        }

        if (boundary.isIncluding()) {
            return io.lettuce.core.Range.Boundary.including(boundary.getValue());
        }

        return io.lettuce.core.Range.Boundary.excluding(boundary.getValue());
    }

    /**
     * {@link Limit} 转换为 Lettuce {@link io.lettuce.core.Limit}
     *
     * @param limit {@link Limit} 对象
     * @return Lettuce {@link io.lettuce.core.Limit} 对象
     */
    public static io.lettuce.core.Limit toLimit(Limit limit) {
        if (limit == null) {
            return null;
        }

        if (limit.isUnlimited()) {
            return io.lettuce.core.Limit.unlimited();
        }

        return io.lettuce.core.Limit.create(limit.getOffset(), limit.getCount());
    }

}
