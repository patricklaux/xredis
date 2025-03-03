## Xredis

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/v/release/patricklaux/xredis)](https://github.com/patricklaux/xredis/releases) [![Maven](https://img.shields.io/maven-central/v/com.igeeksky.xredis/xredis.svg)](https://central.sonatype.com/namespace/com.igeeksky.xredis) [![Last commit](https://img.shields.io/github/last-commit/patricklaux/xredis)](https://github.com/patricklaux/xredis/commits)

## 1. 简介

Xredis 是基于 `Lettuce` 实现的 Redis 客户端，用于简化 `Redis` 数据操作。

## 2. 特性

Xredis 是对 `Lettuce` 的一个非常非常薄的封装。

1. 统一 `standalone`、`sentinel` 和 `cluster` 的 API，统一通过 `RedisOperator` 操作数据。
2. 提供 `RedisSyncOperator` 、`RedisAsyncOperator` 和 `RedisReactiveOperator` 接口，可以灵活使用不同编程范式。
3. 提供 `Pipeline` 接口，支持批提交命令。
4. 提供 `StreamContainer` 和 `StreamPublisher` ，简化 `Redis-Stream` 的订阅发布。
5. 提供 `RedisOperatorProxy`，简化批数据操作，提高批数据操作性能。
6. 提供 `SpringBoot` 自动配置，可以通过配置文件直接配置 `Lettuce` 的绝大部分配置项（有些特殊配置项需编程实现）。

总之，项目初衷是希望保留性能强大且功能灵活的 `Lettuce` 原生 API，在此基础上再去扩展一些实用的常用的功能。同时，能够支持 ``SpringBoot `` 的自动配置，做到开箱即用。

## 3. 运行环境

|                | 版本           | 关键理由                                                     |
| -------------- | -------------- | ------------------------------------------------------------ |
| **JDK**        | 21+            | 虚拟线程                                                     |
| **Lettuce**    | 6.5.4.RELEASE+ | 支持 `Redis-JSON` 操作，支持 `Redis-Hash` 设置字段的过期时间 |
| **SpringBoot** | 3.3.0+         | 虚拟线程                                                     |



## 4. 开始使用

**示例项目**：[xredis-samples](https://github.com/patricklaux/xredis-samples)

### 4.1. 第一步：引入依赖

```xml
<dependencies>
    <!-- ... xredis 依赖 ... -->
    <dependency>
        <groupId>com.igeeksky.xredis</groupId>
        <artifactId>xredis-lettuce-spring-boot-autoconfigure</artifactId>
        <version>${xredis.version}</version>
    </dependency>

    <!-- ... 其它：假定使用 SpringWeb ... -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>${spring.boot.version}</version>
    </dependency>
</dependencies>
```

### 4.2. 第二步：编写配置

```yaml
xredis:
  lettuce: # Lettuce 客户端配置
    standalone: # 单机模式 或 副本集模式
      node: 127.0.0.1:6379 # Redis 节点
```

以上，就是 xredis 的最简配置，其余配置项都采用默认配置。

当然，如果你希望更精细地控制 Lettuce 客户端，或者想看看完整的配置项，那么，请查看示例项目的 ``application-all.yml`` 文件。

### 4.3. 第三步：调用方法

这里将 RedisServer 作为用户信息数据库来使用，仅演示了 async 异步操作。

```java
@Service
public class UserService {

    private final RedisOperator<String, String> redisOperator;
    private final JacksonCodec<User> codec = new JacksonCodec<>(User.class);

    /**
     * 使用 Spring 注入的 RedisOperator，创建 UserService
     *
     * @param redisOperator RedisOperator
     */
    public UserService(RedisOperator<String, String> redisOperator) {
        this.redisOperator = redisOperator;
    }

    /**
     * 添加用户信息
     *
     * @param user 用户信息
     * @return 添加结果
     */
    public CompletableFuture<Response<Void>> addUser(User user) {
        return redisOperator.async().set(user.getId() + "", codec.encode(user))
                .toCompletableFuture()
                .thenApply(result -> {
                    if (Objects.equals("OK", result)) {
                        return Response.ok();
                    }
                    return Response.error("Failed to add user.");
                });
    }

    /**
     * 获取用户信息
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    public CompletableFuture<Response<User>> getUser(Long id) {
        return redisOperator.async().get(id + "")
                .toCompletableFuture()
                .thenApply(s -> {
                    if (s == null) {
                        return Response.error("User not found.");
                    }
                    return Response.ok(codec.decode(s));
                });
    }

    /**
     * 删除用户信息
     *
     * @param id 用户 ID
     * @return 删除结果
     */
    public CompletableFuture<Response<Void>> deleteUser(Long id) {
        return redisOperator.async().del(id + "")
                .toCompletableFuture()
                .thenApply(result -> {
                    if (Objects.equals(1L, result)) {
                        return Response.ok();
                    }
                    return Response.error("User doesn't exist.");
                });
    }

}
```

## 5. 项目构建

如希望尝试新特性，或者修改源码，可以将项目克隆到本地进行编译。

```bash
# 1. git clone项目到本地
git clone https://github.com/patricklaux/xredis.git

# 2. 进入项目目录
cd xredis

# 3. 执行 maven 命令编译
mvn clean install
```

## 6. 项目参与

### 6.1. 分支开发

| 分支       | 说明           |
|----------|--------------|
| **main** | 主分支，用于版本发布   |
| **dev**  | 开发分支，用于接受 PR |

如您希望参与开发，请 fork 项目到您的仓库，修改 dev 分支并提交 pr。

### 6.2. 寻求帮助

https://github.com/patricklaux/xredis/discussions

如您希望了解如何使用 xcache，或在使用中遇到问题无法解决，欢迎在此提问。

### 6.3. 建议反馈

https://github.com/patricklaux/xredis/issues

如您发现功能缺陷，或有任何开发建议，欢迎在此提交。

如您发现安全漏洞，请私信与我联系。

## 7. 许可证

Xredis 采用 Apache License Version 2.0 进行许可。有关详细信息，请参阅 [LICENSE](LICENSE) 文件。

