# 命令完全手册

本页覆盖根命令 `fakeplayer`（别名 `fp`）的全部子命令、参数、权限和边界行为。输入 `/fp help` 或 `/fp ?` 可以在游戏内查看自动生成的帮助；`/fp help <子命令>` 查看具体子命令。

## 1. 通用规则

### 1.1 权限与可见范围

- OP 和控制台可以按名字操作全服任何假人；
- 普通玩家只能按名字操作自己创建的假人；
- 省略名字时按「选中假人 → 唯一假人」回退；
- 显式写错名字不会回退，直接报错。

聚合权限组：

| 权限组 | 包含 |
|---|---|
| `fakeplayer.spawn` | spawn / kick / list / distance / select / selection / drop / dropstack / dropinv / skin / invsee / enderchest / status / respawn / config / set / setping |
| `fakeplayer.tp` | tp / tphere / tps |
| `fakeplayer.exp` | expme |
| `fakeplayer.action` | attack / mine / use / jump / sneak / sprint / look / turn / move / goto / ride / swap / sleep / wakeup / autosleep / hold / stop，以及部分 config 权限 |
| `fakeplayer.*` | 上述四组 + cmd / say / password / changepassword + spawn.name.force-real |

::: warning
`fakeplayer.*` **不包含** OP 级命令：`kickall`、`killall`、`reload`、`reload-translation`、`migrate-db`、`update`、`sysinfo`、`debug`、`aichat model`。它们用的是 `CommandPermission.OP`。
:::

`version` 和 `lang` 无需任何权限。

### 1.2 目标选择优先级

1. 命令显式给了名字；
2. `/fp select` 选中的假人；
3. 发送者只有 1 个假人时自动使用；
4. 否则报错。

`kick` / `kill` 支持 `[names]` 多个目标；未给时同样按「选中 → 唯一」回退。

### 1.3 `-a` 批量目标

任何以假人名为参数的命令都支持 `-a`：

- OP / 控制台：全服假人；
- 普通玩家：自己创建的假人。

不支持：`select`、`spawn`、`spawnuuid`、`rename`。

### 1.4 守卫函数

| 守卫 | 条件 |
|---|---|
| `hasFakeplayer` | OP 或该发送者至少有一个假人 |
| `hasFakeplayerForRespawn` | `kick-on-dead: false` 且 `hasFakeplayer` |
| `needSelect` | OP，或该发送者上限 >1 且已有假人 |
| `isCmdAvailable` | 有 `fakeplayer.command.cmd` 且 `hasFakeplayer` |

默认配置下 `/fp respawn` 不可用，`/fp select` 对普通玩家不可用。

### 1.5 自动帮助

含有子命令的节点会自动获得 `help` 子命令，别名 `?`：

```text
/fp help
/fp help say
/fp ? 2 20
```

第二个参数是页码，第三个是每页条数。`config` 命令使用手写帮助文案。

## 2. 生成与移除

### `/fp spawn [name] [world] [location]`

权限 `fakeplayer.command.spawn`；world / location 额外需要 `fakeplayer.command.spawn.location`，自定义名字需要 `fakeplayer.command.spawn.name`。

- `name` 为空或 `-` 时视为未指定；`-` 是给只有 location 权限的玩家跳过名字的转义；
- 给了世界和坐标就组合；只给世界用该世界出生点；都不给时用玩家自己位置（控制台用第一个世界出生点）；
- 寿命取 `lifespan`，默认 0 表示永久；
- 成功后若这是玩家第一个假人，会刷新命令可见性；
- 名称冲突、已达上限、生成失败分别有对应错误提示。

```text
/fp spawn
/fp spawn Bot_1
/fp spawn Bot_1 world 100 64 -50
```

### `/fp spawnjson <json>`

权限 `fakeplayer.command.spawn.json`（默认 OP）。JSON 最大 4096 字符，允许字段：

```text
name, uuid, world, x, y, z, yaw, pitch, lifespan_seconds
```

规则：

- 未知字段一律拒绝；
- `x/y/z` 必须同时提供；
- `lifespan_seconds` 必须是 >0 的有限数；
- `uuid` 必须和 `name` 同时给，且需 `fakeplayer.command.spawn.uuid`；
- 世界用 `Bukkit.getWorld(name)`，不通维度 key；
- 不支持 NBT。

```text
/fp spawnjson {"name":"Bot_1","world":"world","x":0,"y":64,"z":0,"lifespan_seconds":3600}
```

### `/fp spawnuuid <name> <uuid> [world] [location]`

已弃用。权限 `fakeplayer.command.spawn.uuid`（默认 OP）+ `spawn.name`。UUID 格式错误会直接提示并返回。

### `/fp rename <name> <new-name> [force]`

权限 `fakeplayer.command.rename`；`force` 需要 `fakeplayer.command.rename.force`（默认 OP）。

- 异步重建假人；
- 新名为空或空白时静默返回；
- 若被重命名的是当前选中假人，重建后重新选中；
- 成功后刷新命令并保存快照，防止崩溃恢复出旧名字。

### `/fp kick [names]` 与 `/fp kill [names]`

| 命令 | 行为 | 是否死亡 |
|---|---|---|
| `/fp kick` | 从服务端安全移除 | 否 |
| `/fp kill` | `setHealth(0)` | 是，可能掉落物品 |

`kickall` / `killall` 是全服版本，OP 权限且无成功反馈。

## 3. 查看与状态

### `/fp list [page] [size]`

- OP 看全服，普通玩家看自己；
- `page` 默认 1，`size` 默认 10；
- 每行显示名字、创建者、世界和坐标；
- 玩家且拥有 `tp` 权限时带传送按钮；踢出按钮始终显示，点击后再判权。

### `/fp status [name]`

显示生命、饥饿、经验、游戏模式、特性和动作。生命按当前/最大比例着色。经验行需要有 `expme` 权限才显示。特性行里的值可点击执行 `/fp set <feature> <option> <假人>`。

### `/fp distance [name]`

仅玩家。比较方块坐标与直线距离，0.5 精度。不同世界报「太远」。

### `/fp selection` / `/fp select [name]`

`selection` 显示当前选中假人，`select` 设置选中假人。默认受 `needSelect` 限制。

### `/fp expme [name]`

仅玩家。把假人全部经验取走给自己。经验为 0 时提示无经验。

### `/fp setping <ping> [name]`

修改 Tab 列表显示的延迟。值必须 ≥0。找不到 Tab 列表项时静默返回。

### `/fp sysinfo`

OP 权限。在 `plugins/FakePlayer-CE/sysinfo/` 下生成 JSON，包含 CPU、内存、磁盘、TPS 和假人状态，并附带相关 JVM 启动参数。性能问题反馈时应附上这个文件。

### `/fp version`

无权限要求。输出插件版本、commit、服务端版本和 Java 版本，最后提示 `/fp update check`。

## 4. 物品、背包与皮肤

### `/fp invsee [name]` 与 `/fp enderchest [name]`（别名 `/fp ec`）

- 仅玩家；
- 必须与假人同世界；
- 权限检查在 `InvseeManager` 中：非 OP、非创建者且 `allow-non-op-open-inv: false` 时拒绝；
- `invsee-implement: AUTO` 时自动检测 OpenInv；`SIMPLE` 不能操作装备栏。

### `/fp hold <slot> [name]`

`slot` 是 1–9 的快捷栏格子，内部转成 0 基索引。

### `/fp swap [name]`

交换主手与副手物品，无成功提示。

### `/fp skin <player> [name]`

权限 `fakeplayer.command.skin`。设置假人皮肤；安装 SkinsRestorer 时可使用正版名。

## 5. 姿态、移动与寻路

### `/fp look <方向|at|me|entity> [name]`

| 子命令 | 说明 |
|---|---|
| `north/south/east/west/up/down` | 设定固定朝向 |
| `at <location>` | 看向坐标 |
| `me` | 看向自己，仅同世界有效 |
| `entity once/continuous/interval/stop` | 看向最近实体，走动作系统 |

方向对应 yaw/pitch：N=180/0，S=0/0，E=-90/0，W=90/0，UP=当前 yaw/-90，DOWN=当前 yaw/90。

### `/fp turn <left|right|back|to> [rotation] [name]`

`left/right/back` 是相对当前朝向旋转；`to` 设置绝对 yaw/pitch。

### `/fp move [forward|backward|left|right] [name]`

- 无子命令等于 `forward`；
- 潜行时速度降为 0.3；
- 20 tick（或疾跑时 40 tick）后自动归零；重复执行会取消上一个停止任务。

### `/fp goto <location> [name]` 与 `/fp gotostop [name]`

- 最大距离 1024 格，防误操作；
- 目标世界强制为假人当前世界；
- 依次校验高度、世界边界、距离、落脚点；
- 设置持续 GOTO 动作；`/fp gotostop` 只停 GOTO。

`gotostop` 必须独立成命令，不能做 `goto` 的子命令，否则 CommandAPI 注册期会抛 `OptionalArgumentException`。

### `/fp tp [name]`、`/fp tphere [name]`、`/fp tps [name]`

仅玩家。`tp` 传送到假人，`tphere` 把假人传到自己，`tps` 交换位置。

### `/fp ride <me|target|anything|vehicle|entity|stop> [name]`

| 模式 | 范围与限制 |
|---|---|
| `me` | 同世界且 ≤20 格 |
| `target` | 准星指向 5 格内实体 |
| `anything` | 4.5³ 范围内任意非自身实体 |
| `vehicle` | 矿车、船、马等可骑乘实体 |
| `entity <entity>` | 同世界且 ≤24 格，不能是自己 |
| `stop` | 停止骑乘 |

多数失败分支静默返回，没有消息。

## 6. 动作类命令

以下命令结构完全一致：

```text
/fp <action>                # 一次
/fp <action> once           # 一次
/fp <action> continuous     # 每 tick
/fp <action> interval <ticks> # 每 ticks tick
/fp <action> stop           # 停止
```

适用动作：

| 命令 | 动作 |
|---|---|
| `/fp attack` | 攻击 |
| `/fp mine` | 挖掘 |
| `/fp use` | 使用/交互 |
| `/fp jump` | 跳跃 |
| `/fp drop` | 掉落单个物品 |
| `/fp dropstack` | 掉落一组 |
| `/fp dropinv` | 掉落整包 |

`USE` 且主手是钓鱼竿且开启自动钓鱼时会强制降级为一次，避免冲突。玩家执行 `once` 时没有成功提示。

## 7. 睡眠与自动钓鱼

| 命令 | 说明 |
|---|---|
| `/fp sleep [name]` | 在 4 格内找未被占用的床头，找不到静默返回 |
| `/fp wakeup [name]` | 仅在睡眠中生效 |
| `/fp autosleep on|off [name]` | 开启/关闭自动睡觉 |
| `/fp autofish` | 通过 `/fp config set autofish true` 控制，自动钓鱼 |

## 8. 聊天与 AI

### `/fp say [once|continuous|interval|stop]`

- `name` 是字符串参数，这是为了兼容「只给一条消息」的写法；
- `message` 为空时，唯一参数被当作消息，目标按选中/唯一假人回退；
- `once` 不走 ticker，直接调度一次，避免同 tick 覆盖丢消息；
- 私聊与公共聊天都触发聊天事件；假人自己的发言不会触发 AI 自动接话。

```text
/fp say once 你好
/fp say interval 20 Bot_1 你好
```

### `/fp aichat <public|private> [once|continuous|interval]`

前置检查顺序：

1. `ai-chat.yml` 的 `enabled`；
2. 当前模型和 API key；
3. `continuous` / `interval` 还需要 `allow-continuous: true`。

`public` 发公共频道；`private <target>` 发给指定在线玩家；`interval` 最小 20 tick。

```text
/fp aichat public once
/fp aichat private Steve continuous
/fp aichat stop Bot_1
/fp aichat model          # OP：查看当前/可用模型
/fp aichat model ollama   # OP：切换模型并写回 ai-chat.yml
```

### `/fp cmd <name> <command>`

权限 `fakeplayer.command.cmd`，以假人身份执行控制台命令：

- 非 OP 禁止让假人执行 `fakeplayer` / `fp` 命令；
- 假人自身必须对该命令有权限；
- 成功会写服务器日志；
- 不要把假人设为 OP。

## 9. 登录与密码

### `/fp password <name> <password>`

保存密码到插件数据库。密码为空会报错。随机密码生成由 `auto-login.random-password` 控制。

### `/fp changepassword <name> <new> [old]`

- 新密码不能为空；
- 从没设置过密码会报错；
- 省略旧密码时使用数据库已有密码，方便重置忘记的随机密码；
- 成功后按 `change-password` 模板执行命令，同步到登录插件。

## 10. 配置命令

### `/fp config`

无参列出当前配置；`help` 显示帮助。

```text
/fp config list [player]
/fp config set <feature> <value> [player]
```

- 玩家默认改自己；OP / 控制台可用 `[player]` 指定他人；
- 非 OP 指定他人会报错；
- 在线目标必须有对应权限；
- 控制台不指定玩家时修改/查看全局默认值；
- `skin`、`aichat_fake_inventory`、`aichat_questioner_inventory` 只能在这里改，不能用于 `/fp set`。

### `/fp set <feature> <value> [name]`

直接改假人当前值，并立即应用到该创建者的所有在线假人（若该特性有 modifier）。

## 11. 管理与运维

| 命令 | 权限 | 说明 |
|---|---|---|
| `/fp reload` | OP | 重载配置、AI 配置、延迟、TPS、翻译 |
| `/fp reload-translation` | OP | 只重载翻译 |
| `/fp migrate-db` | OP | 从 SQLite 迁移到 MySQL |
| `/fp update check` | OP | 检查新版本 |
| `/fp update config` | OP | 补齐配置缺失项 |
| `/fp kickall` | OP | 移除所有假人 |
| `/fp killall` | OP | 杀死所有假人 |
| `/fp lang [code|reset]` | 无 | 设置自己的语言，控制台不能覆盖 |
| `/fp debug send-plugin-message ...` | OP + `debug: true` | 测试插件消息通道 |

## 12. 命令排错清单

| 现象 | 原因 |
|---|---|
| 提示需要名字但只操作一个假人 | 显式写错名字不会回退；检查拼写与归属 |
| `/fp respawn` 直接不可用 | `kick-on-dead: true` |
| `/fp select` 没权限 | `needSelect` 要求 OP 或上限 >1 |
| `-a` 不生效 | 该命令不支持 `-a`，或目标范围理解错误 |
| `/fp say once 1234` 把 1234 当消息 | 这是刻意设计，兼容单参数消息 |
| 动作 once 没提示 | 玩家执行时默认不回消息 |
| `invsee` 被拒 | 不同世界，或不是创建者且无 `invsee` 权限 |
| `look me` / `ride me` 没反应 | 不同世界时静默返回 |
| `update config` 提示冲突 | 模板节与现有叶子冲突，只报告不覆盖 |
