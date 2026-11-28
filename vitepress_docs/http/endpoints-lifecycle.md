# 生命周期接口

生成、移除、复活与停止 AI 发言。

<ApiBadge text="需要鉴权" type="info" />
<ApiBadge text="会改变状态" type="warn" />

## GET /spawn

生成假人。

<ApiEndpoint method="GET" path="/spawn" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称，需符合命名规则 |

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success", "name": "Bot_1" }
```

  </template>
</ApiResponse>

<ApiResponse :status="400" title="超出上限或名称非法">
  <template #code>

```json
{ "status": "failure", "msg": "Spawn limit reached" }
```

  </template>
</ApiResponse>

### 快速上手

<ApiCodeTabs>
  <template #curl>

```bash
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=Bot_1"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/spawn?name=Bot_1" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## GET /kick

把假人从服务器移除。

<ApiEndpoint method="GET" path="/kick" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称 |

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

## GET /kill

让假人真实死亡，可能掉落物品。

<ApiEndpoint method="GET" path="/kill" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称 |

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

## GET /respawn

复活已死亡假人；需要 `kick-on-dead: false`。

<ApiEndpoint method="GET" path="/respawn" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称 |

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

## GET /aichat/stop

停止假人的 AI 持续发言。

<ApiEndpoint method="GET" path="/aichat/stop" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称 |

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success", "stopped": true }
```

  </template>
</ApiResponse>

### 快速上手

<ApiCodeTabs>
  <template #curl>

```bash
curl -H "Authorization: Bearer $TOKEN" "$API/aichat/stop?name=Bot_1"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/aichat/stop?name=Bot_1" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## 常见问题

<details>
<summary>/kill 与 /kick 有什么区别？</summary>

`/kick` 直接把假人从服务端移除；`/kill` 让假人真实死亡，可能掉落物品。需要 `/fp respawn` 复活时必须关闭 `kick-on-dead`。

</details>

## 相关接口

- [动作与移动接口](/http/endpoints-action)
- [高危接口](/http/endpoints-admin)
