# 创建第一个假人

## 生成一个假人

进入游戏，执行：

```
/fp spawn
```

默认会按你的名字生成假人。如果启用了 `spawn-limit` 或名字模板，实际名称可能带前缀、后缀或序号。

## 查看与选择

```
/fp list
/fp selection
/fp select <假人>
```

拥有多个假人时，可以用 `/fp select` 设置默认操作对象；之后不带名字的命令都会作用于选中的假人。

## 让假人动起来

```
/fp move north 10
/fp look north
/fp jump
/fp attack once
```

动作类命令大多支持 `once`（一次）、`continuous`（持续）与 `interval <tick>` 三种模式。终止所有动作：

```
/fp stop
```

## 批量操作

凡是假人名称参数都可以用 `-a` 一次操作范围内所有假人：

```
/fp status -a
/fp look north -a
```

OP 和控制台的 `-a` 作用于全服假人；普通玩家只能作用于自己召唤的假人。`select`、`spawn`/`spawnuuid`、`rename` 不支持 `-a`。

## 下一步

- [配置文件总览](/guide/config/)
- [指令总览](/commands/)
