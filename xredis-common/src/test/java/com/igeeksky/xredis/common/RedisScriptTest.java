package com.igeeksky.xredis.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
class RedisScriptTest {

    @Test
    void getResultType() {
        RedisScript script = new RedisScript("return redis.call('GET', KEYS[1])");
        Assertions.assertEquals(ResultType.VALUE, script.getResultType());
    }

    @Test
    void getScript() {
        RedisScript script = new RedisScript("return redis.call('GET', KEYS[1])");
        Assertions.assertEquals("return redis.call('GET', KEYS[1])", script.getScript());
    }

    @Test
    void getScriptBytes() {
        String s = "return redis.call('GET', KEYS[1])";
        RedisScript script = new RedisScript(s);
        Assertions.assertArrayEquals(s.getBytes(StandardCharsets.UTF_8), script.getScriptBytes());
    }

    @Test
    void setSha1() {
        RedisScript script = new RedisScript("return redis.call('GET', KEYS[1])");
        Assertions.assertEquals("d3c21d0c2b9ca22f82737626a27bcaf5d288f99f", script.getSha1());
        script.setSha1("ab");
        Assertions.assertEquals("ab", script.getSha1());
    }

    @Test
    void getSha1() {
        RedisScript script = new RedisScript("return redis.call('GET', KEYS[1])");
        Assertions.assertEquals("d3c21d0c2b9ca22f82737626a27bcaf5d288f99f", script.getSha1());
    }

    @Test
    void testToString() {
        RedisScript script = new RedisScript("return redis.call('GET', KEYS[1])");
        System.out.println(script);
        String expected = "{\"resultType\":\"VALUE\"," +
                "\"scriptBytes\":\"cmV0dXJuIHJlZGlzLmNhbGwoJ0dFVCcsIEtFWVNbMV0p\"," +
                "\"script\":\"return redis.call('GET', KEYS[1])\"," +
                "\"sha1\":\"d3c21d0c2b9ca22f82737626a27bcaf5d288f99f\"" +
                "}";
        Assertions.assertEquals(expected, script.toString());
    }

}