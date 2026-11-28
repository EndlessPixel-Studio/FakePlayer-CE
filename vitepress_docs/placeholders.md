# 占位符变量

安装 PlaceholderAPI 后可用 `fakeplayer_` 前缀的占位符。扩展标识固定为 `fakeplayer`，不随插件改名变化。

## 全局占位符

| 占位符 | 说明 |
|---|---|
| `%fakeplayer_total%` | 服务器当前假人总数 |
| `%fakeplayer_list_size%` | 假人数量（别名） |
| `%fakeplayer_list%` | 假人名称列表，默认用 `, ` 分隔 |
| `%fakeplayer_list_<序号>%` | 第 N 个假人的名称（从 0 开始） |
| `%fakeplayer_list_<序号>_<属性>%` | 第 N 个假人的指定属性，省略属性时取 `name` |
| `%fakeplayer_isfake%` | 对任意玩家判断是否为假人 |

分隔符来自翻译键 `fakeplayer.placeholder.separator`，默认 `", "`；时间格式来自 `fakeplayer.placeholder.time-format`，默认 `yyyy-MM-dd HH:mm:ss`。

## 假人属性

| 属性 | 说明 |
|---|---|
| `name` | 假人名称 |
| `uuid` | 假人 UUID |
| `creator` / `spawner` | 创建者名称 |
| `spawntime` | 生成时间 |
| `world` | 所在世界 |
| `x` / `y` / `z` | 坐标 |
| `health` | 生命值 |
| `food` | 饥饿值 |
| `level` | 经验等级 |
| `gamemode` | 游戏模式 |
| `actions` | 正在进行的动作 |
| `actions_translated` | 已本地化的动作名 |

动作列表以 `|` 连接。

## 示例

```
%fakeplayer_total%
%fakeplayer_list_0_name%
%fakeplayer_list_0_world%
%player_name% 是否为假人: %fakeplayer_isfake%
```
