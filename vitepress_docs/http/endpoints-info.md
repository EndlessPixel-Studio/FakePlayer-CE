# 信息类接口

## GET /list

- 参数：无
- 返回：在线假人列表

```
curl -H "Authorization: Bearer $TOKEN" "$API/list"
```

## GET /status

- 参数：`name`（可选，假人名称）
- 省略 `name` 时返回全部假人状态
- 返回字段：坐标、朝向、血量、饥饿、经验、游戏模式、创建者、手持物品、进行中的动作

```
curl -H "Authorization: Bearer $TOKEN" "$API/status?name=Bot_1"
```

## GET /info

- 参数：无
- 返回：插件与服务端信息（版本、MC 版本、服务端、假人数量、在线玩家数、数量上限）

```
curl -H "Authorization: Bearer $TOKEN" "$API/info"
```
