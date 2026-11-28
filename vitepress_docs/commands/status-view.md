# 状态与查看

## /fp select

- 权限：`fakeplayer.command.select`
- 参数：`[假人]`
- 设置默认操作对象；创建多个假人后可用

```
/fp select Bot_1
```

## /fp selection

- 权限：`fakeplayer.command.selection`
- 无参数，查看当前选中的假人

## /fp list

- 权限：`fakeplayer.command.list`
- 无参数，列出在线假人
- OP / 控制台看到全部，普通玩家只看到自己召唤的；`[<--踢出]` 是可点击的踢出按钮

## /fp distance

- 权限：`fakeplayer.command.distance`
- 参数：`[假人]`

## /fp status

- 权限：`fakeplayer.command.status`
- 参数：`[假人]`，支持 `-a`
- 显示坐标、朝向、血量、饥饿、经验、模式、创建者、手持物品与正在进行的动作

```
/fp status Bot_1
/fp status -a
```

## /fp respawn

- 权限：`fakeplayer.command.respawn`
- 参数：`[假人]`，支持 `-a`
- 复活已死亡假人；需要 `kick-on-dead: false`

## /fp set

- 权限：`fakeplayer.command.set`
- 参数：`<配置项> <值> [假人]`
- 修改单个假人的独立配置

```
/fp set invulnerable false Bot_1
```

## /fp config

- 权限：`fakeplayer.command.config`
- 子命令：`set <项> <值> [玩家]` / `list [玩家]` / `help`
- 控制台可用；`[玩家]` 用于指定他人

```
/fp config list
/fp config set autofish true
```

## /fp setping

- 权限：`fakeplayer.command.setping`
- 参数：`<延迟> [假人]`，支持 `-a`
- `0` 表示使用真实延迟

```
/fp setping 50 Bot_1
```

## /fp expme

- 权限：`fakeplayer.command.expme`
- 参数：`[假人]`
- 把假人的经验转移给自己（仅玩家可用）
