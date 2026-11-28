# 占位符变量

FakePlayer-CE 通过 PlaceholderAPI 提供 `%fakeplayer_*%` 变量，可用于计分板、聊天插件、Tab 列表、菜单等支持 PlaceholderAPI 的组件。

## 1. 前置条件

- 安装 PlaceholderAPI；
- 插件正常启用；
- 在支持 PlaceholderAPI 的插件中使用标准占位符语法。

扩展标识固定为 `fakeplayer`，不是 `fakeplayer-ce`。插件禁用时会通过 `PluginDisableEvent` 自行注销。

::: warning
占位符参数统一转小写。无法识别的占位符会原样返回参数字符串，而不是变成空值，方便排查拼写错误。
:::

## 2. 全局占位符

全局占位符不需要玩家上下文。

| 占位符 | 含义 | 示例 |
|---|---|---|
| `%fakeplayer_total%` | 服务器假人总数 | `3` |
| `%fakeplayer_list_size%` | 同上，别名 | `3` |
| `%fakeplayer_list%` | 所有在线假人名称，默认 `, ` 分隔 | `Bot_1, Bot_2` |
| `%fakeplayer_list_<序号>%` | 第 N 个假人名称，从 0 开始 | `%fakeplayer_list_0%` → `Bot_1` |
| `%fakeplayer_list_<序号>_<属性>%` | 第 N 个假人的属性 | `%fakeplayer_list_1_world%` |

索引不是数字时返回原参数；索引越界返回空字符串。省略属性时默认取 `name`。

## 3. 玩家上下文占位符

| 占位符 | 含义 |
|---|---|
| `%fakeplayer_isfake%` | `true` / `false`，当前玩家是否为假人 |
| `%fakeplayer_name%` | 假人名称 |
| `%fakeplayer_uuid%` | 假人 UUID |
| `%fakeplayer_creator%` | 创建者名称 |
| `%fakeplayer_spawner%` | 创建者名称，别名 |
| `%fakeplayer_spawntime%` | 生成时间 |
| `%fakeplayer_world%` | 所在世界 |
| `%fakeplayer_x%` / `%fakeplayer_y%` / `%fakeplayer_z%` | 方块坐标 |
| `%fakeplayer_health%` | 生命值 |
| `%fakeplayer_food%` | 饥饿值 |
| `%fakeplayer_level%` | 经验等级 |
| `%fakeplayer_gamemode%` | 游戏模式名 |
| `%fakeplayer_actions%` | 进行中的动作，如 `USE\|ATTACK` |
| `%fakeplayer_actions_translated%` | 本地化后的动作名 |

假人属性占位符要求当前玩家本身就是假人。`isfake` 可以在真人和假人上使用，但必须有玩家上下文。

## 4. 属性列表用法

`%fakeplayer_list_<序号>_<属性>%` 中的属性名与玩家上下文占位符去掉 `fakeplayer_` 前缀后一致，例如：

```text
%fakeplayer_list_0%
%fakeplayer_list_0_name%
%fakeplayer_list_0_creator%
%fakeplayer_list_0_world%
%fakeplayer_list_0_x%
%fakeplayer_list_0_health%
%fakeplayer_list_0_actions_translated%
```

可以用于做「假人列表看板」：

```yaml
scoreboard:
  title: 假人监控
  lines:
    - '总数: %fakeplayer_total%'
    - '第一个: %fakeplayer_list_0%'
    - '位置: %fakeplayer_list_0_world% %fakeplayer_list_0_x%,%fakeplayer_list_0_y%,%fakeplayer_list_0_z%'
    - '动作: %fakeplayer_list_0_actions_translated%'
```

## 5. 分隔符与时间格式

两个值来自翻译键，可通过自定义语言文件覆盖：

| 翻译键 | 默认 | 用途 |
|---|---|---|
| `fakeplayer.placeholder.separator` | `, ` | `%fakeplayer_list%` 的分隔符 |
| `fakeplayer.placeholder.time-format` | `yyyy-MM-dd HH:mm:ss` | `%fakeplayer_spawntime%` 格式 |

时间格式非法时会回退默认值。`spawntime` 由生成 tick 推算，按系统默认时区格式化。

## 6. 与命令钩子的 `%p` / `%u` / `%c` 区别

命令钩子（见 [配置完全手册](/guide/config/)）使用自己的占位符：

| 占位符 | 含义 |
|---|---|
| `%p` | 假人名 |
| `%u` | UUID |
| `%c` | 创建者名 |

它们只在 `pre-spawn-commands`、`post-spawn-commands`、`after-spawn-commands`、`self-commands`、`post-quit-commands`、`after-quit-commands` 中生效，**不是** PlaceholderAPI 变量，不能写成 `%fakeplayer_p%`。

## 7. 常见问题

<details>
<summary>占位符显示成原样字符串？</summary>

说明参数无法识别或 PlaceholderAPI 未正确加载。检查拼写、扩展标识 `fakeplayer`，以及 `%` 是否被其他插件转义。

</details>

<details>
<summary>%fakeplayer_list_1% 是空的？</summary>

索引从 0 开始，且假人数量可能变化。越界时返回空字符串，这是预期行为。

</details>

<details>
<summary>%fakeplayer_actions% 里的竖线是什么？</summary>

多个动作用 `|` 连接，例如 `USE|ATTACK`。`_translated` 版本会按玩家语言本地化。

</details>
