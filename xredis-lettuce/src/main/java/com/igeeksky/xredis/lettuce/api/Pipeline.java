package com.igeeksky.xredis.lettuce.api;

import com.igeeksky.xredis.common.AsyncCloseable;

/**
 * Redis 管道接口
 * <p>
 * <b>注意：</b>
 * <p>
 * 1. 提交命令后，只有调用 {@code flushCommands()} 后才会真正将命令发送到 Redis 服务器。<p>
 * 2. {@link Pipeline} 是线程安全的，支持并发操作。<p>
 * 3. 如在 {@link Pipeline} 中提交阻塞命令，那么所有使用此 {@link Pipeline} 的线程的命令都可能会被阻塞。<p>
 * 4. 设有 “线程 1”、“线程 2” 同时使用同一个 pipeline，如 “线程 1” 调用了 {@code flushCommands()}，
 * 即使 “线程 2” 没有调用 {@code flushCommands()}，“线程 2” 当前已提交的命令也会被立即发送到 Redis 服务器。<br>
 * 因为 “线程 2” 不知晓 {@code flushCommands()} 是否已被其它线程调用，所以提交命令完毕后仍需调用 {@code flushCommands()}，
 * 确保命令被发送到 Redis 服务器。
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Patrick.Lau
 * @see <a href="https://redis.github.io/lettuce/advanced-usage/#pipelining-and-command-flushing">pipelining-and-command-flushing</a>
 * @since 1.0.0
 */
public interface Pipeline<K, V> extends RedisAsyncCommands<K, V>, AsyncCloseable {

    /**
     * 提交当前连接的所有命令
     */
    void flushCommands();

}
