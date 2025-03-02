package com.igeeksky.xredis.lettuce.props;

/**
 * Lettuce 通用配置
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public sealed class LettuceGeneric permits LettuceStandalone, LettuceSentinel, LettuceCluster {

    private String username;
    private String password;
    private int database = 0;
    private String clientName;
    private Boolean ssl;
    private Boolean startTls;
    private String sslVerifyMode;
    private Long timeout;
    private Long shutdownTimeout;
    private Long shutdownQuietPeriod;

    /**
     * 默认构造器
     */
    public LettuceGeneric() {
    }

    /**
     * Redis 用户名
     *
     * @return {@link String} – 用户名
     */
    public String getUsername() {
        return username;
    }

    /**
     * Redis 用户名
     *
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Redis 密码
     *
     * @return {@link String} – 密码
     */
    public String getPassword() {
        return password;
    }

    /**
     * Redis 密码
     *
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Redis 数据库索引
     * <p>
     * 默认值：0
     *
     * @return {@code int} – 数据库索引
     */
    public int getDatabase() {
        return database;
    }

    /**
     * Redis 数据库索引
     * <p>
     * 默认值：0
     *
     * @param database 数据库索引
     */
    public void setDatabase(int database) {
        this.database = database;
    }

    /**
     * Redis 客户端名称
     *
     * @return {@link String} – 客户端名称
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * Redis 客户端名称
     *
     * @param clientName 客户端名称
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * 是否启用 SSL
     * <p>
     * 默认值：false
     *
     * @return {@link Boolean} – 是否启用 SSL
     */
    public Boolean getSsl() {
        return ssl;
    }

    /**
     * 是否启用 SSL
     * <p>
     * 默认值：false
     *
     * @param ssl 是否启用 SSL
     */
    public void setSsl(Boolean ssl) {
        this.ssl = ssl;
    }

    /**
     * 是否启用 TLS
     * <p>
     * 默认值：false
     *
     * @return {@link Boolean} – 是否启用 TLS
     */
    public Boolean getStartTls() {
        return startTls;
    }

    /**
     * 是否启用 TLS
     * <p>
     * 默认值：false
     *
     * @param startTls 是否启用 TLS
     */
    public void setStartTls(Boolean startTls) {
        this.startTls = startTls;
    }

    /**
     * SSL 验证模式
     * <p>
     * 默认值：FULL
     * <p>
     * 可选值：FULL、NONE、CA
     *
     * @return {@link String} – SSL 验证模式
     * @see io.lettuce.core.SslVerifyMode
     */
    public String getSslVerifyMode() {
        return sslVerifyMode;
    }

    /**
     * SSL 验证模式
     * <p>
     * 默认值：FULL
     * <p>
     * 可选值：FULL、NONE、CA
     *
     * @param sslVerifyMode SSL 验证模式
     * @see io.lettuce.core.SslVerifyMode
     */
    public void setSslVerifyMode(String sslVerifyMode) {
        this.sslVerifyMode = sslVerifyMode;
    }

    /**
     * 等待 RedisServer 响应的最大时长
     * <p>
     * 默认值：60000  单位：毫秒
     * <p>
     * 如执行类似于 mset、mget、hmget、hmset……等批处理命令，
     * 且单次操作的数据量大，则需结合网络情况，RedisServer 性能等条件适当调整此值。
     *
     * @return {@link Long} – 等待 RedisServer 响应的最大时长
     */
    public Long getTimeout() {
        return timeout;
    }

    /**
     * 等待 RedisServer 响应的最大时长
     * <p>
     * 默认值：60000  单位：毫秒
     * <p>
     * 如执行类似于 mset、mget、hmget、hmset……等批处理命令，
     * 且单次操作的数据量大，则需结合网络情况，RedisServer 性能等条件适当调整此值。
     *
     * @param timeout 等待 RedisServer 响应的最大时长
     */
    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    /**
     * 客户端资源关闭最大等待时长，单位：毫秒
     * <p>
     * 默认为 2000 ms
     *
     * @return {@code long} – 客户端资源关闭最大等待时长，单位：毫秒
     */
    public Long getShutdownTimeout() {
        return shutdownTimeout;
    }

    /**
     * 客户端资源关闭最大等待时长，单位：毫秒
     * <p>
     * 默认为 2000 ms
     *
     * @param shutdownTimeout 客户端资源关闭最大等待时长，单位：毫秒
     */
    public void setShutdownTimeout(Long shutdownTimeout) {
        this.shutdownTimeout = shutdownTimeout;
    }

    /**
     * 客户端资源关闭静默时长（优雅关闭），单位：毫秒
     * <p>
     * 默认为 100 ms
     * <p>
     * 必须大于等于 0
     *
     * @return {@code long} – 客户端资源关闭静默时长
     * @see <a href="https://redis.github.io/lettuce/advanced-usage/#shutdown">ClientResources-shutdown</a>
     * @see <a href="https://www.javadoc.io/static/io.lettuce/lettuce-core/6.5.3.RELEASE/io/lettuce/core/AbstractRedisClient.html#shutdown">RedisClient-shutdown</a>
     */
    public Long getShutdownQuietPeriod() {
        return shutdownQuietPeriod;
    }

    /**
     * 客户端资源关闭静默时长（优雅关闭），单位：毫秒
     * <p>
     * 默认为 100 ms
     * <p>
     * 必须大于等于 0
     *
     * @param shutdownQuietPeriod 客户端关闭安静时间，单位：毫秒
     * @see <a href="https://redis.github.io/lettuce/advanced-usage/#shutdown">ClientResources-shutdown</a>
     * @see <a href="https://www.javadoc.io/static/io.lettuce/lettuce-core/6.5.3.RELEASE/io/lettuce/core/AbstractRedisClient.html#shutdown">RedisClient-shutdown</a>
     */
    public void setShutdownQuietPeriod(Long shutdownQuietPeriod) {
        this.shutdownQuietPeriod = shutdownQuietPeriod;
    }

}
