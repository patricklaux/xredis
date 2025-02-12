package com.igeeksky.xredis.autoconfigure;

import com.igeeksky.xredis.config.ClientOptionsBuilderCustomizer;
import com.igeeksky.xredis.config.RedisNode;
import com.igeeksky.xredis.props.Lettuce;
import com.igeeksky.xtool.core.collection.CollectionUtils;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.*;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import io.lettuce.core.protocol.ProtocolVersion;
import io.netty.handler.ssl.SslProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 客户端选项转换辅助类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public abstract class ClientOptionsHelper {

    /**
     * 私有构造器，仅提供静态方法
     */
    private ClientOptionsHelper() {
    }

    /**
     * 转换客户端选项
     * <p>
     * {@code com.igeeksky.xredis.props.Lettuce.ClientOptions} 配置转换成 {@code io.lettuce.core.ClientOptions}
     *
     * @param id            客户端ID
     * @param clientOptions 客户端配置选项
     * @param customizers   客户端自定义选项
     * @return 客户端选项
     */
    public static ClientOptions clientOptions(String id, Lettuce.ClientOptions clientOptions,
                                              ObjectProvider<ClientOptionsBuilderCustomizer> customizers) {

        ClientOptions.Builder builder = ClientOptions.builder();

        setClientOptionsBuilder(id, clientOptions, builder, customizers);

        customizers.orderedStream().forEach(c -> c.customizeClient(id, builder));

        return builder.build();
    }

    /**
     * 转换集群客户端选项
     * <p>
     * {@code com.igeeksky.xredis.props.Lettuce.ClusterClientOptions} 配置转换成 {@code io.lettuce.core.ClusterClientOptions}
     *
     * @param id            客户端ID
     * @param clientOptions 客户端配置选项
     * @param customizers   客户端自定义选项
     * @return 客户端选项
     */
    public static ClusterClientOptions clusterClientOptions(String id, Lettuce.ClusterClientOptions clientOptions,
                                                            ObjectProvider<ClientOptionsBuilderCustomizer> customizers) {

        ClusterClientOptions.Builder builder = ClusterClientOptions.builder();

        setClientOptionsBuilder(id, clientOptions, builder, customizers);

        Integer maxRedirects = clientOptions.getMaxRedirects();
        if (maxRedirects != null) {
            builder.maxRedirects(maxRedirects);
        }

        Boolean validateClusterNodeMembership = clientOptions.getValidateClusterNodeMembership();
        if (validateClusterNodeMembership != null) {
            builder.validateClusterNodeMembership(validateClusterNodeMembership);
        }

        Set<String> nodeFilter = clientOptions.getNodeFilter();
        if (CollectionUtils.isNotEmpty(nodeFilter)) {
            Set<RedisNode> allows = new HashSet<>(nodeFilter.size());
            nodeFilter.forEach(s -> allows.add(new RedisNode(s)));

            builder.nodeFilter(node -> {
                RedisURI uri = node.getUri();
                return allows.contains(new RedisNode(uri.getHost(), uri.getPort(), uri.getSocket()));
            });
        }

        setRefreshOptionsBuilder(clientOptions.getTopologyRefreshOptions(), builder);

        customizers.orderedStream().forEach(c -> c.customizeClusterClient(id, builder));
        return builder.build();
    }


    private static void setClientOptionsBuilder(
            String id, Lettuce.ClientOptions clientOptions,
            ClientOptions.Builder builder, ObjectProvider<ClientOptionsBuilderCustomizer> customizers) {

        TimeoutOptions.Builder timeoutOptionsBuilder = TimeoutOptions.builder();
        SslOptions.Builder sslOptionsBuilder = SslOptions.builder();
        SocketOptions.Builder socketOptionsBuilder = SocketOptions.builder();

        if (clientOptions != null) {
            Boolean autoReconnect = clientOptions.getAutoReconnect();
            if (autoReconnect != null) {
                builder.autoReconnect(autoReconnect);
            }

            String disconnectedBehavior = StringUtils.toUpperCase(clientOptions.getDisconnectedBehavior());
            if (disconnectedBehavior != null) {
                builder.disconnectedBehavior(ClientOptions.DisconnectedBehavior.valueOf(disconnectedBehavior));
            }

            Boolean publishOnScheduler = clientOptions.getPublishOnScheduler();
            if (publishOnScheduler != null) {
                builder.publishOnScheduler(publishOnScheduler);
            }

            Boolean pingBeforeActivateConnection = clientOptions.getPingBeforeActivateConnection();
            if (pingBeforeActivateConnection != null) {
                builder.pingBeforeActivateConnection(pingBeforeActivateConnection);
            }

            String protocolVersion = StringUtils.toUpperCase(clientOptions.getProtocolVersion());
            if (protocolVersion != null) {
                builder.protocolVersion(ProtocolVersion.valueOf(protocolVersion));
            }

            Boolean suspendReconnectOnProtocolFailure = clientOptions.getSuspendReconnectOnProtocolFailure();
            if (suspendReconnectOnProtocolFailure != null) {
                builder.suspendReconnectOnProtocolFailure(suspendReconnectOnProtocolFailure);
            }

            Integer requestQueueSize = clientOptions.getRequestQueueSize();
            if (requestQueueSize != null) {
                builder.requestQueueSize(requestQueueSize);
            }

            setSslOptionsBuilder(clientOptions.getSslOptions(), sslOptionsBuilder);

            setSocketOptionsBuilder(clientOptions.getSocketOptions(), socketOptionsBuilder);

            setTimeoutOptionsBuilder(clientOptions.getTimeoutOptions(), timeoutOptionsBuilder);
        }

        customizers.orderedStream().forEach(c -> c.customizeTimeout(id, timeoutOptionsBuilder));
        builder.timeoutOptions(timeoutOptionsBuilder.build());

        customizers.orderedStream().forEach(c -> c.customizeSsl(id, sslOptionsBuilder));
        builder.sslOptions(sslOptionsBuilder.build());
    }


    private static void setTimeoutOptionsBuilder(Lettuce.TimeoutOptions options, TimeoutOptions.Builder builder) {
        if (options == null) {
            return;
        }

        Long fixedTimeout = options.getFixedTimeout();
        if (fixedTimeout != null) {
            builder.fixedTimeout(Duration.ofMillis(fixedTimeout));
        }
    }


    private static void setSocketOptionsBuilder(Lettuce.SocketOptions options, SocketOptions.Builder socketBuilder) {
        if (options == null) {
            return;
        }

        Boolean tcpNoDelay = options.getTcpNoDelay();
        if (tcpNoDelay != null) {
            socketBuilder.tcpNoDelay(tcpNoDelay);
        }

        Long connectTimeout = options.getConnectTimeout();
        if (connectTimeout != null) {
            socketBuilder.connectTimeout(Duration.ofMillis(connectTimeout));
        }

        Lettuce.KeepAliveOptions keepAlive = options.getKeepAlive();
        if (keepAlive != null) {
            SocketOptions.KeepAliveOptions.Builder builder = SocketOptions.KeepAliveOptions.builder();

            Integer count = keepAlive.getCount();
            if (count != null) {
                builder.count(count);
            }

            Boolean enabled = keepAlive.getEnabled();
            if (enabled != null) {
                builder.enable(enabled);
            }

            Long idle = keepAlive.getIdle();
            if (idle != null) {
                builder.idle(Duration.ofMillis(idle));
            }

            Long interval = keepAlive.getInterval();
            if (interval != null) {
                builder.interval(Duration.ofMillis(interval));
            }

            socketBuilder.keepAlive(builder.build());
        }

        Lettuce.TcpUserTimeoutOptions tcpUserTimeout = options.getTcpUserTimeout();
        if (tcpUserTimeout != null) {
            SocketOptions.TcpUserTimeoutOptions.Builder builder = SocketOptions.TcpUserTimeoutOptions.builder();
            Boolean enabled = tcpUserTimeout.getEnabled();
            if (enabled != null) {
                builder.enable(enabled);
            }
            Long timeOut = tcpUserTimeout.getTcpUserTimeout();
            if (timeOut != null) {
                builder.tcpUserTimeout(Duration.ofMillis(timeOut));
            }

            socketBuilder.tcpUserTimeout(builder.build());
        }
    }


    private static void setSslOptionsBuilder(Lettuce.SslOptions options, SslOptions.Builder sslBuilder) {
        if (options == null) {
            return;
        }

        String sslProvider = StringUtils.toUpperCase(options.getSslProvider());
        if (sslProvider != null) {
            SslProvider provider = SslProvider.valueOf(sslProvider);
            if (SslProvider.JDK == provider) {
                sslBuilder.jdkSslProvider();
            } else {
                sslBuilder.openSslProvider();
            }
        }

        String keyStoreType = StringUtils.trim(options.getKeyStoreType());
        if (StringUtils.hasLength(keyStoreType)) {
            sslBuilder.keyStoreType(keyStoreType);
        }

        String keystore = StringUtils.trim(options.getKeystore());
        if (StringUtils.hasLength(keystore)) {
            try {
                String keystorePassword = StringUtils.trim(options.getKeystorePassword());
                URL url = ResourceUtils.getURL(keystore);
                if (StringUtils.hasLength(keystorePassword)) {
                    sslBuilder.keystore(url, keystorePassword.toCharArray());
                } else {
                    sslBuilder.keystore(url);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        String truststore = StringUtils.trim(options.getTruststore());
        if (StringUtils.hasLength(truststore)) {
            String truststorePassword = StringUtils.trim(options.getTruststorePassword());
            try {
                URL url = ResourceUtils.getURL(truststore);
                if (StringUtils.hasLength(truststorePassword)) {
                    sslBuilder.truststore(url, truststorePassword);
                } else {
                    sslBuilder.truststore(url);
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        List<String> protocols = options.getProtocols();
        if (CollectionUtils.isNotEmpty(protocols)) {
            sslBuilder.protocols(protocols.toArray(new String[0]));
        }

        List<String> cipherSuites = options.getCipherSuites();
        if (CollectionUtils.isNotEmpty(cipherSuites)) {
            sslBuilder.cipherSuites(cipherSuites.toArray(new String[0]));
        }

        Long handshakeTimeout = options.getHandshakeTimeout();
        if (handshakeTimeout != null) {
            sslBuilder.handshakeTimeout(Duration.ofMillis(handshakeTimeout));
        }
    }


    private static void setRefreshOptionsBuilder(Lettuce.ClusterTopologyRefreshOptions refreshOptions, ClusterClientOptions.Builder builder) {
        if (refreshOptions == null) {
            return;
        }

        ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder();

        Set<String> adaptiveRefreshTriggers = refreshOptions.getAdaptiveRefreshTriggers();
        if (CollectionUtils.isNotEmpty(adaptiveRefreshTriggers)) {
            int size = adaptiveRefreshTriggers.size();
            List<ClusterTopologyRefreshOptions.RefreshTrigger> triggers = new ArrayList<>(size);
            for (String trigger : adaptiveRefreshTriggers) {
                String upperCase = StringUtils.toUpperCase(trigger);
                if (upperCase != null) {
                    triggers.add(ClusterTopologyRefreshOptions.RefreshTrigger.valueOf(upperCase));
                }
            }
            if (CollectionUtils.isNotEmpty(triggers)) {
                refreshBuilder.enableAdaptiveRefreshTrigger(triggers.toArray(new ClusterTopologyRefreshOptions.RefreshTrigger[0]));
            }
        }

        Long adaptiveRefreshTimeout = refreshOptions.getAdaptiveRefreshTimeout();
        if (adaptiveRefreshTimeout != null) {
            refreshBuilder.adaptiveRefreshTriggersTimeout(Duration.ofMillis(adaptiveRefreshTimeout));
        }

        Boolean closeStaleConnections = refreshOptions.getCloseStaleConnections();
        if (closeStaleConnections != null) {
            refreshBuilder.closeStaleConnections(closeStaleConnections);
        }

        Boolean dynamicRefreshSources = refreshOptions.getDynamicRefreshSources();
        if (dynamicRefreshSources != null) {
            refreshBuilder.dynamicRefreshSources(dynamicRefreshSources);
        }

        Long refreshPeriod = refreshOptions.getRefreshPeriod();
        if (refreshPeriod != null) {
            refreshBuilder.refreshPeriod(Duration.ofMillis(refreshPeriod));
        }

        Boolean periodicRefreshEnabled = refreshOptions.getPeriodicRefreshEnabled();
        if (periodicRefreshEnabled != null) {
            refreshBuilder.enablePeriodicRefresh(periodicRefreshEnabled);
        }

        Integer refreshTriggersReconnectAttempts = refreshOptions.getRefreshTriggersReconnectAttempts();
        if (refreshTriggersReconnectAttempts != null) {
            refreshBuilder.refreshTriggersReconnectAttempts(refreshTriggersReconnectAttempts);
        }

        builder.topologyRefreshOptions(refreshBuilder.build());
    }


}
