package com.igeeksky.xredis;

import com.igeeksky.xredis.stream.XAddOptions;
import com.igeeksky.xredis.stream.XReadOptions;
import com.igeeksky.xtool.core.KeyValue;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import io.lettuce.core.XAddArgs;
import io.lettuce.core.XReadArgs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lettuce 对象转换器
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
     * {@link XReadOptions} 转换为 Lettuce {@link XReadArgs} 对象
     *
     * @param options 流消息读取选项
     * @return Lettuce {@link XReadArgs} 对象
     */
    public static XReadArgs toReadArgs(XReadOptions options) {
        int count = options.count();
        long block = options.block();
        if (block >= 0) {
            return XReadArgs.Builder.block(block).count(count).noack(options.noack());
        } else {
            return XReadArgs.Builder.count(count).noack(options.noack());
        }
    }

    /**
     * {@link XAddOptions} 转换为 Lettuce {@link XAddArgs} 对象
     *
     * @param options 流消息发布选项
     * @return Lettuce {@link XAddArgs} 对象
     */
    public static XAddArgs toAddArgs(XAddOptions options) {
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
     * Lettuce {@link io.lettuce.core.KeyValue} 转换为 {@link KeyValue} 对象
     *
     * @param keyValues Lettuce {@link io.lettuce.core.KeyValue} 对象列表
     * @return {@link KeyValue} 对象列表
     */
    public static List<KeyValue<byte[], byte[]>> fromKeyValues(List<io.lettuce.core.KeyValue<byte[], byte[]>> keyValues) {
        if (CollectionUtils.isEmpty(keyValues)) {
            return Collections.emptyList();
        }
        List<KeyValue<byte[], byte[]>> results = new ArrayList<>(keyValues.size());
        keyValues.forEach(keyValue -> {
            if (keyValue != null && keyValue.hasValue()) {
                results.add(KeyValue.create(keyValue.getKey(), keyValue.getValue()));
            }
        });
        return results;
    }

}
