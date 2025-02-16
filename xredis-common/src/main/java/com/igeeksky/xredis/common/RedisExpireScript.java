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
     * KEYS[i+1] key <br>
     * ARGV[i+2] value <br>
     * ...
     * <p>
     * {@code @return: OK }
     *
     * @since 1.0.0
     */
    public static final RedisScript PSETEX = new RedisScript(
            "local ttl = ARGV[1];" +
                    "for i = 1, #KEYS do" +
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
     * <p>
     * KEYS[i+1] key <br>
     * ARGV[j+2] ttl <br>
     * ARGV[j+3] value <br>
     * ...
     * <p>
     * {@code @return: OK }
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
     * 如果客户端分开两个命令设置值和过期时间，当客户端异常时，那么 field 的过期时间就可能未设置成功。<br>
     * 通过脚本处理，类似于事务，除非 RedisServer 恰好宕机，才可能会导致某个 field 的过期时间未设置成功。<br>
     * RedisServer 出故障的概率远比客户端出异常的概率小，所以通过脚本处理。
     * <p>
     * KEYS[1] key <br>
     * ARGV[1] ttl <br>
     * ARGV[2] field <br>
     * ARGV[3] value
     * <p>
     * {@code @return: Long }
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


    /**
     * HMSET_HPEXPIRE
     * <p>
     * 如果客户端分开两个命令设置值和过期时间，当客户端异常时，那么 field 的过期时间就可能未设置成功。<br>
     * 通过脚本处理，类似于事务，除非 RedisServer 恰好宕机，才可能会导致某个 field 的过期时间未设置成功。<br>
     * RedisServer 出故障的概率远比客户端出异常的概率小，所以通过脚本处理。
     * <p>
     * KEYS[1] key <br>
     * ARGV[1] ttl <br>
     * ARGV[i] field1 (i=2)<br>
     * ARGV[i+1] value1 <br>
     * ARGV[i+2] field1 <br>
     * ARGV[i+3] value1 <br>
     * ...
     * <p>
     * {@code @return: List<Long> }
     *
     * @since 1.0.0
     */
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
     * 如果客户端分开两个命令设置值和过期时间，当客户端异常时，那么 field 的过期时间就可能未设置成功。<br>
     * 通过脚本处理，类似于事务，除非 RedisServer 恰好宕机，才可能会导致某个 field 的过期时间未设置成功。<br>
     * RedisServer 出故障的概率远比客户端出异常的概率小，所以通过脚本处理。
     * <p>
     * KEYS[1] key <br>
     * ARGV[i] ttl <br>
     * ARGV[i+1] field1 <br>
     * ARGV[i+2] value1 <br>
     * ...
     * <p>
     * {@code @return: List<Long> }
     *
     * @since 1.0.0
     */
    public static final RedisScript HMSET_HPEXPIRE_RANDOM = new RedisScript(
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
