# 生成与移除

## /fp spawn

- 权限：`fakeplayer.command.spawn`
- 参数：`[名称] [世界] [坐标]`
- 名称需 `fakeplayer.command.spawn.name`；世界/坐标需 `fakeplayer.command.spawn.location`
- 名称填 `-` 可跳过，直接指定世界与坐标

```
/fp spawn
/fp spawn Bot_1
/fp spawn - world 0 64 0
```

## /fp spawnjson

- 权限：`fakeplayer.command.spawn.json`（默认 OP）
- 参数：`<json>`，字段均可省略，未知字段会报错

支持字段：`name`、`uuid`、`world`、`x/y/z`、`yaw/pitch`、`lifespan_seconds`。暂不支持 NBT。

```
/fp spawnjson {"name":"Bot_1","world":"world","x":0,"y":64,"z":0,"lifespan_seconds":600}
```

## /fp spawnuuid

- 权限：`fakeplayer.command.spawn.uuid`（默认 OP）
- **已弃用**，请使用 `/fp spawnjson`
- 参数：`<名称> <UUID> [世界] [坐标]`

## /fp kick

- 权限：`fakeplayer.command.kick`
- 参数：`[假人]`，支持 `-a`
- 把假人从服务器移除

```
/fp kick Bot_1
/fp kick -a
```

## /fp kill

- 权限：`fakeplayer.command.kill`
- 参数：`[假人]`，支持 `-a`
- 让假人真实死亡，可能掉落物品；需要 `kick-on-dead: false` 才能 `/fp respawn`

```
/fp kill Bot_1
```

## /fp kickall / /fp killall

- 权限：OP
- 无参数，作用于服务器全部假人

::: warning
`/fp killall` 会让所有假人死亡并可能掉落物品，请确认清楚再执行。
:::
