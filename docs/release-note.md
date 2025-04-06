## xRedis release note

### 1.0.9

- [X] `RedisOperatorProxy`：isCompatible

### 1.0.8

- [X] `RedisOperatorProxy`：支持 Redis-Compatible DB，当处于兼容模式时，不使用脚本执行数据操作

### 1.0.7

- [X] `RedisHelper.get(Future<T> future,…… )` when timeout, cancel task

### 1.0.6

- [X] fix bug: `ClusterClientOptions` NPE

### 1.0.5

- [X] `StreamPublisher` remove `AsyncCloseable`
- [X] upgrade Lettuce version to 6.5.5.RELEASE

### 1.0.4

- [X] add `@Role(BeanDefinition.ROLE_INFRASTRUCTURE)`

### 1.0.3

- [X] `RedisOperator`、`Pipeline` support graceful close

### 1.0.2

- [X] upgrade `xtool` to 1.2.1

### 1.0.1

- [X] `RedisOperatorProxy` extend `AsyncCloseable`

### 1.0.0

- [X] The first stable version.