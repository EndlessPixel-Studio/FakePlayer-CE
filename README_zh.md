# FakePlayer CE（社区版）

<div align="center">

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.2-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

[English](README.md) | 简体中文 | [繁體中文](README_zh_TW.md)

---

> **FakePlayer CE** 是基于 FakePlayer 原项目的社区维护分支，通过 Gradle 多模块架构重构，实现了对 Minecraft `1.20.1` 至 `26.2` 全版本的**单一 Jar 包兼容**。

## ⚠️ 社区版声明

本仓库为 **FakePlayer CE（Community Edition）** —— 一个独立的社区维护分支，**并非 FakePlayer 官方原版**。

- 本项目**不由原作者发布、维护或背书**，系基于开源协议二次开发的社区版本。
- 核心目标：重构并扩展至 Minecraft `1.20.1` ~ `26.2` 全版本跨版本兼容，单一 Jar 包通吃。
- 所有 Bug、功能需求、问题反馈**请仅提交至本仓库**，切勿提交至上游原作者仓库。

---

## 概述

FakePlayer 是一款受 [Carpet-Mod](https://github.com/gnembon/fabric-carpet) 启发的服务端假人插件，可在 Minecraft 服务器上生成高度逼真的虚拟玩家。本社区版在原版基础上扩展了多版本兼容能力，并持续跟进维护。

📺 [观看演示视频](https://youtu.be/NePaDz-P5nI)

## 功能特性

- 生成对服务器完全透明的假人玩家，适用于区块常驻加载
- 完美支持原版及插件指令管控（传送、封禁、背包编辑等）
- 完整操控假人行为：移动、跳跃、攻击、挖矿等，支持周期性自动化
- 每位玩家拥有独立的个性化默认配置模板

### FakePlayer CE 专属增强

| 增强项 | 说明 |
|---|---|
| **单 Jar 多版本** | 一个通用 Jar 覆盖 MC `1.20.1 ~ 26.2`，无需分版本下载 |
| **Gradle Kotlin DSL 构建** | 从 Maven 迁移至现代化 Gradle 多模块工程架构 |
| **NMS 版本隔离** | 各版本 NMS 代码独立封装，降低未来 MC 版本适配成本 |
| **持续兼容维护** | 持续跟进 Paper/Purpur 最新版本兼容性修复 |
| **HTTP 管理接口** | 内置轻量 HTTP 接口，可远程查询状态、生成 / 移除假人，并控制其动作、朝向、背包与命令 |

## 运行前置依赖

- [Paper](https://papermc.io) 或 [Purpur](http://purpurmc.org) 核心服务端
- [CommandAPI](https://commandapi.jorel.dev) 前置插件（**请勿使用 `10.0.0` 版本**）

## 下载

FakePlayer CE 以**单一通用 jar**（`fakeplayer-fp.buildX.jar`）形式发布，覆盖 Minecraft `1.20.1 ~ 26.2` 全版本。

### 1. 稳定版发布（推荐）

从 GitHub Releases 页面下载最新稳定构建：

- 📦 [Release 发布下载](https://github.com/EndlessPixel/FakePlayer-CE/releases)

每个 Release 均包含预构建的 `fakeplayer-fp.buildX.jar`，将其放入服务端的 `plugins/` 目录即可。

### 2. 自动构建（CI / 每次提交）

每次 push 与 Pull Request 都会通过 GitHub Actions 触发 Gradle 构建。可在任意一次成功的构建运行中，于 **Artifacts（产物）** 区域获取最新 jar：

- 🔧 [GitHub Actions 自动构建](https://github.com/EndlessPixel/FakePlayer-CE/actions)

打开某次构建运行记录，向下滚动到 **Artifacts**，下载 `fakeplayer-dist` 产物（内含 `fakeplayer-fp.buildX.jar`）。此类构建适合在稳定版发布前抢先测试最新修复。

> 下载完成后，将 `fakeplayer-fp.buildX.jar` 放入服务端 `plugins/` 目录并重启服务器。需先安装 CommandAPI 前置插件。

## 配置文件说明

插件首次加载时仅生成模板文件 `config.tmpl.yml`，需手动重命名为 `config.yml` 后生效。该模板机制可让你在升级时直观预览新增配置项，避免覆盖已有设置。

[查看配置文件示例](fakeplayer-core/src/main/resources/config.yml)

## 指令列表
| 指令 | 功能说明 | 权限节点 | 备注 |
|------|---------|---------|------|
| /fp spawn | 创建假人 | fakeplayer.command.spawn | |
| /fp kick | 踢出假人（从服务器移除） | fakeplayer.command.kick | |
| /fp kickall | 踢出服务器所有假人 | OP | |
| /fp kill | 杀死假人（真实死亡，可能掉落物品） | fakeplayer.command.kill | |
| /fp killall | 杀死服务器所有假人 | OP | |
| /fp select | 设置默认操作假人 | fakeplayer.command.select | 创建多个假人后可用 |
| /fp selection | 查看当前选中假人 | fakeplayer.command.selection | 创建多个假人后可用 |
| /fp list | 列出所有在线假人 | fakeplayer.command.list | |
| /fp distance | 查看与假人间距 | fakeplayer.command.distance | |
| /fp drop | 假人丢弃手中单个物品 | fakeplayer.command.drop | |
| /fp dropstack | 假人丢弃手中整组物品 | fakeplayer.command.dropstack | |
| /fp dropinv | 假人清空全部背包物品 | fakeplayer.command.dropinv | |
| /fp skin | 复制其他玩家皮肤 | fakeplayer.command.skin | 离线玩家复制存在60秒冷却 |
| /fp invsee | 打开假人背包界面 | fakeplayer.command.invsee | 右键假人可触发同等效果 |
| /fp sleep | 假人进入睡觉状态 | fakeplayer.command.sleep | |
| /fp wakeup | 唤醒睡觉假人 | fakeplayer.command.wakeup | |
| /fp status | 查看假人当前状态 | fakeplayer.command.status | |
| /fp respawn | 复活已死亡假人 | fakeplayer.command.respawn | 仅关闭假人死亡踢出配置时可用 |
| /fp tp | 传送到假人位置 | fakeplayer.command.tp | |
| /fp tphere | 将假人传送至自身位置 | fakeplayer.command.tphere | |
| /fp tps | 与假人互换位置 | fakeplayer.command.tps | |
| /fp set | 修改单个假人独立配置 | fakeplayer.command.set | |
| /fp config | 修改自身创建假人默认配置 | fakeplayer.command.config | |
| /fp expme | 提取假人经验至自身 | fakeplayer.command.expme | |
| /fp attack | 假人发起攻击 | fakeplayer.command.attack | |
| /fp mine | 假人挖掘方块 | fakeplayer.command.mine | |
| /fp use | 假人交互/放置方块/使用物品 | fakeplayer.command.use | |
| /fp jump | 假人跳跃 | fakeplayer.command.jump | |
| /fp stop | 终止假人所有动作 | fakeplayer.command.stop | |
| /fp turn | 假人原地转向 | fakeplayer.command.turn | |
| /fp look | 假人看向指定坐标 | fakeplayer.command.look | |
| /fp move | 假人定向移动 | fakeplayer.command.move | |
| /fp ride | 假人骑乘实体 | fakeplayer.command.ride | |
| /fp sneak | 假人进入潜行模式 | fakeplayer.command.sneak | |
| /fp sprint | 假人疾跑 | fakeplayer.command.sprint | |
| /fp swap | 切换主手副手物品 | fakeplayer.command.swap | |
| /fp hold | 切换快捷栏指定格子物品 | fakeplayer.command.hold | |
| /fp cmd | 让假人执行控制台指令 | fakeplayer.command.cmd | |
| /fp say | 让假人发送聊天消息 | fakeplayer.command.say | |
| /fp password | 设置假人登录密码 (存入插件数据库) | fakeplayer.command.password | 需配合 auto-login 使用 |
| /fp changepassword | 修改假人登录密码 (旧密码可省略, 留空则使用已保存密码) | fakeplayer.command.changepassword | |
| /fp reload | 重载插件配置文件 | OP | |

## HTTP 管理接口

FakePlayer CE 内置一个可选的轻量 HTTP 接口，用于远程管理假人（对接 Web 面板、自动化脚本等）。该功能**默认关闭**，基于 JDK 内置 HTTP 服务器实现，无需额外依赖。

在 `config.yml` 中启用：

```yaml
http-admin:
  enabled: true          # 是否启用 HTTP 接口
  host: 0.0.0.0          # 监听地址
  port: 3253             # 监听端口
  token: ""              # 鉴权令牌；留空时启动会自动生成随机令牌并打印到控制台
  allow-get: true        # 是否允许 GET；设为 false 后仅接受 POST
  allowed-hosts: []      # Host / Origin 白名单，留空不校验；配置后可缓解 DNS rebinding
  rate-limit:
    requests-per-minute: 120   # 每个来源 IP 每分钟请求上限
    auth-failures: 10          # 连续鉴权失败多少次后锁定来源 IP（0 = 不锁定）
    lockout-seconds: 60        # 锁定时长（秒）
  interface:
    # 查询类
    list: true           # 启用 GET /list
    status: true         # 启用 GET /status
    info: true           # 启用 GET /info
    # 生命周期
    spawn: true          # 启用 GET /spawn
    kick: true           # 启用 GET /kick
    kill: true           # 启用 GET /kill
    respawn: true        # 启用 GET /respawn
    # 行为控制
    action: true         # 启用 GET /action
    stop: true           # 启用 GET /stop
    say: true            # 启用 GET /say
    teleport: true       # 启用 GET /teleport
    look: true           # 启用 GET /look
    hold: true           # 启用 GET /hold
    swap: true           # 启用 GET /swap
    # 其他
    cmd: true            # 启用 GET /cmd
    batch: true          # 启用 GET /kickall、/killall、/sayall
```

所有接口默认接受 `GET`，同时也接受 `POST`（参数一律走 query）。**令牌统一通过 `Authorization: Bearer <token>` 请求头传递，不支持 `?token=` 查询参数** —— 令牌出现在 URL 里会进入代理日志、浏览器历史与 `Referer`，而且每个接口都要重复拼一遍参数。

路径为**精确匹配**：只有上表中的路径会被处理，`/listfoo` 之类的路径返回 `404`，不会落到 `/list` 上。

| 接口 | 说明 | 成功返回 | 失败返回 |
|---|---|---|---|
| `GET /list` | 列出所有在线假人 | `{"fakeplayer":["name1","name2"]}` | — |
| `GET /status[?name=<名字>]` | 查询假人的详细状态（坐标、朝向、血量、饥饿、经验、模式、创建者、手持物品、进行中的动作等）；省略 `name` 时返回全部 | `{"fakeplayer":[{...}]}` | `{"status":"failure","msg":"..."}` |
| `GET /info` | 插件与服务端信息（插件版本、MC 版本、服务端、假人数量、在线玩家数、数量上限） | `{"plugin":"...","minecraft":"...",...}` | — |
| `GET /spawn?name=<名字>` | 在主世界出生点生成一个假人 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kick?name=<名字>` | 踢出（移除）一个假人 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kill?name=<名字>` | 杀死一个假人（真实死亡，可能掉落物品） | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /respawn?name=<名字>` | 让已死亡的假人重生 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /say?name=<名字>&message=<内容>` | 以假人身份发送聊天消息（内容需 URL 编码） | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /action?name=<名字>&action=<动作>` | 触发任意动作，参数见下方 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /stop?name=<名字>` | 停止假人当前的所有动作 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /teleport?name=<名字>[&world=&x=&y=&z=&yaw=&pitch=]` | 传送假人，未给出的参数保持当前值 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<名字>&direction=<方向>` | 让假人转向 `north`/`south`/`east`/`west`/`up`/`down` | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<名字>&at=<x,y,z>` | 让假人看向指定坐标 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<名字>&yaw=<角度>&pitch=<角度>` | 直接设置假人朝向 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /hold?name=<名字>&slot=<1-9>` | 切换假人主手槽位 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /swap?name=<名字>` | 交换假人主副手物品 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /cmd?name=<名字>&command=<命令>` | 以假人身份执行一条命令 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kickall` | 移除服务器全部假人 | `{"status":"success","count":n}` | — |
| `GET /killall` | 杀死服务器全部假人 | `{"status":"success","count":n}` | — |
| `GET /sayall?message=<内容>` | 让全部假人发言 | `{"status":"success","count":n}` | — |

`/action` 的参数：

| 参数 | 默认 | 说明 |
|---|---|---|
| `action` | 必填 | 动作名，不区分大小写，`-` 与 `_` 等价。可选：`ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | 执行次数；`-1` 表示持续执行 |
| `interval` | `0` | 两次执行之间间隔的 tick 数；只给 `interval` 不给 `count` 时视为持续执行 |
| `wait` | `0` | 首次执行前等待的 tick 数 |
| `message` | — | 仅 `SAY` 动作需要 |

常见失败提示：

- 生成 — `The dummy's name conflicts with that of an actual player.`（与真实玩家重名）/ `The dummy is already online.`（假人已在线）
- 踢出 — `Dummies do not exist.`（假人不存在）
- 杀死 — `The dummy does not exist.`（假人不存在）/ `The dummy is already dead.`（假人已死亡）
- 重生 — `The dummy does not exist.`（假人不存在）/ `The dummy is not dead.`（假人未死亡）
- 发言 — `Name is required`（缺少假人名）/ `Message is required`（缺少消息内容）
- 动作 — `Action is required, available actions: ...`（缺少或未知动作）/ `Message is required for the say action`（SAY 缺少内容）
- 传送 — `Invalid number: x`（坐标不是数字）/ `Unknown world: xxx`（世界不存在）
- 鉴权与开关 — `Unauthorized: pass the token via the Authorization: Bearer header`（401，令牌缺失或错误）/ `Interface disabled`（403，对应接口已关闭）
- 限流与锁定 — `Too many requests`（429，超过每 IP 每分钟上限）/ `Too many failed attempts, try again later`（429，连续鉴权失败被锁定）
- 主机与来源 — `Host not allowed`（403，Host 或跨源 Origin 不在白名单内）
- 其他 — `Not found`（404，路径不存在或不是精确路径）/ `Request URI too long`（414）/ `Method not allowed`（405）

调用示例：

```bash
# 令牌统一走请求头，URL 里只放业务参数
TOKEN="YOUR_TOKEN"
API="http://localhost:3253"

# 列出所有假人
curl -H "Authorization: Bearer $TOKEN" "$API/list"

# 查看假人详细状态（省略 name 则返回全部）
curl -G -H "Authorization: Bearer $TOKEN" "$API/status" --data-urlencode "name=klmgun"

# 服务端与插件信息
curl -H "Authorization: Bearer $TOKEN" "$API/info"

# 生成假人
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"

# 踢出假人
curl -H "Authorization: Bearer $TOKEN" "$API/kick?name=klmgun"

# 杀死假人
curl -H "Authorization: Bearer $TOKEN" "$API/kill?name=klmgun"

# 让假人持续攻击（每 10 tick 一次）
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"

# 停止假人所有动作
curl -H "Authorization: Bearer $TOKEN" "$API/stop?name=klmgun"

# 把假人传送到指定坐标
curl -H "Authorization: Bearer $TOKEN" "$API/teleport?name=klmgun&world=world&x=0&y=64&z=0"

# 让假人看向东方
curl -H "Authorization: Bearer $TOKEN" "$API/look?name=klmgun&direction=east"

# 让假人执行命令
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"

# 让假人发言（消息内容需 URL 编码）
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=你好 世界"
```

### 安全说明

内置的防护（无需额外配置）：

- 令牌使用定长比较，避免通过响应时间逐字节推断；`Authorization: Bearer` 不区分大小写。
- **鉴权先于接口开关**：未携带正确令牌时一律返回 `401`，无法通过 `401` / `403` 的差异枚举哪些接口被启用。
- **精确路径匹配**：只有文档中的路径会被处理，`/listfoo` 返回 `404`，避免绕过按精确路径放行的外部 ACL / WAF。
- **日志安全**：名称含 `token` 的参数一律脱敏；控制字符会被替换、长度会被截断；未授权请求不记录 query。避免日志被伪造或撑爆。
- **限流与失败锁定**：默认每个 IP 每分钟 120 次，连续 10 次鉴权失败锁定 60 秒。
- **主机 / 同源校验**：配置 `allowed-hosts` 后校验 Host；浏览器发起的跨源请求（带 `Origin`）主机不符会被拒绝（默认开启），可缓解 DNS rebinding。
- 请求 URI 超过 4096 字符直接返回 `414`；响应带 `Cache-Control: no-store`，不会被浏览器或中间层缓存。

仍需管理员注意：

- 接口**没有 TLS**，令牌以明文经过网络。请务必使用足够长的随机令牌，并且只在可信网络（或置于反向代理之后）暴露该接口。
- `/cmd` 会让假人执行命令，其权限等同于该假人在服务端的权限；不要把假人设为 OP。
- 建议把 `host` 改为 `127.0.0.1`（或只在内网地址上监听），并在防火墙层面限制来源，避免直接暴露到公网。

## 个人个性化配置

每位玩家均可自定义专属创建参数，修改后**下次生成假人时自动生效**。

使用示例：
- `/fp config list` — 查看全部可配置项
- `/fp config set collidable false` — 修改指定配置

| 配置项 | 说明 |
|--------|------|
| `collidable`      | 是否开启碰撞箱 |
| `invulnerable`    | 是否开启无敌模式 |
| `wolverine`       | 是否开启自动回血（快速再生） |
| `look_at_entity`  | 自动看向周边可攻击实体；搭配攻击指令可实现自动刷怪 |
| `pickup_items`    | 是否开启物品拾取 |
| `skin`            | 是否默认使用创建者皮肤 |
| `replenish`       | 自动补充消耗或使用完的物品；`/fp drop` 或 `/fp dropstack` 丢掉主手最后一个物品后也会补货 |
| `replace_tools`   | GCA 风格的工具替换：非经验修补工具损坏后更换，经验修补工具在低耐久阈值附近更换 |
| `autofish`        | 是否开启自动钓鱼 |

在 `config.yml` 中通过 `tool-replacement.remaining-durability-threshold` 设置经验修补工具的剩余耐久阈值（默认 `10`）；使用 `/fp config set replace_tools true` 为假人开启。

遵循 GCA 的默认模式，非经验修补工具损坏后才会更换；经验修补工具达到阈值时，如果背包中有同类型的非经验修补工具，或剩余耐久高于阈值的经验修补工具，就会更换。匹配只看物品类型，附魔差异不会阻止更换；按背包顺序选择第一个合格工具。

开启 `replenish` 后，消耗奶桶、炖菜、蜂蜜瓶或可饮用药水时，如果背包里有同款补货物且能存下返回的空桶、碗或玻璃瓶，就会将容器放进背包并补回原物；否则容器会留在手上。`/fp dropinv` 仍会直接清空背包，不会自动补货。

## 权限分组说明

<details>
<summary>点击展开查看详情</summary>

每条指令均设有独立权限节点，插件同时提供了便捷的权限分组：

### 权限组 `fakeplayer.spawn`

包含基础假人管理权限：
- `fakeplayer.command.spawn` — 创建假人
- `fakeplayer.command.kick` — 踢出假人
- `fakeplayer.command.list` — 查看假人列表
- `fakeplayer.command.distance` — 查询距离
- `fakeplayer.command.select` — 选中假人
- `fakeplayer.command.selection` — 查看选中假人
- `fakeplayer.command.drop` — 丢弃物品
- `fakeplayer.command.dropstack` — 丢弃整组物品
- `fakeplayer.command.dropinv` — 清空背包
- `fakeplayer.command.skin` — 复制皮肤
- `fakeplayer.command.invsee` — 查看背包
- `fakeplayer.command.status` — 查看状态
- `fakeplayer.command.respawn` — 复活假人
- `fakeplayer.command.config` — 修改默认配置
- `fakeplayer.command.set` — 修改单假人配置

### 权限组 `fakeplayer.tp`

传送相关权限：
- `fakeplayer.command.tp`
- `fakeplayer.command.tphere`
- `fakeplayer.command.tps`

### 权限组 `fakeplayer.action`

行为动作权限：
- `fakeplayer.command.attack` — 攻击
- `fakeplayer.command.mine` — 挖矿
- `fakeplayer.command.use` — 交互使用
- `fakeplayer.command.jump` — 跳跃
- `fakeplayer.command.sneak` — 潜行
- `fakeplayer.command.sprint` — 疾跑
- `fakeplayer.command.look` — 看向目标
- `fakeplayer.command.turn` — 转向
- `fakeplayer.command.move` — 移动
- `fakeplayer.command.ride` — 骑乘
- `fakeplayer.command.swap` — 主副手切换
- `fakeplayer.command.sleep` — 睡觉
- `fakeplayer.command.wakeup` — 唤醒
- `fakeplayer.command.stop` — 停止动作
- `fakeplayer.command.hold` — 切换快捷栏
- `fakeplayer.command.say` — 发送聊天消息
- `fakeplayer.config.replenish` — 自动补物
- `fakeplayer.config.replenish.chest` — 从附近箱子补货
- `fakeplayer.config.replace-tools` — 自动替换低耐久工具
- `fakeplayer.config.autofish` — 自动钓鱼

若服务器无需严格权限管控，可直接分配 `fakeplayer.basic` 权限组，该组包含除 `/fp cmd` 高危指令外的全部安全权限。

</details>

## 占位符变量

| 占位符 | 说明 |
|---|---|
| `%fakeplayer_total%` | 当前服务器假人总数 |
| `%fakeplayer_creator%` | 假人创建者名称 |
| `%fakeplayer_actions%` | 假人当前活跃动作，如 `USE\|ATTACK` |

## 自定义本地化翻译

内置语言：`en`、`zh`、`zh_tw`、`zh_hk`，在 `config.yml` 中修改 `i18n.locale` 即可切换（之后执行 `/fp reload`）。

1. 在 `plugins/fakeplayer/` 下创建 `message` 目录
2. 将[模板翻译文件](fakeplayer-core/src/main/resources/message/message.properties)复制到该目录
3. 重命名为 `message_<语言>_<地区>.properties`，如 `message_zh_cn.properties`
4. 修改 `config.yml` 中 `i18n.locale` 为对应后缀名，如 `zh_cn`
5. 执行 `/fp reload-translation` 重载翻译；若修改了语言配置，需先执行 `/fp reload`

> **注意：** 翻译文件必须使用 **UTF-8** 编码保存。

## 上游版本区别说明

### FakePlayer 官方原版

为本项目的修改基础。原版每个版本仅适配单个 Minecraft 版本，采用 Maven 构建体系发布。

### FakePlayer CE 修改汇总

1. **构建体系**：从 Maven 迁移至 Gradle Kotlin DSL 多模块工程
2. **跨版本适配**：NMS 代码按版本拆分为独立模块，覆盖 `1.20.1 ~ 26.2`
3. **发布形式**：统一单通用 Jar 包，不再分版本单独分发
4. **长期维护**：持续跟进 Paper/Purpur 新版本兼容性问题修复
5. **多版本修复**：针对性修复跨版本运行时冲突 Bug

> 如需了解 FakePlayer 官方原版更新，请前往原作者上游仓库查阅。

## 常见问题

### 断开连接：PacketEvents 2.0 failed to inject

部分插件会篡改假人的网络连接对象，修改以下配置即可解决：

```yaml
# config.yml
prevent-kicking: ALWAYS
```

### 假人不被怪物攻击

假人默认开启无敌模式。执行 `/fp config set invulnerable false` 关闭无敌后，假人才会承受生命值与饥饿值伤害。可搭配生命恢复药水或信标维持生存。

### 假人一段时间后自动掉线

AuthMe 等登录插件会判定假人长时间未登录而踢出。在配置文件的 `self-commands` 中填入注册/登录指令可规避：

```yaml
# 请设置高强度密码，避免被 AuthMe 安全策略拦截
self-commands:
  - '/register abc123! abc123!'
  - '/login abc123!'
```

## 项目构建

详细步骤请参阅 [BUILD_zh.md](./BUILD_zh.md)。

> 该构建文档仅适用于 **FakePlayer CE Gradle 多模块编译流程**，无法用于原 Maven 架构官方项目的构建。
