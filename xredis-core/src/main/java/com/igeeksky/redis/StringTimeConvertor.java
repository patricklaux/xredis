package com.igeeksky.redis;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TimeConvertor（处理 {@code List<String>} 类型）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class StringTimeConvertor implements TimeConvertor<String> {

    private static final StringTimeConvertor INSTANCE = new StringTimeConvertor();

    /**
     * 私有构造函数
     */
    private StringTimeConvertor() {
    }

    /**
     * 获取单例
     *
     * @return StringTimeConvertor
     */
    public static StringTimeConvertor getInstance() {
        return INSTANCE;
    }

    @Override
    public long milliseconds(List<String> serverTime) {
        long seconds = Long.parseLong(serverTime.get(0));
        long micros = Long.parseLong(serverTime.get(1));
        return TimeUnit.SECONDS.toMillis(seconds) + TimeUnit.MICROSECONDS.toMillis(micros);
    }

    @Override
    public long microseconds(List<String> serverTime) {
        long seconds = Long.parseLong(serverTime.get(0));
        long micros = Long.parseLong(serverTime.get(1));
        return TimeUnit.SECONDS.toMicros(seconds) + micros;
    }

    @Override
    public long seconds(List<String> serverTime) {
        return Long.parseLong(serverTime.getFirst());
    }

}