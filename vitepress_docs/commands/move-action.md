# 移动与动作

动作类命令大多带 `once`（一次）、`continuous`（持续）与 `interval <tick>` 子命令。例：

```
/fp attack once Bot_1
/fp mine continuous
/fp use interval 20
/fp stop
```

## /fp move

- 权限：`fakeplayer.command.move`
- 子命令：`forward` / `backward` / `left` / `right`，后接可选 `[假人]`
- 不带子命令时等同于 `forward`

```
/fp move forward Bot_1
/fp move left
```

## /fp goto

- 权限：`fakeplayer.command.goto`
- 参数：`<x> <y> <z> [假人]`，支持 `-a`
- 自动寻路，目标必须可达且距离不超过 1024 格

```
/fp goto 100 64 -200 Bot_1
```

## /fp gotostop

- 权限：`fakeplayer.command.goto`
- 无参数，中止 `/fp goto` 的寻路，不影响其它动作

## /fp turn

- 权限：`fakeplayer.command.turn`
- 子命令：`left` / `right` / `<角度>`，后接可选 `[假人]`

```
/fp turn left Bot_1
```

## /fp look

- 权限：`fakeplayer.command.look`
- 子命令：`north` / `south` / `east` / `west` / `up` / `down` / `at <坐标>` / `me` / `entity`，后接可选 `[假人]`

```
/fp look north Bot_1
/fp look at 100 64 200
```

## /fp jump

- 权限：`fakeplayer.command.jump`
- 参数：`[假人]`，支持 `-a`

## /fp attack

- 权限：`fakeplayer.command.attack`
- 参数：`[假人]`，支持 `-a`；动作模式子命令同上

## /fp mine

- 权限：`fakeplayer.command.mine`
- 参数：`[假人]`，支持 `-a`

## /fp use

- 权限：`fakeplayer.command.use`
- 交互 / 放置方块 / 使用物品
- 参数：`[假人]`，支持 `-a`

## /fp stop

- 权限：`fakeplayer.command.stop`
- 参数：`[假人]`，支持 `-a`
- 终止假人所有动作

## /fp ride

- 权限：`fakeplayer.command.ride`
- 子命令：`me` / `target` / `anything` / `vehicle` / `entity <实体>` / `stop`

```
/fp ride me Bot_1
/fp ride stop
```

## /fp sneak

- 权限：`fakeplayer.command.sneak`
- 参数：`[假人] [true|false]`，省略状态时切换

## /fp sprint

- 权限：`fakeplayer.command.sprint`
- 参数：`[假人] [true|false]`，省略状态时切换
