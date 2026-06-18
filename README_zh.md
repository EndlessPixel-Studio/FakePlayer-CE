# FakePlayer CE（社区修改版）

![BANNER_IMAGE](.github/README/BANNER.png)

[English](README.md) | 简体中文

⚠️ **重要版本免责声明**
本仓库为 **FakePlayer CE 社区修改版（Community Edition）**
1. 本项目**并非 FakePlayer 官方原版**，不由原作者发布、维护、背书；
2. 本分支基于原开源项目二次重构，核心目标是实现 Minecraft `1.20.1 ~ 1.21.11` 全版本单包兼容；
3. 本版本所有 Bug、功能需求、问题反馈仅可提交至本仓库，请勿提交至上游原作者仓库。

本插件是受 Carpet-Mod 启发的服务端假人插件，适配 Minecraft 1.20.x、1.21.x 全系列版本。

[点击查看演示视频](https://youtu.be/NePaDz-P5nI)

## 功能特性
- 生成对服务器完全透明的假人玩家，可用于区块常驻加载
- 假人支持原版/插件指令管控（传送、封禁、背包编辑等）
- 完整控制假人移动、攻击、挖矿、周期性自动化动作
- 每位玩家拥有独立个性化默认配置

### FakePlayer CE 专属改动
✅ 单Jar通吃 `1.20.1 ~ 1.21.11` 全版本，无需分版本下载
✅ 构建体系从 Maven 重构为 Gradle Kotlin DSL 多模块工程
✅ NMS 版本隔离封装，后续MC新版本适配成本更低
✅ 持续跟进 Paper/Purpur 新版本兼容性修复

## 运行前置依赖
+ [Paper](https://papermc.io) 或 [Purpur](http://purpurmc.org) 核心服务端
+ [CommandAPI](https://commandapi.jorel.dev) 前置插件（**禁止使用 10.0.0 版本**）

## 配置文件说明
插件首次加载仅会生成模板文件 `config.tmpl.yml`，你需要手动将其重命名为 `config.yml` 作为正式配置文件。
该设计可以在版本升级时直观预览新增配置项。

[点击查看配置文件示例](fakeplayer-core/src/main/resources/config.yml)

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
每位玩家都可以自定义专属创建参数，修改后**下次生成假人自动生效**

使用示例：
+ `/fp config list` - 查看全部可配置项
+ `/fp config set collidable false` - 修改指定配置

| 配置项 | 说明 |
|--------|------|
| collidable | 是否开启碰撞箱 |
| invulnerable | 是否开启无敌模式 |
| wolverine | 是否开启自动回血模式 |
| look_at_entity | 自动看向周边可攻击实体，可搭配攻击指令实现自动打怪 |
| pickup_items | 是否开启拾取物品 |
| skin | 是否默认使用创建者皮肤 |
| replenish | 是否开启物品自动补充 |
| autofish | 是否开启自动钓鱼 |

## 权限分组说明
<details>
<summary>点击展开查看详情</summary>

每条指令都拥有独立权限节点，插件同时封装了便捷权限组：

### 权限组 `fakeplayer.spawn`
包含以下权限：
- fakeplayer.command.spawn - 创建假人
- fakeplayer.command.kill - 击杀假人
- fakeplayer.command.list - 查看假人列表
- fakeplayer.command.distance - 查询距离
- fakeplayer.command.select - 选中假人
- fakeplayer.command.selection - 查看选中假人
- fakeplayer.command.drop - 丢弃物品
- fakeplayer.command.dropstack - 丢弃整组物品
- fakeplayer.command.dropinv - 清空背包
- fakeplayer.command.skin - 复制皮肤
- fakeplayer.command.invsee - 查看背包
- fakeplayer.command.status - 查看状态
- fakeplayer.command.respawn - 复活假人
- fakeplayer.command.config - 修改默认配置
- fakeplayer.command.set - 修改单假人配置

### 权限组 `fakeplayer.tp`
包含以下权限：
- fakeplayer.command.tp
- fakeplayer.command.tphere
- fakeplayer.command.tps

### 权限组 `fakeplayer.action`
包含以下权限：
- fakeplayer.command.attack - 攻击
- fakeplayer.command.mine - 挖矿
- fakeplayer.command.use - 使用交互
- fakeplayer.command.jump - 跳跃
- fakeplayer.command.sneak - 潜行
- fakeplayer.command.sprint - 疾跑
- fakeplayer.command.look - 看向目标
- fakeplayer.command.turn - 转向
- fakeplayer.command.move - 移动
- fakeplayer.command.ride - 骑乘
- fakeplayer.command.swap - 主副手切换
- fakeplayer.command.sleep - 睡觉
- fakeplayer.command.wakeup - 唤醒
- fakeplayer.command.stop - 停止动作
- fakeplayer.command.hold - 切换快捷栏
- fakeplayer.config.replenish - 自动补物
- fakeplayer.config.replenish.chest - 自动从附近箱子补货
- fakeplayer.config.autofish - 自动钓鱼

如果服务器不需要严格权限管控，可直接分配 `fakeplayer.basic`，该分组包含除 `/fp cmd` 高危指令以外全部安全权限。
</details>

## 占位符变量
+ `%fakeplayer_total%`：当前服务器假人总数
+ `%fakeplayer_creator%`：假人创建者名称
+ `%fakeplayer_actions%`：假人当前活跃动作，示例：`USE|ATTACK`

# 自定义本地化翻译
1. 在 `plugins/fakeplayer` 文件夹新建 `message` 目录
2. 将 [模板翻译文件](fakeplayer-core/src/main/resources/message/message.properties) 复制进该目录
3. 重命名为 `message_语言_地区.properties`，例如 `message_zh_cn.properties`
4. 修改 `config.yml` 内 `i18n.locale` 配置为对应后缀名，示例：`zh_cn`
5. 执行 `/fp reload-translation` 重载翻译；若修改语言配置项，需先执行 `/fp reload`

**翻译文件必须使用 UTF-8 编码保存**

# 上游版本区别说明
### FakePlayer 官方原版
为本项目修改基础，原版每个版本仅适配单个 Minecraft 版本，使用 Maven 构建体系发布。

### FakePlayer CE 修改汇总
1. 构建迁移：Maven 项目重构为 Gradle Kotlin DSL 多模块工程
2. 跨版本适配：NMS 代码按版本拆分独立模块，覆盖 `1.20.1 ~ 1.21.11`
3. 发布形式：统一单通用Jar包，不再分版本单独分发
4. 长期维护：持续跟进 Paper/Purpur 新版本兼容性问题修复
5. 针对性修复多版本运行时冲突Bug

> 如需查看 FakePlayer 官方原版更新内容，请前往原作者上游仓库查阅。

# 常见问题
## 断开连接：PacketEvents 2.0 failed to inject
部分插件篡改假人网络连接对象，修改配置项即可解决：
```yaml
# config.yml
prevent-kicking: ALWAYS
```

## 假人不会被怪物攻击
假人默认开启无敌模式，执行 `/fp config set invulnerable false` 关闭后，假人才会受到生命值、饥饿值伤害；可搭配生命恢复药水、信标维持生存。

## 假人一段时间后自动下线
AuthMe 等登录插件会判定假人长时间未登录踢出，可在配置 `self-commands` 填入注册登录指令规避：
```yaml
# 注意设置高强度密码，避免被AuthMe安全策略拦截
self-commands:
  - '/register abc123! abc123!'
  - '/login abc123!'
```

# 项目编译构建
详细步骤查看 [BUILD.md](./BUILD.md)
> 该编译文档仅适配 **FakePlayer CE Gradle 多模块编译流程**，无法用于原Maven架构官方项目编译。