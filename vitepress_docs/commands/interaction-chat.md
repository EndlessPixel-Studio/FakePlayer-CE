# 交互与聊天

## /fp tp

- 权限：`fakeplayer.command.tp`
- 参数：`[假人]`
- 传送到假人位置

## /fp tphere

- 权限：`fakeplayer.command.tphere`
- 参数：`[假人]`，支持 `-a`
- 把假人传送到自己位置

## /fp tps

- 权限：`fakeplayer.command.tps`
- 参数：`[假人]`，支持 `-a`
- 与假人互换位置

## /fp say

- 权限：`fakeplayer.command.say`
- 参数：`<消息> [假人]` 或子命令 `once` / `continuous` / `interval <tick>`
- 兼容聊天格式化插件

```
/fp say hello Bot_1
/fp say continuous Bot_1
```

## /fp aichat

- 权限：`fakeplayer.command.aichat`
- 需先配置 `ai-chat.yml`

| 子命令 | 说明 |
|---|---|
| `public [once\|continuous\|interval <tick>] [假人]` | 发到公共聊天 |
| `private <玩家> [once\|continuous\|interval <tick>] [假人]` | 只发给指定玩家 |
| `stop [假人]` | 停止持续发言 |
| `model [名称]` | 查看/切换 AI 模型（仅 OP） |

## /fp password

- 权限：`fakeplayer.command.password`
- 参数：`<假人> <密码>`
- 把假人登录密码存入插件数据库，配合 `auto-login.command` 使用

## /fp changepassword

- 权限：`fakeplayer.command.changepassword`
- 参数：`<假人> <新密码> [旧密码]`
- 旧密码可省略（例如使用登录插件 API 免登录时）
