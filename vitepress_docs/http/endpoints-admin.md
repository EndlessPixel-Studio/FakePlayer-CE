# 高危接口

::: danger
这些接口可以重载配置、以假人身份执行控制台命令或一次性移除所有假人。请关闭不需要的接口，并不要把管理端口暴露到公网。
:::

## GET /reload

- 参数：无
- 重载插件配置与翻译

```
curl -H "Authorization: Bearer $TOKEN" "$API/reload"
```

## GET /cmd

- 参数：`name`（必填）、`command`（必填，需 URL 编码）
- 以假人身份执行控制台命令

```
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" \
  --data-urlencode "name=Bot_1" --data-urlencode "command=say hello"
```

> 假人执行控制台命令的权限等同该假人。**不要把假人设为 OP。**

## GET /kickall

- 参数：无
- 踢出服务器所有假人，返回 `count`

## GET /killall

- 参数：无
- 杀死服务器所有假人，返回 `count`

## GET /sayall

- 参数：`message`（必填，需 URL 编码）
- 让所有假人发送同一条消息

```
curl -G -H "Authorization: Bearer $TOKEN" "$API/sayall" \
  --data-urlencode "message=服务器即将重启"
```

## 逐接口开关

`config.yml` 的 `http-admin.interface.*` 可以为每个端点单独开关；关闭的接口返回 `403`。鉴权在接口开关之前检查，因此无法用 `401`/`403` 的差异探测已启用的端点。
