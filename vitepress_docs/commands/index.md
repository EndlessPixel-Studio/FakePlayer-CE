# 指令总览

所有指令都以 `/fp` 开头（`fakeplayer` 是完整别名）。

## 名称与 `-a`

凡是假人名称参数都可以用 `-a` 一次操作范围内所有假人：

- OP 与控制台：全服假人
- 普通玩家：仅自己召唤的假人

`select`、`spawn`/`spawnuuid`、`rename` 不支持 `-a`。未指定名称时，优先使用 `/fp select` 选中的假人；没有选中且只有一个假人时自动使用它。

## 指令分类

- [生成与移除](/commands/spawn-remove)
- [状态与查看](/commands/status-view)
- [移动与动作](/commands/move-action)
- [物品与容器](/commands/inventory)
- [交互与聊天](/commands/interaction-chat)
- [管理与运维](/commands/admin)

完整权限节点见各命令条目。OP 自动拥有所有权限。
