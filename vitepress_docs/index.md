# FakePlayer-CE 文档

FakePlayer-CE 是 `tanyaofei/minecraft-fakeplayer` 的社区维护分支：一个服务端假人插件，用单个 jar 覆盖 Minecraft 1.20.1 ~ 26.3，支持 Paper / Purpur / Leaf / Spigot 与 Folia。

这里不是 README 的目录副本，而是按主题整理的 Wiki。每个页面都包含原理、默认值、边界条件和排错线索。

## 能力地图

| 你想做什么 | 去哪里 |
|---|---|
| 安装插件、升级、第一次召唤假人 | [入门指南](/guide/) |
| 查清每个配置项的含义和默认值 | [配置完全手册](/guide/config/) |
| 查某个命令的参数、权限和坑 | [命令完全手册](/commands/) |
| 用脚本或面板远程管理 | [HTTP 管理接口](/http/) |
| 在计分板/聊天里显示假人信息 | [占位符变量](/placeholders) |
| 接登录插件、皮肤、OpenInv 等 | [联动插件](/compat/) |
| 查某版本改了什么、有无破坏性变更 | [版本变更史](/changelog/) |
| 提交 Issue、写文档、发 PR | [贡献指南](/contributing/) |

## 核心概念

### 假人

假人是通过 NMS 构造的服务端玩家实体，对服务器来说接近真实在线玩家。它没有真实客户端连接，但参与区块加载、模拟、事件与大多数玩家 API。

### 创建者

谁召唤的假人，谁就是创建者。普通玩家只能按名字操作自己创建的假人；OP 和控制台可以操作全服假人。假人背包上下文、跟随下线等逻辑都围绕创建者展开。

### 特性（Feature）

`collidable`、`invulnerable`、`autofish`、`replenish`、`simulation_distance` 等是假人的可配置项。它们有全局默认值，也可以由玩家通过 `/fp config set` 覆盖，某些特性还能通过 `/fp set` 立即作用于当前假人。详见 [配置完全手册](/guide/config/)。

### 动作（Action）

攻击、挖掘、交互、跳跃、说话、寻路、看向实体等都被统一抽象为动作。动作命令通常支持 `once` / `continuous` / `interval <ticks>` / `stop` 四种形态。

### 选中假人

用 `/fp select <name>` 设定默认操作对象。命令省略名字时优先使用选中假人；没选且只有一个假人时自动使用。

## 快速上手

```text
1. 安装 CommandAPI
2. 放入 fakeplayer-fp.buildX.jar
3. 首次启动后把 config.tmpl.yml 改名为 config.yml
4. 重启或 /fp reload
5. /fp spawn
6. /fp list
```

完整步骤与排错见 [入门指南](/guide/)。

## 版本与兼容

- 单 jar 支持 MC 1.20.1 ~ 26.3；
- 当前构建需要 Java 25；
- CommandAPI 是硬依赖，MC 26.3 需要 12.1.0+；
- MySQL 驱动与 Undertow 按配置自动下载，不打包进主 jar；
- `plugin.yml` 声明 `folia-supported: true`。

## 安全提醒

::: danger
HTTP 管理接口默认关闭，但启用后默认监听 `0.0.0.0:3253`，且所有接口默认开放。请改成内网地址、设置 token，并关闭不需要的接口。不要把假人设为 OP，也不要把管理端口直接暴露到公网。
:::

## 文档约定

- 配置键用反引号，例如 `player-limit`；
- 命令用反引号，例如 `/fp spawn`；
- 默认值以仓库内模板为准，模板与代码回退默认值不一致时会明确标注；
- 每个页面尽量写清「为什么这样设计」和「什么情况下会踩坑」。

发现文档与代码不一致，欢迎按 [贡献指南](/contributing/) 提交修正。
