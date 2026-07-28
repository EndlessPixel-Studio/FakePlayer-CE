# FakePlayer-CE NMS 变更总结报告

生成时间: 2026-07-28

---

## 一、模块架构总览

项目通过版本特定模块适配不同 Minecraft 版本的 NMS API，分为两类：

### 完整 NMS 实现模块（含完整 action/network/spi 15 个文件）

| 模块 | 版本 |
|------|------|
| `fakeplayer-v1_21_11` | 1.21.11 |
| `fakeplayer-v1_21_9`  | 1.21.9 |
| `fakeplayer-v1_21_6`  | 1.21.6 |
| `fakeplayer-v1_21_5`  | 1.21.5 |
| `fakeplayer-v1_21_4`  | 1.21.4 |
| `fakeplayer-v1_21_3`  | 1.21.3 |
| `fakeplayer-v1_21`    | 1.21 |
| `fakeplayer-v1_20_6`  | 1.20.6 |
| `fakeplayer-v1_20_4`  | 1.20.4 |
| `fakeplayer-v1_20_2`  | 1.20.2 |
| `fakeplayer-v1_20_1`  | 1.20.1 |
| `fakeplayer-v26_1`    | 26.1 (新版本号方案) |
| `fakeplayer-v26_2`    | 26.2 (新版本号方案) |

### 桥接模块（仅有 NMSBridgeImpl.java，复用邻近完整模块的 NMS 代码）

| 模块 | 声明支持版本 | 代理到 |
|------|-------------|--------|
| `fakeplayer-v1_21_10` | 1.21.10 | v1_21_9 |
| `fakeplayer-v1_21_8`  | 1.21.8  | v1_21_6 |
| `fakeplayer-v1_21_7`  | 1.21.7  | v1_21_6 |
| `fakeplayer-v1_21_1`  | 1.21.1  | v1_21 |
| `fakeplayer-v1_20_5`  | 1.20.5  | v1_20_6 |
| `fakeplayer-v1_20_3`  | 1.20.3  | v1_20_4 |
| `fakeplayer-v26_1_1`  | 26.1.1  | v26_1 |
| `fakeplayer-v26_1_2`  | 26.1.2  | v26_1 |

**意义**: Minecraft 1.21.7/1.21.8/1.21.10 等版本未对 NMS API 引入破坏性变更，因此只需桥接即可兼容。

---

## 二、Minecraft 26.x 系列（新版本号方案）

Minecraft 在 1.21.x 之后采用了新的版本号方案（如 26.0、26.1、26.2），不再使用 `1.x.y` 格式。对应的 NMS 模块以 `v26_x` 命名。

| 模块 | 类型 | 版本 | 代理到 |
|------|------|------|--------|
| `fakeplayer-v26_1`   | 完整模块 | 26.1 | - |
| `fakeplayer-v26_1_1` | 桥接模块 | 26.1.1 | v26_1 |
| `fakeplayer-v26_1_2` | 桥接模块 | 26.1.2 | v26_1 |
| `fakeplayer-v26_2`   | 完整模块 | 26.2 | - |

### 2.0 整体设计

- `fakeplayer-v26_1` 是 26.1.x 系列的完整 NMS 实现（15 个文件），基于 v1_21_11 适配到 Paper 26.1 API。
- `fakeplayer-v26_1_1` / `fakeplayer-v26_1_2` 是桥接模块，仅包含 `NMSBridgeImpl.java`，委托到 `v26_1` 的 NMS 实现。
- `fakeplayer-v26_2` 是 26.2 的完整 NMS 实现（15 个文件），独立的完整模块。

### 2.1 预期 NMS API 变更（参考 FakePlayerPlus 适配经验）

基于 FakePlayerPlus 从 v1_21_11 到 v26_1_1 再到 v26_2 的适配经验，以下 API 在 26.x 中已变更：

| API 变更 | v1_21_11 | v26.x |
|----------|----------|-------|
| 实体运动数据包 | `packet.getXa()`/`getYa()`/`getZa()` (反射) | `packet.movement` (属性) |
| 断开连接 | `disconnect(Component)` | `disconnect(DisconnectionDetails)` |
| 骑乘实体 | `startRiding(entity, force)` | `startRiding(entity, force, boolean)` |
| 包发送监听器 | `ChannelFutureListener` | `PacketSendListener` |
| 玩家数据加载 | `playerIo.load()` | `playerIo.load()` (已存在) |

> **注意**: FakePlayer-CE v1_21_11 已提前使用了部分 26.x API（如 `playerIo.load()`、`CommonListenerCookie`、`getDestroyProgress`），因此 26.x 模块的适配工作量可能比预期的少。实际编译时需要根据 Paper 26.1/26.2 的具体 API 调整。

### 2.2 版本兼容策略

- `fakeplayer-v26_1` 的 `isSupported()` 精确匹配 `"26.1"`，桥接模块 `v26_1_1` / `v26_1_2` 分别精确匹配 `"26.1.1"` / `"26.1.2"`。
- `fakeplayer-v26_2` 的 `isSupported()` 精确匹配 `"26.2"`。
- 若 26.3 未引入破坏性变更，可创建桥接模块 `v26_3` 复用到最近的完整模块。
- 若 26.x 中某个次版本引入重大变更，则创建新的完整模块。

---

## 三、1.21.x 系列 NMS 变更详情

### 3.1 v1_21_11 vs v1_21_9

**变更文件数**: 2 (NMSBridgeImpl, NMSServerImpl)

#### NMSServerImpl — 新增玩家数据从磁盘加载

```diff
+ import net.minecraft.nbt.CompoundTag;
+ import net.minecraft.util.ProblemReporter;
+ import net.minecraft.world.level.storage.TagValueInput;

  public @NotNull NMSServerPlayer newPlayer(...) {
+     var server = new NMSServerImpl(Bukkit.getServer()).getHandle();
      var handle = new ServerPlayer(...);
+     server.getPlayerList().playerIo.load(handle.nameAndId()).ifPresent(nbt -> {
+         var valueInput = TagValueInput.create(
+                 ProblemReporter.DISCARDING,
+                 server.registryAccess(),
+                 nbt
+         );
+         handle.load(valueInput);
+     });
```

**影响**: 创建假玩家时会尝试从 `playerdata/<uuid>.dat` 加载已保存的玩家数据（物品栏、位置等），使假玩家能继承之前保存的状态。这是 1.21.11 引入的 PlayerData I/O 变化。

---

### 3.2 v1_21_9 vs v1_21_6

**变更文件数**: 3 (NMSBridgeImpl, NMSServerPlayerImpl, FakeServerGamePacketListenerImpl)

#### NMSServerPlayerImpl — startRiding 方法签名变化

```diff
- return handle.startRiding(new NMSEntityImpl(entity).getHandle(), force);
+ return handle.startRiding(new NMSEntityImpl(entity).getHandle(), force, true);
```

**影响**: `startRiding` 新增第 3 个 boolean 参数（是否强制骑乘）。1.21.9+ 需要显式传递此参数。

#### FakeServerGamePacketListenerImpl — ServerboundMovePlayerPacket API 重构

v1_21_9 中 `ServerboundMovePlayerPacket` 的 `getXa()/getYa()/getZa()` 方法被移除，改用反射兼容新旧 API：

```diff
- this.player.lerpMotion(packet.getXa(), packet.getYa(), packet.getZa());
+ // Reflection fallback: try getXa/getYa/getZa, then getX/getY/getZ
+ try { packet.getClass().getMethod("getXa")... } catch { ... }
```

**影响**: Leaves/Paper 1.21.9 重构了移动数据包结构，需要使用反射来处理不同子类的方法。

---

### 3.3 v1_21_6 vs v1_21_5

**变更文件数**: 4 (NMSBridgeImpl, NMSServerPlayerImpl, UseAction, FakeConnection)

#### NMSServerPlayerImpl — readExtraData 导入变化

```diff
- import net.minecraft.nbt.CompoundTag;
+ import net.minecraft.core.HolderLookup;
+ import net.minecraft.world.level.storage.ValueInputContextHelper;
```

`readExtraData` 方法参数类型从 `CompoundTag` 变为 `HolderLookup` 相关。

#### UseAction — 世界获取方式变化

```diff
- var world = player.serverLevel();
+ var world = player.level();
```

#### FakeConnection — 包发送监听器类型变化

```diff
- import io.netty.channel.ChannelFutureListener;
+ import net.minecraft.network.PacketSendListener;

- public void send(Packet<?> packet, @Nullable ChannelFutureListener listener) {}
+ public void send(Packet<?> packet, @Nullable PacketSendListener listener) {}

- public void send(Packet<?> packet, @Nullable ChannelFutureListener listener, boolean flag) {}  // 已移除
```

**影响**: Minecraft 1.21.5+ 使用 Netty 层的 `PacketSendListener` 替代 `ChannelFutureListener`，同时移除了三参数版本的 send 方法。

---

### 3.4 v1_21_5 vs v1_21_4

**变更文件数**: 3 (NMSBridgeImpl, NMSServerPlayerImpl, FakePlayerAdvancements)

#### NMSServerPlayerImpl — absMoveTo 替换 absSnapTo

```diff
- handle.absSnapTo(x, y, z, yRot, xRot);
+ handle.absMoveTo(x, y, z, yRot, xRot);
```

**影响**: `absSnapTo` 重命名为 `absMoveTo`，Minecraft 1.21.5 重构了实体移动 API。

#### FakePlayerAdvancements — flushDirty 参数简化

```diff
- public void flushDirty(ServerPlayer player, boolean flag) {
+ public void flushDirty(ServerPlayer player) {
```

**影响**: `flushDirty` 方法移除了 `boolean flag` 参数。

---

### 3.5 v1_21_4 vs v1_21_3

**变更文件数**: 2 (NMSBridgeImpl, FakeServerGamePacketListenerImpl)

#### FakeServerGamePacketListenerImpl — BungeeCord 消息数据提取

```diff
- var message = p.data().array();
+ var message = p.data();
```

**影响**: `CustomPacketPayload.data()` 返回类型从 `ByteBuf` 变为 `byte[]`，不再需要调用 `.array()`。

---

### 3.6 v1_21_3 vs v1_21

**变更文件数**: 4 (NMSBridgeImpl, NMSServerPlayerImpl, MineAction, UseAction)

#### NMSServerPlayerImpl — ParticleStatus 移除

```diff
- import net.minecraft.server.level.ParticleStatus;
  ...
- handle.sendSystemMessage(message, true, ParticleStatus.MINIMAL);
+ handle.sendSystemMessage(message, true);
```

**影响**: `sendSystemMessage` 移除了 `ParticleStatus` 参数。

#### MineAction / UseAction — getMaxBuildHeight 替换 getMaxY

```diff
- player.level().getMaxY()
+ player.level().getMaxBuildHeight()
```

**影响**: Minecraft 1.21.2+ 中 `getMaxY()` 被重命名为 `getMaxBuildHeight()`，涉及 MineAction 中 5 处和 UseAction 中 1 处调用。

---

## 四、1.20.x 系列 NMS 变更详情

### 3.1 v1_20_6 vs v1_20_4

**变更文件数**: 3 (NMSNetworkImpl, FakeConnection, FakeServerGamePacketListenerImpl)

#### NMSNetworkImpl — ConnectionProtocol 初始化

```diff
+ this.connection.setProtocolAttr(ConnectionProtocol.PLAY);
- var cookie = CommonListenerCookie.createInitial(gameProfile, false);
+ var cookie = CommonListenerCookie.createInitial(gameProfile);
```

#### FakeConnection — 新增 setProtocolAttr 方法

```diff
+ public void setProtocolAttr(@NotNull ConnectionProtocol protocol) {
+     this.channel.attr(Connection.ATTRIBUTE_SERVERBOUND_PROTOCOL).set(protocol.codec(PacketFlow.SERVERBOUND));
+     this.channel.attr(Connection.ATTRIBUTE_CLIENTBOUND_PROTOCOL).set(protocol.codec(PacketFlow.CLIENTBOUND));
+ }
```

#### FakeServerGamePacketListenerImpl — CustomPayload 处理大重构

```diff
- import net.minecraft.network.protocol.common.custom.DiscardedPayload;
+ import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

- this.handleCustomPayloadPacket(p);
+ this.handleCustomPayloadPacket(p.payload());

- private void handleCustomPayloadPacket(ClientboundCustomPayloadPacket packet) {
-     var payload = packet.payload();
-     var resourceLocation = payload.type().id();
-     ...
-     if (!(payload instanceof DiscardedPayload p)) {
+ private void handleCustomPayloadPacket(CustomPacketPayload payload) {
+     var channel = payload.id().getNamespace() + ":" + payload.id().getPath();
```

**影响**: 1.20.5+ 中 `CustomPacketPayload` 和 `ClientboundCustomPayloadPacket` API 全面重构。

---

### 3.2 v1_20_4 vs v1_20_2

**变更文件数**: 2 (NMSServerPlayerImpl 格式, FakeServerGamePacketListenerImpl)

#### FakeServerGamePacketListenerImpl — 反射方式获取 BungeeCord 通道数据

v1_20_4 使用直接的 `CustomPacketPayload` API，而 v1_20_2 使用反射兼容不同版本的 `ClientboundCustomPayloadPacket`：

```diff
// v1_20_2 中通过反射获取标识符和数据
+ try {
+     var identifier = packet.getClass().getMethod("getIdentifier").invoke(packet);
+     channel = ...
+ } catch (Exception e) {
+     try { /* 尝试 getId() */ } catch { return; }
+ }
```

---

### 3.3 v1_20_2 vs v1_20_1

**变更文件数**: 7 — 这是 1.20.x 系列中**最大的版本跳跃**

#### NMSNetworkImpl — placeNewPlayer 参数增加

```diff
+ import net.minecraft.network.ConnectionProtocol;
+ import net.minecraft.server.network.CommonListenerCookie;

- MinecraftServer.class.getMethod("placeNewPlayer", Connection.class, ServerPlayer.class);
+ MinecraftServer.class.getMethod("placeNewPlayer", Connection.class, ServerPlayer.class, CommonListenerCookie.class);
- method.invoke(mcServer, connection, handle);
+ method.invoke(mcServer, connection, handle, cookie);
```

#### NMSServerImpl — ClientInformation 参数新增

```diff
+ import net.minecraft.server.level.ClientInformation;
- new ServerPlayer(server, world, new GameProfile(uuid, name));
+ new ServerPlayer(server, world, new GameProfile(uuid, name), ClientInformation.createDefault());
```

#### NMSServerPlayerImpl — ClientInformation 重构

```diff
- import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
+ import net.minecraft.server.level.ClientInformation;
- var option = new ServerboundClientInformationPacket(...);
+ var option = new ClientInformation(...);
```

#### FakeConnection — 连接协议初始化重构

```diff
+ import net.minecraft.network.ConnectionProtocol;
- public void handleDisconnection() { }
+ public void setProtocolAttr(ConnectionProtocol protocol) { ... }
```

#### FakePlayerAdvancements — Advancement 类型重命名

```diff
- import net.minecraft.advancements.Advancement;
+ import net.minecraft.advancements.AdvancementHolder;
- public boolean award(Advancement advancement, String s) { }
+ public boolean award(AdvancementHolder advancement, String s) { }
```

（涉及所有 5 个方法签名的参数类型变更）

#### FakeServerGamePacketListenerImpl — 包位置迁移 + 直接 API vs 反射

```diff
- import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
+ import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;

- super(server, connection, player);
+ import net.minecraft.server.network.CommonListenerCookie;
+ super(server, connection, player, cookie);
```

v1_20_1 使用直接方法调用（`packet.getIdentifier()`, `packet.getData().array()`），v1_20_2 改用反射。

---

## 五、未发生 NMS 变更的版本

以下版本通过桥接模块复用邻近版本的 NMS 代码，表示其 NMS API 与对应版本完全兼容：

| 版本 | 复用版本 | 说明 |
|------|---------|------|
| 1.21.10 | 1.21.9 | 无 NMS 变更 |
| 1.21.8 | 1.21.6 | 无 NMS 变更 |
| 1.21.7 | 1.21.6 | 无 NMS 变更 |
| 1.21.1 | 1.21 | 无 NMS 变更 |
| 1.20.5 | 1.20.6 | 无 NMS 变更 |
| 1.20.3 | 1.20.4 | 无 NMS 变更 |

---

## 六、NMS 最多变更的模块（Top 5）

| 排名 | 模块 | 变更文件数 | 主要变更类别 |
|------|------|-----------|-------------|
| 1 | v1_20_2 (vs v1_20_1) | 7 | 连接协议、玩家创建、数据包、成就系统全面重构 |
| 2 | v26_2 (vs v1_21_11) | 15 | 新版本号方案首个完整模块，版本检测逻辑适配，API 变更待编译验证 |
| 3 | v1_21_6 (vs v1_21_5) | 4 | 实体数据加载、世界获取、包发送监听器、玩家骑乘 |
| 4 | v1_21_3 (vs v1_21) | 4 | 粒子系统、高度获取 API 重命名 |
| 5 | v1_21_9 (vs v1_21_6) | 3 | 移动数据包重构、骑乘方法变化 |
| - | v1_20_6 (vs v1_20_4) | 3 | 连接协议、自定义数据包处理重构 |
| - | v1_21_5 (vs v1_21_4) | 3 | 实体移动 API 重命名、成就方法简化 |
| - | v1_21_11 (vs v1_21_9) | 2 | 玩家数据 IO 新增 |
| - | v1_21_4 (vs v1_21_3) | 2 | BungeeCord 消息数据类型变化 |
| - | v1_20_4 (vs v1_20_2) | 2 | 数据包处理反射 → 直接 API |

---

## 七、NMS 变更趋势总结

1. **数据包处理 API 迭代最频繁**: `FakeServerGamePacketListenerImpl` 和 `FakeConnection` 在几乎所有版本跳跃中都有变化，主要涉及：
   - `ClientboundCustomPayloadPacket` → `CustomPacketPayload` 的迁移
   - `ChannelFutureListener` → `PacketSendListener` 的类型替换
   - `ConnectionProtocol` 属性的显式设置

2. **玩家创建与实体加载**: 从 `ClientInformation` 参数的引入到 `playerIo.load()` 的添加，Minecraft 在逐步细化玩家数据的加载和序列化流程。

3. **API 重命名趋势**: 
   - `getMaxY()` → `getMaxBuildHeight()` (1.21.2+)
   - `absSnapTo()` → `absMoveTo()` (1.21.5+)
   - `Advancement` → `AdvancementHolder` (1.20.2+)

4. **桥接模块证明**: Minecraft 1.21.7/1.21.8/1.21.10 未引入破坏性 NMS 变更，完整的 NMS 模块只需在关键版本创建。

5. **Minecraft 26.x 新版本号方案**: 从 26.0 开始，Minecraft 使用整数版本号（26.x）替代 1.x.y 格式。`NMSBridgeImpl.isSupported()` 的版本检测逻辑从精确匹配改为数值范围比较。Paper 的 `paperDevBundle` 也同步更新为 `26.2-R0.1-SNAPSHOT` 格式。

6. **API 前向兼容**: FakePlayer-CE 的 v1_21_11 模块已预先使用了部分 26.x 的 API（如 `playerIo.load()`、`CommonListenerCookie`），说明 Paper 团队在 1.21.11 中已开始向后移植 26.x 的 API，降低了后续适配的复杂度。
