# FakePlayer CE（社区版）

<div align="center">

<img src=".github/assets/logo.png" alt="FakePlayer CE" width="160">

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Hangar](https://img.shields.io/badge/Hangar-FakePlayer--CE-1f7ae0)](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

[English](README.md) | 简体中文 | [繁體中文](README_zh_TW.md) | [日本語](README_ja.md)

---

> **FakePlayer CE** 是 [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer) 的社区维护分支：受 [Carpet-Mod](https://github.com/gnembon/fabric-carpet) 启发，在服务端生成对服务器完全透明的假人玩家，并用 Gradle 多模块架构做到 **Minecraft `1.20.1` ~ `26.3` 单一 Jar 兼容**。

⭐ 如果你喜欢 FakePlayer CE，给它点个 Star 吧！这对项目的成长非常有帮助。

## ⚠️ 社区版声明

- 本仓库是**独立的社区维护分支**，不由原作者发布、维护或背书。
- 所有 Bug 与功能需求**请只提交到本仓库**，不要提交给上游。

## 快速上手

1. 安装 [CommandAPI](https://commandapi.jorel.dev) 到 `plugins/`
2. 把 `fakeplayer-fp.buildX.jar` 放进 `plugins/`
3. 启动服务器一次，把 `config.tmpl.yml` 重命名为 `config.yml`
4. 执行 `/fp spawn`

## 功能特性

- 生成对服务器完全透明的假人玩家，适用于区块常驻加载
- 支持原版与插件指令管控（传送、封禁、背包编辑等）
- 完整操控假人行为：移动、跳跃、攻击、挖矿等，支持周期性自动化
- 每位玩家拥有独立的个性化默认配置模板
- **单 Jar 多版本**：一个 Jar 覆盖 `1.20.1 ~ 26.3`，NMS 代码按版本隔离
- **HTTP 管理接口**：远程查询状态、生成 / 移除假人，控制动作、朝向、背包与命令
- **登录插件兼容**：AuthMe / CatSeedLogin / LibreLogin 免配置免登录

## 运行前置依赖

- [Paper](https://papermc.io) 或 [Purpur](http://purpurmc.org) 服务端
- [CommandAPI](https://commandapi.jorel.dev) 前置插件（**请勿使用 `10.0.0`**；**MC 26.3 需 `12.1.0` 及以上**）

## 下载

发布物为单一 `fakeplayer-fp.buildX.jar`，放入服务端 `plugins/` 目录并重启即可（需先装 CommandAPI）。

- **稳定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推荐）
- **Hangar**：[hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)，可在网页端直接下载与更新
- **最新构建**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 任一次成功的运行 → Artifacts → `fakeplayer-dist`

## 配置文件说明

首次加载只生成模板 `config.tmpl.yml`，需手动改名为 `config.yml` 后生效。该机制便于升级时对比新增配置项，不会覆盖已有设置。示例见 [config.yml](fakeplayer-core/src/main/resources/config.yml)。

## AI 聊天

`/fp aichat` 让假人通过 OpenAI 兼容接口生成聊天内容。首次使用需在 `plugins/FakePlayer-CE/ai-chat.yml` 里填写 `api` 与 `api-key`（或设置 `api-key-env` 指定的环境变量），把 `enabled` 改为 `true`，再执行 `/fp reload`。

| 指令 | 说明 |
|---|---|
| `/fp aichat public [once\|continuous\|interval <tick>] [假人]` | 发到公共聊天，`continuous` 持续发言直到 `stop` |
| `/fp aichat private <玩家> [once\|continuous\|interval <tick>] [假人]` | 只发给指定玩家 |
| `/fp aichat stop [假人]` | 停止持续发言 |

推荐开启 `auto-reply`：只在玩家说了值得回应的话时才接一句（短消息、纯符号与刷屏会先被过滤），由 `chance` 与 `cooldown-seconds` 控制频率。`continuous` 为定时发言，默认关闭，需要打开 `allow-continuous`。人设、提示词、上下文与节奏分别由 `system-prompt` / `turn-prompt` / `max-context` / `recent-chat-lines` / `interval-ticks` 控制。

## 指令列表

| 指令 | 功能说明 | 权限节点 | 备注 |
|------|---------|---------|------|
| /fp spawn | 创建假人 | fakeplayer.command.spawn | |
| /fp spawnuuid | 用指定 UUID 创建假人 | fakeplayer.command.spawn.uuid | 参数 `<名称> <UUID> [世界] [坐标]`；名称已绑定其他 UUID 时拒绝 |
| /fp kick | 踢出假人（从服务器移除） | fakeplayer.command.kick | |
| /fp kickall | 踢出服务器所有假人 | OP | |
| /fp kill | 杀死假人（真实死亡，可能掉落物品） | fakeplayer.command.kill | |
| /fp killall | 杀死服务器所有假人 | OP | |
| /fp select | 设置默认操作假人 | fakeplayer.command.select | 创建多个假人后可用 |
| /fp selection | 查看当前选中假人 | fakeplayer.command.selection | |
| /fp list | 列出所有在线假人 | fakeplayer.command.list | |
| /fp distance | 查看与假人间距 | fakeplayer.command.distance | |
| /fp drop | 假人丢弃手中单个物品 | fakeplayer.command.drop | |
| /fp dropstack | 假人丢弃手中整组物品 | fakeplayer.command.dropstack | |
| /fp dropinv | 假人清空全部背包物品 | fakeplayer.command.dropinv | |
| /fp skin | 复制其他玩家皮肤 | fakeplayer.command.skin | 装有 SkinsRestorer 时走其 API；离线名 60 秒冷却 |
| /fp invsee | 打开假人背包界面 | fakeplayer.command.invsee | 右键假人可触发 |
| /fp enderchest | 打开假人末影箱 | fakeplayer.command.enderchest | 别名 `/fp ec`；潜行右键；需同世界 |
| /fp sleep | 假人进入睡觉状态 | fakeplayer.command.sleep | |
| /fp wakeup | 唤醒睡觉假人 | fakeplayer.command.wakeup | |
| /fp status | 查看假人当前状态 | fakeplayer.command.status | |
| /fp respawn | 复活已死亡假人 | fakeplayer.command.respawn | 需 `kick-on-dead: false` |
| /fp tp | 传送到假人位置 | fakeplayer.command.tp | |
| /fp tphere | 将假人传送至自身位置 | fakeplayer.command.tphere | |
| /fp tps | 与假人互换位置 | fakeplayer.command.tps | |
| /fp set | 修改单个假人独立配置 | fakeplayer.command.set | |
| /fp setping | 设置假人显示的延迟 | fakeplayer.command.setping | 0 表示使用真实值 |
| /fp config | 修改自身创建假人默认配置 | fakeplayer.command.config | 控制台可用；`[玩家]` 指定他人 |
| /fp expme | 提取假人经验至自身 | fakeplayer.command.expme | |
| /fp attack | 假人发起攻击 | fakeplayer.command.attack | |
| /fp mine | 假人挖掘方块 | fakeplayer.command.mine | |
| /fp use | 假人交互 / 放置方块 / 使用物品 | fakeplayer.command.use | |
| /fp jump | 假人跳跃 | fakeplayer.command.jump | |
| /fp stop | 终止假人所有动作 | fakeplayer.command.stop | |
| /fp turn | 假人原地转向 | fakeplayer.command.turn | |
| /fp look | 假人看向指定坐标 | fakeplayer.command.look | |
| /fp move | 假人定向移动 | fakeplayer.command.move | |
| /fp goto | 让假人寻路前往指定坐标 | fakeplayer.command.goto | 自动寻路；无法抵达则拒绝 |
| /fp ride | 假人骑乘实体 | fakeplayer.command.ride | |
| /fp sneak | 假人进入潜行模式 | fakeplayer.command.sneak | |
| /fp sprint | 假人疾跑 | fakeplayer.command.sprint | |
| /fp swap | 切换主手副手物品 | fakeplayer.command.swap | |
| /fp hold | 切换快捷栏指定格子物品 | fakeplayer.command.hold | |
| /fp cmd | 让假人执行控制台指令 | fakeplayer.command.cmd | 高危 |
| /fp say | 让假人发送聊天消息 | fakeplayer.command.say | 兼容格式化插件 |
| /fp aichat | 让假人用 AI 生成聊天内容 | fakeplayer.command.aichat | 需配置 `ai-chat.yml` |
| /fp password | 设置假人登录密码（存入插件数据库） | fakeplayer.command.password | 配合 `auto-login.command` 使用 |
| /fp changepassword | 修改假人登录密码（旧密码可省略） | fakeplayer.command.changepassword | |
| /fp reload | 重载插件配置文件 | OP | |

## HTTP 管理接口

可选的轻量 HTTP 接口（默认关闭，无额外依赖），用于 Web 面板与自动化脚本。

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1        # 建议只监听本机或内网
  port: 3253
  token: ""              # 留空则启动时生成随机令牌并打印到控制台
  allow-get: true        # false 时只接受 POST
  allowed-hosts: []      # Host / Origin 白名单，留空不校验
  rate-limit:
    requests-per-minute: 120   # 每 IP 每分钟请求上限
    auth-failures: 10          # 连续鉴权失败锁定阈值（0 = 不锁定）
    lockout-seconds: 60        # 锁定时长
  interface:
    list: true           # 每个接口都有同名开关，关闭后返回 403
```

- 令牌只走 `Authorization: Bearer <token>` 请求头，**不支持 `?token=`**。
- 参数一律走 query，`GET` / `POST` 均可。

| 接口 | 说明 |
|---|---|
| `GET /list` | 列出所有在线假人 |
| `GET /status[?name=<名字>]` | 假人详细状态（坐标、朝向、血量、饥饿、经验、模式、创建者、手持物品、进行中的动作）；省略 `name` 返回全部 |
| `GET /info` | 插件与服务端信息（版本、MC 版本、服务端、假人数量、在线玩家数、数量上限） |
| `GET /spawn?name=<名字>` | 在主世界出生点生成假人 |
| `GET /kick?name=<名字>` | 踢出（移除）假人 |
| `GET /kill?name=<名字>` | 杀死假人（真实死亡，可能掉落） |
| `GET /respawn?name=<名字>` | 复活已死亡假人 |
| `GET /say?name=<名字>&message=<内容>` | 以假人身份发言（内容需 URL 编码） |
| `GET /aichat?name=<名字>[&mode=public\|private][&target=<玩家>][&continuous=true][&interval=<tick>]` | 让假人用 AI 发言，需启用 `ai-chat.yml`；`continuous=true` 持续到 `/aichat/stop` |
| `GET /aichat/stop?name=<名字>` | 停止该假人的 AI 持续发言 |
| `GET /action?name=<名字>&action=<动作>` | 触发动作，参数见下表 |
| `GET /stop?name=<名字>` | 停止假人当前所有动作 |
| `GET /teleport?name=<名字>[&world=&x=&y=&z=&yaw=&pitch=]` | 传送假人，未给出的参数保持当前值 |
| `GET /look?name=<名字>&direction=<方向>` | 转向 `north`/`south`/`east`/`west`/`up`/`down` |
| `GET /look?name=<名字>&at=<x,y,z>` | 看向指定坐标 |
| `GET /look?name=<名字>&yaw=<角度>&pitch=<角度>` | 直接设置朝向 |
| `GET /hold?name=<名字>&slot=<1-9>` | 切换主手槽位 |
| `GET /swap?name=<名字>` | 交换主副手物品 |
| `GET /goto?name=<名字>&x=&y=&z=[&world=]` | 寻路前往坐标（无法抵达则拒绝） |
| `GET /setping?name=<名字>&ping=<毫秒>` | 设置 Tab 列表显示的延迟 |
| `GET /sneak?name=<名字>[&state=true\|false]` | 切换（或设置）潜行 |
| `GET /sprint?name=<名字>[&state=true\|false]` | 切换（或设置）疾跑 |
| `GET /reload` | 重载 `config.yml` 与语言文件 |
| `GET /cmd?name=<名字>&command=<命令>` | 以假人身份执行命令 |
| `GET /kickall` / `/killall` / `/sayall?message=` | 批量移除 / 杀死 / 发言 |

`/action` 参数：

| 参数 | 默认 | 说明 |
|---|---|---|
| `action` | 必填 | 动作名，不区分大小写，`-` 与 `_` 等价：`ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | 执行次数，`-1` 表示持续 |
| `interval` | `0` | 间隔 tick；只给 `interval` 不给 `count` 视为持续 |
| `wait` | `0` | 首次执行前等待的 tick |
| `message` | — | 仅 `SAY` 需要 |

成功返回 `{"status":"success"}`（批量接口附带 `count`），失败返回 `{"status":"failure","msg":"..."}` 说明原因。HTTP 层错误：`401` 令牌错误、`403` 接口已关闭或 Host 不被允许、`404` 路径不存在、`405` 方法不对、`414` 请求过长、`429` 限流或锁定。

```bash
TOKEN="YOUR_TOKEN"; API="http://localhost:3253"

curl -H "Authorization: Bearer $TOKEN" "$API/list"
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=你好 世界"
```

## 安全说明

内置防护（无需配置）：**鉴权先于接口开关**（无法用 `401` / `403` 差异探测已启用的接口）；日志脱敏并替换控制字符；按 IP 限流，连续失败后锁定；可选 `allowed-hosts` 校验 Host 与 `Origin`；响应带 `Cache-Control: no-store`。

仍需注意：

- 接口**没有 TLS**，令牌明文传输——使用足够长的随机令牌，只在可信网络或反向代理之后暴露。
- `/cmd` 以假人身份执行命令，权限等同该假人，**不要把假人设为 OP**。
- 建议 `host` 用 `127.0.0.1` 并在防火墙层面限制来源。

## 个人个性化配置

每位玩家可保存专属创建参数，修改后**下次生成假人时生效**：`/fp config list` 查看，`/fp config set <项> <值>` 修改。

| 配置项 | 说明 |
|--------|------|
| `collidable` | 是否开启碰撞箱 |
| `invulnerable` | 是否开启无敌模式 |
| `wolverine` | 是否开启自动回血（快速再生） |
| `look_at_entity` | 自动看向周边可攻击实体，搭配攻击指令可自动刷怪 |
| `pickup_items` | 是否开启物品拾取 |
| `skin` | 是否默认使用创建者皮肤 |
| `replenish` | 自动补充消耗或使用完的物品 |
| `replace_tools` | GCA 风格的工具替换 |
| `autofish` | 是否开启自动钓鱼 |
| `simulation_distance` | 假人的模拟距离（区块半径），`-1` 沿用世界设置 |

- **`replace_tools`**：GCA 风格——非经验修补工具损坏后、经验修补工具磨损到 `tool-replacement.remaining-durability-threshold`（默认 `10`）以下时，若背包中有同类物品即更换，不看附魔。
- **`replenish`**：`/fp drop` / `/fp dropstack` 丢空主手后自动补货，食物与药水的容器在放得下时归还。`/fp dropinv` 不会补货。
- **`simulation_distance`**：以假人为中心的区块刻半径；默认写在 `default-features`，可用 `/fp config set` 或 `/fp set` 覆盖。

## 权限分组说明

<details>
<summary>点击展开查看详情</summary>

每条指令都有独立权限节点，插件另提供便捷分组：

### 权限组 `fakeplayer.spawn`

基础管理权限（均为 `fakeplayer.command.*`）：`spawn` `kick` `list` `distance` `select` `selection` `drop` `dropstack` `dropinv` `skin` `invsee` `enderchest` `status` `respawn` `config` `set` `setping`

### 权限组 `fakeplayer.tp`

`fakeplayer.command.tp`、`fakeplayer.command.tphere`、`fakeplayer.command.tps`

### 权限组 `fakeplayer.action`

动作权限：`attack` `mine` `use` `jump` `sneak` `sprint` `look` `turn` `move` `ride` `swap` `sleep` `wakeup` `stop` `hold` `say`（均为 `fakeplayer.command.*`）

特性权限：`fakeplayer.config.replenish`、`fakeplayer.config.replenish.chest`（从附近箱子补货）、`fakeplayer.config.replace-tools`、`fakeplayer.config.autofish`

> 无需严格管控时可直接分配 `fakeplayer.basic`，它包含除 `/fp cmd` 外的全部安全权限。

</details>

## 占位符变量

| 占位符 | 作用域 | 说明 |
|---|---|---|
| `%fakeplayer_total%` | 全局 | 当前服务器假人总数 |
| `%fakeplayer_list%` | 全局 | 所有在线假人名称，分隔符取自 `fakeplayer.placeholder.separator`（默认 `, `） |
| `%fakeplayer_list_<序号>%` | 全局 | 指定序号（从 0 开始）假人的名称，越界为空 |
| `%fakeplayer_list_<序号>_<属性>%` | 全局 | 指定序号假人的属性，如 `%fakeplayer_list_0_uuid%` |
| `%fakeplayer_isfake%` | 任意玩家 | `true` / `false` —— 是否为假人 |
| `%fakeplayer_name%` | 假人 | 假人名称 |
| `%fakeplayer_uuid%` | 假人 | 假人 UUID |
| `%fakeplayer_creator%` | 假人 | 创建者名称（`%fakeplayer_spawner%` 为别名） |
| `%fakeplayer_spawntime%` | 假人 | 生成时间，格式取自 `fakeplayer.placeholder.time-format` |
| `%fakeplayer_world%` | 假人 | 所在世界名 |
| `%fakeplayer_x%` / `%fakeplayer_y%` / `%fakeplayer_z%` | 假人 | 所在方块坐标 |
| `%fakeplayer_health%` | 假人 | 当前生命值 |
| `%fakeplayer_food%` | 假人 | 当前饥饿值 |
| `%fakeplayer_level%` | 假人 | 当前经验等级 |
| `%fakeplayer_gamemode%` | 假人 | 当前游戏模式 |
| `%fakeplayer_actions%` | 假人 | 当前活跃动作，如 `USE\|ATTACK` |
| `%fakeplayer_actions_translated%` | 假人 | 同上，输出为本地化文本 |

## 自定义本地化翻译

内置 `en` / `zh` / `zh_tw` / `zh_hk` / `ja`，改 `config.yml` 的 `i18n.locale` 切换（之后 `/fp reload`）。

自定义：在 `plugins/fakeplayer/message/` 放入 `message_<语言>_<地区>.properties`（复制[模板](fakeplayer-core/src/main/resources/message/message.properties)后改），把 `i18n.locale` 设为对应后缀（如 `zh_cn`），执行 `/fp reload-translation` 生效。文件必须使用 **UTF-8** 编码。

## 登录插件兼容与聊天事件

装了登录插件时，假人会因未登录被踢出或冻结。FakePlayer CE 会在假人加入时直接把它们标记为「已登录」；`/fp say` 也会完整触发聊天事件，使格式化插件正常工作。

| 登录插件 | 兼容方式 | 说明 |
|---|---|---|
| AuthMe / AuthMeReloaded | `forceLogin` / `forceRegister` | 经公开 API 标记已登录；未注册的名称用随机短密码自动注册 |
| CatSeedLogin / ReCatSeedLogin | 加入登录名单 | 写入其 `loginPlayers` 名单，不会被 `auto-kick` 踢出 |
| LibreLogin（Paper）/ LibreLoginNext（Paper） | API 授权 | 在线期间临时创建无密码账号，断开时清理 |

- **自动生效**：无需配置，仅在装有对应插件时启用。

### 自动登录（两套独立机制）

| 机制 | 配置 | 需要密码 |
|---|---|---|
| **登录插件 API 免登录**（推荐） | 无需配置，装有上面任一插件即生效 | 否 |
| **命令模板登录** | `auto-login.command.enabled: true` | 是（`/fp password` 设置或自动生成） |

第一种直接通过插件 API 放行，假人不会进入登录界面；第二种以假人身份执行命令，适用于没有 API 兼容的插件：

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
    change-password: "/changepassword %oldpassword% %newpassword% %newpassword%"
  random-password:
    charset: "abcdefghijklmnopqrstuvwxyz0123456789"   # 留空表示不自动生成
    length: 12
```

两套机制可同时开启；已被 API 放行时通常不需要命令模板。

### 聊天事件兼容（/fp say）

`/fp say` 完整触发 `AsyncPlayerChatEvent` 与 `AsyncChatEvent`，EssentialsX Chat、Vane 等格式化插件可修改格式与内容；事件未被取消时按事件结果广播。所有支持版本（`1.20.1 ~ 26.3`）行为一致。

## 常见问题

### 断开连接：PacketEvents 2.0 failed to inject

部分插件会篡改假人的网络连接对象，修改配置即可：

```yaml
prevent-kicking: ALWAYS
```

### 假人不被怪物攻击

假人默认开启无敌模式。执行 `/fp config set invulnerable false` 后才会承受生命值与饥饿值伤害，可搭配生命恢复药水或信标维持生存。

### 假人一段时间后自动掉线

通常是登录插件在踢未登录玩家。上面三个插件已内置兼容；若所用插件暂无兼容，可改用命令模板登录兜底：

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
```

> 请用足够强的密码，避免被 AuthMe 安全策略拦截。

## 项目构建

详见 [BUILD_zh.md](./BUILD_zh.md)。该文档只适用于本项目的 Gradle 多模块流程，不能用于上游 Maven 架构。
