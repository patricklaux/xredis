package com.igeeksky.xredis.lettuce;


import com.igeeksky.xredis.lettuce.config.LettuceGenericConfig;
import com.igeeksky.xredis.lettuce.config.LettuceSentinelConfig;
import com.igeeksky.xredis.lettuce.config.RedisNode;
import com.igeeksky.xtool.core.concurrent.VirtualThreadFactory;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.AbstractRedisClient;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.RedisURI;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Lettuce 辅助工具类
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public final class LettuceHelper {

    private static final ExecutorService EXECUTOR =
            Executors.newThreadPerTaskExecutor(new VirtualThreadFactory("stream-thread-"));

    /**
     * 私有构造器
     */
    private LettuceHelper() {
    }

    /**
     * 构建 RedisURI
     *
     * @param config 配置信息
     * @param node   节点信息
     * @return {@link RedisURI} – redisURI
     */
    public static RedisURI redisURI(LettuceGenericConfig config, RedisNode node) {
        String socket = node.getSocket();
        if (socket != null) {
            return process(RedisURI.Builder.socket(socket), config).build();
        }

        return process(RedisURI.builder(), config).withHost(node.getHost()).withPort(node.getPort()).build();
    }

    /**
     * 构建 哨兵模式 RedisURI
     *
     * @param config 配置信息
     * @return {@link RedisURI} – redisURI
     */
    public static RedisURI sentinelURIBuilder(LettuceSentinelConfig config) {
        RedisURI.Builder builder = process(RedisURI.builder(), config).withSentinelMasterId(config.getMasterId());

        List<RedisURI> sentinels = sentinels(config);
        for (RedisURI sentinel : sentinels) {
            builder.withSentinel(sentinel);
        }

        return builder.build();
    }

    /**
     * 构建 哨兵模式 节点信息
     *
     * @param config 哨兵模式配置信息
     * @return {@link List} – 哨兵模式节点信息
     */
    private static List<RedisURI> sentinels(LettuceSentinelConfig config) {
        List<RedisURI> sentinels = new ArrayList<>();
        List<RedisNode> nodes = config.getNodes();
        for (RedisNode node : nodes) {
            String host = node.getHost();
            int port = node.getPort();
            RedisURI sentinelURI = RedisURI.create(host, port);

            String sentinelUsername = StringUtils.trimToNull(config.getSentinelUsername());
            String sentinelPassword = StringUtils.trimToNull(config.getSentinelPassword());

            if (sentinelUsername != null || sentinelPassword != null) {
                RedisCredentials redisCredentials = RedisCredentials.just(sentinelUsername, sentinelPassword);
                sentinelURI.setCredentialsProvider(RedisCredentialsProvider.from(() -> redisCredentials));
            }

            sentinels.add(sentinelURI);
        }
        return sentinels;
    }

    /**
     * 构建 RedisURI
     *
     * @param builder 创建器
     * @param config  配置信息
     * @return {@link RedisURI.Builder} – RedisURI.Builder
     */
    private static RedisURI.Builder process(RedisURI.Builder builder, LettuceGenericConfig config) {
        builder.withDatabase(config.getDatabase())
                .withSsl(config.isSsl())
                .withStartTls(config.isStartTls())
                .withTimeout(Duration.ofMillis(config.getTimeout()))
                .withVerifyPeer(config.getSslVerifyMode());

        String clientName = config.getClientName();
        if (clientName != null) {
            builder.withClientName(clientName);
        }

        String username = config.getUsername();
        String password = config.getPassword();
        if (username != null && password != null) {
            builder.withAuthentication(username, password);
        } else if (username == null && password != null) {
            builder.withPassword(password.toCharArray());
        }

        return builder;
    }


    /**
     * 获取虚拟线程池
     *
     * @return {@linkplain ExecutorService} – 虚拟线程池
     */
    public static ExecutorService getVirtualThreadPerTaskExecutor() {
        return EXECUTOR;
    }

    /**
     * 关闭 Redis 客户端
     *
     * @param quietPeriod 客户端优雅关闭静默期，单位：毫秒
     * @param timeout     客户端关闭超时，单位：毫秒
     * @param executor    虚拟线程池
     * @param client      Redis 客户端
     */
    public static void shutdown(long quietPeriod, long timeout,
                                ExecutorService executor, AbstractRedisClient client) {
        try {
            try {
                if (quietPeriod > 0) {
                    boolean ignored = executor.awaitTermination(quietPeriod, TimeUnit.MILLISECONDS);
                }
                executor.shutdown();
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        } catch (Exception ignored) {
        }
        client.shutdown(quietPeriod, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 关闭 Redis 客户端
     *
     * @param quietPeriod 客户端优雅关闭静默期，单位：毫秒
     * @param timeout     客户端关闭超时，单位：毫秒
     * @param executor    虚拟线程池
     * @param client      Redis 客户端
     * @return {@code CompletableFuture<Void>}
     */
    public static CompletableFuture<Void> shutdownAsync(long quietPeriod, long timeout,
                                                        ExecutorService executor, AbstractRedisClient client) {
        return CompletableFuture.completedFuture(Boolean.TRUE)
                .thenApply(bool -> {
                    boolean terminated = false;
                    try {
                        try {
                            if (quietPeriod > 0) {
                                terminated = executor.awaitTermination(quietPeriod, TimeUnit.MILLISECONDS);
                            }
                            executor.shutdown();
                        } catch (InterruptedException e) {
                            executor.shutdownNow();
                        }
                    } catch (Exception ignored) {
                    }
                    return terminated;
                }).thenCompose(ignored -> client.shutdownAsync(quietPeriod, timeout, TimeUnit.MILLISECONDS));
    }

}