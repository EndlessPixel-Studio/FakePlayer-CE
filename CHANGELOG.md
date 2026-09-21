# Changelog

FakePlayer CE 版本更新日志。版本号 `fp.buildN` 与 git tag / GitHub Release 一一对应。
Changelog for FakePlayer CE. Version `fp.buildN` matches the git tag / GitHub Release.

## fp.build10 - Unreleased

### English

- Added an independent GCA-style `replace_tools` feature: non-Mending tools are replaced after breaking, while Mending tools are replaced at the configured low-durability threshold. Same-type non-Mending tools are eligible at any durability; Mending replacements must remain above the threshold. The first eligible inventory item is used.
- Broadened `replenish`: `/fp drop` and `/fp dropstack` refill the main hand after its last item is dropped; consumed milk buckets, stews, honey bottles, and drinkable potions can be refilled while their returned bucket, bowl, or bottle is stored in inventory. If there is no matching refill item or no room for the remainder, it stays in hand.
- **New: built-in HTTP admin API.** Optional remote management for web panels and automation: `GET /list`, `/spawn`, `/kick`, `/kill`, `/say`. Authenticated via `?token=` or `Authorization: Bearer`; each endpoint can be toggled independently under `http-admin.interface`; every call is logged with the token redacted.
- **Breaking: `/fp kill` now truly kills.** `/fp kill` / `/fp killall` go through the real death flow (`setHealth(0)`), following the `kick-on-dead` config: by default the fake player is removed after death (death event cancelled, no loot); with `kick-on-dead: false` the corpse remains and can be revived via `/fp respawn`, with normal loot rules applied. The previous "remove without death" behavior is now `/fp kick` (single) / `/fp kickall` (all).
- New permission node `fakeplayer.command.kick`; the `fakeplayer.spawn` permission group now includes `kick` instead of `kill` (the latter now controls the real kill commands).
- Fixed `/fp say` fallback when only a message is given (e.g. `/fp say once 1234`).
- Build: version modules consolidated under `versions/` (repository layout only, no runtime change).

### 中文

- 新增独立的 GCA 风格 `replace_tools` 特性：非经验修补工具损坏后才更换；经验修补工具在达到配置的低耐久阈值时更换。同类型非经验修补工具无论耐久多少都可作为候选；经验修补候选的剩余耐久必须高于阈值，并按背包顺序选择第一个符合条件的工具。
- 扩展 `replenish`：`/fp drop` 与 `/fp dropstack` 丢掉主手最后一个物品后会自动补货；消耗奶桶、炖菜、蜂蜜瓶和可饮用药水后，可将返回的空桶、碗或玻璃瓶放入背包并补回原物。没有同款补货物或背包没有余物空间时，返回容器会留在手上。
- **新增：内置 HTTP 管理接口。** 可选的远程管理能力，适用于 Web 面板与自动化脚本：`GET /list`、`/spawn`、`/kick`、`/kill`、`/say`。通过 `?token=` 或 `Authorization: Bearer` 鉴权；各接口可在 `http-admin.interface` 下独立开关；所有调用均记录日志（token 已脱敏）。
- **破坏性变更：`/fp kill` 现为"真正杀死"。** `/fp kill` / `/fp killall` 现在通过真实死亡流程（`setHealth(0)`）击杀假人，并遵循 `kick-on-dead` 配置：默认死亡后即被移除（死亡事件被取消，不掉落物品）；`kick-on-dead: false` 时保留尸体并可用 `/fp respawn` 复活，掉落按服务端规则生效。原先"不造成死亡直接下线"的行为改为 `/fp kick`（单个）/ `/fp kickall`（批量）。
- 新增权限节点 `fakeplayer.command.kick`；`fakeplayer.spawn` 权限组现包含 `kick` 而非 `kill`（`kill` 现在对应真杀死命令）。
- 修复 `/fp say` 仅给消息时的参数回退（如 `/fp say once 1234`）。
- 构建：版本模块统一收纳至 `versions/` 目录（仅仓库结构变化，运行时无影响）。

## fp.build9 - 2026-09-11

### English

- **Auto-login support.** Fake players can automatically complete registration and login after spawning; `/fp password` sets a login password (stored in the plugin database) and `/fp changepassword` changes it (old password optional).
- Random password: when enabled, a random password is generated and reported to the creator.
- New permissions: `fakeplayer.command.say`, `fakeplayer.command.password`, `fakeplayer.command.changepassword`.
- New config option to force running on unsupported server versions.
- Fixed placeholders (`%fakeplayer_*%`) not resolving — identifier fixed to `fakeplayer` (upstream #172).

### 中文

- **支持自动登录。** 假人生成后可自动完成注册与登录；新增 `/fp password` 设置登录密码（存入插件数据库）、`/fp changepassword` 修改密码（旧密码可省略）。
- 随机密码：开启后自动生成随机密码并提示创建者。
- 新增权限：`fakeplayer.command.say`、`fakeplayer.command.password`、`fakeplayer.command.changepassword`。
- 新增配置项：可在不受支持的服务器版本上强制运行。
- 修复占位符（`%fakeplayer_*%`）无效：identifier 固定为 `fakeplayer`（上游 #172）。

## fp.build8 - 2026-09-11

### English

- **invsee access control.** Non-OP players can be allowed to open fake player inventories via config, with proper permission feedback.
- Privileged mode: reuse a real player's data to spawn a fake player when the name is already taken (PR #8).
- Replenish now validates player/inventory state before refilling (#10); fixed the fake player limit not being restored in the scheduler.
- Fixed the `tps` command description key; improved `/fp say` empty-message handling.

### 中文

- **invsee 权限控制。** 可通过配置允许非 OP 玩家打开假人背包，并完善无权限提示。
- 特权模式：名称被真实玩家占用时，可复用其数据生成假人（PR #8）。
- 补货前校验玩家与背包状态（#10）；修复调度器中假人上限未恢复的问题。
- 修复 `tps` 命令描述键名；完善 `/fp say` 空消息错误处理。

## fp.build7 - 2026-08-14

### English

- **Fixed fake player chat.** `/fp say` now supports `once / continuous / interval / stop` modes.
- Fixed "ghost" fake players that could not be removed.
- Docs: added a download section (Releases + Actions artifacts) to README.

### 中文

- **修复假人聊天失效。** `/fp say` 支持 `once / continuous / interval / stop` 子命令。
- 修复"幽灵假人"无法移除的问题。
- 文档：README 新增下载章节（Release 与 Actions 产物）。

## fp.build6 - 2026-08-13

### English

- **Fixed fake players could not be removed (Issue #3).** `/fp kill` and `/fp killall` now actually deregister the fake player entity from the server. Fake players are injected through a custom `NetworkManager`, where `Player.kick()` is a no-op on the fake connection; the cleanup flow now calls NMS `PlayerList.remove(ServerPlayer)` to truly remove them, and is compatible with Leaf forks.
- Includes the build5 fix: resolved `ClassCastException` when spawning on Leaf 1.21.11 / 26.2.
- Note: vanilla `/kick` does not affect fake players by design (they are not real connections). Use `/fp kill` or `/fp killall` to remove them.

### 中文

- **修复假人无法移除（Issue #3）。** `/fp kill` 与 `/fp killall` 现在会真正从服务器注销假人实体。假人通过自定义 `NetworkManager` 注入，`Player.kick()` 在 fake 连接上为空操作，旧逻辑仅清空内部记录而未断开实体。清理流程现改为调用 NMS `PlayerList.remove(ServerPlayer)` 真正移除，并兼容 Leaf 分支。
- 包含此前 build5 的修复：解决 Leaf 1.21.11 / 26.2 上生成假人的 `ClassCastException`。
- 说明：vanilla `/kick` 对假人无效为预期设计（假人并非真实连接），请使用 `/fp kill` 或 `/fp killall` 移除假人。

## fp.build5 - 2026-08-12

### English

- Upgraded to CommandAPI 12.0.0 and migrated off the removed `UTF8ResourceBundleControl` (now self-hosted), fixing `NoClassDefFoundError` on load.
- Resolved `ClassCastException` when spawning on Leaf 1.21.11 / 26.2 (`DedicatedPlayerList` cannot be cast to `MinecraftServer`).
- Build: configuration-cache compatible `processResources`; CI line-ending and gradlew handling fixes.

### 中文

- 升级至 CommandAPI 12.0.0，并迁移掉已被移除的 `UTF8ResourceBundleControl`（改为内置实现），修复加载时的 `NoClassDefFoundError`。
- 解决 Leaf 1.21.11 / 26.2 上生成假人时的 `ClassCastException`（`DedicatedPlayerList` 无法转换为 `MinecraftServer`）。
- 构建：`processResources` 兼容配置缓存；修复 CI 换行符与 gradlew 处理。

## fp.build4 - 2026-08-10

### English

- Auto-repair for worn tools (replenish).
- Feature config changes now apply to online fake players immediately.
- Compatibility with Mohist: removed the dependency on Paper's `LookAnchor` (upstream #200).
- CI now uploads build artifacts; the jar file name follows the version.

### 中文

- 自动修复磨损工具（补货）。
- 修改特性配置时实时应用到已生成的在线假人。
- 兼容 Mohist：移除对 Paper `LookAnchor` 的依赖（上游 #200）。
- CI 上传构建产物；jar 文件名跟随版本号。

## fp.build3 - 2026-08-10

### English

- **New `/fp say` command** — fake players can send chat messages (via NMS chat).
- Support for Minecraft 26.2; build upgraded to Java 25.
- Fixed NMS/plugin lifecycle stability and server-handle resolution; fixed the chat method signature mismatch.
- CI: added GitHub Actions build verification.

### 中文

- **新增 `/fp say` 命令** —— 假人可发送聊天消息（基于 NMS chat 实现）。
- 适配 Minecraft 26.2，构建升级至 Java 25。
- 修复 NMS 与插件生命周期稳定性、server handle 解析；修复聊天方法签名不匹配。
- CI：新增 GitHub Actions 构建验证。

## fp.build2 - 2026-07-28

### English

- Support for Minecraft 26.1 / 26.2 (new versioning scheme).
- Build: resource filters and runtime dependency packaging.

### 中文

- 支持 Minecraft 26.1 / 26.2（新版本号方案）。
- 构建：资源过滤与运行时依赖打包。

## fp.build1 - 2026-06-19

### English

- First community edition release: Gradle multi-module architecture with a single universal jar for Minecraft 1.20.1+.

### 中文

- 首个社区版发布：Gradle 多模块架构，单一通用 Jar 支持 Minecraft 1.20.1+。
