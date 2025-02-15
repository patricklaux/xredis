package com.igeeksky.xredis.common;

/**
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisExpireScript {

    /**
     * PSETEX
     * <p>
     * 相同过期时间
     * <p>
     * ARGV[1] ttl
     * <br>
     * KEYS[i] key <br>
     * ARGV[i+1] value <br>
     * ...
     *
     * @since 1.0.0
     */
    public static final RedisScript PSETEX = new RedisScript(
            "local ttl = ARGV[1];" +
                    "for i = 1, #KEYS, do" +
                    "    redis.call('PSETEX', KEYS[i], ttl, ARGV[i+1]);" +
                    "end;" +
                    "return 'OK';"
            , ResultType.STATUS);

    /**
     * PSETEX_RANDOM
     * <p>
     * 随机过期时间
     * <p>
     * KEYS[i] key <br>
     * ARGV[j] ttl <br>
     * ARGV[j+1] value <br>
     * ...
     *
     * @since 1.0.0
     */
    public static final RedisScript PSETEX_RANDOM = new RedisScript(
            "local i = 1;" +
                    "for j = 1, #ARGV, 2 do" +
                    "    redis.call('PSETEX', KEYS[i], ARGV[j], ARGV[j+1]);" +
                    "    i = i + 1;" +
                    "end;" +
                    "return 'OK';"
            , ResultType.STATUS);


    /**
     * HSET_HPEXPIRE
     * <p>
     * KEYS[1] key
     * ARGV[1] ttl
     * ARGV[2] field
     * ARGV[3] value
     *
     * @since 1.0.0
     */
    public static final RedisScript HSET_HPEXPIRE = new RedisScript(
            "local key = KEYS[1];" +
                    "local ttl = ARGV[1];" +
                    "local field = ARGV[2];" +
                    "local value = ARGV[3];" +
                    "redis.call('HSET', key, field, value);" +
                    "local result = redis.call('HPEXPIRE', key, ttl, 'FIELDS', 1, field);" +
                    "return result[1];"
            , ResultType.INTEGER);


    public static final RedisScript HMSET_HPEXPIRE = new RedisScript(
            "local j = 1;" +
                    "local results = {};" +
                    "local key = KEYS[1];" +
                    "local ttl = ARGV[1];" +
                    "for i = 2, #ARGV, 2 do" +
                    "    local field = ARGV[i];" +
                    "    local value =  ARGV[i+1];" +
                    "    redis.call('HSET', key, field, value);" +
                    "    local states = redis.call('HPEXPIRE', key, ttl, 'FIELDS', 1, field);" +
                    "    results[j] = states[1];" +
                    "    j = j + 1;" +
                    "end;" +
                    "return results;"
            , ResultType.MULTI);


    /**
     * HMSET_RANDOM_HPEXPIRE
     * <p>
     * KEYS[1] key <br>
     * ARGV[i] ttl <br>
     * ARGV[i+1] field1 <br>
     * ARGV[i+2] value1 <br>
     * ...
     *
     * @since 1.0.0
     */
    public static final RedisScript HMSET_RANDOM_HPEXPIRE = new RedisScript(
            "local j = 1;" +
                    "local results = {};" +
                    "local key = KEYS[1];" +
                    "for i = 1, #ARGV, 3 do" +
                    "    local ttl = ARGV[i];" +
                    "    local field = ARGV[i+1];" +
                    "    local value =  ARGV[i+2];" +
                    "    redis.call('HSET', key, field, value);" +
                    "    local states = redis.call('HPEXPIRE', key, ttl, 'FIELDS', 1, field);" +
                    "    results[j] = states[1];" +
                    "    j = j + 1;" +
                    "end;" +
                    "return results;"
            , ResultType.MULTI);

}
