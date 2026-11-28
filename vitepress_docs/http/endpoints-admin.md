# 高危接口

::: danger
这些接口可以重载配置、以假人身份执行控制台命令或一次性移除所有假人。请关闭不需要的接口，不要把管理端口暴露到公网，也不要把假人设为 OP。
:::

<ApiBadge text="需要鉴权" type="info" />
<ApiBadge text="默认全部开启" type="warn" />
<ApiBadge text="可逐接口关闭" type="ok" />

## GET /reload

重载插件配置与翻译。

<ApiEndpoint method="GET" path="/reload" />

### 参数

无。

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

## GET /cmd

以假人身份执行控制台命令。

<ApiEndpoint method="GET" path="/cmd" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `name` | 是 | 假人名称 |
| `command` | 是 | 控制台命令，需 URL 编码 |

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success" }
```

  </template>
</ApiResponse>

### 快速上手

<ApiCodeTabs>
  <template #curl>

```bash
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" \
  --data-urlencode "name=Bot_1" --data-urlencode "command=say hello"
```

  </template>
  <template #powershell>

```powershell
Invoke-RestMethod -Uri "$API/cmd?name=Bot_1&command=say%20hello" -Headers @{ Authorization = "Bearer $TOKEN" }
```

  </template>
</ApiCodeTabs>

## GET /kickall

踢出服务器所有假人。

<ApiEndpoint method="GET" path="/kickall" />

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success", "count": 5 }
```

  </template>
</ApiResponse>

## GET /killall

杀死服务器所有假人，可能掉落物品。

<ApiEndpoint method="GET" path="/killall" />

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success", "count": 5 }
```

  </template>
</ApiResponse>

## GET /sayall

让所有假人发送同一条消息。

<ApiEndpoint method="GET" path="/sayall" />

### 参数

| 参数 | 必填 | 说明 |
|---|---|---|
| `message` | 是 | 消息内容，需 URL 编码 |

<ApiResponse :status="200" title="请求成功">
  <template #code>

```json
{ "status": "success", "count": 5 }
```

  </template>
</ApiResponse>

## 常见问题

<details>
<summary>如何单独关闭某个高危接口？</summary>

在 `config.yml` 的 `http-admin.interface.*` 中把对应项设为 `false`。关闭后接口返回 `403`。鉴权在接口开关之前检查，因此无法用 `401`/`403` 差异探测已启用端点。

</details>

<details>
<summary>为什么假人执行命令的权限很大？</summary>

`/cmd` 以假人身份执行控制台命令，权限等同该假人。不要把假人设为 OP，也只对可信调用方开放该接口。

</details>

## 相关接口

- [信息类接口](/http/endpoints-info)
- [动作与移动接口](/http/endpoints-action)
