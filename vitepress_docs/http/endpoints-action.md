# 动作与移动接口

驱动假人发言、执行动作、移动与调整状态。所有响应都遵循通用格式 `{"status":"success"}` 或 `{"status":"failure","msg":"..."}`。

<ApiBadge text="需要鉴权" type="info" />
<ApiBadge text="GET / POST" type="info" />

## GET /say

<ApiEndpoint method="GET" path="/say" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称 |
| `message` | 是 | 消息内容，需 URL 编码 |

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

<ApiCodeTabs>
  <template #curl>

```bash
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" \
  --data-urlencode "name=Bot_1" --data-urlencode "message=你好 世界"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/say?name=Bot_1&message=$([uri]::EscapeDataString('你好 世界'))" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## GET /aichat

<ApiEndpoint method="GET" path="/aichat" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `mode` | 否 | `public` 或 `private` |
| `target` | private 时必填 | 私聊目标玩家 |
| `continuous` | 否 | `true` 开启持续发言 |
| `interval` | 否 | 间隔 tick |

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

## GET /action

<ApiEndpoint method="GET" path="/action" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `action` | 是 | 动作名，大小写、`-`、`_` 不敏感 |
| `count` | 否 | 次数，默认 1；`-1` 持续 |
| `interval` | 否 | 间隔 tick，默认 0 |
| `wait` | 否 | 首次执行前等待 tick，默认 0 |
| `message` | SAY 时必填 | 发言内容 |

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

<ApiCodeTabs>
  <template #curl>

```bash
curl -G -H "Authorization: Bearer $TOKEN" "$API/action" \
  --data-urlencode "name=Bot_1" --data-urlencode "action=attack" --data-urlencode "interval=10"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/action?name=Bot_1&action=attack&interval=10" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## GET /stop

<ApiEndpoint method="GET" path="/stop" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |

停止假人当前所有动作。响应同通用格式。

## GET /teleport

<ApiEndpoint method="GET" path="/teleport" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `x` / `y` / `z` | 否 | 未给出的坐标保持当前值 |
| `yaw` / `pitch` | 否 | 朝向 |
| `world` | 否 | 世界名，不存在时报错 |

## GET /look

<ApiEndpoint method="GET" path="/look" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `direction` | 四选一 | `NORTH` / `SOUTH` / `EAST` / `WEST` / `UP` / `DOWN` |
| `at` | 四选一 | `x,y,z` 坐标 |
| `yaw` / `pitch` | 四选一 | 精确角度 |

四种方式至少提供其一。

## GET /hold

<ApiEndpoint method="GET" path="/hold" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `slot` | 是 | 快捷栏格子 1–9 |

## GET /swap

<ApiEndpoint method="GET" path="/swap" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |

交换主手与副手物品。

## GET /goto

<ApiEndpoint method="GET" path="/goto" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `x` / `y` / `z` | 是 | 目标坐标 |
| `world` | 否 | 必须是假人当前世界 |

<ApiResponse :status="400" title="目标不可达">
  <template #code>

```json
{ "status": "failure", "msg": "Target out of range or unreachable" }
```

  </template>
</ApiResponse>

距离上限 1024 格。

## GET /setping

<ApiEndpoint method="GET" path="/setping" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `ping` | 是 | 延迟值，≥0 |

## GET /sneak、/sprint

<ApiEndpoint method="GET" path="/sneak | /sprint" />

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称 |
| `state` | 否 | `true` / `false`，省略则切换 |

## 常见问题

<details>
<summary>/action 的 count、interval 与 continuous 怎么配合？</summary>

`count: -1` 表示持续；不写 `count` 但写了 `interval` 也视为持续。两者都没写时执行一次。

</details>

## 相关接口

- [生命周期接口](/http/endpoints-lifecycle)
- [高危接口](/http/endpoints-admin)
