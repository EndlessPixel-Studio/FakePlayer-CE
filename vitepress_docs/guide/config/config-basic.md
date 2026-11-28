# 基础设置

对应 `config.yml` 中的数量、命名、寿命与创建者行为。

## 数量上限

```yaml
server-limit: 1000
player-limit: 1
spawn-limit:
  # vip: 10
detect-ip: true
```

- `server-limit`：全服同时存在的假人总数。
- `player-limit`：每名玩家的默认上限，设为 `0` 表示不限。
- `spawn-limit`：按权限覆盖上限。配置 `vip: 10` 后，拥有 `fakeplayer.spawn.limit.vip` 的玩家上限为 10；命中多个节点时取最大值；OP 不受限制。
- `detect-ip`：同一 IP 的玩家共享 `player-limit`，防止开小号刷假人。IP 校验始终使用全局 `player-limit`，不受个人权限上限影响。

## 命名

```yaml
name-template: ''
name-prefix: ''
name-suffix: ''
name-pattern: '^[a-zA-Z0-9_]+$'
name-style:
  - gray
  - italic
```

- `name-template`：自动生成名称模板，`%c` 会替换为创建者名。
- `name-prefix` / `name-suffix`：自定义名与自动生成名都会加上；自动生成名的序号插在后缀之前，例如 `Bot_1_bot`。
- `name-pattern`：自定义名称的正则，使用自定义名需要 `fakeplayer.spawn.name` 权限。
- `name-style`：Tab 列表中的显示样式。

名称只允许字母、数字和下划线，最长 16 字符，且不能以 `-` 开头。

## 寿命与存档

```yaml
lifespan: 0
persistent-data: true
restore-on-start: false
```

- `lifespan`：假人存活分钟数，`0` 表示永久。
- `persistent-data`：是否保存假人数据。
- `restore-on-start`：启动后恢复上次在线的假人；需要同时开启 `persistent-data`。

## 跟随下线

```yaml
follow-quiting: true
follow-quiting-force: false
follow-quiting-force-delay: 3
```

创建者下线时假人是否跟随下线；跨服切换不会触发。强制模式会在重连后给予宽限时间，避免误删。

## 移除与死亡

```yaml
drop-inventory-on-quiting: false
kick-on-dead: true
death-message: false
advancements: true
share-fakeplayer: false
```

- `kick-on-dead`：假人死亡时直接移除；关闭后可用 `/fp respawn` 复活。
- `share-fakeplayer`：同名假人是否共享存档/UUID；关闭时 usercache 会为每次召唤留下记录，属于已知行为。
