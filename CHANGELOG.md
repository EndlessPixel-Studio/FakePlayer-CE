# Changelog

FakePlayer CE 版本更新日志。版本号 `fp.buildN` 与 git tag / GitHub Release 一一对应。
Changelog for FakePlayer CE. Version `fp.buildN` matches the git tag / GitHub Release.

## 编写规范 (Writing rules)

- 只记录影响用户 / 运行时行为的变更（新功能、修复、破坏性变更、配置或权限变化）。不写构建工具链、静态分析、死代码清理、重构等内部维护，也不写仓库文件（LICENSE、CI 脚本、README 章节）与发布渠道维护（顺带修复的功能缺陷只写修复本身）。
- 未发布的版本写作 `## fp.buildN - (Not published | 未发布)`：版本号可在开发期预置为下一版本，发布时把标题改成发布日期。
- 每条条目以加粗短语概括要点，中英文段落条目数量与顺序保持一致。

## fp.build17 - (Not published | 未发布)

### English

- **Japanese localization.** Messages, command descriptions and help text now ship in Japanese; set `i18n.locale: ja` to use it. `config.yml` bumped to 33.
- **New `/fp goto stop [name]`.** Cancels an ongoing walk without touching other actions; previously only `/fp stop` could end it, which also aborted everything else.
- **Fixed `/fp kick -a` and `/fp kill -a` doing nothing for operators.** The `-a` shortcut was filtering by creator even for ops and the console, so it only ever matched fake players that sender had spawned; it now means every fake player on the server, matching `/fp kickall`. A name that matches nothing also reports an error instead of falling back to the selected fake player.
- **`/fp say` help now matches every other command.** `/fp say help` prints the same paginated list as `/fp say` subcommands elsewhere, and the usage line spells out each subcommand's arguments (including `interval <ticks>`), so following the docs no longer sends the name where a tick count is expected.
- **Fixed messages being dropped when `/fp say once` is used repeatedly.** Once commands were queued as a single replaceable action, so two in the same tick left only the last one; each call now runs on its own.

### 中文

- **新增日语本地化。** 消息、命令描述与帮助文本均已提供日语版本，把 `i18n.locale` 设为 `ja` 即可使用。`config.yml` 版本提升至 33。
- **新增 `/fp goto stop [假人]`。** 只中止正在进行的寻路，不影响其它动作；此前只能用 `/fp stop` 把该假人的所有动作一起停掉。
- **修复 `/fp kick -a` 与 `/fp kill -a` 对 op 无效。** `-a` 原先只按创建者过滤，即使 op / 控制台执行也只看自己召唤的假人；现在与 `/fp kickall` 一致，作用于全服假人。另外，名字匹配不到时改为直接报错，不再回退到选中的假人——避免打错名字误操作。
- **`/fp say` 的帮助与其他命令统一。** `/fp say help` 现在输出与其他子命令一致的分页列表，用法行也逐一写清每个子命令的参数（含 `interval <ticks>`），照文档传参不会再出现「名字被当整数」的报错。
- **修复连续执行 `/fp say once` 丢消息。** once 原先作为可替换的单条动作排队，同一 tick 内连发两次只会执行最后一条；现在每次调用各自执行。

## fp.build16 - 2026-10-31

### English

- **New `simulation_distance` option.** Fake players can now keep a configurable simulation distance (chunk radius): set the default in `default-features`, override per creator with `/fp config set simulation_distance <n>`, or change one fake player on the fly with `/fp set <name> simulation_distance <n>`; `-1` keeps the world setting. Useful for keeping a distant fake player's chunks ticking without raising the server's simulation distance (#83). `config.yml` bumped to 29.
- **Fixed restored fake players no longer performing actions.** With `restore-on-start`, fake players came back online but stopped doing anything because their action state was never persisted. The snapshot now also records active actions (remaining count, interval, message and pathfinding target) and re-applies them after respawn (#84).
- **Fixed fake players vanishing without a "left the game" message.** On newer server versions the quit message is no longer broadcast automatically when a fake player is removed; it is now sent explicitly, so spawning and removal show matching join / leave lines.
- **New `death-message` option.** With `kick-on-dead` enabled the death event is cancelled, so no death message was shown; set `death-message: true` to broadcast it anyway. `config.yml` bumped to 30.
- **New `/fp aichat` command.** Fake players can generate chat lines through an OpenAI-compatible endpoint and either broadcast them (`public`) or whisper a single player (`private`), once or continuously until `/fp aichat stop` (#92). All settings live in a separate `ai-chat.yml` (endpoint, key or environment variable, model, sampling, context length, prompts and interval), so `config.yml` is untouched.
- **`/fp aichat` can follow the conversation.** Set `recent-chat-lines` in `ai-chat.yml` to feed the last N public chat lines into the prompt, so fake players can pick up what players are talking about.
- **`/fp aichat` gained an event-driven `auto-reply` mode.** Instead of talking on a timer, a fake player can now answer when a player says something sensible: short messages, symbol-only messages and repeated spam are filtered first, and `chance` / `cooldown-seconds` keep the token cost down. Continuous mode still exists, but it is off unless `allow-continuous` is enabled in `ai-chat.yml`.
- **HTTP Admin API: `/aichat` endpoints.** `GET /aichat?name=<name>&mode=public|private&target=<player>&continuous=true&interval=<ticks>` lets a fake player talk through AI, and `GET /aichat/stop?name=<name>` ends continuous mode. Both are gated by `http-admin.interface.aichat` and need `ai-chat.yml` to be enabled. `config.yml` bumped to 31 (#96).
- **New `/fp spawnuuid <name> <uuid> [world] [location]`.** Spawns a fake player with a specific UUID, which helps when migrating from another fake-player plugin or keeping an external record in sync. The command is rejected when the name is already bound to a different UUID, when the UUID belongs to an online player, or when another fake player uses it. New permission `fakeplayer.command.spawn.uuid` (default op) (#95).
- **Fixed fake players being immune for a few seconds after spawning.** Since 1.21.11 the server keeps a player invulnerable until its client reports the world as loaded, and a fake player never answers; it is now marked as loaded right after the spawn teleport, so it can take damage immediately (#94).
- **Fixed `/fp set` values being lost on restart.** With `restore-on-start` a fake player came back with the creator's defaults, because the snapshot only stored name, creator, position and actions. Per-fake-player features (`pickup_items`, `invulnerable`, `collidable`, `replenish`, `autofish`, `simulation_distance`, ...) are now snapshotted and re-applied (#103).
- **Fake players now earn and announce advancements like real players.** Progress was previously suppressed entirely, which also broke advancement rewards, advancement-triggered functions and quest plugins (#98). Set `advancements: false` to restore the old behaviour (no progress awarded, nothing saved). `config.yml` bumped to 32.

### 中文

- **新增 `simulation_distance` 配置项。** 假人现在可以拥有独立的模拟距离（以假人为中心的区块半径）：在 `default-features` 里设全局默认，用 `/fp config set simulation_distance <n>` 按创建者覆盖，或用 `/fp set <假人> simulation_distance <n>` 即时调整单个假人；`-1` 表示沿用世界设置。适合让远处的假人继续刻区块而不必抬高整服模拟距离（#83）。`config.yml` 版本提升至 29。
- **修复恢复后的假人不再执行动作。** 开启 `restore-on-start` 时假人虽然回来了，但动作全部丢失。快照现在同时记录进行中的动作（剩余次数、间隔、消息与寻路目标），并在重生后重新应用（#84）。
- **修复假人被移除时不显示「离开了游戏」消息。** 新版服务端不再自动广播该消息，现在移除假人时会补发，加入与离开的提示保持一致。
- **新增 `death-message` 配置项。** 开启 `kick-on-dead` 时死亡事件会被取消，因此不会显示死亡消息；把 `death-message` 设为 `true` 即可照常广播。`config.yml` 版本提升至 30。
- **新增 `/fp aichat` 命令。** 假人可通过 OpenAI 兼容接口生成聊天内容，可选发到公共聊天（`public`）或只发给某个玩家（`private`），支持说一次或 `continuous` 持续发言直到 `/fp aichat stop`（#92）。设置独立放在 `ai-chat.yml`（接口地址、密钥或环境变量、模型、采样参数、上下文长度、提示词与间隔），`config.yml` 保持不变。
- **`/fp aichat` 现在可以接话。** 在 `ai-chat.yml` 设置 `recent-chat-lines` 后，最近 N 条公共聊天会作为上下文传给模型，假人能够接着玩家正在聊的话题说。
- **`/fp aichat` 新增事件驱动的 `auto-reply`。** 不再靠定时器主动发话，而是玩家说了像样的话时才可能接一句：短消息、纯符号和重复刷屏会先被过滤，再由 `chance` / `cooldown-seconds` 控制频率以节省 token。`continuous` 仍保留，但必须在 `ai-chat.yml` 打开 `allow-continuous` 才能用。
- **HTTP 管理接口新增 `/aichat` 端点。** `GET /aichat?name=<名字>&mode=public|private&target=<玩家>&continuous=true&interval=<tick>` 让假人通过 AI 发言，`GET /aichat/stop?name=<名字>` 停止持续发言。两者都由 `http-admin.interface.aichat` 控制，且需要先在 `ai-chat.yml` 启用。`config.yml` 版本提升至 31（#96）。
- **新增 `/fp spawnuuid <名称> <UUID> [世界] [坐标]`。** 用指定 UUID 创建假人，便于从其他假人插件迁移数据或与外部记录保持一致。名称已绑定其他 UUID、UUID 属于在线玩家、或已被其他假人占用时会拒绝。新增权限 `fakeplayer.command.spawn.uuid`（默认 op）（#95）。
- **修复假人出生后有几秒免疫伤害的问题。** 1.21.11 起服务端要等客户端上报「世界已加载」才允许受伤，而假人永远不会回应；现在生成传送完成后会替它标记为已加载，出生即可承伤（#94）。
- **修复 `/fp set` 的值在重启后丢失。** 开启 `restore-on-start` 时假人只恢复了名称、创建者、位置与动作，特性回落到创建者默认值。现在单个假人的特性（`pickup_items`、`invulnerable`、`collidable`、`replenish`、`autofish`、`simulation_distance` 等）也会写入快照并在恢复后回放（#103）。
- **假人现在会像真实玩家一样达成并播报成就。** 此前进度被完全屏蔽，连带进度奖励、进度触发函数与任务类插件全部失效（#98）。设为 `advancements: false` 可恢复原行为（不授予进度、不写存档）。`config.yml` 版本提升至 32。

## fp.build15 - 2026-10-24

### English

- **`/fp skin` now resolves premium names through SkinsRestorer.** With SkinsRestorer installed the skin is looked up through its API and the choice survives reconnects; without it the Mojang API path is used. Invalid names now report a clear error.
- **Auto-login config reorganised.** Settings now live under `auto-login`, documenting command-template login (`auto-login.command.enabled`) next to the no-config login-plugin exemption. Old flat keys are still read, so existing configs keep working.
- **HTTP Admin API: five new endpoints.** `/goto` (pathfind), `/setping` (tab list ping), `/sneak`, `/sprint` and `/reload`, each with its own switch under `http-admin.interface`.
- **Releases are now distributed through GitHub Releases only.** The Modrinth listing was removed (it did not pass review), and the download links in the READMEs now point to GitHub Releases.
- **New `share-fakeplayer` option.** A fake player's UUID is derived from its name, so the same name always maps to the same UUID — players can take turns controlling one fake player and `usercache.json` stays clean. `config.yml` bumped to 28 (#169, thanks to @fuzhulike).
- **Fixed translations breaking when Nexo is loaded.** `/fp` help and descriptions could show raw keys because message files were loaded through `ResourceBundle`; they are now read directly from the jar (#150, thanks to @fuzhulike).
- **Fixed `/fp say once` / `continuous` / `interval` silently doing nothing on Paper.** Fake players had a restricted chat visibility that made the server drop the message early; they now use full visibility and send through the normal chat event pipeline.
- **`/fp say` without a subcommand now displays usage** instead of silently failing. Fixed message-only invocation (`/fp say once hello`) ignoring the selected fake player when more than one exists.
- **New command `/fp goto <x> <y> <z>`: make a fake player walk to a location.** It pathfinds on its own (steps up obstacles, swims), teleports a short distance when stuck, and rejects targets it cannot stand on. `/fp stop` cancels the trip.
- **Fixed `/fp goto` rejecting reachable destinations** on slopes and stairs: the footing search no longer gives up on a direction early and now checks every layer (#77, thanks to @lldxlzy).

### 中文

- **`/fp skin` 现可通过 SkinsRestorer 解析正版玩家皮肤。** 装有时经其 API 查询并持久化，重新进服仍生效；未安装时仍走 Mojang API。名称无效或查询失败会给出明确提示。
- **自动登录配置重排。** 相关配置统一收进 `auto-login`，命令模板登录（`auto-login.command.enabled`）与无需配置的登录插件免登录并列说明。旧平铺键仍会读取，现有配置继续有效。
- **HTTP 管理接口新增五个端点。** `/goto`（寻路）、`/setping`（Tab 列表延迟）、`/sneak`、`/sprint` 与 `/reload`（重载配置与语言文件），各在 `http-admin.interface` 下有独立开关。
- **发布渠道统一为 GitHub Releases。** Modrinth 资源已下架（未通过审核），README 中的下载链接现均指向 GitHub Releases。
- **新增 `share-fakeplayer` 配置项。** 假人 UUID 由名称确定性生成，同一名称始终对应同一 UUID——不同玩家可轮流控制同一假人，`usercache.json` 不再每次召唤累积记录。`config.yml` 版本提升至 28（#169，感谢 @fuzhulike）。
- **修复与 Nexo 共载时语言文件失效。** 消息文件改用直接读取后，`/fp` 帮助与描述不再显示原始 key（#150，感谢 @fuzhulike）。
- **修复 `/fp say once` / `continuous` / `interval` 在 Paper 上执行成功却不发消息。** 假人原先的聊天可见性会让服务端提前丢弃消息，现改为完整可见性并走原有聊天事件管线，兼容格式化插件。
- **直接输入 `/fp say` 现在会显示用法。** 修复只输入消息（如 `/fp say once hello`）时，在多个假人且已选择目标的情况下无法正确使用已选假人的问题。
- **新命令 `/fp goto <x> <y> <z>`：让假人走到指定坐标。** 假人自行寻路（可跨障碍、游泳），卡住时短暂传送续行；无法站立的目标、超出世界边界或距离过远会直接拒绝。`/fp stop` 可中止行程。
- **修复 `/fp goto` 误拒可抵达目标**（斜坡、阶梯地形）：落点搜索不再提前放弃某个方向，现逐层查找（#77，感谢 @lldxlzy 的修复）。

## fp.build14 - 2026-10-16

### English

- **The plugin now requires Java 25 to run.** The build target was raised from Java 21 to Java 25 because Paper 26.x's `paper-api` is compiled for Java 25; servers still running Java 21 must upgrade their runtime before installing this build.
- **Invincible mode now really blocks damage.** A creative-mode attacker could still hurt an `invulnerable` fake player because vanilla lets creative attacks bypass invulnerability; that damage is now cancelled. Sources meant to bypass it (the void, `/kill`) still work (#67).
- **`invulnerable` is enabled by default again**, matching the documentation ("fake players spawn with invincible mode enabled by default"). It had silently defaulted to disabled since an upstream commit, which the changelog never recorded. `config.yml` bumped to 25 (#68).
- **Command help now accepts subcommand names.** `/fp help <name>` prints that command's description, usage, aliases and subcommands (permission-filtered, localized, with tab completion); `/fp help <page> [size]` still pages the full list (#62).
- **Fixed duplicate quit events when removing a fake player.** On Leaf/Folia servers the quit flow could run twice, dispatching after-quit commands again and logging login-plugin errors; removal is now idempotent (#70).

### 中文

- **运行要求升至 Java 25。** 编译目标由 Java 21 提升至 Java 25——Paper 26.x 的 `paper-api` 以 Java 25 编译，引用它必须 target ≥ 25；仍以 Java 21 运行的服务端需要先升级运行环境再安装本版本。
- **无敌模式现在真的免伤。** 创造模式玩家的攻击此前仍能打伤开启 `invulnerable` 的假人（原版对创造攻击放行无敌判定），现该伤害会被取消；虚空、`/kill` 等本应无视无敌的来源仍然有效（#67）。
- **`invulnerable` 默认值改回开启**，与文档「假人默认开启无敌模式」一致。此前默认关闭是上游某次提交悄悄改的，更新日志从未记录过。`config.yml` 版本提升至 25（#68）。
- **命令帮助支持子命令名查询。** `/fp help <子命令名>` 直接输出该命令的描述、用法、别名与子命令（按权限过滤、按配置语言本地化、支持 Tab 补全）；`/fp help <页码> [每页数]` 仍按页浏览全部命令（#62）。
- **修复移除假人时的重复退出 / 二次移除。** Leaf/Folia 系服务端上退出流程可能触发两次（after-quit 命令重复派发、登录插件报错），现移除流程幂等（#70）。

## fp.build13 - 2026-10-03

### English

- **Support for Minecraft 26.3.** A new full `fakeplayer-v26_3` module adapts to the 26.3 API changes. CommandAPI `12.1.0` or newer is required on 26.3.
- **Removed the deprecated `allow-commands` option.** `/fp cmd` now always requires the `fakeplayer.command.cmd` permission — assign it with a permissions plugin. The option had been deprecated and only printed a removal warning on startup.
- **`check-for-updates` now checks this repository.** It queries the latest GitHub release of `EndlessPixel-Studio/FakePlayer-CE` and compares the `fp.buildN` build number, instead of the original upstream project (which made the check meaningless for this fork and usually failed).
- **Fake player ping simulation.** The new `custom-ping` option sets the tab-list ping of fake players — a fixed value (`30`) or a range (`20,50`); `custom-ping-dynamic` lets it drift slightly, and leaving it empty keeps the real value. Runtime change: `/fp setping <ping> [name]`.
- **`config.yml` reordered into themed sections** with condensed comments; bumped to version 24.
- **Fixed `follow-quiting-force` removing a creator's fake players even when the creator reconnected within the configured delay** (quick reconnect, or coming back from another server) — the fake players are now kept when the creator is online again.
- **`/fp config` now works from the console.** `list` shows the global defaults; operators and the console can target a player with the new optional `[player]` argument, while regular players still configure only themselves. `/fp config help`, which previously printed nothing, was added as well.
- **Fixed `/fp say` (and `say continuous` / `say interval`) rejecting the name of a fake player owned by someone else:** it answered "you haven't spawned a fake player yet", which from the console made named addressing impossible. Names now follow the same rules as every other command.
- **`/fp say once` / `continuous` / `interval` now TAB-complete fake player names** — the name argument previously offered no suggestions at all (unlike every other command), because it is parsed as a plain string so that a single argument still falls back to being the message.
- **Fixed `/fp say stop` being entirely unusable.** It threw a `ClassCastException` when given a name, and demanded a message when not; `stop` no longer requires one.

### 中文

- **支持 Minecraft 26.3。** 新增完整 NMS 模块 `fakeplayer-v26_3` 适配 26.3 的 API 变更；在 26.3 上需 CommandAPI `12.1.0` 及以上。
- **移除已废弃的 `allow-commands` 配置。** `/fp cmd` 现在始终需要 `fakeplayer.command.cmd` 权限，请通过权限插件分配。该配置早已标记废弃，启动时仅打印一条移除警告。
- **`check-for-updates` 改为检查本仓库。** 现查询 `EndlessPixel-Studio/FakePlayer-CE` 的最新 GitHub Release，并按 `fp.buildN` 的构建号比较；此前检查的是原上游项目，对本分支没有意义且通常直接失败。
- **新增假人延迟（ping）模拟。** 新配置 `custom-ping` 设置假人在 Tab 列表显示的延迟，可填固定值（`30`）或范围（`20,50`）；`custom-ping-dynamic` 让其小幅波动，留空则用真实值。运行时可用 `/fp setping <ping> [name]` 修改。
- **`config.yml` 按主题重新分组并精简注释**，版本提升至 24。
- **修复 `follow-quiting-force` 在创建者于延迟内重新上线（快速重连，或从其他服务器返回）时仍会清理其假人的问题**；创建者已回到服务器时现在会保留假人。
- **`/fp config` 现在可以在控制台使用。** `list` 展示全局默认值；op 与控制台可用新增的可选 `[player]` 参数指定玩家，普通玩家仍只能配置自己；并补上了此前没有任何输出的 `/fp config help`。
- **修复 `/fp say`（及 `say continuous` / `say interval`）指定「他人的假人名称」时报「你还没召唤假人呢」**，在控制台里等于无法按名字指定假人。名称解析现与其他命令一致：op 与控制台可指定任意假人，普通玩家仅限自己的。
- **`/fp say once` / `continuous` / `interval` 的假人名字现在支持 TAB 补全**——此前该参数没有任何补全（与其他命令不同），因为为了保留「单参数即消息」的回退，它按字符串解析。
- **修复 `/fp say stop` 完全不可用的问题。** 带名字时抛 `ClassCastException`，不带名字时又要求消息；`stop` 现已不要求消息。

## fp.build12 - 2026-09-26

### English

- **Folia support.** The plugin now runs on Folia and other regionised servers: scheduling goes through region/entity/global schedulers, spawning uses `teleportAsync`, and the connection answers keep-alive itself so fake players are no longer kicked for timing out (#48).
- **Login plugin compatibility.** Fake players are marked as logged in for AuthMe, CatSeedLogin and LibreLogin / LibreLoginNext without running register or login commands; all are declared as `softdepend` and only load when installed (#17, #19, #26).
- **`/fp say` now goes through the chat event pipeline.** Messages are sent as unsigned chat through `ChatProcessor`, so `AsyncPlayerChatEvent` / `AsyncChatEvent` fire and formatting plugins can rewrite them (#21).
- **New `follow-quiting-force` option.** A creator's fake players are removed `follow-quiting-force-delay` seconds (default 3) after the creator quits instead of waiting for the periodic cleanup; disabled by default. On a proxy it also triggers when the creator merely switches servers. `config.yml` bumped to 22 (#50).
- Fixed AuthMe login failing with short passwords, and CatSeedLogin compatibility when its classes live in a different location.
- Fixed command help ignoring the configured locale: help text is now resolved with the plugin's configured locale (#20).

### 中文

- **新增 Folia 支持。** 插件现可在 Folia 及区域化服务端运行：调度统一走区域/实体/全局调度器，生成使用 `teleportAsync`，连接自行回应 keep-alive，不再被误判超时踢出（#48）。
- **新增登录插件兼容。** 假人生成时会被自动标记为已登录，支持 AuthMe、CatSeedLogin 与 LibreLogin / LibreLoginNext，无需执行注册/登录命令；均以 `softdepend` 声明，仅在安装时加载（#17、#19、#26）。
- **`/fp say` 现走聊天事件管线。** 消息以未签名形式交给 `ChatProcessor`，完整触发 `AsyncPlayerChatEvent` / `AsyncChatEvent`，格式化插件可修改格式（#21）。
- **新增 `follow-quiting-force` 配置。** 创建者退出 `follow-quiting-force-delay` 秒（默认 3）后即清理其假人，无需等待定时轮询；默认关闭。代理服上仅切换服务器也会触发。`config.yml` 版本提升至 22（#50）。
- 修复 AuthMe 短密码登录失败，以及 CatSeedLogin 类位于不同位置时的兼容问题。
- 修复命令帮助未遵循配置语言：帮助文本现按插件配置的 locale 解析（#20）。

## fp.build11 - 2026-09-23

### English

- **New command `/fp enderchest`** (alias `/fp ec`): open a fake player's ender chest (same rules as `/fp invsee`); shift-right-click a fake player also opens it. New permission `fakeplayer.command.enderchest`.
- **More PlaceholderAPI placeholders.** Added list placeholders (`%fakeplayer_list%`, `%fakeplayer_list_<index>%`, `%fakeplayer_isfake%`) and fake-player attributes (`%fakeplayer_name%`, `%fakeplayer_uuid%`, `%fakeplayer_world%`, coordinates, health, food, level, gamemode, creator, spawn time, running actions, etc.).
- **HTTP admin API expanded from 5 to 18 endpoints.** New `/status`, `/info`, `/action`, `/stop`, `/teleport`, `/look`, `/hold`, `/swap`, `/respawn`, `/cmd`, and batch `/kickall`, `/killall`, `/sayall`; each toggleable under `http-admin.interface`, `config.yml` bumped to 20.
- **Breaking: the HTTP token is now header-only.** `Authorization: Bearer <token>` is the only auth method; `?token=` is no longer accepted.
- **HTTP API security hardening.** Constant-time token comparison; `token` params redacted from logs; responses escape control characters; per-IP rate limit and lockout; `Host`/`Origin` checks against DNS rebinding; exact-path routing; `POST` supported.
- **New `replace_tools` feature.** Non-Mending tools are replaced after breaking; Mending tools are replaced at a low-durability threshold.
- **Broadened `replenish`.** `/fp drop` / `/fp dropstack` refill the main hand after the last item is dropped; consumed milk/stews/honey/potions are refilled from their returned container.
- Fixed command tab-completion in non-English locales (e.g. Turkish `tr_TR`): case conversions now use `Locale.ROOT`.

### 中文

- **新增指令 `/fp enderchest`**（别名 `/fp ec`）：打开假人的末影箱（规则同 `/fp invsee`）；潜行右键假人也可打开。新增权限 `fakeplayer.command.enderchest`。
- **扩充 PlaceholderAPI 占位符。** 新增列表占位符（`%fakeplayer_list%`、`%fakeplayer_list_<序号>%`、`%fakeplayer_isfake%`）与假人属性（`%fakeplayer_name%`、`%fakeplayer_uuid%`、`%fakeplayer_world%`、坐标、血量、饥饿、等级、模式、创建者、生成时间、正在进行的动作等）。
- **HTTP 管理接口从 5 个扩展到 18 个。** 新增 `/status`、`/info`、`/action`、`/stop`、`/teleport`、`/look`、`/hold`、`/swap`、`/respawn`、`/cmd` 及批量 `/kickall`、`/killall`、`/sayall`；各接口可在 `http-admin.interface` 下独立开关，`config.yml` 版本提升至 20。
- **破坏性变更：HTTP 令牌改为只走请求头。** 统一使用 `Authorization: Bearer <token>`，不再接受 `?token=`。
- **HTTP 接口安全加固。** 令牌定长比较；日志脱敏 `token` 参数；响应转义控制字符；按来源 IP 限流与失败锁定；校验 `Host`/`Origin` 缓解 DNS rebinding；路径精确匹配；支持 `POST`。
- **新增 `replace_tools` 特性。** 非经验修补工具损坏后更换；经验修补工具在达到低耐久阈值时更换。
- **扩展 `replenish`。** `/fp drop` 与 `/fp dropstack` 丢空主手后自动补货；消耗奶桶/炖菜/蜂蜜瓶/药水后从返回的容器补回原物。
- 修复非英文区域（如土耳其语 `tr_TR`）下的指令补全：大小写转换现统一使用 `Locale.ROOT`。

## fp.build10 - 2026-09-12

### English

- **New: built-in HTTP admin API.** Optional remote management for web panels and automation: `GET /list`, `/spawn`, `/kick`, `/kill`, `/say`. Authenticated via `?token=` or `Authorization: Bearer`; each endpoint can be toggled independently under `http-admin.interface`; every call is logged with the token redacted.
- **Breaking: `/fp kill` now truly kills.** `/fp kill` / `/fp killall` go through the real death flow and follow `kick-on-dead`: by default the fake player is removed after death (no loot); with `kick-on-dead: false` the corpse remains and `/fp respawn` revives it. The old behavior is now `/fp kick` / `/fp kickall`.
- New permission node `fakeplayer.command.kick`; the `fakeplayer.spawn` permission group now includes `kick` instead of `kill` (the latter now controls the real kill commands).
- Fixed `/fp say` fallback when only a message is given (e.g. `/fp say once 1234`).

### 中文

- **新增：内置 HTTP 管理接口。** 可选的远程管理能力，适用于 Web 面板与自动化脚本：`GET /list`、`/spawn`、`/kick`、`/kill`、`/say`。通过 `?token=` 或 `Authorization: Bearer` 鉴权；各接口可在 `http-admin.interface` 下独立开关；所有调用均记录日志（token 已脱敏）。
- **破坏性变更：`/fp kill` 现为「真正杀死」。** `/fp kill` / `/fp killall` 走真实死亡流程并遵循 `kick-on-dead`：默认死亡后移除（不掉落）；`kick-on-dead: false` 时保留尸体，可用 `/fp respawn` 复活。原先「不造成死亡直接下线」的行为改为 `/fp kick` / `/fp kickall`。
- 新增权限节点 `fakeplayer.command.kick`；`fakeplayer.spawn` 权限组现包含 `kick` 而非 `kill`（`kill` 现在对应真杀死命令）。
- 修复 `/fp say` 仅给消息时的参数回退（如 `/fp say once 1234`）。

## fp.build9 - 2026-09-11

### English

- **Auto-login support.** Fake players can automatically complete registration and login after spawning; `/fp password` sets a login password (stored in the plugin database) and `/fp changepassword` changes it (old password optional).
- Random password: when enabled, a random password is generated and reported to the creator.
- New permissions: `fakeplayer.command.say`, `fakeplayer.command.password`, `fakeplayer.command.changepassword`.
- New config option `forced-execution` to run on unsupported server versions (ignores the NMSBridge version check).
- Fixed placeholders (`%fakeplayer_*%`) not resolving — identifier fixed to `fakeplayer` (upstream #172).

### 中文

- **支持自动登录。** 假人生成后可自动完成注册与登录；新增 `/fp password` 设置登录密码（存入插件数据库）、`/fp changepassword` 修改密码（旧密码可省略）。
- 随机密码：开启后自动生成随机密码并提示创建者。
- 新增权限：`fakeplayer.command.say`、`fakeplayer.command.password`、`fakeplayer.command.changepassword`。
- 新增配置项 `forced-execution`：可在不受支持的服务器版本上强制运行（忽略 NMSBridge 版本检查）。
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

### 中文

- **修复假人聊天失效。** `/fp say` 支持 `once / continuous / interval / stop` 子命令。
- 修复"幽灵假人"无法移除的问题。

## fp.build6 - 2026-08-13

### English

- **Fixed fake players could not be removed (Issue #3).** `/fp kill` / `/fp killall` now deregister the entity from the server instead of only clearing internal records; compatible with Leaf forks. Vanilla `/kick` does not affect fake players by design.
- Includes the build5 fix: resolved `ClassCastException` when spawning on Leaf 1.21.11 / 26.2.
- Note: vanilla `/kick` does not affect fake players by design (they are not real connections). Use `/fp kill` or `/fp killall` to remove them.

### 中文

- **修复假人无法移除（Issue #3）。** `/fp kill` 与 `/fp killall` 现会真正从服务器注销假人实体，而非仅清空内部记录；兼容 Leaf 分支。vanilla `/kick` 对假人无效为预期设计。
- 包含此前 build5 的修复：解决 Leaf 1.21.11 / 26.2 上生成假人的 `ClassCastException`。
- 说明：vanilla `/kick` 对假人无效为预期设计（假人并非真实连接），请使用 `/fp kill` 或 `/fp killall` 移除假人。

## fp.build5 - 2026-08-12

### English

- Upgraded to CommandAPI 12.0.0 and migrated off the removed `UTF8ResourceBundleControl` (now self-hosted), fixing `NoClassDefFoundError` on load.
- Resolved `ClassCastException` when spawning on Leaf 1.21.11 / 26.2 (`DedicatedPlayerList` cannot be cast to `MinecraftServer`).

### 中文

- 升级至 CommandAPI 12.0.0，并迁移掉已被移除的 `UTF8ResourceBundleControl`（改为内置实现），修复加载时的 `NoClassDefFoundError`。
- 解决 Leaf 1.21.11 / 26.2 上生成假人时的 `ClassCastException`（`DedicatedPlayerList` 无法转换为 `MinecraftServer`）。

## fp.build4 - 2026-08-10

### English

- Auto-repair for worn tools (replenish).
- Feature config changes now apply to online fake players immediately.
- Compatibility with Mohist: removed the dependency on Paper's `LookAnchor` (upstream #200).

### 中文

- 自动修复磨损工具（补货）。
- 修改特性配置时实时应用到已生成的在线假人。
- 兼容 Mohist：移除对 Paper `LookAnchor` 的依赖（上游 #200）。

## fp.build3 - 2026-08-10

### English

- **New `/fp say` command** — fake players can send chat messages (via NMS chat).
- Support for Minecraft 26.2; build upgraded to Java 25.
- Fixed NMS/plugin lifecycle stability and server-handle resolution; fixed the chat method signature mismatch.

### 中文

- **新增 `/fp say` 命令** —— 假人可发送聊天消息（基于 NMS chat 实现）。
- 适配 Minecraft 26.2，构建升级至 Java 25。
- 修复 NMS 与插件生命周期稳定性、server handle 解析；修复聊天方法签名不匹配。

## fp.build2 - 2026-07-28

### English

- Support for Minecraft 26.1 / 26.2 (new versioning scheme).

### 中文

- 支持 Minecraft 26.1 / 26.2（新版本号方案）。

## fp.build1 - 2026-06-19

### English

- First community edition release: Gradle multi-module architecture with a single universal jar for Minecraft 1.20.1+.

### 中文

- 首个社区版发布：Gradle 多模块架构，单一通用 Jar 支持 Minecraft 1.20.1+。
