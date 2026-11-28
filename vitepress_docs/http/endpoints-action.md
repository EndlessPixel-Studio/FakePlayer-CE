# 动作与移动接口

## GET /say

- 参数：`name`（必填）、`message`（必填，需 URL 编码）
- 以假人身份发言

```
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" \
  --data-urlencode "name=Bot_1" --data-urlencode "message=你好 世界"
```

## GET /aichat

- 参数：`name`、`mode=public|private`、`target`（private 时必填）、`continuous=true`、`interval=<tick>`
- 需启用 `ai-chat.yml`

```
curl -H "Authorization: Bearer $TOKEN" "$API/aichat?name=Bot_1&mode=public"
```

## GET /action

- 参数：
  - `name`：假人名称
  - `action`：动作名，必填；大小写、`-`、`_` 不敏感
  - `count`：次数，默认 1；`-1` 持续；`interval > 0` 且未给 `count` 时视为持续
  - `interval`：间隔 tick，默认 0
  - `wait`：首次执行前等待 tick，默认 0
  - `message`：仅 `SAY` 动作需要

```
curl -G -H "Authorization: Bearer $TOKEN" "$API/action" \
  --data-urlencode "name=Bot_1" --data-urlencode "action=attack" --data-urlencode "interval=10"
```

## GET /stop

- 参数：`name`
- 停止假人当前所有动作

## GET /teleport

- 参数：`name`、`x`/`y`/`z`、`yaw`/`pitch`、`world`
- 未给出的坐标保持当前值；`world` 不存在时会报错

## GET /look

- 参数：`name`、`direction`、`at=x,y,z`、`yaw`、`pitch`
- `direction` 可选 `NORTH` / `SOUTH` / `EAST` / `WEST` / `UP` / `DOWN`
- 四种方式至少提供其一

## GET /hold

- 参数：`name`、`slot`（必填，1–9）

## GET /swap

- 参数：`name`
- 交换主手与副手物品

## GET /goto

- 参数：`name`、`x`/`y`/`z`（必填）、`world`
- 目标必须在当前世界，距离上限 1024 格

## GET /setping

- 参数：`name`、`ping`（必填，≥0）

## GET /sneak、/sprint

- 参数：`name`、`state`（省略则切换状态）
