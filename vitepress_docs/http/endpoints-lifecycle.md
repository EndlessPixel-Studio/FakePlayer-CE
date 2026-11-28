# 生命周期接口

## GET /spawn

- 参数：`name`（必填）
- 返回：生成结果

```
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=Bot_1"
```

## GET /kick

- 参数：`name`（必填）
- 把假人从服务器移除

```
curl -H "Authorization: Bearer $TOKEN" "$API/kick?name=Bot_1"
```

## GET /kill

- 参数：`name`（必填）
- 让假人真实死亡，可能掉落物品

```
curl -H "Authorization: Bearer $TOKEN" "$API/kill?name=Bot_1"
```

## GET /respawn

- 参数：`name`（必填）
- 复活已死亡假人；需要 `kick-on-dead: false`

```
curl -H "Authorization: Bearer $TOKEN" "$API/respawn?name=Bot_1"
```

## GET /aichat/stop

- 参数：`name`（必填）
- 停止该假人的 AI 持续发言
- 返回 `stopped` 表示是否真的停止了任务

```
curl -H "Authorization: Bearer $TOKEN" "$API/aichat/stop?name=Bot_1"
```
