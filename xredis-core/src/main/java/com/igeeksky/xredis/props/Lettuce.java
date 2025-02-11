package com.igeeksky.xredis.props;

import java.security.KeyStore;
import java.util.List;
import java.util.Set;

/**
 * Lettuce 客户端配置选项
 *
 * @author Patrick.Lau
 * @since 1.0.0 2024/12/22
 */
public final class Lettuce {

    /**
     * 私有构造函数
     */
    private Lettuce() {
    }

    /**
     * 客户端配置选项
     * <p>
     * 注意：{@link io.lettuce.core.protocol.DecodeBufferPolicy} 需要编程实现，无法配置
     *
     * @see io.lettuce.core.ClientOptions
     */
    public static class ClientOptions {

        /**
         * 默认构造函数
         */
        public ClientOptions() {
        }

        private Boolean autoReconnect;

        private String disconnectedBehavior;

        private Boolean pingBeforeActivateConnection;

        private String protocolVersion;

        private Boolean publishOnScheduler;

        private Integer requestQueueSize;

        private Boolean suspendReconnectOnProtocolFailure;

        private SocketOptions socketOptions;

        private SslOptions sslOptions;

        private TimeoutOptions timeoutOptions;

        /**
         * 是否自动重连
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_AUTO_RECONNECT}
         *
         * @return {@link Boolean} – 是否自动重连
         */
        public Boolean getAutoReconnect() {
            return autoReconnect;
        }

        /**
         * 设置：是否自动重连
         *
         * @param autoReconnect 是否自动重连
         * @see io.lettuce.core.ClientOptions#DEFAULT_AUTO_RECONNECT
         */
        public void setAutoReconnect(Boolean autoReconnect) {
            this.autoReconnect = autoReconnect;
        }

        /**
         * 连接断开后是否接受命令
         * <p>
         * 默认值：DEFAULT <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_DISCONNECTED_BEHAVIOR}
         * <p>
         * <b>可选项</b>：<p>
         * DEFAULT：如果 auto-reconnect 为 true 则连接断开时接受命令；如果 auto-reconnect 为 false, 则连接断开时拒绝命令。 <br>
         * ACCEPT_COMMANDS：连接断开时接受命令； <br>
         * REJECT_COMMANDS：连接断开时拒绝命令。
         *
         * @return {@link String} – 连接断开后是否接受命令
         * @see io.lettuce.core.ClientOptions.DisconnectedBehavior
         */
        public String getDisconnectedBehavior() {
            return disconnectedBehavior;
        }

        /**
         * 设置：连接断开后是否接受命令
         *
         * @param disconnectedBehavior 连接断开后是否接受命令
         * @see io.lettuce.core.ClientOptions.DisconnectedBehavior
         */
        public void setDisconnectedBehavior(String disconnectedBehavior) {
            this.disconnectedBehavior = disconnectedBehavior;
        }

        /**
         * 连接激活前是否发送 PING 消息
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_PING_BEFORE_ACTIVATE_CONNECTION}
         *
         * @return {@link Boolean} – 是否在连接激活前发送 PING 消息
         */
        public Boolean getPingBeforeActivateConnection() {
            return pingBeforeActivateConnection;
        }

        /**
         * 设置：是否在连接激活前发送 PING 消息
         *
         * @param pingBeforeActivateConnection 是否在连接激活前发送 PING 消息
         */
        public void setPingBeforeActivateConnection(Boolean pingBeforeActivateConnection) {
            this.pingBeforeActivateConnection = pingBeforeActivateConnection;
        }

        /**
         * 协议版本
         * <p>
         * 默认值：RESP3 <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_PROTOCOL_VERSION}
         *
         * @return {@link String} – 协议版本
         */
        public String getProtocolVersion() {
            return protocolVersion;
        }

        /**
         * 设置：协议版本
         *
         * @param protocolVersion 协议版本
         */
        public void setProtocolVersion(String protocolVersion) {
            this.protocolVersion = protocolVersion;
        }

        /**
         * 是否使用调度器发布事件（mono，flux）
         * <p>
         * 默认值：false <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_PUBLISH_ON_SCHEDULER}
         *
         * @return {@link Boolean} – 是否使用调度器发布事件
         */
        public Boolean getPublishOnScheduler() {
            return publishOnScheduler;
        }

        /**
         * 设置：是否使用调度器发布事件
         *
         * @param publishOnScheduler 是否使用调度器发布事件
         */
        public void setPublishOnScheduler(Boolean publishOnScheduler) {
            this.publishOnScheduler = publishOnScheduler;
        }

        /**
         * 请求队列大小
         * <p>
         * 默认值：Integer.MAX_VALUE (2147483647) <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_REQUEST_QUEUE_SIZE}
         *
         * @return {@link Integer} – 请求队列大小
         */
        public Integer getRequestQueueSize() {
            return requestQueueSize;
        }

        /**
         * 设置：请求队列大小
         *
         * @param requestQueueSize 请求队列大小
         */
        public void setRequestQueueSize(Integer requestQueueSize) {
            this.requestQueueSize = requestQueueSize;
        }

        /**
         * 是否在协议失败时暂停重连
         * <p>
         * 如 ping 失败，SSL校验错误……
         * <p>
         * 默认值：false <br>
         * {@link io.lettuce.core.ClientOptions#DEFAULT_SUSPEND_RECONNECT_PROTO_FAIL}
         *
         * @return {@link Boolean} – 是否在协议失败时暂停重连
         */
        public Boolean getSuspendReconnectOnProtocolFailure() {
            return suspendReconnectOnProtocolFailure;
        }

        /**
         * 设置：是否在协议失败时暂停重连
         *
         * @param suspendReconnectOnProtocolFailure 是否在协议失败时暂停重连
         */
        public void setSuspendReconnectOnProtocolFailure(Boolean suspendReconnectOnProtocolFailure) {
            this.suspendReconnectOnProtocolFailure = suspendReconnectOnProtocolFailure;
        }

        /**
         * socket 配置选项
         *
         * @return {@link SocketOptions} – socket 配置选项
         * @see io.lettuce.core.SocketOptions
         */
        public SocketOptions getSocketOptions() {
            return socketOptions;
        }

        /**
         * 设置：socket 配置选项
         *
         * @param socketOptions socket 配置选项
         * @see io.lettuce.core.SocketOptions
         */
        public void setSocketOptions(SocketOptions socketOptions) {
            this.socketOptions = socketOptions;
        }

        /**
         * SSL 配置选项
         * <p>
         * {@link io.lettuce.core.SslOptions.Builder#sslContext} 如有特殊场景无法配置处理，可自行编程实现。
         * <p>
         * 属性 {@code Supplier<SSLParameters> sslParametersSupplier} 由 {@link io.lettuce.core.SslOptions} 根据配置生成，无需处理。
         * <p>
         * 属性 {@code io.lettuce.core.SslOptions.KeystoreAction keymanager} 由 {@link io.lettuce.core.SslOptions} 根据配置生成，无需处理。
         * <p>
         * 属性 {@code io.lettuce.core.SslOptions.KeystoreAction trustmanager} 由 {@link io.lettuce.core.SslOptions} 根据配置生成，无需处理。
         *
         * @return {@link SslOptions} – SSL 配置选项
         * @see io.lettuce.core.SslOptions
         * @see io.lettuce.core.SslOptions.Builder
         * @see io.netty.handler.ssl.SslContextBuilder
         */
        public SslOptions getSslOptions() {
            return sslOptions;
        }

        /**
         * 设置：SSL 配置选项
         *
         * @param sslOptions SSL 配置选项
         * @see io.lettuce.core.SslOptions
         * @see io.lettuce.core.SslOptions.Builder
         * @see io.netty.handler.ssl.SslContextBuilder
         */
        public void setSslOptions(SslOptions sslOptions) {
            this.sslOptions = sslOptions;
        }

        /**
         * 命令超时配置选项
         * <p>
         * 如希望不同的命令采用不同的超时配置，需自行实现 {@link io.lettuce.core.TimeoutOptions.TimeoutSource} 抽象类。
         *
         * @return {@link TimeoutOptions} – 超时配置选项
         * @see io.lettuce.core.TimeoutOptions
         * @see io.lettuce.core.TimeoutOptions.Builder
         */
        public TimeoutOptions getTimeoutOptions() {
            return timeoutOptions;
        }

        /**
         * 设置：超时配置选项
         *
         * @param timeoutOptions 超时配置选项
         * @see io.lettuce.core.TimeoutOptions
         * @see io.lettuce.core.TimeoutOptions.Builder
         */
        public void setTimeoutOptions(TimeoutOptions timeoutOptions) {
            this.timeoutOptions = timeoutOptions;
        }

    }


    /**
     * 集群客户端配置选项
     * <p>
     * 注意： {@link io.lettuce.core.protocol.DecodeBufferPolicy} 需要编程实现，无法配置
     *
     * @see io.lettuce.core.cluster.ClusterClientOptions
     */
    public static class ClusterClientOptions extends ClientOptions {

        private Integer maxRedirects;

        private Boolean validateClusterNodeMembership;

        private Set<String> nodeFilter;

        private ClusterTopologyRefreshOptions topologyRefreshOptions;

        /**
         * 默认构造函数
         */
        public ClusterClientOptions() {
        }

        /**
         * 重定向最大重试次数
         * <p>
         * 默认值：5
         *
         * @return {@link Integer} – 重定向最大重试次数
         * @see io.lettuce.core.cluster.ClusterClientOptions#DEFAULT_MAX_REDIRECTS
         */
        public Integer getMaxRedirects() {
            return maxRedirects;
        }

        /**
         * 设置：重定向最大重试次数
         *
         * @param maxRedirects 重定向最大重试次数
         */
        public void setMaxRedirects(Integer maxRedirects) {
            this.maxRedirects = maxRedirects;
        }

        /**
         * 是否验证集群节点成员
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.cluster.ClusterClientOptions#DEFAULT_VALIDATE_CLUSTER_MEMBERSHIP}
         *
         * @return {@link Boolean} – 是否验证集群节点成员
         */
        public Boolean getValidateClusterNodeMembership() {
            return validateClusterNodeMembership;
        }

        /**
         * 设置：是否验证集群节点成员
         *
         * @param validateClusterNodeMembership 是否验证集群节点成员
         */
        public void setValidateClusterNodeMembership(Boolean validateClusterNodeMembership) {
            this.validateClusterNodeMembership = validateClusterNodeMembership;
        }

        /**
         * 节点白名单
         * <p>
         * 如果未配置，默认连接所有节点；如果有配置，只连接白名单节点。<br>
         * {@link io.lettuce.core.cluster.ClusterClientOptions#DEFAULT_NODE_FILTER}
         *
         * @return {@link Set<String>} – 节点白名单
         */
        public Set<String> getNodeFilter() {
            return nodeFilter;
        }

        /**
         * 设置：节点白名单
         *
         * @param nodeFilter 节点白名单
         */
        public void setNodeFilter(Set<String> nodeFilter) {
            this.nodeFilter = nodeFilter;
        }

        /**
         * 拓扑刷新配置选项
         *
         * @return {@link ClusterTopologyRefreshOptions} – 拓扑刷新配置选项
         * @see io.lettuce.core.cluster.ClusterTopologyRefreshOptions
         */
        public ClusterTopologyRefreshOptions getTopologyRefreshOptions() {
            return topologyRefreshOptions;
        }

        /**
         * 设置：拓扑刷新配置选项
         *
         * @param topologyRefreshOptions 拓扑刷新配置选项
         */
        public void setTopologyRefreshOptions(ClusterTopologyRefreshOptions topologyRefreshOptions) {
            this.topologyRefreshOptions = topologyRefreshOptions;
        }

    }


    /**
     * 集群拓扑刷新配置选项
     *
     * @see io.lettuce.core.cluster.ClusterTopologyRefreshOptions
     */
    public static class ClusterTopologyRefreshOptions {

        /**
         * 默认构造参数
         */
        public ClusterTopologyRefreshOptions() {
        }

        private Set<String> adaptiveRefreshTriggers;

        private Long adaptiveRefreshTimeout;

        private Boolean closeStaleConnections;

        private Boolean dynamicRefreshSources;

        private Boolean periodicRefreshEnabled;

        private Long refreshPeriod;

        private Integer refreshTriggersReconnectAttempts;

        /**
         * 动态刷新触发器
         * <p>
         * 默认值：空集 <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_ADAPTIVE_REFRESH_TRIGGERS}
         *
         * @return {@link Set<String>} – 动态刷新触发器
         */
        public Set<String> getAdaptiveRefreshTriggers() {
            return adaptiveRefreshTriggers;
        }

        /**
         * 设置：动态刷新触发器
         *
         * @param adaptiveRefreshTriggers 动态刷新触发器
         */
        public void setAdaptiveRefreshTriggers(Set<String> adaptiveRefreshTriggers) {
            this.adaptiveRefreshTriggers = adaptiveRefreshTriggers;
        }

        /**
         * 动态刷新超时
         * <p>
         * 默认值：30000 单位：毫秒 <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_ADAPTIVE_REFRESH_TIMEOUT}
         *
         * @return {@link Long} – 动态刷新超时
         */
        public Long getAdaptiveRefreshTimeout() {
            return adaptiveRefreshTimeout;
        }

        /**
         * 设置：动态刷新超时
         *
         * @param adaptiveRefreshTimeout 动态刷新超时
         */
        public void setAdaptiveRefreshTimeout(Long adaptiveRefreshTimeout) {
            this.adaptiveRefreshTimeout = adaptiveRefreshTimeout;
        }

        /**
         * 是否关闭旧连接
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_CLOSE_STALE_CONNECTIONS}
         *
         * @return {@link Boolean} – 是否关闭旧连接
         */
        public Boolean getCloseStaleConnections() {
            return closeStaleConnections;
        }

        /**
         * 设置：是否关闭旧连接
         *
         * @param closeStaleConnections 是否关闭旧连接
         */
        public void setCloseStaleConnections(Boolean closeStaleConnections) {
            this.closeStaleConnections = closeStaleConnections;
        }

        /**
         * 是否动态刷新节点源
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_DYNAMIC_REFRESH_SOURCES}
         *
         * @return {@link Boolean} – 是否动态刷新节点源
         */
        public Boolean getDynamicRefreshSources() {
            return dynamicRefreshSources;
        }

        /**
         * 设置：是否动态刷新节点源
         *
         * @param dynamicRefreshSources 是否动态刷新节点源
         */
        public void setDynamicRefreshSources(Boolean dynamicRefreshSources) {
            this.dynamicRefreshSources = dynamicRefreshSources;
        }

        /**
         * 是否启用周期刷新
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_PERIODIC_REFRESH_ENABLED}
         *
         * @return {@link Boolean} – 是否启用周期刷新
         */
        public Boolean getPeriodicRefreshEnabled() {
            return periodicRefreshEnabled;
        }

        /**
         * 设置：是否启用周期刷新
         *
         * @param periodicRefreshEnabled 是否启用周期刷新
         */
        public void setPeriodicRefreshEnabled(Boolean periodicRefreshEnabled) {
            this.periodicRefreshEnabled = periodicRefreshEnabled;
        }

        /**
         * 刷新周期
         * <p>
         * 默认值：30000 单位：毫秒 <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_REFRESH_PERIOD}
         *
         * @return {@link Long} – 刷新周期
         */
        public Long getRefreshPeriod() {
            return refreshPeriod;
        }

        /**
         * 设置：刷新周期
         *
         * @param refreshPeriod 刷新周期
         */
        public void setRefreshPeriod(Long refreshPeriod) {
            this.refreshPeriod = refreshPeriod;
        }

        /**
         * 刷新触发器重连尝试次数
         * <p>
         * 默认值：3 <br>
         * {@link io.lettuce.core.cluster.ClusterTopologyRefreshOptions.RefreshTrigger#DEFAULT_REFRESH_TRIGGERS_RECONNECT_ATTEMPTS}
         *
         * @return {@link Integer} – 刷新触发器重连尝试次数
         */
        public Integer getRefreshTriggersReconnectAttempts() {
            return refreshTriggersReconnectAttempts;
        }

        /**
         * 设置：刷新触发器重连尝试次数
         *
         * @param refreshTriggersReconnectAttempts 刷新触发器重连尝试次数
         */
        public void setRefreshTriggersReconnectAttempts(Integer refreshTriggersReconnectAttempts) {
            this.refreshTriggersReconnectAttempts = refreshTriggersReconnectAttempts;
        }

    }


    /**
     * KeepAlive 配置选项
     *
     * @see io.lettuce.core.SocketOptions.KeepAliveOptions
     */
    public static class KeepAliveOptions {

        /**
         * 默认构造函数
         */
        public KeepAliveOptions() {
        }

        private Integer count;

        private Boolean enabled;

        private Long idle;

        private Long interval;

        /**
         * KeepAlive 重试次数
         * <p>
         * 默认值：9 <br>
         * {@link io.lettuce.core.SocketOptions.KeepAliveOptions#DEFAULT_COUNT}
         *
         * @return {@link Integer} – KeepAlive 重试次数
         */
        public Integer getCount() {
            return count;
        }

        /**
         * 设置：KeepAlive 重试次数
         *
         * @param count KeepAlive 重试次数
         */
        public void setCount(Integer count) {
            this.count = count;
        }

        /**
         * 是否启用 KeepAlive
         * <p>
         * 默认值：false <br>
         * {@link io.lettuce.core.SocketOptions#DEFAULT_SO_KEEPALIVE}
         *
         * @return {@link Boolean} – 是否启用 KeepAlive
         */
        public Boolean getEnabled() {
            return enabled;
        }

        /**
         * 设置：是否启用 KeepAlive
         *
         * @param enabled 是否启用 KeepAlive
         */
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * KeepAlive 空闲时间
         * <p>
         * 默认值：7200000， 单位：毫秒
         * <p>
         * {@link io.lettuce.core.SocketOptions.KeepAliveOptions#DEFAULT_IDLE}
         *
         * @return {@link Long} – KeepAlive 空闲时间
         */
        public Long getIdle() {
            return idle;
        }

        /**
         * 设置：KeepAlive 空闲时间
         *
         * @param idle KeepAlive 空闲时间
         */
        public void setIdle(Long idle) {
            this.idle = idle;
        }

        /**
         * KeepAlive 发送间隔时长
         * <p>
         * 默认值：75000， 单位：毫秒 <br>
         * {@link io.lettuce.core.SocketOptions.KeepAliveOptions#DEFAULT_INTERVAL}
         *
         * @return {@link Long} – KeepAlive 发送间隔时长
         */
        public Long getInterval() {
            return interval;
        }

        /**
         * 设置：KeepAlive 发送间隔时长
         *
         * @param interval KeepAlive 发送间隔时长
         */
        public void setInterval(Long interval) {
            this.interval = interval;
        }

    }


    /**
     * Socket 配置选项
     *
     * @see io.lettuce.core.SocketOptions
     */
    public static class SocketOptions {

        /**
         * 默认构造函数
         */
        public SocketOptions() {
        }

        private Long connectTimeout;

        private Boolean tcpNoDelay;

        private KeepAliveOptions keepAlive;

        private TcpUserTimeoutOptions tcpUserTimeout;

        /**
         * Socket 连接超时
         * <p>
         * 默认值：10000， 单位：毫秒 <br>
         * {@link io.lettuce.core.SocketOptions#DEFAULT_CONNECT_TIMEOUT_DURATION}
         *
         * @return {@link Long} – Socket 连接超时
         */
        public Long getConnectTimeout() {
            return connectTimeout;
        }

        /**
         * 设置：Socket 连接超时
         *
         * @param connectTimeout Socket 连接超时
         */
        public void setConnectTimeout(Long connectTimeout) {
            this.connectTimeout = connectTimeout;
        }

        /**
         * 是否启用 TCP_NODELAY
         * <p>
         * 默认值：true <br>
         * {@link io.lettuce.core.SocketOptions#DEFAULT_SO_NO_DELAY}
         *
         * @return {@link Boolean} – 是否启用 TCP_NODELAY
         */
        public Boolean getTcpNoDelay() {
            return tcpNoDelay;
        }

        /**
         * 设置：是否启用 TCP_NODELAY
         *
         * @param tcpNoDelay 是否启用 TCP_NODELAY
         */
        public void setTcpNoDelay(Boolean tcpNoDelay) {
            this.tcpNoDelay = tcpNoDelay;
        }

        /**
         * KeepAlive 配置选项
         * <p>
         * 高级配置项，一般无需配置。
         * <p>
         * 仅适用于 epoll、 io_uring、Java 11 及之后版本的 NIO。
         * <p>
         * 默认不启用
         *
         * @return {@link KeepAliveOptions} – KeepAlive 配置选项
         * @see io.lettuce.core.SocketOptions.KeepAliveOptions
         */
        public KeepAliveOptions getKeepAlive() {
            return keepAlive;
        }

        /**
         * 设置：KeepAlive 配置选项
         *
         * @param keepAlive KeepAlive 配置选项
         */
        public void setKeepAlive(KeepAliveOptions keepAlive) {
            this.keepAlive = keepAlive;
        }

        /**
         * TCP_USER_TIMEOUT 配置选项
         * <p>
         * 高级配置项，一般无需配置。
         * <p>
         * 仅适用于 epoll 和 io_uring。
         * <p>
         * 默认不启用
         *
         * @return {@link TcpUserTimeoutOptions} – TCP_USER_TIMEOUT 配置选项
         * @see io.lettuce.core.SocketOptions.TcpUserTimeoutOptions
         */
        public TcpUserTimeoutOptions getTcpUserTimeout() {
            return tcpUserTimeout;
        }

        /**
         * 设置：TCP_USER_TIMEOUT 配置选项
         *
         * @param tcpUserTimeout TCP_USER_TIMEOUT 配置选项
         */
        public void setTcpUserTimeout(TcpUserTimeoutOptions tcpUserTimeout) {
            this.tcpUserTimeout = tcpUserTimeout;
        }

    }


    /**
     * SSL 配置选项
     *
     * @see io.lettuce.core.SslOptions
     */
    public static class SslOptions {

        /**
         * 默认构造函数
         */
        public SslOptions() {
        }

        private String sslProvider;

        private String keyStoreType;

        private String keystore;

        private String keystorePassword;

        private String truststore;

        private String truststorePassword;

        private List<String> protocols;

        private List<String> cipherSuites;

        private Long handshakeTimeout;

        /**
         * SSL Provider
         * <p>
         * 默认值：JDK <br>
         * {@link io.lettuce.core.SslOptions#DEFAULT_SSL_PROVIDER}
         * <p>
         * 可选值：JDK, OPENSSL,OPENSSL_REFCNT
         *
         * @return {@link String} – SSL Provider
         * @see io.netty.handler.ssl.SslProvider
         */
        public String getSslProvider() {
            return sslProvider;
        }

        /**
         * 设置：SSL Provider
         *
         * @param sslProvider SSL Provider
         */
        public void setSslProvider(String sslProvider) {
            this.sslProvider = sslProvider;
        }

        /**
         * 密钥库格式
         * <p>
         * 默认值：jks <br>
         * {@link KeyStore#getDefaultType()}
         *
         * @return {@link String} – 密钥库格式
         */
        public String getKeyStoreType() {
            return keyStoreType;
        }

        /**
         * 设置：密钥库格式
         *
         * @param keyStoreType 密钥库格式
         */
        public void setKeyStoreType(String keyStoreType) {
            this.keyStoreType = keyStoreType;
        }

        /**
         * 密钥库路径
         *
         * @return {@link String} – 密钥库路径
         */
        public String getKeystore() {
            return keystore;
        }

        /**
         * 设置：密钥库路径
         *
         * @param keystore 密钥库路径
         */
        public void setKeystore(String keystore) {
            this.keystore = keystore;
        }

        /**
         * 密钥库密码
         *
         * @return {@link String} – 密钥库密码
         */
        public String getKeystorePassword() {
            return keystorePassword;
        }

        /**
         * 设置：密钥库密码
         *
         * @param keystorePassword 密钥库密码
         */
        public void setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
        }

        /**
         * 信任库路径
         *
         * @return {@link String} – 信任库路径
         */
        public String getTruststore() {
            return truststore;
        }

        /**
         * 设置：信任库路径
         *
         * @param truststore 信任库路径
         */
        public void setTruststore(String truststore) {
            this.truststore = truststore;
        }

        /**
         * 信任库密码
         *
         * @return {@link String} – 信任库密码
         */
        public String getTruststorePassword() {
            return truststorePassword;
        }

        /**
         * 设置：信任库密码
         *
         * @param truststorePassword 信任库密码
         */
        public void setTruststorePassword(String truststorePassword) {
            this.truststorePassword = truststorePassword;
        }

        /**
         * 支持的安全协议
         * <p>
         * 例如：TLSv1.3, TLSv1.2
         *
         * @return {@link List<String>} – 安全协议
         */
        public List<String> getProtocols() {
            return protocols;
        }

        /**
         * 设置：支持的安全协议
         *
         * @param protocols 安全协议
         */
        public void setProtocols(List<String> protocols) {
            this.protocols = protocols;
        }

        /**
         * 支持的加密套件
         *
         * @return {@link List<String>} – 加密套件
         */
        public List<String> getCipherSuites() {
            return cipherSuites;
        }

        /**
         * 设置：支持的加密套件
         *
         * @param cipherSuites 加密套件
         */
        public void setCipherSuites(List<String> cipherSuites) {
            this.cipherSuites = cipherSuites;
        }

        /**
         * 握手超时
         * <p>
         * 默认值：10000 单位：毫秒
         *
         * @return {@link Long} – 握手超时
         */
        public Long getHandshakeTimeout() {
            return handshakeTimeout;
        }

        /**
         * 设置：握手超时
         *
         * @param handshakeTimeout 握手超时
         */
        public void setHandshakeTimeout(Long handshakeTimeout) {
            this.handshakeTimeout = handshakeTimeout;
        }

    }


    /**
     * TCP User Timeout 配置选项
     *
     * @see io.lettuce.core.SocketOptions.TcpUserTimeoutOptions
     */
    public static class TcpUserTimeoutOptions {

        /**
         * 默认构造函数
         */
        public TcpUserTimeoutOptions() {
        }

        private Boolean enabled;

        private Long tcpUserTimeout;

        /**
         * 是否启用 TCP_USER_TIMEOUT
         * <p>
         * 默认值：false
         * <p>
         * {@link io.lettuce.core.SocketOptions#DEFAULT_TCP_USER_TIMEOUT_ENABLED}
         *
         * @return {@link Boolean} – 是否启用 TCP_USER_TIMEOUT
         */
        public Boolean getEnabled() {
            return enabled;
        }

        /**
         * 设置：是否启用 TCP_USER_TIMEOUT
         *
         * @param enabled 是否启用 TCP_USER_TIMEOUT
         */
        public void setEnabled(Boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * TCP User Timeout
         * <p>
         * 默认值：7875000 单位：毫秒
         * <p>
         * 计算过程： <br>
         * TCP_KEEPIDLE(2 hour) + TCP_KEEPINTVL(75 s) * TCP_KEEPCNT(9) <br>
         * 2 * 3600 + 75 * 9 = 7875
         * <p>
         * {@link io.lettuce.core.SocketOptions.TcpUserTimeoutOptions#DEFAULT_TCP_USER_TIMEOUT}
         *
         * @return {@link Long} – TCP User Timeout
         */
        public Long getTcpUserTimeout() {
            return tcpUserTimeout;
        }

        /**
         * 设置：TCP User Timeout
         *
         * @param tcpUserTimeout TCP User Timeout
         */
        public void setTcpUserTimeout(Long tcpUserTimeout) {
            this.tcpUserTimeout = tcpUserTimeout;
        }

    }


    /**
     * 命令超时配置选项
     *
     * @see io.lettuce.core.TimeoutOptions
     */
    public static class TimeoutOptions {
        /**
         * 默认构造函数
         */
        public TimeoutOptions() {
        }

        private Long fixedTimeout;

        /**
         * 固定超时时间
         * <p>
         * 默认值：-1（无超时配置）  单位：毫秒
         * <p>
         * 配置此选项后，则 {@linkplain io.lettuce.core.TimeoutOptions.Builder#timeoutCommands()} 自动为true。
         * <p>
         * 此处配置，所有命令的超时时间相同；
         * 如希望不同命令采用不同的超时配置，需自行编程实现 {@link io.lettuce.core.TimeoutOptions.TimeoutSource} 抽象类。
         *
         * @return {@link Long} – 固定超时时间
         */
        public Long getFixedTimeout() {
            return fixedTimeout;
        }

        /**
         * 设置：固定超时时间
         *
         * @param fixedTimeout 固定超时时间
         */
        public void setFixedTimeout(Long fixedTimeout) {
            this.fixedTimeout = fixedTimeout;
        }

    }


}