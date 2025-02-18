package com.igeeksky.xredis.common;

import com.igeeksky.xtool.core.json.SimpleJSON;
import com.igeeksky.xtool.core.lang.Assert;
import com.igeeksky.xtool.core.security.DigestUtils;

import java.nio.charset.StandardCharsets;

/**
 * Redis Lua 脚本
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class RedisScript {

    /**
     * 脚本返回类型
     */
    private final ResultType resultType;

    /**
     * Lua 脚本内容序列化数据
     */
    private final byte[] scriptBytes;

    /**
     * Lua 脚本内容
     */
    private final String script;

    /**
     * 脚本 SHA1 值
     */
    private String sha1;

    /**
     * 构造函数
     *
     * @param script Lua 脚本
     */
    public RedisScript(String script) {
        this(script, ResultType.VALUE);
    }

    /**
     * 构造函数
     *
     * @param script Lua 脚本
     * @param resultType   脚本返回类型
     */
    public RedisScript(String script, ResultType resultType) {
        Assert.notNull(resultType, "type must not be null");
        Assert.notNull(script, "script must not be null");
        this.script = script;
        this.scriptBytes = script.getBytes(StandardCharsets.UTF_8);
        this.sha1 = DigestUtils.sha1(script);
        this.resultType = resultType;
    }

    /**
     * 获取脚本返回类型
     *
     * @return {@link ResultType} – 脚本返回类型
     */
    public ResultType getResultType() {
        return resultType;
    }

    /**
     * 获取脚本内容
     *
     * @return {@link String} – 脚本内容
     */
    public String getScript() {
        return script;
    }

    /**
     * 获取脚本内容序列化数据
     *
     * @return {@code byte[]} – 脚本内容序列化数据
     */
    public byte[] getScriptBytes() {
        return scriptBytes;
    }

    /**
     * 设置脚本数据摘要
     *
     * @param sha1 SHA1 数据摘要
     */
    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    /**
     * 获取脚本数据摘要
     *
     * @return {@link String} – SHA1 数据摘要
     */
    public String getSha1() {
        return this.sha1;
    }

    @Override
    public String toString() {
        return SimpleJSON.toJSONString(this);
    }

}