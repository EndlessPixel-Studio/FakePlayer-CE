# 信息类接口

只读查询假人与服务器状态，不会改变服务端数据。

<ApiBadge text="需要鉴权" type="info" />
<ApiBadge text="GET / POST" type="info" />
<ApiBadge text="默认关闭" type="warn" />

## GET /list

列出当前在线的假人。

<ApiEndpoint method="GET" path="/list" />

### 功能概述

返回调用者可见的假人列表：OP 与控制台看到全服假人，普通玩家只看到自己召唤的假人。

### 参数

无。

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{
  "status": "success",
  "fakeplayer": ["Bot_1", "Bot_2"]
}
```

  </template>
</ApiResponse>

<ApiResponse :status="401" title="鉴权失败">
  <template #code>

```json
{ "status": "failure", "msg": "Unauthorized" }
```

  </template>
</ApiResponse>

### 快速上手

<ApiCodeTabs>
  <template #curl>

```bash
curl -H "Authorization: Bearer $TOKEN" "$API/list"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/list" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## GET /status

查看假人详细状态。

<ApiEndpoint method="GET" path="/status" />

### 功能概述

返回坐标、朝向、血量、饥饿、经验、游戏模式、创建者、手持物品与正在进行的动作。

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 否 | 假人名称；省略时返回全部假人状态 |

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{
  "status": "success",
  "fakeplayer": [
    { "name": "Bot_1", "world": "world", "x": 0, "y": 64, "z": 0 }
  ]
}
```

  </template>
</ApiResponse>

<ApiResponse :status="404" title="假人不存在">
  <template #code>

```json
{ "status": "failure", "msg": "Fake player not found" }
```

  </template>
</ApiResponse>

### 快速上手

<ApiCodeTabs>
  <template #curl>

```bash
curl -H "Authorization: Bearer $TOKEN" "$API/status?name=Bot_1"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/status?name=Bot_1" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## GET /info

查看插件与服务端信息。

<ApiEndpoint method="GET" path="/info" />

### 功能概述

返回插件版本、MC 版本、服务端类型、假人数量、在线玩家数与假人数量上限。

### 参数

无。

### 响应

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{
  "status": "success",
  "version": "fp.build23",
  "fakeplayer_count": 2,
  "player_count": 1
}
```

  </template>
</ApiResponse>

### 快速上手

<ApiCodeTabs>
  <template #curl>

```bash
curl -H "Authorization: Bearer $TOKEN" "$API/info"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/info" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## 常见问题

<details>
<summary>为什么返回 401？</summary>

鉴权只认 `Authorization: Bearer <token>` 请求头，不支持 `?token=`。请确认 `http-admin.token` 与请求头一致。

</details>

<details>
<summary>为什么普通玩家看不到别人的假人？</summary>

信息类接口沿用游戏内的可见范围：OP/控制台看到全服，普通玩家只看到自己召唤的假人。

</details>

## 相关接口

- [生命周期接口](/http/endpoints-lifecycle)
- [动作与移动接口](/http/endpoints-action)
