# 高级设置

## 命令钩子

占位符：

- `%p`：假人名称
- `%u`：假人 UUID
- `%c`：创建者名称

可配置的钩子：

```yaml
pre-spawn-commands: ['']
post-spawn-commands: ['']
after-spawn-commands: ['']
self-commands: ['']
post-quit-commands: ['']
after-quit-commands: ['']
```

- `pre-spawn-commands`：召唤前以控制台执行。
- `post-spawn-commands` / `after-spawn-commands`：召唤成功后的不同阶段。
- `self-commands`：假人自己执行的命令（以假人身份）。
- `post-quit-commands` / `after-quit-commands`：假人退出时执行；上下文在假人移出列表前捕获，避免拿到错误对象。

## 自动登录

两套机制互相独立：

1. **登录插件 API 免登录**：安装 AuthMe / CatSeedLogin / LibreLogin / LibreLoginNext 后自动生效，零配置。
2. **命令模板登录**：用 `/fp password` 存密码，或由随机密码生成器创建。

```yaml
auto-login:
  command:
    enabled: false
    register: '/reg %password% %password%'
    login: '/login %password%'
    change-password: '/changepassword %oldpassword% %newpassword% %newpassword%'
  random-password:
    charset: ''
    length: 12
```

`charset` 留空表示不自动生成密码，必须手动 `/fp password <名称> <密码>`。

## 兼容性

```yaml
prevent-kicking: ON_SPAWNING
forced-execution: false
```

- `prevent-kicking`：防止假人被其他插件误踢。`NEVER` / `ON_SPAWNING` / `ALWAYS`；遇到 PacketEvents 注入冲突可设为 `ALWAYS`。
- `forced-execution`：跳过兼容性探测，强制使用当前版本的 NMS 实现。

## 更新检查

```yaml
check-for-updates: true
update-check-interval-hours: 24
update-download: false
update-download-timeout-seconds: 60
update-ignore-ssl-errors: false
```

开启 `update-download` 后，更新会下载到 `plugins/update/`，由 Paper 系核心在重启时替换。`update-ignore-ssl-errors` 只影响更新下载的单个连接。

## HTTP 管理接口

```yaml
http-admin:
  enabled: false
  host: 0.0.0.0
  port: 3253
  token: ''
```

默认关闭。启用前请阅读 [HTTP 管理接口](/http/) 与 [安全说明](/contributing/issues)。
