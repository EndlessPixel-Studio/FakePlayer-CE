# Changelog

## fp.build6

### English

- **Fixed fake players could not be removed (Issue #3).** `/fp kill` and `/fp killall` now actually deregister the fake player entity from the server. Fake players are injected through a custom `NetworkManager`, where `Player.kick()` is a no-op on the fake connection; the old logic only cleared the internal record without disconnecting the entity. The cleanup flow now calls NMS `PlayerList.remove(ServerPlayer)` to truly remove them, and is compatible with Leaf forks (`CraftServer#getServer()` returns the `PlayerList` directly on Leaf).
- Includes the previous build5 fix: resolved `ClassCastException` when spawning on Leaf 1.21.11 / 26.2 (`DedicatedPlayerList` cannot be cast to `MinecraftServer`).
- Note: vanilla `/kick` does not affect fake players by design (they are not real connections). Use `/fp kill` or `/fp killall` to remove them.
- Requires the CommandAPI 12.0.0 dependency plugin.

### 中文

- **修复假人无法移除（Issue #3）。** `/fp kill` 与 `/fp killall` 现在会真正从服务器注销假人实体。假人通过自定义 `NetworkManager` 注入，`Player.kick()` 在 fake 连接上为空操作，旧逻辑仅清空内部记录而未断开实体。清理流程现改为调用 NMS `PlayerList.remove(ServerPlayer)` 真正移除，并兼容 Leaf 分支（`CraftServer#getServer()` 在 Leaf 上直接返回 `PlayerList`）。
- 包含此前 build5 的修复：解决 Leaf 1.21.11 / 26.2 上 `/fp spawn` 的 `ClassCastException`（`DedicatedPlayerList` 无法转换为 `MinecraftServer`）。
- 说明：vanilla `/kick` 对假人无效为预期设计（假人并非真实连接），请使用 `/fp kill` 或 `/fp killall` 移除假人。
- 需要 CommandAPI 12.0.0 前置插件。

## fp.build5

### English

- Upgraded to CommandAPI 12.0.0 and migrated off the removed `UTF8ResourceBundleControl` (now self-hosted) to keep translations loading correctly on modern runtimes.
- Resolved `NoClassDefFoundError: net/kyori/adventure/util/UTF8ResourceBundleControl` on load.

### 中文

- 升级至 CommandAPI 12.0.0，并迁移掉已被移除的 `UTF8ResourceBundleControl`（改为内置实现），以在现代运行环境正确加载翻译。
- 解决加载时的 `NoClassDefFoundError: net/kyori/adventure/util/UTF8ResourceBundleControl`。
