# 数据库与依赖下载

## 选择数据库

```yaml
database:
  type: sqlite
  mysql:
    host: 127.0.0.1
    port: 3306
    database: fakeplayer
    username: root
    password: ''
    parameters: 'useSSL=false&characterEncoding=utf8'
    max-pool-size: 6
    connection-timeout: 5000
```

- 默认 SQLite，数据库文件是 `plugins/FakePlayer-CE/data.db`，不要改名。
- 使用 MySQL 时先创建空数据库，再把 `type` 改成 `mysql` 并填写连接信息；表会自动创建。
- 改完必须**重启服务器**，数据源只在启用时构造。

::: warning
SQLite 适合单服；多服共享数据才需要 MySQL。切换后可用 `/fp migrate-db`（仅 OP，异步）把 `data.db` 的四张表迁移到当前 MySQL 库，SQLite 文件会保留。
:::

## MySQL 驱动与 Undertow 不打包

以下依赖已改为按需下载，不在插件 jar 内：

- MySQL 驱动（仅 `database.type: mysql` 时）
- Undertow 及其传递依赖（仅 `http-admin.enabled: true` 时）

插件启动时会把它们下载到 `plugins/FakePlayer-CE/libs/`，使用 `.part` 临时文件与原子移动写入。下载坐标全部写死在代码里，并逐段校验路径，目标文件被限制在 `libs` 目录内，避免路径穿越。

默认仓库是 Maven Central。可以通过 JVM 参数改用镜像：

```
-DLeaf.library-download-repo=https://maven.aliyun.com/repository/public
```

只允许 `http(s)` 地址。下载失败时插件不会整体禁用：HTTP 接口会记录错误并跳过启动；MySQL 配置下数据库连接会失败。

参考：[Leaf 官方文档](https://www.leafmc.one/zh/docs/config/jvm-flags#-dleaflibrary-download-repo)。
