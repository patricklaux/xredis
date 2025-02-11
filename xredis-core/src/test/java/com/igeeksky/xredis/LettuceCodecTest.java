package com.igeeksky.xredis;

import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class LettuceCodecTest {

    private static final String key = "shop:user:zhang san";

    @Test
    @Disabled
    public void testStringCodecPerformance() {
        RedisCodec<String, String> codec = StringCodec.UTF8;
        for (int i = 0; i < 10000; i++) {
            ByteBuffer buffer = codec.encodeKey(key);
            if (i == 1000) {
                System.out.println(buffer.get());
            }
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            ByteBuffer buffer = codec.encodeKey(key);
            if (i == 100000) {
                System.out.println(buffer.get());
            }
        }
        System.out.println(System.currentTimeMillis() - start);
    }

    @Test
    @Disabled
    public void testByteArrayCodecPerformance() {
        RedisCodec<byte[], byte[]> codec = ByteArrayCodec.INSTANCE;
        for (int i = 0; i < 10000; i++) {
            ByteBuffer buffer = codec.encodeKey(key.getBytes());
            if (i == 1000) {
                System.out.println(buffer.get());
            }
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000000; i++) {
            ByteBuffer buffer = codec.encodeKey(key.getBytes());
            if (i == 100000) {
                System.out.println(buffer.get());
            }
        }
        System.out.println(System.currentTimeMillis() - start);
    }

}
