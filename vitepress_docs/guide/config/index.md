# 配置完全手册

本页覆盖 `config.yml` 与 `ai-chat.yml` 的全部配置项、默认值、解析边界和相互关系。所有默认值以仓库内模板为准；少数「模板默认值」与「代码回退默认值」不一致的地方会单独标注。

## 1. 配置文件基础

### 1.1 模板与生效文件

首次启动生成 `config.tmpl.yml`，但它不生效。把它复制或改名为 `config.yml` 后才会被读取。升级时模板会更新，但 `config.yml` 不会被覆盖。

::: tip
用 `/fp update config` 可以把模板新增的键补进现有 `config.yml`，保留已有值和注释，并在改写前备份为 `config.yml.bak`。
:::

### 1.2 重载范围

`/fp reload` 依次重载：

- `FakeplayerConfig`；
- `AiChatConfig`；
- 延迟显示设置；
- TPS 自适应限制；
- 翻译器。

**不会**热重载：数据库类型、HTTP 端口 / host / token、Undertow 监听器、可选依赖。这些需要重启服务器。配置过时时 `/fp reload` 还会提示 `config.yml is out of date`。

### 1.3 配置版本

`config.yml` 顶部 `version` 当前为 **42**。每次修改配置模板都必须递增版本，并在 CHANGELOG 登记。`version` 只用于升级器判断，插件逻辑不直接依赖它。

## 2. 国际化 `i18n`

| 键 | 默认 | 说明 |
|---|---|---|
| `i18n.locale` | `zh` | 可用值：`en`、`zh`、`zh_cn`、`zh_tw`、`zh_hk`、`ja`。玩家可用 `/fp lang <code>` 覆盖自己的显示语言；`reset` 恢复默认。 |

`message/*.properties` 共五份：英文、简体中文、繁体中文（台湾/香港）和日语。非英文使用 `\uXXXX` ASCII 转义保存。新增文案必须五份同步。

## 3. 数量限制

### 3.1 基础上限

| 键 | 默认 | 解析 |
|---|---|---|
| `server-limit` | `1000` | `<=0` 表示不限（内部变成 `Integer.MAX_VALUE`） |
| `player-limit` | `1` | `<=0` 表示不限 |
| `detect-ip` | `true` | 同一 IP 共享名额；代码回退默认其实是 `false`，以模板为准 |

`detect-ip` 生效时，检查的是 TPS 修正后的 `effectiveLimit()`，不是原始的 `player-limit`。

### 3.2 按权限提高上限

```yaml
spawn-limit:
  vip: 10
  svip: 30
```

权限节点是 `fakeplayer.spawn.limit.<名称>`：

- 命中多个时取最大值；
- 没有命中时沿用 `player-limit`；
- OP 不受限制；
- 配置键只允许 `[A-Za-z0-9_-]+`，值必须为正；
- `fakeplayer.spawn.limit` 根权限在 `plugin.yml` 中默认 `false`。

`getMaxSpawnLimit()` 还会用于名称序号池初始化和 TPS 扣减范围。

## 4. 命名

| 键 | 默认 | 说明 |
|---|---|---|
| `name-template` | `''` | 自动生成名模板；`%c` 替换为创建者名。以 `-` 或 `@` 开头会被判非法并回退为空 |
| `name-prefix` | `''` | 前缀，自动生成名和自定义名都会加 |
| `name-suffix` | `''` | 后缀，总长不超过 16；自动生成名的序号插在后缀之前 |
| `name-pattern` | `'^[a-zA-Z0-9_]+$'` | 自定义名正则；编译失败回退默认值 |
| `name-style` | `gray, italic` | **标量字符串**，用逗号分隔；颜色取最后一个可识别颜色，装饰取全部可识别装饰 |

::: warning
`name-style` 必须是 `gray, italic` 这种标量，不能写成 YAML 列表。写成列表时 Bukkit `getString` 取不到值，会变成白色无装饰。
:::

## 5. 存活、存档与死亡

| 键 | 默认 | 说明 |
|---|---|---|
| `lifespan` | `0` | 分钟；`<=0` 表示永久 |
| `persistent-data` | `true` | 写入 `Player#setPersistent`；关闭时建议同时开启掉落背包 |
| `restore-on-start` | `false` | 启动时恢复上次快照（名称、创建者、位置、特性、动作）；代码未强制要求 `persistent-data` 同时开启，但建议一起开 |
| `follow-quiting` | `true` | 创建者下线时移除假人。跨服切换不触发 |
| `follow-quiting-force` | `false` | 立即清理，绕过「仅切换服务器不触发」判断 |
| `follow-quiting-force-delay` | `3` | 延迟秒数；延迟内重连则保留 |
| `drop-inventory-on-quiting` | `false` | 退出时掉落背包。代码回退默认其实是 `true`，以模板为准 |
| `kick-on-dead` | `true` | 死亡时移除假人；关闭后才能用 `/fp respawn` |
| `death-message` | `false` | 死亡播报，仅在 `kick-on-dead: true` 时生效 |
| `advancements` | `true` | 是否授予并写入进度存档 |
| `share-fakeplayer` | `false` | 开启后用基于名称的确定性 UUID；关闭时每次召唤 UUID 可能不同，usercache 会累积多条记录 |

## 6. 数据库

```yaml
database:
  type: sqlite
  mysql:
    host: 127.0.0.1
    port: 3306
    database: fakeplayer
    username: root
    password: ''
    parameters: useSSL=false&characterEncoding=utf8
    max-pool-size: 6
    connection-timeout: 5000
```

- `type` 只识别 `sqlite` 和 `mysql`（`mariadb` 是 mysql 别名）；未知值静默按 SQLite；
- SQLite 文件固定为 `plugins/FakePlayer-CE/data.db`，不要改名；连接池固定 1，超时固定 5000 ms；
- MySQL 需先创建空库，表由插件创建；最小连接池 1，最小超时 1000 ms；
- 改类型后必须重启，再执行 `/fp migrate-db` 迁移；
- `/fp reload` 不会重建连接池。

## 7. 兼容与底层

| 键 | 默认 | 说明 |
|---|---|---|
| `prevent-kicking` | `ON_SPAWNING` | `NEVER` 不处理；`ON_SPAWNING` 仅创建时防踢；`ALWAYS` 始终防踢 |
| `forced-execution` | `false` | 忽略 NMS 版本检查，用第一个可用实现兜底，可能导致崩溃 |
| `invsee-implement` | `AUTO` | `AUTO` 自动检测 OpenInv；`SIMPLE` 用内置实现，不能查看/编辑装备栏 |
| `allow-non-op-open-inv` | `false` | 允许非 OP 打开假人背包；创建者本人默认可打开自己假人的背包 |

旧键 `prevent-kicked-on-spawning: true` 会映射到 `ON_SPAWNING` 并警告。

## 8. 虚拟延迟（Tab 列表 Ping）

| 键 | 默认 | 说明 |
|---|---|---|
| `custom-ping` | `''` | 空/非数字/任一负数 → 不模拟；1 个值固定；2 个值排序成 `[min,max]` 随机；超过 2 个只取前两个 |
| `custom-ping-dynamic` | `false` | 在设定值上下 ±8 ms 波动 |
| `custom-ping-dynamic-interval` | `200` | tick；最小 20 |

## 9. 自动补货与工具替换

| 键 | 默认 | 说明 |
|---|---|---|
| `replenish.tools.durability-threshold` | `0` | 磨损百分比达到阈值时修复；0 关闭 |
| `tool-replacement.remaining-durability-threshold` | `10` | 非经验修补工具损坏后替换；经验修补工具耐久低于该值时替换 |

工具替换需要玩家通过 `/fp config set replace_tools true` 开启，并要求 `fakeplayer.config.replace-tools` 权限。

## 10. 命令钩子

六个钩子都是字符串列表，按 `getStringList` 读取，默认 `['', '']`：

| 键 | 触发时机 |
|---|---|
| `pre-spawn-commands` | 生成前 |
| `post-spawn-commands` | 预登录事件后、派发加入事件前 |
| `after-spawn-commands` | 假人完全加入服务器后 |
| `self-commands` | 由假人自己执行 |
| `post-quit-commands` | 退出流程中 |
| `after-quit-commands` | 退出完成后 |

占位符：

- `%p` 假人名；
- `%u` UUID；
- `%c` 创建者名。

旧键 `preparing-commands` 会追加到 `post-spawn-commands`，`destroy-commands` 会追加到 `post-quit-commands`，并记录弃用警告。

## 11. TPS 自适应保护

```yaml
tps-adaptive:
  enabled: false
  interval: 60
  threshold: 17.0
  min-count: 1
```

- 每个 `interval` 秒检查一次 1 分钟 TPS；
- 低于 `threshold` 时，所有玩家的上限各减 1；恢复后每周期加 1；
- `min-count` 是下限；
- 旧键 `kale-tps` 若非 0，会自动转成「启用 + 阈值」。

上限按玩家实际额度计算：命中 `spawn-limit` 的玩家先拿到更高额度，再扣减 TPS。超额假人会被移除。

## 12. 默认特性 `default-features`

这些值决定新玩家的个人配置初始值：

| 键 | 默认 |
|---|---|
| `collidable` | `true` |
| `pickup_items` | `true` |
| `skin` | `true` |
| `look_at_entity` | `false` |
| `invulnerable` | `true` |
| `replenish` | `false` |
| `replace_tools` | `false` |
| `autofish` | `false` |
| `wolverine` | `false` |
| `aichat_fake_inventory` | `false` |
| `aichat_questioner_inventory` | `false` |
| `simulation_distance` | `-1` |

生效优先级：

1. 玩家没有对应权限 → 强制使用默认值；
2. 玩家个人数据库配置；
3. `default-features` 配置；
4. `Feature` 枚举内置默认值。

## 13. 自动登录

两套机制互相独立：

1. **登录插件 API 免登录**：安装 CatSeedLogin / AuthMe / LibreLogin / LibreLoginNext 后自动生效，零配置；
2. **命令模板登录**：用 `auto-login.command` 定义注册/登录/改密命令，密码存插件数据库。

```yaml
auto-login:
  command:
    enabled: false
    register: /reg %password% %password%
    login: /login %password%
    change-password: /changepassword %oldpassword% %newpassword% %newpassword%
  random-password:
    charset: ''
    length: 12
```

- `charset` 留空表示不自动生成密码，必须先用 `/fp password` 手动设置；
- `length <= 0` 回退 12；
- 旧版平铺键 `auto-login`、`register`、`login`、`random-password`、`random-password-length`、`change-password` 仍兼容。

## 14. 更新检查

| 键 | 默认 | 说明 |
|---|---|---|
| `check-for-updates` | `true` | 只检查，不自动下载 |
| `update-check-interval-hours` | `24` | 0 表示只在开服检查一次 |
| `update-download` | `false` | 下载到 `plugins/update/` |
| `update-download-timeout-seconds` | `60` | 最小 5 秒 |
| `update-ignore-ssl-errors` | `false` | 跳过证书校验，有风险 |

## 15. HTTP 管理接口

```yaml
http-admin:
  enabled: false
  host: 0.0.0.0
  port: 3253
  token: ''
  allow-get: true
  allowed-hosts: []
  cors:
    enabled: false
    allowed-origins: []
    max-age: 600
  rate-limit:
    requests-per-minute: 120
    auth-failures: 10
    lockout-seconds: 60
```

- 整个功能默认关闭；**启用后所有 `interface.*` 开关默认 true**；
- `host` 默认 `0.0.0.0`，但 `enabled: false` 时不会监听；强烈建议改为 `127.0.0.1` 或内网地址；
- `token` 为空会在启动时生成 32 位随机串并打印到控制台；
- `allow-get: false` 时只允许 POST，但参数仍然只从 URL query 读取；
- 改 host / port / token 必须重启；
- `interface.*` 共 22 个开关，全部默认 `true`，见 [HTTP 管理接口](/http/)；
- `batch` 一个开关同时控制 `/kickall`、`/killall`、`/sayall`。

## 16. `ai-chat.yml` 完整配置

文件不存在时自动生成。由 `/fp reload` 重载。

### 16.1 基础与模型

| 键 | 默认 | 说明 |
|---|---|---|
| `enabled` | `false` | 总开关；校验失败会被强制关回 false |
| `api` | `https://api.openai.com/v1` | 不含 `/chat/completions`；尾部 `/` 会被去掉 |
| `api-key` | `''` | 回退密钥 |
| `api-key-env` | `FAKEPLAYER_AI_KEY` | 环境变量优先 |
| `model` | `gpt-4o` | 顶层默认模型，同时作为 `models.*` 的默认值 |
| `active-model` | `default` | 当前模型名；OP 用 `/fp aichat model <name>` 切换并写回文件 |
| `models` | `default: {model: gpt-4o}` | 每项可覆盖 `model`/`api`/`api-key`/`api-key-env` |

多模型示例：

```yaml
models:
  default:
    model: gpt-4o
  ollama:
    model: qwen2.5
    api: http://127.0.0.1:11434/v1
    api-key: ollama
```

`active-model` 不存在时回退 `default`，再回退第一个模型。请求 URL 是 `api + /chat/completions`，使用 `Authorization: Bearer <key>`；429 / 5xx 最多重试 2 次。

### 16.2 背包上下文

```yaml
inventory-context:
  fakeplayer: false
  questioner: false
```

这是**全局总开关**，还需要玩家个人开关同时开启：

| 上下文 | 全局键 | 个人开关 |
|---|---|---|
| 假人背包 | `inventory-context.fakeplayer` | `aichat_fake_inventory`（按假人创建者判断） |
| 提问者背包 | `inventory-context.questioner` | `aichat_questioner_inventory`（按提问玩家判断） |

控制台创建的假人没有个人配置，只看全局开关。背包摘要只统计物品类型与数量，作为 system 消息进入提示词。

### 16.3 采样参数

| 键 | 默认 | 说明 |
|---|---|---|
| `temperature` | `1.0` | 直接进请求体 |
| `top-p` | `1.0` | 请求字段 `top_p` |
| `presence-penalty` | `0.0` | — |
| `frequency-penalty` | `0.0` | — |
| `max-tokens` | `0` | 0 表示不发送 `max_tokens` |
| `max-context` | `20` | 历史轮数；队列上限为 `值*2` 条消息 |
| `recent-chat-lines` | `0` | 最近公共聊天行数，最多 50；作为 system 消息加入 |

### 16.4 自动接话

| 键 | 默认 | 说明 |
|---|---|---|
| `auto-reply.enabled` | `false` | 总开关 |
| `auto-reply.chance` | `0.15` | 命中概率，钳制 `[0,1]` |
| `auto-reply.cooldown-seconds` | `90` | 全局冷却，不是按假人 |
| `auto-reply.min-length` | `4` | 最短消息长度，最小 1 |
| `auto-reply.max-length` | `120` | 最长消息长度，钳制为不小于 `min-length` |

过滤链：忽略假人自己的发言 → 记录公共聊天 → 检查开关 → 长度检查 → 字母数字码点 `×2 >= 总长度`（过滤纯符号）→ 不同字符 `×3 >= 总长度`（过滤刷屏）→ 冷却与概率 → 随机选一个空闲假人生成回复。

### 16.5 主动发言

| 键 | 默认 | 说明 |
|---|---|---|
| `allow-continuous` | `false` | 是否允许定时主动发言，会持续消耗 token |
| `timeout-seconds` | `30` | 单次请求超时，最小 1 |
| `interval-ticks` | `200` | 默认间隔，最小 20；命令可覆盖 |

### 16.6 提示词与格式

| 键 | 默认 | 说明 |
|---|---|---|
| `system-prompt` | 模板中的两行中文提示词 | `{name}` 替换为假人名 |
| `turn-prompt` | `说一句话。` | 每轮追加的 user 消息 |
| `private-format` | `<gray>[{name} -> 你] {message}</gray>` | MiniMessage；模型输出先转组件再注入，避免执行模型写的标签 |

## 17. Feature 枚举与个人开关

`/fp config set <feature> <value> [player]` 修改玩家个人配置；`/fp set <feature> <value> [name]` 直接改假人当前值。OP / 控制台可以指定其他玩家，普通玩家只能改自己。

| Feature | 选项 | 默认 | 额外权限 |
|---|---|---|---|
| `collidable` | true/false | `true` | — |
| `invulnerable` | true/false | `true` | — |
| `wolverine` | true/false | `false` | — |
| `look_at_entity` | true/false | `false` | `fakeplayer.command.look` |
| `pickup_items` | true/false | `true` | — |
| `skin` | true/false | `true` | — |
| `replenish` | true/false | `false` | `fakeplayer.config.replenish` |
| `replace_tools` | true/false | `false` | `fakeplayer.config.replace-tools` |
| `autofish` | true/false | `false` | `fakeplayer.config.autofish` |
| `aichat_fake_inventory` | true/false | `false` | — |
| `aichat_questioner_inventory` | true/false | `false` | — |
| `simulation_distance` | 任意整数 | `-1` | — |

`skin`、`aichat_fake_inventory`、`aichat_questioner_inventory` 没有实时 modifier，不能作为 `/fp set` 的目标；它们只在 `/fp config set` 与个人配置里使用。`simulation_distance` 为 `-1` 时沿用世界设置。

## 18. 配置排错清单

| 现象 | 先检查 |
|---|---|
| 改了配置没生效 | 是否只改了 `config.tmpl.yml`；是否需要 `/fp reload` 或重启 |
| 生成上限不对 | `player-limit`、`spawn-limit`、权限、`detect-ip`、TPS 自适应 |
| 假人一上线就被踢 | `prevent-kicking`，登录插件的登录时机 |
| 假人死亡后消失 | `kick-on-dead` 是否为 true |
| AI 不回复 | `ai-chat.yml` 的 `enabled`、模型/密钥、`auto-reply.enabled`、消息是否被过滤 |
| HTTP 403 | 接口开关 `interface.*`、Host/Origin 白名单、CORS |
| HTTP 401 | token 是否放在 `Authorization: Bearer` 头中 |
| 名称样式没生效 | `name-style` 是否被写成 YAML 列表 |
| 数据库切换后没数据 | 是否重启并执行 `/fp migrate-db` |
