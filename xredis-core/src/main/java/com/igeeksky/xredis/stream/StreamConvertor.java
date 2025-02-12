package com.igeeksky.xredis.stream;

import io.lettuce.core.XAddArgs;

/**
 * 流对象转换器
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StreamConvertor {

    /**
     * 私有构造器
     */
    private StreamConvertor() {
    }

    /**
     * 转换
     *
     * @param options 流消息发布选项
     * @return Lettuce {@link XAddArgs} 对象
     */
    public static XAddArgs to(XAddOptions options) {
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

}
