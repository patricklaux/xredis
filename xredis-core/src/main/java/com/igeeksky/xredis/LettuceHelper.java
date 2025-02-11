package com.igeeksky.xredis;


import com.igeeksky.xredis.config.LettuceGenericConfig;
import com.igeeksky.xredis.config.LettuceSentinelConfig;
import com.igeeksky.xredis.config.RedisNode;
import com.igeeksky.xtool.core.lang.StringUtils;
import io.lettuce.core.RedisCredentials;
import io.lettuce.core.RedisCredentialsProvider;
import io.lettuce.core.RedisURI;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Lettuce 辅助工具类
 *
 * @author Patrick.Lau
 * @since 0.0.4 2023-10-03
 */
public final class LettuceHelper {

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

}