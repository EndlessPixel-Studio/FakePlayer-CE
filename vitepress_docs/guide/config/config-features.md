# 特性与性能

## 默认特性

`default-features` 决定玩家未单独修改配置时假人的默认行为：

| 配置项 | 默认 | 说明 |
|---|---|---|
| `collidable` | `true` | 是否有碰撞箱 |
| `invulnerable` | `true` | 是否无敌 |
| `wolverine` | `false` | 超强再生 |
| `look_at_entity` | `false` | 自动看向附近可攻击实体 |
| `pickup_items` | `true` | 是否拾取物品 |
| `skin` | `true` | 是否使用创建者皮肤 |
| `replenish` | `false` | 自动补货 |
| `replace_tools` | `false` | GCA 风格工具替换 |
| `autofish` | `false` | 自动钓鱼 |
| `aichat_fake_inventory` | `false` | 允许 AI 读取假人背包 |
| `aichat_questioner_inventory` | `false` | 允许 AI 读取提问玩家背包 |
| `simulation_distance` | `-1` | 模拟距离，`-1` 沿用世界设置 |

玩家可用 `/fp config set <项> <值>` 修改自己的默认值，或用 `/fp set <项> <值> [假人]` 修改单个假人。

## 补货与工具替换

```yaml
replenish:
  tools:
    durability-threshold: 0
tool-replacement:
  remaining-durability-threshold: 10
```

- `durability-threshold`：工具磨损到该百分比时自动修复，`0` 关闭。
- `remaining-durability-threshold`：经验修补工具耐久低于该值时，用背包里的同类工具替换；非经验修补工具损坏后替换。

`/fp drop`、`/fp dropstack` 丢空主手后会补货；`/fp dropinv` 不会补货。

## TPS 动态上限

```yaml
tps-adaptive:
  enabled: false
  interval: 60
  threshold: 17.0
  min-count: 1
```

TPS 低于阈值时每隔一个周期把每名玩家的上限减 1，并移除创建者名下最新召唤的超额假人；恢复后每周期加回 1。扣减只影响运行期上限，不修改配置文件。按权限设置的上限也会参与扣减。

## 背包查看

```yaml
invsee-implement: AUTO
allow-non-op-open-inv: false
```

`AUTO` 会在安装 OpenInv 时自动使用其实现。普通玩家默认不能通过右键打开假人背包。

## 自定义延迟

```yaml
custom-ping: ''
custom-ping-dynamic: false
custom-ping-dynamic-interval: 200
```

可给假人设置固定延迟；动态模式下按周期随机变化。
