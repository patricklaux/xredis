## Xredis

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://img.shields.io/github/v/release/patricklaux/xredis)](https://github.com/patricklaux/xredis/releases) [![Maven](https://img.shields.io/maven-central/v/com.igeeksky.xredis/xredis.svg)](https://central.sonatype.com/namespace/com.igeeksky.xredis) [![Last commit](https://img.shields.io/github/last-commit/patricklaux/xredis)](https://github.com/patricklaux/xredis/commits)

## 1. 简介

Xredis 是基于 Lettuce 实现的 Redis 客户端，用于简化 Redis 数据操作。

## 2. 特性

Xredis 是对 Lettuce 非常薄的一个封装，最大限度地保留了 Lettuce 的原 API。

1. 统一 standalone、sentinel 和 cluster 的 API，统一通过 RedisOperator 操作数据。
2. 提供了 RedisSyncOperator 、RedisAsyncOperator 和 RedisReactiveOperator  接口，可以根据业务场景灵活使用各种编程范式。
3. 提供了 Pipeline 接口，支持批提交命令。
4. 提供了 StreamContainer，简化 stream 的订阅发布。
5. 提供了 RedisOperatorProxy，简化大批量数据操作。

## 3. 运行环境

SpringBoot：3.3.0+

Lettuce：6.5.0.RELEASE+

JDK：21+

## 4. 开始使用

### 4.1. 第一步：引入依赖

```xml
<dependencies>
    <dependency>
        <groupId>com.igeeksky.xredis</groupId>
        <artifactId>xredis-spring-boot-autoconfigure</artifactId>
        <version>${xredis.version}</version>
    </dependency>
    <!-- ... other ... -->
</dependencies>
```

### 4.2. 第二步：编写配置

```yaml

```

### 4.3. 第三步：调用方法

```java

```

## 5. 项目构建

如希望尝试新特性，可以将项目克隆到本地进行编译（需要 JDK21）。

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

