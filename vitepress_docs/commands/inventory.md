# 物品与容器

## /fp drop

- 权限：`fakeplayer.command.drop`
- 参数：`[假人]`，支持 `-a`
- 丢弃手中单个物品；丢空后按配置补货

## /fp dropstack

- 权限：`fakeplayer.command.dropstack`
- 参数：`[假人]`，支持 `-a`
- 丢弃手中整组物品

## /fp dropinv

- 权限：`fakeplayer.command.dropinv`
- 参数：`[假人]`，支持 `-a`
- 清空全部背包物品；不会触发补货

## /fp invsee

- 权限：`fakeplayer.command.invsee`
- 参数：`[假人]`
- 打开假人背包界面；右键假人也能触发
- 普通玩家默认不能右键打开，需 `allow-non-op-open-inv: true`

## /fp enderchest

- 权限：`fakeplayer.command.enderchest`
- 参数：`[假人]`
- 别名 `/fp ec`；潜行右键假人触发；需与假人同世界

## /fp skin

- 权限：`fakeplayer.command.skin`
- 参数：`<玩家名> [假人]`
- 复制其他玩家皮肤；安装 SkinsRestorer 时走其 API；离线名有 60 秒冷却

```
/fp skin Notch Bot_1
```

## /fp hold

- 权限：`fakeplayer.command.hold`
- 参数：`<格子 1-9> [假人]`，支持 `-a`

```
/fp hold 1 Bot_1
```

## /fp swap

- 权限：`fakeplayer.command.swap`
- 参数：`[假人]`，支持 `-a`
- 交换主手与副手物品
