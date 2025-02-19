package com.igeeksky.xredis.lettuce.generator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.igeeksky.xredis.lettuce.generator.MethodInfo.parseParams;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class MethodInfoTest {

    @Test
    public void test1() {
        String params = "KeyValueStreamingChannel<K, V, T>  channel, K... keys";
        String expected = "[{\"key\":\"KeyValueStreamingChannel<K, V, T>\", \"value\":\"channel\"}, {\"key\":\"K...\", \"value\":\"keys\"}]";
        String parameters = parseParams(params).toString();
        System.out.println(parameters);
        Assertions.assertEquals(expected, parameters);
    }

    @Test
    public void test2() {
        String params = "K key, Range<? extends Number> range";
        String expected = "[{\"key\":\"K\", \"value\":\"key\"}, {\"key\":\"Range<? extends Number>\", \"value\":\"range\"}]";
        String parameters = parseParams(params).toString();
        System.out.println(parameters);
        Assertions.assertEquals(expected, parameters);
    }

    @Test
    public void test3() {
        String params = "List<JsonMsetArgs<K, V>> arguments";
        String expected = "[{\"key\":\"List<JsonMsetArgs<K, V>>\", \"value\":\"arguments\"}]";
        String parameters = parseParams(params).toString();
        System.out.println(parameters);
        Assertions.assertEquals(expected, parameters);
    }

    @Test
    public void test4() {
        String params = "K key, double longitude, double latitude, double distance, GeoArgs.Unit unit,GeoRadiusStoreArgs<K> geoRadiusStoreArgs";
        String expected = "[{\"key\":\"K\", \"value\":\"key\"}, {\"key\":\"double\", \"value\":\"longitude\"}, {\"key\":\"double\", \"value\":\"latitude\"}, {\"key\":\"double\", \"value\":\"distance\"}, {\"key\":\"GeoArgs.Unit\", \"value\":\"unit\"}, {\"key\":\"GeoRadiusStoreArgs<K>\", \"value\":\"geoRadiusStoreArgs\"}]";
        String parameters = parseParams(params).toString();
        System.out.println(parameters);
        Assertions.assertEquals(expected, parameters);
    }

}
