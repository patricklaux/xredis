### 已完成任务

- [X] 【开发】 Lettuce 单机连接
- [X] 【开发】 Lettuce 连接池（无需配置连接池）
- [X] 【开发】 Redis 单机 clear 操作
- [X] 【开发】 Redis 集群 clear 操作
- [X] 【开发】 Lettuce 主从连接
- [X] 【开发】 Lettuce 哨兵连接
- [X] 【开发】 Lettuce 集群连接
- [X] 【测试】 Lettuce 单机基本测试
- [X] 【测试】 Lettuce 主从基本测试
- [X] 【测试】 Lettuce 集群基本测试
- [X] 【测试】 Lettuce 哨兵基本测试
- [X] 【测试】 Lettuce 配置测试
- [X] 【开发】 Lettuce unixSocket
- [X] 【开发】 Redis Lua 脚本执行失败重加载机制（RedisNoScriptException 异常处理）
- [X] 【开发】 RedisSyncOperator, RedisAsyncOperator, RedisReactiveOperator
- [X] 【开发】 MavenCentral：注册、试发布组件
- [X] 【开发】 StreamContainer（合并多个 Stream 为一个 xread 命令调用）
- [X] 【开发】 StreamGenericContainer
- [X] 【开发】 补充代码注释
- [X] 【开发】 RedisAsyncOperator, RedisReactiveOperator：evalRedisScript
- [X] 【文档】 application-all.yml 示例
- [X] 【开发】 AsyncCloseable（资源释放）：RedisOperator，Pipeline，StreamOperator
- [X] 【开发】 GracefulShutdown（优雅关闭）：ClientResources，RedisOperatorFactory，StreamContainer，StreamGenericContainer
- [X] 【测试】 RedisOperatorProxy 同步接口
- [X] 【文档】 README.md
- [X] 【文档】 Reference.md
- [X] 【开发】 RedisPooledOperator（放弃：真正需要连接池的只有事务命令，但可以采用 script、function替代，因此放弃）

### 待完成任务

- [ ] 【开发】 添加方法：LettuceHelper.createFactory()，方便在不使用 Spring 的情况下创建 RedisOperatorFactory