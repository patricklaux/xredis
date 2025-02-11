package com.igeeksky.xredis;

import com.igeeksky.xtool.core.lang.codec.StringCodec;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * TimeConvertor（处理 {@code List<byte[]>} 类型）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class ByteArrayTimeConvertor implements TimeConvertor<byte[]> {

    private static final StringCodec CODEC = StringCodec.getInstance(StandardCharsets.UTF_8);

    private static final ByteArrayTimeConvertor INSTANCE = new ByteArrayTimeConvertor();

    /**
     * 私有构造函数
     */
    private ByteArrayTimeConvertor() {
    }

    /**
     * 获取单例
     *
     * @return ByteArrayTimeConvertor
     */
    public static ByteArrayTimeConvertor getInstance() {
        return INSTANCE;
    }

    @Override
    public long milliseconds(List<byte[]> serverTime) {
        long seconds = Long.parseLong(CODEC.decode(serverTime.get(0)));
        long micros = Long.parseLong(CODEC.decode(serverTime.get(1)));
        return TimeUnit.SECONDS.toMillis(seconds) + TimeUnit.MICROSECONDS.toMillis(micros);
    }

    @Override
    public long microseconds(List<byte[]> serverTime) {
        long seconds = Long.parseLong(CODEC.decode(serverTime.get(0)));
        long micros = Long.parseLong(CODEC.decode(serverTime.get(1)));
        return TimeUnit.SECONDS.toMicros(seconds) + micros;
    }

    @Override
    public long seconds(List<byte[]> serverTime) {
        return Long.parseLong(CODEC.decode(serverTime.getFirst()));
    }

}