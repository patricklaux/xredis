package com.igeeksky.xredis.lettuce.autoconfigure;

import com.igeeksky.xtool.core.lang.Assert;
import io.lettuce.core.resource.ClientResources;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ClientResources 持有者（支持优雅关闭）
 *
 * @author Patrick.Lau
 * @since 1.0.0
 */
public class ClientResourcesHolder {

    private static final Logger log = LoggerFactory.getLogger(ClientResourcesHolder.class);

    private final long timeout;
    private final long quietPeriod;
    private final TimeUnit timeUnit;
    private final ClientResources clientResources;

    /**
     * 采用默认参数创建 ClientResourcesHolder
     * <p>
     * 默认：超时时间为 2000ms， 静默时间为 200ms
     *
     * @param clientResources ClientResources
     */
    public ClientResourcesHolder(ClientResources clientResources) {
        this(clientResources, 2000, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * 根据传入参数创建 ClientResourcesHolder
     *
     * @param clientResources ClientResources
     * @param timeout         超时时间
     * @param quietPeriod     静默时间
     * @param timeUnit        时间单位
     */
    public ClientResourcesHolder(ClientResources clientResources, long timeout, long quietPeriod, TimeUnit timeUnit) {
        Assert.isTrue(quietPeriod < timeout, "quietPeriod must be less than timeout.");
        Assert.isTrue(quietPeriod >= 0, "quietPeriod must be greater or equal to 0.");
        this.timeout = timeout;
        this.quietPeriod = quietPeriod;
        this.timeUnit = timeUnit;
        this.clientResources = clientResources;
    }

    /**
     * 获取 ClientResources
     *
     * @return {@link ClientResources}
     */
    public ClientResources get() {
        return clientResources;
    }

    /**
     * 根据预设参数，优雅关闭 ClientResources
     */
    public void shutdown() {
        this.shutdown(quietPeriod, timeout, timeUnit);
    }

    /**
     * 根据传入参数，优雅关闭 ClientResources
     *
     * @param quietPeriod 静默时间
     * @param timeout     超时世界
     * @param timeUnit    时间单位
     */
    public void shutdown(long quietPeriod, long timeout, TimeUnit timeUnit) {
        try {
            Boolean success = clientResources.shutdown(quietPeriod, timeout, timeUnit).get(timeout, timeUnit);
            if (success != null && success) {
                log.info("ClientResources shutdown success.");
            } else {
                log.warn("ClientResources shutdown fail.");
            }
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 根据预设参数，异步优雅关闭 ClientResources
     *
     * @return {@code Future<Boolean>} 是否无异常关闭成功
     */
    public Future<Boolean> shutdownAsync() {
        return this.shutdownAsync(quietPeriod, timeout, timeUnit);
    }

    /**
     * 根据传入参数，异步优雅关闭 ClientResources
     *
     * @param timeout     超时时间
     * @param quietPeriod 静默时间
     * @param timeUnit    时间单位
     * @return {@code Future<Boolean>} 是否无异常关闭成功
     */
    public Future<Boolean> shutdownAsync(long quietPeriod, long timeout, TimeUnit timeUnit) {
        return clientResources.shutdown(quietPeriod, timeout, timeUnit);
    }

}
