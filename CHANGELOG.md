# Changelog

FakePlayer CE 版本更新日志。版本号 `fp.buildN` 与 git tag / GitHub Release 一一对应。
Changelog for FakePlayer CE. Version `fp.buildN` matches the git tag / GitHub Release.

## fp.build11 - Not Published

### English

- **New command `/fp enderchest`** (alias `/fp ec`): open the ender chest of a fake player. It follows the same rules as `/fp invsee` — the viewer must be in the same world, and OP / the creator without the permission is still allowed unless `allow-non-op-open-inv` denies it. Shift-right-clicking a fake player opens its ender chest as well (a plain right-click still opens the inventory). New permission node `fakeplayer.command.enderchest`, already included in the `fakeplayer.spawn` permission group.
- **More PlaceholderAPI placeholders.** Added `%fakeplayer_list%` (names of all online fake players, joined by the `fakeplayer.placeholder.separator` translation), `%fakeplayer_list_<index>%` and `%fakeplayer_list_<index>_<attribute>%`, `%fakeplayer_isfake%`, plus the fake player attributes `%fakeplayer_name%`, `%fakeplayer_uuid%`, `%fakeplayer_spawner%` (alias of `creator`), `%fakeplayer_spawntime%` (formatted with `fakeplayer.placeholder.time-format`), `%fakeplayer_world%`, `%fakeplayer_x%` / `y` / `z`, `%fakeplayer_health%`, `%fakeplayer_food%`, `%fakeplayer_level%`, `%fakeplayer_gamemode%` and `%fakeplayer_actions_translated%`.
- **HTTP admin API expanded from 5 to 18 endpoints.** New query endpoints: `GET /status` (detailed fake player state — position, rotation, health, food, exp, game mode, creator, held items, hotbar slot and the actions currently running; omit `name` to get every fake player) and `GET /info` (plugin, Minecraft and server version, fake player / online player counts and limits). New control endpoints: `GET /action` (drive any action with `count`, `interval`, `wait` and `message`), `GET /stop`, `GET /teleport`, `GET /look` (`direction`, `at=x,y,z` or `yaw`+`pitch`), `GET /hold`, `GET /swap`, `GET /respawn`, `GET /cmd`, plus the batch endpoints `GET /kickall`, `GET /killall` and `GET /sayall`. Batch endpoints return `{"status":"success","count":n}`.
- Every new endpoint has its own switch under `http-admin.interface` (`status`, `info`, `action`, `stop`, `teleport`, `look`, `hold`, `swap`, `respawn`, `cmd`, `batch`), all defaulting to `true`. `config.yml` version bumped to 20; existing configs keep working and fall back to the defaults for the new keys.
- Parameters are validated and reported with proper status codes: unknown action / direction / world, malformed `at`, non-numeric coordinates, out-of-range slot, `count < -1`, respawning a living fake player and missing required parameters all return `400` with a descriptive message (`401` for a bad token, `403` for a disabled endpoint, `405` for non-GET requests).
- **Security hardening of the HTTP API.** The token is now compared in constant time, and `Authorization: Bearer` is accepted case-insensitively. Log lines redact any parameter whose name contains `token` (regardless of casing) and strip control characters plus cap the length, so an unauthenticated caller can no longer forge or flood the server log with a crafted query string. JSON output escapes every control character, so responses are always valid JSON. Requests are handled by a small daemon thread pool instead of the default single dispatcher thread, so one slow call no longer stalls the rest.
- **Breaking: the HTTP token is now header-only.** `Authorization: Bearer <token>` is the single way to authenticate — `?token=` is no longer accepted, so the token can no longer leak through URLs, proxy logs, browser history or `Referer` headers, and no endpoint has to document a token parameter.
- Further HTTP API hardening. Requests are routed by exact path, so `/listfoo` returns `404` instead of falling through to `/list` — this also stops an external ACL/WAF that filters by exact path from being bypassed. Authentication now runs before the per-endpoint switch, so an unauthenticated caller can no longer tell which endpoints are enabled. Requests are rate limited per source IP (`requests-per-minute`, default 120) and an IP is locked (`lockout-seconds`, default 60) after `auth-failures` (default 10) consecutive failures. `allowed-hosts` validates the `Host` header, and cross-origin browser requests (those carrying an `Origin`) are rejected unless they match, which mitigates DNS rebinding. `POST` is supported everywhere so the token can travel in the `Authorization` header, and `allow-get: false` restricts the API to POST only. Request URIs longer than 4096 characters are rejected with `414`, responses carry `Cache-Control: no-store`, and the request handler now uses a bounded queue with backpressure instead of an unbounded one.
- Added an independent GCA-style `replace_tools` feature: non-Mending tools are replaced after breaking, while Mending tools are replaced at the configured low-durability threshold. Same-type non-Mending tools are eligible at any durability; Mending replacements must remain above the threshold. The first eligible inventory item is used.
- Broadened `replenish`: `/fp drop` and `/fp dropstack` refill the main hand after its last item is dropped; consumed milk buckets, stews, honey bottles, and drinkable potions can be refilled while their returned bucket, bowl, or bottle is stored in inventory. If there is no matching refill item or no room for the remainder, it stays in hand.

### 中文

- **新增指令 `/fp enderchest`**（别名 `/fp ec`）：打开假人的末影箱，判定规则与 `/fp invsee` 完全一致 —— 查看者需与假人处于同一世界；OP 或假人的创建者即使没有权限也允许打开（除非关闭 `allow-non-op-open-inv`）。潜行右键假人也可直接打开末影箱（普通右键仍是打开背包）。新增权限节点 `fakeplayer.command.enderchest`，已纳入 `fakeplayer.spawn` 权限组。
- **扩充 PlaceholderAPI 占位符。** 新增 `%fakeplayer_list%`（所有在线假人名称，分隔符取自 `fakeplayer.placeholder.separator`）、`%fakeplayer_list_<序号>%` 与 `%fakeplayer_list_<序号>_<属性>%`、`%fakeplayer_isfake%`，以及假人属性 `%fakeplayer_name%`、`%fakeplayer_uuid%`、`%fakeplayer_spawner%`（`creator` 的别名）、`%fakeplayer_spawntime%`（格式取自 `fakeplayer.placeholder.time-format`）、`%fakeplayer_world%`、`%fakeplayer_x%` / `y` / `z`、`%fakeplayer_health%`、`%fakeplayer_food%`、`%fakeplayer_level%`、`%fakeplayer_gamemode%` 和 `%fakeplayer_actions_translated%`。
- **HTTP 管理接口从 5 个端点扩展到 18 个。** 新增查询类接口：`GET /status`（假人详细状态：坐标、朝向、血量、饥饿、经验、游戏模式、创建者、手持物品、主手槽位以及正在进行的动作；省略 `name` 时返回全部假人）、`GET /info`（插件、Minecraft 与服务端版本、假人在线数与各项上限）。新增控制类接口：`GET /action`（通过 `count`、`interval`、`wait`、`message` 驱动任意动作）、`GET /stop`、`GET /teleport`、`GET /look`（`direction`、`at=x,y,z` 或 `yaw`+`pitch` 三种写法）、`GET /hold`、`GET /swap`、`GET /respawn`、`GET /cmd`，以及批量接口 `GET /kickall`、`GET /killall`、`GET /sayall`；批量接口返回 `{"status":"success","count":n}`。
- 每个新接口都在 `http-admin.interface` 下拥有独立开关（`status`、`info`、`action`、`stop`、`teleport`、`look`、`hold`、`swap`、`respawn`、`cmd`、`batch`），默认均为 `true`。`config.yml` 版本号提升至 20；旧配置可继续使用，新配置项自动采用默认值。
- 参数会经过校验并返回恰当的响应码：未知动作 / 方向 / 世界、`at` 格式错误、坐标非数字、槽位越界、`count < -1`、对存活假人执行重生以及缺少必填参数均返回 `400` 并附带具体提示（令牌错误 `401`、接口关闭 `403`、非 GET 请求 `405`）。
- **HTTP 接口安全加固。** 令牌改为定长比较，`Authorization: Bearer` 不再区分大小写；日志会脱敏任何名称含 `token` 的参数（不区分大小写），并替换控制字符、限制长度，未授权请求无法再利用构造的 query 伪造或撑爆服务端日志；JSON 输出转义全部控制字符，保证响应始终是合法 JSON；请求改由小型 daemon 线程池处理，替代默认的单线程 dispatcher，单个慢请求不会再阻塞其余请求。
- **破坏性变更：HTTP 令牌改为只走请求头。** 鉴权统一使用 `Authorization: Bearer <token>`，不再接受 `?token=` 查询参数 —— 令牌不会出现在 URL、代理日志、浏览器历史与 `Referer` 中，接口文档也不必再逐个重复令牌参数。
- 进一步加固 HTTP 接口。路径改为精确匹配，`/listfoo` 返回 `404` 而不会落到 `/list`，同时也避免绕过按精确路径放行的外部 ACL / WAF；鉴权改为在接口开关之前执行，未授权调用方无法再判断哪些接口被启用。新增按来源 IP 的限流（`requests-per-minute`，默认 120）与失败锁定（连续 `auth-failures` 次，默认 10 次后锁定 `lockout-seconds` 秒，默认 60）；`allowed-hosts` 用于校验 `Host`，并拒绝主机不符的浏览器跨源请求（带 `Origin`），可缓解 DNS rebinding。所有接口现在都支持 `POST`，便于把 token 放进 `Authorization` 请求头；`allow-get: false` 可只允许 POST。请求 URI 超过 4096 字符直接返回 `414`；响应带 `Cache-Control: no-store`；请求处理改为有界队列 + 反压，不再使用无界队列。
- 新增独立的 GCA 风格 `replace_tools` 特性：非经验修补工具损坏后才更换；经验修补工具在达到配置的低耐久阈值时更换。同类型非经验修补工具无论耐久多少都可作为候选；经验修补候选的剩余耐久必须高于阈值，并按背包顺序选择第一个符合条件的工具。
- 扩展 `replenish`：`/fp drop` 与 `/fp dropstack` 丢掉主手最后一个物品后会自动补货；消耗奶桶、炖菜、蜂蜜瓶和可饮用药水后，可将返回的空桶、碗或玻璃瓶放入背包并补回原物。没有同款补货物或背包没有余物空间时，返回容器会留在手上。

## fp.build10 - 2026-09-12

### English

- **New: built-in HTTP admin API.** Optional remote management for web panels and automation: `GET /list`, `/spawn`, `/kick`, `/kill`, `/say`. Authenticated via `?token=` or `Authorization: Bearer`; each endpoint can be toggled independently under `http-admin.interface`; every call is logged with the token redacted.
- **Breaking: `/fp kill` now truly kills.** `/fp kill` / `/fp killall` go through the real death flow (`setHealth(0)`), following the `kick-on-dead` config: by default the fake player is removed after death (death event cancelled, no loot); with `kick-on-dead: false` the corpse remains and can be revived via `/fp respawn`, with normal loot rules applied. The previous "remove without death" behavior is now `/fp kick` (single) / `/fp kickall` (all).
- New permission node `fakeplayer.command.kick`; the `fakeplayer.spawn` permission group now includes `kick` instead of `kill` (the latter now controls the real kill commands).
- Fixed `/fp say` fallback when only a message is given (e.g. `/fp say once 1234`).
- Build: version modules consolidated under `versions/` (repository layout only, no runtime change).

### 中文

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
