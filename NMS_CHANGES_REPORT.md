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

### 桥接模块（仅有 NMSBridgeImpl.java，复用邻近完整模块的 NMS 代码）

| 模块 | 声明支持版本 | 代理到 |
|------|-------------|--------|
| `fakeplayer-v1_21_10` | 1.21.10 | v1_21_9 |
| `fakeplayer-v1_21_8`  | 1.21.8  | v1_21_6 |
| `fakeplayer-v1_21_7`  | 1.21.7  | v1_21_6 |
| `fakeplayer-v1_21_1`  | 1.21.1  | v1_21 |
| `fakeplayer-v1_20_5`  | 1.20.5  | v1_20_6 |
| `fakeplayer-v1_20_3`  | 1.20.3  | v1_20_4 |

**意义**: Minecraft 1.21.7/1.21.8/1.21.10 等版本未对 NMS API 引入破坏性变更，因此只需桥接即可兼容。

---

## 二、1.21.x 系列 NMS 变更详情

### 2.1 v1_21_11 vs v1_21_9

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

### 2.2 v1_21_9 vs v1_21_6

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

### 2.3 v1_21_6 vs v1_21_5

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

### 2.4 v1_21_5 vs v1_21_4

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

### 2.5 v1_21_4 vs v1_21_3

**变更文件数**: 2 (NMSBridgeImpl, FakeServerGamePacketListenerImpl)

#### FakeServerGamePacketListenerImpl — BungeeCord 消息数据提取

```diff
- var message = p.data().array();
+ var message = p.data();
```

**影响**: `CustomPacketPayload.data()` 返回类型从 `ByteBuf` 变为 `byte[]`，不再需要调用 `.array()`。

---

### 2.6 v1_21_3 vs v1_21

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

## 三、1.20.x 系列 NMS 变更详情

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

## 四、未发生 NMS 变更的版本

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

## 五、NMS 最多变更的模块（Top 5）

| 排名 | 模块 | 变更文件数 | 主要变更类别 |
|------|------|-----------|-------------|
| 1 | v1_20_2 (vs v1_20_1) | 7 | 连接协议、玩家创建、数据包、成就系统全面重构 |
| 2 | v1_21_6 (vs v1_21_5) | 4 | 实体数据加载、世界获取、包发送监听器、玩家骑乘 |
| 3 | v1_21_3 (vs v1_21) | 4 | 粒子系统、高度获取 API 重命名 |
| 4 | v1_21_9 (vs v1_21_6) | 3 | 移动数据包重构、骑乘方法变化 |
| 5 | v1_20_6 (vs v1_20_4) | 3 | 连接协议、自定义数据包处理重构 |
| - | v1_21_5 (vs v1_21_4) | 3 | 实体移动 API 重命名、成就方法简化 |
| - | v1_21_11 (vs v1_21_9) | 2 | 玩家数据 IO 新增 |
| - | v1_21_4 (vs v1_21_3) | 2 | BungeeCord 消息数据类型变化 |
| - | v1_20_4 (vs v1_20_2) | 2 | 数据包处理反射 → 直接 API |

---

## 六、NMS 变更趋势总结

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
