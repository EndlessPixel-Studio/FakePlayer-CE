# Changelog

FakePlayer CE 版本更新日志。版本号 `fp.buildN` 与 git tag / GitHub Release 一一对应。
Changelog for FakePlayer CE. Version `fp.buildN` matches the git tag / GitHub Release.

## 编写规范 (Writing rules)

- 只记录影响用户 / 运行时行为的变更（新功能、修复、破坏性变更、配置或权限变化）。不写构建工具链、静态分析、死代码清理、重构等内部维护，也不写仓库文件（LICENSE、CI 脚本、README 章节）与发布渠道维护（顺带修复的功能缺陷只写修复本身）。
- 未发布的版本写作 `## fp.buildN - (Not published | 未发布)`：版本号可在开发期预置为下一版本，发布时把标题改成发布日期。
- 每条条目以加粗短语概括要点，中英文段落条目数量与顺序保持一致。

## fp.build23 - (Not published | 未发布)

### English

- **Fixed `/fp spawn` world names and permission errors (thanks @Boxendo, commit `eb443564`).** World arguments are now resolved by Bukkit world name (dimension keys still work), and missing `spawn.name` / `spawn.location` permissions produce explicit messages instead of a generic argument error.
- **`-a` now works on every name-based command (#123, #202).** Any command taking a fake-player name accepts `-a` to apply to all fake players you can see (OP/console: everyone; regular players: their own); only `select`, `spawn`/`spawnuuid` and `rename` are excluded. `kick`/`kill` keep their existing multi-target behavior.
- **Fixed `/fp selection` permission and world-only spawning (thanks @Boxendo, commit `c436b5cf`).** `/fp selection` required `fakeplayer.command.select` instead of its own node; `/fp spawn <name> <world>` silently ignored the world, and `-` now skips the name argument so players without `spawn.name` can still set a location.
- **Per-permission fake player limits (#187, thanks @kuxiaole; #64, thanks @Chaostarts).** `player-limit` stays the default; configure `spawn-limit` entries (e.g. `vip: 10`) and grant `fakeplayer.spawn.limit.vip` to raise the cap for specific players. Multiple matching permissions use the highest value; OPs are unaffected and dynamic TPS limits now respect the player's own cap. `config.yml` is now version 42.
- **AI chat can optionally use inventory context.** `ai-chat.yml` gains `inventory-context.fakeplayer` / `inventory-context.questioner`; together with the matching per-player switches (`aichat_fake_inventory`, `aichat_questioner_inventory`, via `/fp config set`) the item types and counts in the fake player's or asking player's inventory are added to the prompt. Both switches are off by default; `config.yml` is now version 41.
- **AI chat now supports multiple models.** `ai-chat.yml` can define named models with their own endpoint and key; `active-model` selects the default and OPs can switch at runtime with `/fp aichat model <name>` (the choice is saved). Existing configs keep working with a single implicit `default` model.
- **New `/fp spawnjson` for advanced spawning.** `/fp spawn` stays the simple entry point, while `/fp spawnuuid`
  is deprecated in favour of one JSON argument: `name`, `uuid`, `world`, `x/y/z`, `yaw/pitch` and
  `lifespan_seconds` can now be combined freely. Unknown fields are rejected so typos do not silently do
  nothing; specifying a UUID still needs `fakeplayer.command.spawn.uuid`. NBT is deliberately not included yet.
- **New `name-suffix` option (#144, thanks @fuzhulike).** Custom and generated fake-player names can now carry a
  suffix as well as a prefix. Generated names put the sequence number before the suffix (`Bot_1_bot`) and keep both
  decorations when the 16-character limit forces the middle part to be shortened. `config.yml` is now version 40.
- **Fixed fake players being removed twice on 1.21.x (thanks @NotCbop / Cbop and co-author Claude Fable 5, `6aaf570`).** On 1.21.11, `Player.kick()`
  already runs the full disconnect path for the fake connection, so the follow-up `PlayerList.remove` fired a
  second `PlayerQuitEvent`, threw `IllegalStateException: Already retired` and left half-removed ghost bots that
  `/fp kick` could never remove again. Removal now only runs when the player is still online after the kick;
  kick failures on ghosts are contained, and a missing creator no longer throws while dispatching quit commands.

### 中文

- **修复  权限与只给世界时的召唤（感谢 @Boxendo，commit ）。**  原先要求  而不是自己的权限节点； 会静默忽略世界，现在用  可跳过名字参数。
- **修复 `/fp spawn` 世界名与权限报错（感谢 @Boxendo，commit `eb443564`）。** 世界参数现在按 Bukkit 世界名解析（同时兼容维度 key）；缺少 `spawn.name` / `spawn.location` 权限时给出明确提示，不再是含糊的参数错误。
- **`-a` 支持所有名称类命令（#123、#202）。** 凡是假人名称参数都能用 `-a` 一次操作可见的全部假人（OP/控制台为全服，普通玩家仅限自己召唤的）；仅 `select`、`spawn`/`spawnuuid`、`rename` 除外。`kick`/`kill` 保持原有批量行为。
- **修复 `/fp selection` 权限与只给世界时的召唤（感谢 @Boxendo，commit `c436b5cf`）。** `/fp selection` 原先要求 `fakeplayer.command.select` 而不是自己的权限节点；`/fp spawn <名> <世界>` 会静默忽略世界，现在用 `-` 可跳过名字参数。
- **按权限设置假人数量上限（#187，感谢 @kuxiaole；#64，感谢 @Chaostarts）。** `player-limit` 仍是默认值；在 `spawn-limit` 中配置如 `vip: 10` 并授予 `fakeplayer.spawn.limit.vip` 即可提高指定玩家上限。命中多个权限时取最大值；OP 不受限制，动态 TPS 上限也会按玩家实际额度扣减。`config.yml` 版本提升至 42。
- **AI 聊天可选择性读取背包上下文。** `ai-chat.yml` 新增 `inventory-context.fakeplayer` / `inventory-context.questioner`，与玩家开关 `aichat_fake_inventory` / `aichat_questioner_inventory`（`/fp config set`）同时开启时，假人或提问玩家背包里的物品种类与数量会进入提示词。默认全部关闭，`config.yml` 版本提升至 41。
- **AI 聊天支持多模型。** `ai-chat.yml` 的 `models` 可配置多个具名模型（各自独立接口与密钥），`active-model` 指定默认，OP 可用 `/fp aichat model <名称>` 运行时切换并保存。旧配置仍按单个 `default` 模型工作。
- **新增 `/fp spawnjson` 处理高级召唤。** `/fp spawn` 保持基础入口，`/fp spawnuuid` 弃用，改由一个 JSON 参数承载
  `name`、`uuid`、`world`、`x/y/z`、`yaw/pitch`、`lifespan_seconds`，可自由组合。未知字段会直接报错，避免拼错后
  静默失效；指定 UUID 仍需要 `fakeplayer.command.spawn.uuid` 权限。NBT 暂不支持。
- **新增 `name-suffix` 名称后缀配置（#144，感谢 @fuzhulike）。** 自定义与自动生成的假人名都可以同时带前缀和后缀；
  自动生成的名称把序号放在后缀之前（`Bot_1_bot`），超过 16 字符时优先缩短中间部分，前后缀不会丢。
  `config.yml` 版本提升至 40。
- **修复 1.21.x 上假人被重复移除（感谢 @NotCbop / Cbop，共同作者 Claude Fable 5，commit `6aaf570`）。** 1.21.11 上 `Player.kick()` 已经替假人连接走完断线流程，
  紧接着的 `PlayerList.remove` 会二次触发 `PlayerQuitEvent`、抛出 `IllegalStateException: Already retired`，并留下
  `/fp kick` 再也删不掉的半移除幽灵假人。现在只有在 kick 之后玩家仍在线时才做兜底移除；幽灵假人 kick 失败会被兜住，
  创建者记录缺失时派发退出命令也不再抛异常。

## fp.build22
 - 2026-11-28

### English

- **Dynamic fake-player limits based on TPS (from FakePlayerPlus).** The old `kale-tps` threshold could only be
  turned on or off and removed every fake player above a limit that could fall to zero. The new `tps-adaptive`
  section (off by default) adds a configurable detection interval, threshold and minimum count: when the
  one-minute average TPS drops below the threshold, each player's limit drops by one per interval and the newest
  fake players above it are removed; when it recovers, the limit climbs back one per interval. Players are told
  what happened, and `/fp reload` picks up changes. Existing `kale-tps` values are still honoured as the threshold.
  `config.yml` is now version 39.
- **New `/fp update config`.** A newer build only warned that `config.yml` was out of date and left every new
  option to be copied by hand out of `config.tmpl.yml`. The command adds the options that exist in the
  template but are missing from the user's file, together with their comments: a missing top-level section
  is appended at the end, an option that belongs to a section the user already has is inserted in place, so
  the file is never rewritten as a whole. Existing values and comments are left untouched, the old file is
  saved as `config.yml.bak` first, options whose structure clashes with the template are reported instead of
  overwritten, and running the command a second time changes nothing.
- **`/fp version` shows the commit the jar was built from (#152).** Bug reports could not tell a released
  build and a CI test build apart, because the version number is preset before the release. The commit is
  injected at build time and printed as its own line.
- **Fixed inline styles breaking at links in AI replies (#154).** `MarkdownRenderer` cut a line at every
  `[label](url)` and styled each piece from a fresh bold / italic state, so `**bold [link] still bold**` lost the
  markup right after the link and the closing `*` leaked into chat. The state now carries across the whole line,
  links inherit the surrounding style, and a closing marker is consumed instead of starting a new run. Markup
  inside a link label stays inside the link, a plain-text URL is never parsed as markup, and a marker that
  directly follows a link still sees the character before it.
- **`/fp kick` can now remove a fake player the plugin has lost track of.** When the internal list no longer had a
  record for a fake player (a ghost left behind by a failed spawn), the cleanup path returned before calling
  `PlayerList.remove`, and a fake player network ignores `kick` — so the entity stayed online forever and kept its
  name, while the command reported success. The removal is now applied to the server list as well, limited to
  entities that still carry the fake player's metadata.
- **A failed `/fp rename ... force` no longer removes the fake player that held the name.** The occupying fake
  player was removed before the new name was validated, so a name that is too long, contains illegal characters
  or belongs to an online player deleted an unrelated fake player and then failed. Validation now runs first, and
  the occupant lookup uses the name with `name-prefix` applied, which never matched before.
- **Waiting on the main thread can no longer hang forever.** The blocking scheduling helpers parked until the
  target thread ran the task; if the plugin was disabled, the server was stopping, or the task was cancelled
  before it ran, the calling thread never woke up. The wait is now bounded and fails with a clear error.
- **Fixed the restore snapshot reading fake players off the main thread.** The 30-second snapshot task ran on an
  async thread and read entity positions, actions and features from there. It now only acts as a timer and hands
  the collection back to the main thread.
- **The HTTP admin API rejects `NaN` and `Infinity`.** `Double.parseDouble` accepts both, so
  `/teleport?x=NaN` produced a teleport to invalid coordinates instead of a 400.
- **Tab completion matches fake player names case-insensitively.** The fragment being typed was compared as-is,
  so `/fp kick AB` suggested nothing while `/fp kick ab` did.
- **`/fp update config` keeps the file's line endings.** The appended block always used the platform separator, so
  a `\n` config edited on Windows came back with a mix of both.
- **A failed AI chat request no longer silences a fake player permanently.** If building the request threw before
  the HTTP call, the fake player stayed marked as "request in flight" and every later reply was skipped.
- **Fixed Velocity forwarding going undetected when the server is started from another directory (#155).** Proxy
  detection read `config/paper-global.yml` and `paper.yml` through relative paths, which resolve against the JVM
  working directory rather than the server directory. Started from outside that directory (`java -jar
  /srv/mc/paper.jar`, a panel, or systemd without `WorkingDirectory`), the files were never found and Velocity was
  treated as absent, so `follow-quiting` removed a player's fake players as soon as they switched servers. The
  paths are now joined to the server directory.

### 中文

- **按 TPS 动态调整假人上限（移植自 FakePlayerPlus）。** 旧的 `kale-tps` 只能开关、可能把上限降到 0 并一次清掉所有超额假人。
  新的 `tps-adaptive` 段（默认关闭）把检测间隔、阈值和最低上限都做成可配置：最近一分钟平均 TPS 低于阈值时，每个周期把每位玩家的
  上限降 1，并移除最新召唤的超额假人；恢复后每周期升回 1。玩家会收到对应提示，`/fp reload` 会重新读取配置。旧的 `kale-tps`
  仍会被当作阈值兼容读取。`config.yml` 版本提升至 39。
- **新增 `/fp update config`。** 以前升级只提示 `config.yml` 版本过旧，新增的配置项仍要照着
  `config.tmpl.yml` 手动补。该命令把模板里有、用户配置里缺的项连同注释一起补进去：缺整个顶层节就追加到
  文件末尾，缺的是已有节里的子项就就地插入，不会把整个文件重新写一遍。已有取值与注释一律不动，改写前
  先备份成 `config.yml.bak`；与模板结构冲突的项只报告、不覆盖。第二次执行不会产生任何改动。
- **`/fp version` 会显示构建该 jar 的 commit（#152）。** 版本号在发布前就预置好了，正式版和 CI 测试包
  版本号可能一样，报问题时对不上代码；现在构建时把 commit 注入产物，`/fp version` 单独输出一行。
- **修复 AI 回复的行内样式在链接处断开（#154）。** `MarkdownRenderer` 每遇到一个 `[文字](地址)` 就把一行切开、
  各段从零开始算加粗 / 斜体状态，`**粗 [链接] 仍是粗**` 在链接之后就丢了样式，收尾的 `*` 还会漏进聊天栏。
  现在状态整行延续，链接本身也继承当前样式，收尾标记会被正常吃掉、不再当成新的起始标记。链接文字里的
  标记只作用于链接自己，纯文本模式的地址不会被当成标记解析，紧跟在链接后面的标记也仍然按前一个字符判断。
- **`/fp kick` 现在能移除插件已经丢失记录的假人。** 内部列表里没有该假人的记录时（例如召唤中途失败留下的幽灵），
  清理流程在调用 `PlayerList.remove` 之前就返回了，而假人网络又不吃 `kick`，于是这个实体一直在线、名字一直被占着，
  命令却返回成功。现在对仍然带有假人元数据的实体也会同步从服务端在线列表里摘掉。
- **`/fp rename ... force` 失败时不再顺手删掉占名的假人。** 之前先移除占用者、再校验新名称，名称超长、含非法字符
  或与在线玩家重名时，先被删的是那个无关的假人，然后命令才报错。现在先校验；判断占用者也改用带上 `name-prefix`
  的全名（之前配了前缀时永远匹配不到）。
- **主线程上的等待不会再永久挂起。** 阻塞式调度助手会一直 park 到目标线程执行完任务，插件停用、服务端关服或任务
  在执行前被取消时，调用线程再也醒不过来。现在等待有上限，超时会带着明确的错误失败。
- **修复恢复快照在非主线程读取假人。** 30 秒一次的快照任务跑在异步线程上，却在那里读实体的位置、动作与特性。
  现在它只当闹钟，采集交回主线程。
- **HTTP 管理接口拒绝 `NaN` 与 `Infinity`。** `Double.parseDouble` 两个都收，于是 `/teleport?x=NaN` 会把假人传到
  非法坐标，而不是返回 400。
- **TAB 补全对假人名大小写不敏感。** 之前拿用户敲的片段直接比较，`/fp kick AB` 补不出任何东西，`/fp kick ab` 才行。
- **`/fp update config` 保留原文件的换行符。** 之前追加的区块固定用平台换行符，在 Windows 上编辑过的 `\n` 配置文件
  会被混进 `\r\n`。
- **AI 聊天请求失败不再让假人永久闭嘴。** 如果在发出 HTTP 请求之前就抛了异常，这个假人会一直停留在「请求进行中」，
  之后的每一轮发言都被跳过。
- **修复从其他目录启动时 Velocity 转发漏判（#155）。** 代理检测用相对路径读 `config/paper-global.yml` 与
  `paper.yml`，而相对路径解析的是 JVM 的工作目录、不是服务端目录。从服务端目录之外启动时（`java -jar
  /srv/mc/paper.jar`、面板，或 systemd 没写 `WorkingDirectory`）读不到文件，Velocity 被当作未启用，
  `follow-quiting` 会在玩家跨服后直接清掉他名下的假人。现在路径拼在服务端目录上。

## fp.build21 - 2026-11-22

### English

- **Fixed the plugin refusing to enable with CommandAPI 12.1.0 (#150).** The optional `force` argument of
  `/fp rename` was created with a node name but no literal, which 12.1.0 rejects with
  `BadLiteralException` while registering commands — every `/fp` command was gone and the plugin was
  disabled at startup. The literal is passed properly now.
- **Update checks and downloads can skip certificate validation.** Some mirrors and proxies serve an
  incomplete chain, which made both fail with `PKIX path building failed` and left no way around it.
  `update-ignore-ssl-errors: true` skips CA and hostname checks for these requests only; it is off by
  default because it also removes the protection against a man-in-the-middle.
- **Updates can be downloaded automatically (opt-in).** With `update-download: true` the plugin fetches the
  new jar from the release page into `plugins/update`; Paper-family cores swap it in on the next restart, so
  updating becomes "download, restart". The periodic check does the same, so an idle server stays one restart
  away from the latest build. Off by default, and a warning is logged when the core does not process that
  folder. `config.yml` is now version 38.
- **AI chat replies no longer leak Markdown.** Models answer in Markdown and the chat box renders it literally, so `**`, `###` and fences showed up as noise. Replies are converted now: bold / italic / strikethrough / inline code get the matching styles, headings turn bold, list markers become `•`, code fences and horizontal rules are dropped, and links become clickable text (`文字 (地址)` in public chat, which only accepts a string). The private-message template also no longer lets model output inject MiniMessage tags.
- **Fake players can be hit and take damage.** They never send the "client loaded" packet a real client sends, and vanilla treats a player whose client has not loaded as invulnerable, so even with invulnerability off they could not be damaged. The fake connection now reports itself as loaded, on 26.1+ and, explicitly, on 1.21.9; 1.21.11 has no equivalent hook yet and is unchanged.
- **The HTTP admin API can serve cross-origin requests.** Off by default: with `http-admin.cors.enabled: false` no `Access-Control-*` header is sent and browsers block cross-origin calls exactly as before. Turning it on with a list of `allowed-origins` (or `*`) makes the API answer preflights and echo the origin, so a panel served from another host can call it; origins outside the list still get `403`.

### 中文

- **修复 CommandAPI 12.1.0 下插件无法启用（#150）。** `/fp rename` 的可选 `force` 参数只传了节点名、没有传字面量，
  12.1.0 在注册命令时直接抛 `BadLiteralException`，导致插件在启动阶段就被禁用、所有 `/fp` 命令消失。
  现在按正确方式传入字面量。
- **更新检查与下载可以跳过证书校验。** 部分镜像 / 代理的证书链不完整，检查与下载都会直接报
  `PKIX path building failed`，且没有绕开的办法。`update-ignore-ssl-errors: true` 只对这两个请求
  跳过 CA 与主机名校验，默认关闭——它同时去掉了对中间人攻击的防护。
- **更新包可以自动下载（默认关闭）。** 打开 `update-download` 后，插件会把新版 jar 下载到 `plugins/update`，
  Paper 系核心会在下次启动时自动替换，更新流程变成「下载 + 重启」。定时检查同样会下载，长期不重启的服务器
  也只是差一次重启。默认关闭；核心不处理该目录时会输出警告。`config.yml` 版本提升至 38。
- **AI 回复不再把 Markdown 原样吐进聊天栏。** 模型按 Markdown 回答，而聊天栏只当普通文字渲染，`**`、`###`、代码围栏全成了噪声。现在会做转换：加粗 / 斜体 / 删除线 / 行内代码映射成对应样式，标题转加粗，列表转 `•`，代码围栏与分隔线直接去掉，链接变成可点击文字（公共聊天只接受字符串，退化为「文字 (地址)」）。私聊模板也不再让模型输出注入 MiniMessage 标签。
- **假人现在可以被攻击、会掉血。** 假人不会发送真实客户端那个「已加载」数据包，而原版把客户端尚未加载的玩家当作无敌，所以即使关掉无敌也打不掉血。现在假人连接会主动报告已加载（26.1+ 与 1.21.9）；1.21.11 还没有对应的钩子，该版本行为不变。
- **HTTP 管理接口支持跨域请求。** 默认关闭：`http-admin.cors.enabled: false` 时不返回任何 `Access-Control-*` 响应头，浏览器照旧拦住跨域调用，行为与之前完全一致。开启并在 `allowed-origins` 里列出面板来源（或写 `*`）后，接口会应答预检请求并回显来源，从而允许部署在别的主机上的面板调用；不在名单里的来源仍返回 `403`。

## fp.build20 - 2026-11-20

### English

- **Fixed command help always using the server's language (#142, thanks @lldxlzy).** Command descriptions were translated once at startup with `i18n.locale`, so `/fp aichat help` and every other help page stayed in the server language even for a Chinese client. Descriptions are now sent as translatable text and follow the client locale (and the player's `/fp lang` choice).
- **Fixed the login-plugin exemption surviving a removed fake player (#143, thanks @lldxlzy).** When the creator had already been cleaned up, the after-quit step was skipped entirely, so an AuthMe / LibreLogin exemption could stay behind for the rest of the session. The cleanup now always runs.
- **New `/fp autosleep on|off [name]`.** A fake player with it enabled walks into a free bed on its own during the night instead of standing awake until morning; `/fp sleep` still sleeps once and `/fp wakeup` still wakes. Overworld only, and it needs an unoccupied bed within 4 blocks.
- **MySQL support, and a command to migrate existing data.** Setting `database.type: mysql` and filling in the new `database.mysql` block stores passwords, UUID mappings, skins and personal configs in MySQL instead of the local `data.db`; `/fp migrate-db` copies an existing SQLite file over once, so switching loses nothing. Changing the setting needs a restart; `config.yml` is now version 35.
- **The HTTP admin API moved to Undertow.** The JDK's built-in HTTP server cannot limit request sizes or idle connections, keeps no keep-alive budget and shuts down abruptly. The API now caps a request URI at 4 KB, reclaims idle connections after 30 seconds and stops gracefully on disable, with no config or endpoint change.
- **"Look at nearby entities" now looks at the nearest entity (upstream #129).** The distance tracker started at `0` and was only updated after the first candidate, so the comparison never matched and the fake player kept facing whichever entity the server listed first — frequently itself, since the area query is centred on its own position, which left it staring at a fixed angle. It now skips itself, measures from a real starting distance, and aims at a living target's eyes instead of its feet.
- **A fake player no longer breaks other plugins' plugin messages.** Our packet handler is invoked from inside `Player#sendPluginMessage`, so anything it threw aborted the caller's own task — PlayerPoints reverted point transactions exactly this way when `DiscardedPayload#data()` was missing on a non-Paper fork. Packet handling now contains its own failures and logs them at most once a minute per cause; a missing NMS method only disables that one feature.
- **Fake players can be renamed (#39, thanks @TWJohnJohn20116).** `/fp rename <name> <new-name> [force]` takes the fake player offline, moves its inventory, advancements, stats and skin to the new UUID, and spawns it again at the same spot with the same lifespan and creator — a name is bound to a UUID, so renaming is really a re-spawn. A name that is already in use is refused unless the operator has `fakeplayer.command.rename.force` and appends `force`, which hands the name (and its data) over.

### 中文

- **修复命令帮助的语言不跟随玩家（#142，感谢 @lldxlzy）。** 命令说明原先在启动时按 `i18n.locale` 一次性翻译成写死的文本，客户端是中文，`/fp aichat help` 等帮助页也只会显示服务器语言。现在这些说明按可翻译文本下发，跟随客户端语言（以及玩家的 `/fp lang` 设置）。
- **修复假人被移除后登录插件豁免残留（#143，感谢 @lldxlzy）。** 创建者已被先行清理时，退出后的收尾步骤会被整段跳过，AuthMe / LibreLogin 的豁免标记可能一直留到关服；现在清理一定会执行。
- **新增 `/fp autosleep on|off [假人]`。** 开启后假人夜里会自己找空床躺下，不再整夜站着；`/fp sleep` 仍是睡一次，`/fp wakeup` 仍是叫醒。仅主世界生效，且 4 格内要有没被占用的床。
- **支持 MySQL，并附带数据迁移命令。** 把 `database.type` 设为 `mysql` 并填好新增的 `database.mysql` 段后，密码、UUID 映射、皮肤与个人配置改存 MySQL（默认仍是插件目录下的 `data.db`）；`/fp migrate-db` 可把已有的 SQLite 数据一次性搬过去，换库不丢数据。改配置需要重启，`config.yml` 版本提升至 35。
- **HTTP 管理接口改用 Undertow。** JDK 自带的 HTTP 服务器无法限制请求体与空闲连接，也没有平滑关闭；现在单请求 URI 上限 4 KB、空闲连接 30 秒回收、插件停用/重启时优雅关闭。接口路径与配置项都没有变化。
- **「看向附近实体」现在真的会看向最近的那个（上游 #129）。** 距离变量的初始值是 `0` 且只在第一个候选之后才更新，比较永远不成立，假人实际一直盯着服务器先返回的那个实体 —— 而查询是以自身为圆心，第一个往往是它自己，于是表现为朝一个固定角度发呆。现在会排除自己、用正确的初始距离比较，并看向活体实体的眼睛而不是脚底。
- **假人不再打断其他插件的插件消息。** 我们的发包重写是在 `Player#sendPluginMessage` 的调用栈里跑的，一旦抛异常，调用方自己的任务就会中断 —— 在缺少 `DiscardedPayload#data()` 的非 Paper 系核心上，PlayerPoints 的点券交易正是这样被回滚的。现在发包逻辑自己兜住异常，同类错误每分钟最多记一条日志，缺 NMS 方法时只禁用受影响的那一个功能。
- **假人支持重命名（#39，感谢 @TWJohnJohn20116）。** `/fp rename <名称> <新名称> [force]` 会让假人下线，把背包、成就、统计与皮肤迁到新 UUID 下，再按原位置、原寿命、原创建者重新召唤 —— 名称与 UUID 绑定，所以重命名本质上是重新召唤。名称已被占用时默认拒绝；拥有 `fakeplayer.command.rename.force` 权限并追加 `force` 才能接管该名称（以及它名下的数据）。

## fp.build19 - 2026-11-14

### English

- **Better Velocity support.** Proxy detection now also looks at `proxies.velocity.enabled` in `config/paper-global.yml`, not just `settings.bungeecord` in `spigot.yml`. On a Velocity network the old check always failed, so `follow-quiting` deleted a player's fake players every time they switched backend servers. The FAQ now explains how to make the proxy's MOTD and player count include fake players (`ping-passthrough = "ALL"` in `velocity.toml`); the tab list needed no change.
- **New `/fp lang <code>` to switch your own display language.** Plugin messages normally follow the client language (or `i18n.locale` for the console); `/fp lang ja` forces one for you, `/fp lang reset` goes back, and bare `/fp lang` lists the current and available codes. Choices are saved to `plugins/FakePlayer-CE/lang.yml`, so they survive restarts.
- **`/fp set` and `/fp config set` now explain each feature in the completion menu.** The names you have to type (`pickup_items`, `invulnerable`, `collidable`, ...) are English-only; tab completion now shows the localized description for every feature as a tooltip, and only lists the ones you have permission to set.
- **Fixed EasyBot treating fake players as real players (#132, thanks @DZY-QAQ).** EasyBot detects fake players by looking up our name map with the original-case name; keys were stored lowercased, so the lookup always missed and the QQ whitelist applied to every fake player. The map is now case-insensitive, so EasyBot skips them in its login, join/leave, death and chat handling again.
- **Fake players now take knockback.** The knockback velocity was applied one tick after the packet arrived, by which point the server had already restored the entity's motion, so hits barely moved a fake player. It is now applied right before the fake player's next tick, like a client does after receiving the velocity packet.
- **A fake player you removed no longer comes back after a crash.** The restore snapshot was only written every 30 seconds, so one removed shortly before an unclean shutdown was spawned again on the next start. Removing a fake player now rewrites the snapshot immediately.
- **AI chat requests survive transient failures (#139, thanks @MC2S-enUS).** 429 and 5xx responses are retried up to twice with a short backoff, any `2xx` (including 201/206) counts as success, and a malformed or unexpected response body reports a readable `IOException` instead of an uncaught parse exception.

### 中文

- **完善 Velocity 支持。** 代理检测不再只认 `spigot.yml` 的 `settings.bungeecord`，也会读 `config/paper-global.yml` 的 `proxies.velocity.enabled`。在 Velocity 网络下旧判断永远为 false，于是 `follow-quiting` 会把「玩家跨服」当成「玩家下线」，每次都清掉他名下的假人。FAQ 补充了让代理的 MOTD 与在线人数包含假人的做法（`velocity.toml` 里 `ping-passthrough = "ALL"`）；tab 列表本来就不需要改。
- **新增 `/fp lang <代码>` 切换自己的显示语言。** 插件消息默认跟随客户端语言（控制台用 `i18n.locale`）；`/fp lang ja` 可强制指定，`/fp lang reset` 恢复默认，不带参数则列出当前与可用语言。设置保存在 `plugins/FakePlayer-CE/lang.yml`，重启后仍有效。
- **`/fp set` 与 `/fp config set` 的补全菜单现在会解释每个配置项。** 必须手打的配置名（`pickup_items`、`invulnerable`、`collidable` 等）只有英文，TAB 补全时为每一项附上对应语言的说明，并且只列出你有权限设置的项。
- **修复 EasyBot 把假人当成真实玩家（#132，感谢 @DZY-QAQ）。** EasyBot 用原始大小写的玩家名查我们的假人名单，而名单键存的是小写，永远查不到，于是 QQ 白名单对假人也生效。名单现在改为大小写不敏感，EasyBot 的登录、进出服、死亡与聊天处理重新跳过假人。
- **假人现在会正常被击退。** 击退速度原先在收到速度包后延迟一个 tick 才应用，那时服务端已经把实体的运动还原，打上去几乎不动；现在改为在假人下一次 tick 之前应用，与客户端收到速度包后的行为一致。
- **手动移除的假人不会再在崩溃后复活。** 恢复快照原先每 30 秒才写一次，若在写快照之前服务器异常关闭，刚移除的假人会在下次启动时被恢复；现在假人一离场就重写快照。
- **AI 聊天请求在瞬时故障下不再直接失败（#139，感谢 @MC2S-enUS）。** 429 与 5xx 会退避重试（最多两次、500ms 起步），201/206 等 2xx 同样视为成功；响应体畸形或结构不符时会报出可读的 `IOException`，不再是未捕获的解析异常。

## fp.build18 - 2026-11-06

### English

- **Fixed build17 failing to enable.** `/fp goto` gained a `stop` subcommand while keeping its optional `[name]`; CommandAPI prepends a parent command's arguments to its subcommands, so the optional argument ended up in front of a required literal and registration threw `OptionalArgumentException`, leaving `/fakeplayer` unregistered on every server. Cancelling a walk is now `/fp gotostop [name]`; `/fp goto <location> [name]` is unchanged.
- **Update checks now repeat on a schedule.** `check-for-updates` only ran at startup, so a server that stayed up for weeks never noticed a new build. `update-check-interval-hours` (default `24`, `0` = startup only) sets the interval; the same new version is announced once, not on every round. `config.yml` bumped to 34.

### 中文

- **修复 build17 无法启用。** `/fp goto` 在保留可选 `[name]` 的同时加了 `stop` 子命令；CommandAPI 会把父命令的参数拼到子命令字面量之前，可选参数后面跟着必选字面量，注册期直接抛 `OptionalArgumentException`，导致 `/fakeplayer` 在所有服务端上都注册失败。现在中止寻路改为 `/fp gotostop [假人]`，`/fp goto <坐标> [假人]` 不受影响。
- **更新检查改为周期执行。** `check-for-updates` 原先只在开服时跑一次，长期不重启的服务器永远看不到新版本。新增 `update-check-interval-hours`（默认 `24`，`0` 表示只在开服检查），同一个新版本只提示一次，不会每轮刷日志；修改间隔需重启生效。`config.yml` 版本提升至 34。

## fp.build17 - 2026-11-06

### English

- **New `/fp sysinfo`.** Writes a JSON snapshot of Java and JVM details (vendor, VM build, `-Xmx`/`-Xms` and GC flags, active collectors), CPU model and core count, JVM and physical memory, disk usage, TPS/MSPT, and every online fake player's state (world, position, running actions) to `plugins/FakePlayer-CE/sysinfo/<timestamp>.json`. Meant to be attached to a performance report instead of retyping six numbers.
- **New `/fp version`.** Prints the running build, the server implementation and version, and the Java version — the three things worth pasting into a bug report. No permission needed.
- **New `/fp update check`.** Prints the build you are running, whether a newer `fp.buildN` release exists on GitHub, and where to download it; `/fp update` does the same. Until now the check only ran at startup and logged to the console, so nobody in-game could see it.
- **Japanese localization.** Messages, command descriptions and help text now ship in Japanese; set `i18n.locale: ja` to use it. `config.yml` bumped to 33.
- **Documentation is multilingual too.** `README`, `BUILD`, `SECURITY`, `CONTRIBUTING` and `CODE_OF_CONDUCT` now all exist in English, Simplified Chinese, Traditional Chinese and Japanese; everything except the READMEs moved into `docs/`. The plugin is also listed on Hangar as another download channel.
- **New `/fp goto stop [name]`.** Cancels an ongoing walk without touching other actions; previously only `/fp stop` could end it, which also aborted everything else.
- **Fixed `/fp kick -a` and `/fp kill -a` doing nothing for operators.** The `-a` shortcut was filtering by creator even for ops and the console, so it only ever matched fake players that sender had spawned; it now means every fake player on the server, matching `/fp kickall`. A name that matches nothing also reports an error instead of falling back to the selected fake player.
- **Fixed LibreLogin authorization running off-thread.** The exemption was applied asynchronously after the fake player had already joined, which makes plugins that watch the login session (FastLogin and friends) see an unauthorized player; it now runs on the entity thread as part of the join.
- **Fake players can no longer be teleported elsewhere while spawning.** Other plugins handling join teleports (Essentials spawn, spawn hubs) could move a fake player away from the position the spawn flow computed; teleports outside the intended destination are refused for the short spawn window.
- **Fixed a typo in a fake player name acting on a different fake player.** When the name did not match, the command fell back to the selected (or only) fake player, so `/fp tp wrongname` could move someone else's fake player; it now reports that nothing matched.
- **Fixed HTTP admin endpoints touching fake players from the wrong thread.** Teleport, look, sneak, sprint, ping and the other per-fake-player writes ran on the HTTP executor thread (setping not even hopped off it), which violates Folia's region threading and races with the entity thread on Paper; they now run on the fake player's own entity thread.
- **`/fp say` help now matches every other command.** `/fp say help` prints the same paginated list as `/fp say` subcommands elsewhere, and the usage line spells out each subcommand's arguments (including `interval <ticks>`), so following the docs no longer sends the name where a tick count is expected.
- **Fixed messages being dropped when `/fp say once` is used repeatedly.** Once commands were queued as a single replaceable action, so two in the same tick left only the last one; each call now runs on its own.
- **Fixed `hasActiveAction` and `/fp stop` disagreeing about continuous actions.** Actions with `remains = -1` (run forever) were not counted as active, and the stop guard compared a ticker object against an `ActionSetting`, which never matched — so "is this fake player busy" and "stop it" told different stories.
- **HTTP `/spawn` now answers `504` instead of blocking.** A slow spawn held the request for the full 15-second timeout and then failed without a usable body; it now returns `Spawn is still in progress, check /list later` and lets the spawn continue in the background.
- **Password and TPS-protection messages are translatable.** Both were hardcoded, so `i18n.locale` had no effect on them.

### 中文

- **新增 `/fp sysinfo`。** 把 Java 与 JVM 信息（厂商、VM 版本、`-Xmx`/`-Xms` 与 GC 参数、实际使用的收集器）、CPU 型号与核心数、JVM 与物理内存、磁盘占用、TPS/MSPT、以及每个在线假人的状态（所在世界、坐标、正在执行的动作）写成 `plugins/FakePlayer-CE/sysinfo/<时间戳>.json`，直接附到性能问题里，不用再手抄一堆数字。
- **新增 `/fp version`。** 输出当前版本、服务端实现与版本、Java 版本——报错时最常要对照的三项，无需权限。
- **新增 `/fp update check`。** 输出当前版本、GitHub 上是否有更新的 `fp.buildN`，以及下载地址；`/fp update` 等价。此前只在开服时往控制台打日志，游戏内看不到。
- **新增日语本地化。** 消息、命令描述与帮助文本均已提供日语版本，把 `i18n.locale` 设为 `ja` 即可使用。`config.yml` 版本提升至 33。
- **文档也补齐了多语言。** `README`、`BUILD`、`SECURITY`、`CONTRIBUTING`、`CODE_OF_CONDUCT` 现在都有中英日繁四版，除 README 外的文件统一收进 `docs/`；插件也已上架 Hangar 作为另一个下载渠道。
- **新增 `/fp goto stop [假人]`。** 只中止正在进行的寻路，不影响其它动作；此前只能用 `/fp stop` 把该假人的所有动作一起停掉。
- **修复 `/fp kick -a` 与 `/fp kill -a` 对 op 无效。** `-a` 原先只按创建者过滤，即使 op / 控制台执行也只看自己召唤的假人；现在与 `/fp kickall` 一致，作用于全服假人。另外，名字匹配不到时改为直接报错，不再回退到选中的假人——避免打错名字误操作。
- **修复 LibreLogin 授权在异步线程执行。** 免登录原先在假人已进服之后异步授权，FastLogin 这类监听登录会话的插件会看到「未登录」的玩家；现在随进服流程在实体线程同步完成。
- **修复出生过程中假人被其它插件传走。** Essentials 的 spawn 处理、出生点插件等可能在假人刚进服时把它传送到别处，导致生成流程算出的落点失效；现在出生窗口内会拒绝目标以外的传送。
- **修复假人名字打错时会操作到另一个假人。** 名字匹配不到时原先会退回到「选中/唯一的假人」，例如 `/fp tp 打错的名字` 会传送别人家的假人；现在直接报「找不到匹配的假人」。
- **修复 HTTP 管理接口在错误线程操作假人。** 传送、朝向、潜行、疾跑、延迟等针对单个假人的写操作原先跑在 HTTP 线程（`/setping` 甚至完全没切线程），Folia 下违反区域线程模型、Paper 下与实体线程竞争；现在统一在假人所属实体线程执行。
- **`/fp say` 的帮助与其他命令统一。** `/fp say help` 现在输出与其他子命令一致的分页列表，用法行也逐一写清每个子命令的参数（含 `interval <ticks>`），照文档传参不会再出现「名字被当整数」的报错。
- **修复连续执行 `/fp say once` 丢消息。** once 原先作为可替换的单条动作排队，同一 tick 内连发两次只会执行最后一条；现在每次调用各自执行。
- **修复 `hasActiveAction` 与 `/fp stop` 对持续动作判断不一致。** `remains = -1`（一直执行）原先不计入活跃动作，而 `stop` 的守卫拿 ticker 对象去比 `ActionSetting`，永远不相等；两者叠加导致「假人是否在忙」和「能不能停」给出不同答案。
- **HTTP `/spawn` 超时改为返回 `504`，不再干等。** 生成较慢时请求原先会阻塞满 15 秒再失败，还没有可用响应；现在直接返回 `Spawn is still in progress, check /list later`，生成继续在后台进行。
- **密码与 TPS 保护的消息改为翻译键。** 这两处原先写死，改 `i18n.locale` 不生效。

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

- **Fixed fake players being unable to chat (Leaf 26.2 and later).** `ServerGamePacketListenerImpl#chat` pushes the message through the signed-chat validation chain, and a fake player has no session, so its null signature was silently dropped. Messages are now broadcast as an unsigned `PlayerChatMessage`, which bypasses the signature check.
- **`/fp say` became a proper action command.** It gained the same `once / continuous / interval / stop` modes as the other actions, a TAB-completable fake player name and a `<name> <msg>` argument layout; the text rides on a new `message` field of `ActionSetting`, and every version module handles the new `ActionType.SAY`.
- **Fixed "ghost" fake players that could not be removed.** They were still in the server's player list while the plugin had lost its own record.
- **Inherits the build6 fixes.** Real removal through the NMS player list, and compatibility with Leaf forks whose `CraftServer#getServer()` returns the player list.

### 中文

- **修复假人无法发言（Leaf 26.2 及以后）。** `ServerGamePacketListenerImpl#chat` 会走聊天签名校验链，而假人没有真实会话，传入的空签名被静默丢弃。现在改为直接广播未签名的 `PlayerChatMessage`，绕过签名校验。
- **`/fp say` 改为标准的动作命令。** 与其他动作一样支持 `once / continuous / interval / stop`，假人名可 TAB 补全，参数为 `<name> <msg>`；文本通过 `ActionSetting` 新增的 `message` 字段携带，各版本模块都补上了新的 `ActionType.SAY` 分支。
- **修复无法移除的「幽灵假人」。** 这类假人仍在服务端的玩家列表里，但插件内部已经丢了记录。
- **包含 build6 的修复。** 通过 NMS 玩家列表真正注销假人，并兼容 `CraftServer#getServer()` 返回玩家列表的 Leaf 分支。

## fp.build6 - 2026-08-13

### English

- **Fake players are really removed now (Issue #3).** `Player#kick` is a no-op for a fake player, because it never had a real connection; the old cleanup only cleared the plugin's own records and left the entity behind. It now calls the NMS `PlayerList.remove(ServerPlayer)`, so `/fp kill` and `/fp killall` actually deregister the entity and stop it from reappearing in `/list` and `/fp list`.
- **Compatible with Leaf forks.** Those return the player list from `CraftServer#getServer()`, which previously made the removal path fail.
- **Not a bug: vanilla `/kick` cannot affect fake players.** They do not occupy a real connection; use `/fp kill` or `/fp killall` instead.
- **Inherits the build5 fixes.** CommandAPI 12.0.0 support, and the `ClassCastException` that broke `/fp spawn` on Leaf 1.21.11 / 26.2.

### 中文

- **假人现在会被真正移除（Issue #3）。** 假人没有真实连接，`Player#kick` 对它是个空操作，旧逻辑只清了插件自己的记录，实体还留在服务端。现在改为调用 NMS 的 `PlayerList.remove(ServerPlayer)`，`/fp kill` 与 `/fp killall` 会真正注销实体，也不会再出现在 `/list`、`/fp list` 里。
- **兼容 Leaf 分支。** 这类服务端的 `CraftServer#getServer()` 返回的是玩家列表，此前会让移除流程直接失败。
- **不是 bug：原版 `/kick` 对假人无效。** 假人不占用真实连接，移除请用 `/fp kill` 或 `/fp killall`。
- **包含 build5 的修复。** CommandAPI 12.0.0 支持，以及 Leaf 1.21.11 / 26.2 上 `/fp spawn` 的 `ClassCastException`。

## fp.build5 - 2026-08-12

### English

- **CommandAPI 12.0.0 support.** Command arguments were migrated to the new construction signature, so the plugin loads on servers that ship CommandAPI 12.
- **Adventure is bundled now.** `adventure-api` and `adventure-text-minimessage` used to be `compileOnly`, so the plugin shared the server's copy; after adventure 4.17 removed `UTF8ResourceBundleControl` the devtools loader threw `NoClassDefFoundError` at startup on Leaf 26.2. Both are shaded now, with a self-contained UTF-8 `ResourceBundle.Control`.
- **Fixed `/fp spawn` throwing `ClassCastException` on Leaf 1.21.11 / 26.2.** `CraftServer#getServer()` returns a `DedicatedPlayerList` there instead of the `MinecraftServer`, so the server handle is now resolved through the typed `console` field and only falls back to `getServer()` when that field is absent.

### 中文

- **支持 CommandAPI 12.0.0。** 命令参数改用新的构造签名，装了 CommandAPI 12 的服务端也能正常加载。
- **adventure 改为内置。** `adventure-api` 与 `adventure-text-minimessage` 原本是 `compileOnly`，用的是服务端自带的副本；adventure 4.17 移除 `UTF8ResourceBundleControl` 之后，devtools 的加载器在 Leaf 26.2 上启动就抛 `NoClassDefFoundError`。现在两者都打进插件，并自带一个基于 JDK 的 UTF-8 `ResourceBundle.Control`。
- **修复 Leaf 1.21.11 / 26.2 上 `/fp spawn` 的 `ClassCastException`。** 这类服务端的 `CraftServer#getServer()` 返回 `DedicatedPlayerList` 而不是 `MinecraftServer`；现在改为按精确类型读 `console` 字段拿服务端句柄，只有该字段不存在时才回退到 `getServer()`。

## fp.build4 - 2026-08-10

### English

- **Feature changes apply to online fake players immediately.** `/fp config` used to only write the database, so `invulnerable false` looked broken until the fake player respawned (upstream #178, #181, #199). Saved changes are pushed to the creator's online fake players right away.
- **Tools are repaired during replenish (upstream #195).** Tools wear through durability loss, which fires no refill event, so a worn pickaxe was never replaced. A periodic check (`replenish.tools.durability-threshold`, wear percentage, off by default) repairs the held item.
- **Mohist compatibility (upstream #200).** The look/move code referenced `io.papermc.paper.entity.LookAnchor`, which Mohist remaps and then cannot resolve; angles are now computed with plain NMS maths, so the plugin loads without `io.papermc` classes.

### 中文

- **修改特性配置立即作用于在线假人。** `/fp config` 原先只写数据库，`invulnerable false` 要等假人重新生成才生效，看起来就是「关不掉无敌」（上游 #178、#181、#199）。现在保存后会立刻推给该创建者名下的在线假人。
- **补货会修复磨损的工具（上游 #195）。** 工具靠耐久磨损消耗，而磨损不触发任何补货事件，磨损的镐子永远不会被换。新增周期性检查（`replenish.tools.durability-threshold`，按磨损百分比，默认关闭），超过阈值就修好手中物品。
- **兼容 Mohist（上游 #200）。** 朝向/移动代码引用了 `io.papermc.paper.entity.LookAnchor`，Mohist 会重映射该类导致解析失败；现在改用纯 NMS 角度计算，加载时不依赖 `io.papermc` 的类。

## fp.build3 - 2026-08-10

### English

- **New `/fp say` command.** Fake players can send chat messages through NMS, i.e. they appear as a normal chat line from them.
- **Fixed the chat method signature.** The newer `chat` overload takes a `lastSeenMessages` argument, which this build was not passing, so sending a message threw `NoSuchMethodError` on newer servers.
- **Fixed crashes on `/reload` and plugin disable.** Commands are unregistered and resources released during shutdown, so the reload no longer ends in `IllegalPluginAccessException`.
- **Fixed NMS packet handling for 1.20.1 – 26.2.** `ClientboundRespawnPacket` (dimension change bookkeeping) and the entity motion packet were handled per version.
- **Fixed the BungeeCord plugin channel check.** The channel name is now validated the same way the server does, so forwarding works behind a proxy.

### 中文

- **新增 `/fp say` 命令。** 假人可以通过 NMS 发送聊天消息，看起来就是它本人说的一句话。
- **修复聊天方法签名不匹配。** 新版 `chat` 重载多了 `lastSeenMessages` 参数，这个版本没传，导致在新服务端上发消息直接 `NoSuchMethodError`。
- **修复 `/reload` 与停用插件时的崩溃。** 关停时会注销命令、释放资源，重载不再以 `IllegalPluginAccessException` 收场。
- **修复 1.20.1 – 26.2 的 NMS 数据包处理。** `ClientboundRespawnPacket`（跨维度状态）与实体运动包都按版本做了处理。
- **修复 BungeeCord 插件通道校验。** 通道名按服务端的方式规范化后再比较，代理网络下能正常转发。

## fp.build2 - 2026-07-28

### English

- **Minecraft 26.1 and 26.2 support.** The new versioning scheme gets its own modules (26.1 / 26.1.1 / 26.1.2 / 26.2) alongside 1.20.1 – 1.21.11, picked at runtime.
- **Java 25 is now required for 26.x.** Those server versions do not run on Java 21; the 1.20/1.21 modules keep working on Java 21+.

### 中文

- **支持 Minecraft 26.1 与 26.2。** 新的版本号方案有独立模块（26.1 / 26.1.1 / 26.1.2 / 26.2），与 1.20.1 – 1.21.11 并列，运行时按服务端版本挑选。
- **26.x 需要 Java 25。** 这些服务端版本无法在 Java 21 上运行；1.20 / 1.21 的模块仍然只要 Java 21+。

## fp.build1 - 2026-06-19

### English

- **First community edition release.** The abandoned upstream project was rebuilt on Gradle Kotlin DSL and Paperweight userdev with Mojang mappings, so a single universal jar covers Minecraft 1.20.1 – 1.21.11 and stays around 500 KB — no dependency conflicts with the server or with other plugins.
- **One NMS module per Minecraft version.** The right bridge is chosen at runtime from the server version, instead of patching a single class set for every release.
- **Fixed on the way:** fake player inventories failing to load, `/openinv` compatibility, untranslated placeholders, `%fakeplayer_actions%` resolution, commands that ran after a fake player had already quit, and the NMS mappings that broke between 1.21.9 and 1.21.11.
- **Requires Java 21+**, Paper 1.20.1+ (Purpur and Pufferfish compatible) and CommandAPI 11.1.0+.

### 中文

- **首个社区版发布。** 弃坑的上游项目改用 Gradle Kotlin DSL 与 Paperweight userdev（Mojang 映射）重建，一个通用 jar 覆盖 Minecraft 1.20.1 – 1.21.11，体积约 500 KB，不会和服务端或其他插件抢依赖。
- **每个 Minecraft 版本一个 NMS 模块。** 运行时按服务端版本挑选对应的桥接实现，而不是让一套类同时兼容所有版本。
- **顺带修复：** 假人背包加载失败、`/openinv` 兼容性、未翻译的占位符、`%fakeplayer_actions%` 解析、假人退出后仍在执行的命令，以及 1.21.9 – 1.21.11 之间断裂的 NMS 映射。
- **需要 Java 21+**、Paper 1.20.1+（兼容 Purpur 与 Pufferfish）以及 CommandAPI 11.1.0+。
