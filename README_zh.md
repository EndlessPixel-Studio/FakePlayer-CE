<div align="center">

<img src=".github/assets/logo.png" alt="FakePlayer CE" width="200">

# FakePlayer CE（社区版）

[English](README.md) | 简体中文 | [繁體中文](README_zh_TW.md) | [日本語](README_ja.md)

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Hangar](https://img.shields.io/badge/Hangar-FakePlayer--CE-1f7ae0)](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

---

> **FakePlayer CE** 是 [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer) 的社区维护分支：受 [Carpet-Mod](https://github.com/gnembon/fabric-carpet) 启发，在服务端生成对服务器完全透明的假人玩家，并用 Gradle 多模块架构做到 **Minecraft `1.20.1` ~ `26.3` 单一 Jar 兼容**。

⭐ 如果你喜欢 FakePlayer CE，给它点个 Star 吧！这对项目的成长非常有帮助。

## 目录

- [⚠️ 社区版声明](#️-社区版声明)
- [快速上手](#快速上手)
- [功能特性](#功能特性)
- [运行前置依赖](#运行前置依赖)
- [下载](#下载)
- [配置文件说明](#配置文件说明)
- [数据库](#数据库)
- [AI 聊天](#ai-聊天)
- [指令列表](#指令列表)
- [HTTP 管理接口](#http-管理接口)
- [安全说明](#安全说明)
- [个人个性化配置](#个人个性化配置)
- [权限分组说明](#权限分组说明)
- [占位符变量](#占位符变量)
- [自定义本地化翻译](#自定义本地化翻译)
- [登录插件兼容与聊天事件](#登录插件兼容与聊天事件)
- [常见问题](#常见问题)
  - [断开连接：PacketEvents 2.0 failed to inject](#断开连接packetevents-20-failed-to-inject)
  - [假人不被怪物攻击](#假人不被怪物攻击)
  - [假人一段时间后自动掉线](#假人一段时间后自动掉线)
- [Star 趋势](#star-趋势)
- [贡献者](#贡献者)

## ⚠️ 社区版声明

- 本仓库是**独立的社区维护分支**，不由原作者发布、维护或背书。

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

- **稳定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推荐）
- **Hangar**：[hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
- **最新构建**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 任一次成功的运行 → Artifacts → `fakeplayer-dist`

## 配置文件说明

首次加载只生成模板 `config.tmpl.yml`，需手动改名为 `config.yml` 后生效。示例见 [config.yml](fakeplayer-core/src/main/resources/config.yml)。

**按权限设置假人上限**：在 `spawn-limit` 下配置如 `vip: 10`，并给玩家 `fakeplayer.spawn.limit.vip` 权限；命中多个权限时取最大值，OP 不受限。

## 数据库

数据默认存在 SQLite（`plugins/FakePlayer-CE/data.db`）。想用 MySQL，先建一个空库，在 `config.yml` 里把 `database.type` 改成 `mysql` 并填好 `database.mysql`，然后重启。

## AI 聊天

`/fp aichat` 让假人通过 OpenAI 兼容接口生成聊天内容。首次使用需在 `plugins/FakePlayer-CE/ai-chat.yml` 里填写 `api` 与 `api-key`（或设置 `api-key-env` 指定的环境变量），把 `enabled` 改为 `true`，再执行 `/fp reload`。

| 指令 | 说明 |
|---|---|
| `/fp aichat public [once\|continuous\|interval <tick>] [假人]` | 发到公共聊天 |
| `/fp aichat private <玩家> [once\|continuous\|interval <tick>] [假人]` | 只发给指定玩家 |
| `/fp aichat stop [假人]` | 停止持续发言 |
| `/fp aichat model [名称]` | 查看或切换 AI 模型（仅 OP） |

可在 `ai-chat.yml` 的 `models:` 下配置多个模型；`auto-reply` 也可在该文件中开启。

## 指令列表

凡是假人名称参数都可以用 `-a` 一次操作范围内所有假人；`select`、`spawn`/`spawnuuid`、`rename` 除外。

| 指令 | 权限节点 |
|------|---------|
| /fp spawn | fakeplayer.command.spawn |
| /fp spawnjson | fakeplayer.command.spawn.json |
| /fp spawnuuid（已弃用） | fakeplayer.command.spawn.uuid |
| /fp kick / kickall | fakeplayer.command.kick / OP |
| /fp kill / killall | fakeplayer.command.kill / OP |
| /fp select / selection | fakeplayer.command.select / .selection |
| /fp list / distance | fakeplayer.command.list / .distance |
| /fp drop / dropstack / dropinv | fakeplayer.command.drop / .dropstack / .dropinv |
| /fp skin / invsee / enderchest | fakeplayer.command.skin / .invsee / .enderchest |
| /fp sleep / wakeup / autosleep | fakeplayer.command.sleep / .wakeup / .autosleep |
| /fp status / respawn | fakeplayer.command.status / .respawn |
| /fp tp / tphere / tps | fakeplayer.command.tp / .tphere / .tps |
| /fp set / setping / config | fakeplayer.command.set / .setping / .config |
| /fp expme | fakeplayer.command.expme |
| /fp attack / mine / use / jump | fakeplayer.command.attack / .mine / .use / .jump |
| /fp stop / turn / look / move | fakeplayer.command.stop / .turn / .look / .move |
| /fp goto / gotostop / ride | fakeplayer.command.goto / .goto / .ride |
| /fp sneak / sprint / swap / hold | fakeplayer.command.sneak / .sprint / .swap / .hold |
| /fp cmd / say / aichat | fakeplayer.command.cmd / .say / .aichat |
| /fp password / changepassword | fakeplayer.command.password / .changepassword |
| /fp reload / update / version / lang | OP / OP / - / - |

```text
/fp spawnjson {"name":"Bot_1","world":"world","x":0,"y":64,"z":0,"lifespan_seconds":600}
```

## HTTP 管理接口

可选的 HTTP 接口（默认关闭），用于 Web 面板与自动化脚本。

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1
  port: 3253
  token: ""
  allow-get: true
  interface:
    list: true
```

令牌只走 `Authorization: Bearer <token>` 请求头；参数走 query，`GET` / `POST` 均可。

| 接口 | 说明 |
|---|---|
| `/list`、`/status`、`/info` | 查询假人与服务器状态 |
| `/spawn`、`/kick`、`/kill`、`/respawn` | 生命周期 |
| `/say`、`/aichat`、`/aichat/stop`、`/action`、`/stop` | 聊天与动作 |
| `/teleport`、`/look`、`/hold`、`/swap`、`/goto`、`/setping`、`/sneak`、`/sprint` | 移动与状态 |
| `/reload`、`/cmd`、`/kickall`、`/killall`、`/sayall` | 管理 |

## 安全说明

鉴权先于接口开关检查，内置按 IP 限流与可选 `allowed-hosts` 校验。

- 接口**没有 TLS**，只在可信网络或反向代理之后暴露。
- `/cmd` 以假人身份执行命令，**不要把假人设为 OP**。
- 建议 `host` 用 `127.0.0.1` 并在防火墙层面限制来源。

## 个人个性化配置

每位玩家可保存专属默认值，修改后**下次生成假人时生效**：`/fp config list` 查看，`/fp config set <项> <值>` 修改。

| 配置项 | 说明 |
|--------|------|
| `collidable` | 是否开启碰撞箱 |
| `invulnerable` | 是否开启无敌模式 |
| `wolverine` | 是否开启自动回血 |
| `look_at_entity` | 自动看向周边可攻击实体 |
| `pickup_items` | 是否开启物品拾取 |
| `skin` | 是否默认使用创建者皮肤 |
| `replenish` | 自动补充消耗的物品 |
| `replace_tools` | GCA 风格的工具替换 |
| `autofish` | 是否开启自动钓鱼 |
| `aichat_fake_inventory` | 是否允许 AI 读取假人背包 |
| `aichat_questioner_inventory` | 是否允许 AI 读取提问玩家背包 |
| `simulation_distance` | 假人的模拟距离，`-1` 沿用世界设置 |

## 权限分组说明

每条指令都有独立权限节点，便捷分组见 `plugin.yml`。

## 占位符变量

安装 PlaceholderAPI 后可使用 `%fakeplayer_total%`、`%fakeplayer_list%`、`%fakeplayer_isfake%`，以及 `%fakeplayer_name%`、`%fakeplayer_world%`、`%fakeplayer_health%` 等假人属性。

## 自定义本地化翻译

内置 `en` / `zh` / `zh_tw` / `zh_hk` / `ja`。改 `config.yml` 的 `i18n.locale` 后 `/fp reload`；玩家可用 `/fp lang <代码>` 单独覆盖。

## 登录插件兼容与聊天事件

装有 AuthMe / CatSeedLogin / LibreLogin 时，假人会自动标记为已登录，无需配置。`/fp say` 会触发正常聊天事件，格式化插件可正常工作。

## 常见问题

### 断开连接：PacketEvents 2.0 failed to inject

在 `config.yml` 设置 `prevent-kicking: ALWAYS`。

### 假人不被怪物攻击

假人默认无敌。执行 `/fp config set invulnerable false` 后才会受伤。

### 假人一段时间后自动掉线

通常是登录插件在踢未登录玩家。上面三个插件已内置兼容；其他插件可用 `auto-login.command` 命令模板兜底。

## Star 趋势

[![Star History Chart](https://api.star-history.com/svg?repos=EndlessPixel-Studio/FakePlayer-CE&type=Date)](https://star-history.com/#EndlessPixel-Studio/FakePlayer-CE&Date)

## 贡献者

[![Contributors](https://contrib.rocks/image?repo=EndlessPixel-Studio/FakePlayer-CE)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/graphs/contributors)
