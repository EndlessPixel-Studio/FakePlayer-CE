# FakePlayer CE（社区版）

![BANNER_IMAGE](.github/README/BANNER.png)

[English](README.md) | 简体中文

---

> **FakePlayer CE** 是基于 FakePlayer 原项目的社区维护分支，通过 Gradle 多模块架构重构，实现了对 Minecraft `1.20.1` 至 `1.21.11` 全版本的**单一 Jar 包兼容**。

## ⚠️ 社区版声明

本仓库为 **FakePlayer CE（Community Edition）** —— 一个独立的社区维护分支，**并非 FakePlayer 官方原版**。

- 本项目**不由原作者发布、维护或背书**，系基于开源协议二次开发的社区版本。
- 核心目标：重构并扩展至 Minecraft `1.20.1` ~ `1.21.11` 全版本跨版本兼容，单一 Jar 包通吃。
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
| **单 Jar 多版本** | 一个通用 Jar 覆盖 MC `1.20.1 ~ 1.21.11`，无需分版本下载 |
| **Gradle Kotlin DSL 构建** | 从 Maven 迁移至现代化 Gradle 多模块工程架构 |
| **NMS 版本隔离** | 各版本 NMS 代码独立封装，降低未来 MC 版本适配成本 |
| **持续兼容维护** | 持续跟进 Paper/Purpur 最新版本兼容性修复 |

## 运行前置依赖

- [Paper](https://papermc.io) 或 [Purpur](http://purpurmc.org) 核心服务端
- [CommandAPI](https://commandapi.jorel.dev) 前置插件（**请勿使用 `10.0.0` 版本**）

## 配置文件说明

插件首次加载时仅生成模板文件 `config.tmpl.yml`，需手动重命名为 `config.yml` 后生效。该模板机制可让你在升级时直观预览新增配置项，避免覆盖已有设置。

[查看配置文件示例](fakeplayer-core/src/main/resources/config.yml)

## 指令列表
| 指令 | 功能说明 | 权限节点 | 备注 |
|------|---------|---------|------|
| /fp spawn | 创建假人 | fakeplayer.command.spawn | |
| /fp kill | 击杀单个假人 | fakeplayer.command.kill | |
| /fp killall | 清空服务器所有假人 | OP | |
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
| /fp reload | 重载插件配置文件 | OP | |

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
| `replenish`       | 是否开启物品自动补充 |
| `autofish`        | 是否开启自动钓鱼 |

## 权限分组说明

<details>
<summary>点击展开查看详情</summary>

每条指令均设有独立权限节点，插件同时提供了便捷的权限分组：

### 权限组 `fakeplayer.spawn`

包含基础假人管理权限：
- `fakeplayer.command.spawn` — 创建假人
- `fakeplayer.command.kill` — 击杀假人
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
- `fakeplayer.config.replenish` — 自动补物
- `fakeplayer.config.replenish.chest` — 从附近箱子补货
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
2. **跨版本适配**：NMS 代码按版本拆分为独立模块，覆盖 `1.20.1 ~ 1.21.11`
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

详细步骤请参阅 [BUILD.md](./BUILD.md)。

> 该构建文档仅适用于 **FakePlayer CE Gradle 多模块编译流程**，无法用于原 Maven 架构官方项目的构建。