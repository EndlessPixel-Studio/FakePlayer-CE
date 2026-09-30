package io.github.hello09x.fakeplayer.v1_20_1.network;

import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jetbrains.annotations.NotNull;

public class FakeServerGamePacketListenerImpl extends ServerGamePacketListenerImpl implements NMSServerGamePacketListener {

    private final FakeplayerManager manager = Main.getInjector().getInstance(FakeplayerManager.class);

    public FakeServerGamePacketListenerImpl(
            @NotNull MinecraftServer server,
            @NotNull Connection connection,
            @NotNull ServerPlayer player
    ) {
        super(server, connection, player);
        Bukkit.getMessenger().registerOutgoingPluginChannel(Main.getInstance(), StandardMessenger.validateAndCorrectChannel(BUNGEE_CORD_CHANNEL));
    }

    @Override
    public void send(Packet<?> packet) {
        if (packet instanceof ClientboundCustomPayloadPacket p) {
            this.handleCustomPayloadPacket(p);
        }
    }

    private void handleCustomPayloadPacket(@NotNull ClientboundCustomPayloadPacket packet) {
        var channel = StandardMessenger.validateAndCorrectChannel(packet.getIdentifier().getNamespace() + ":" + packet.getIdentifier().getPath());
        if (!channel.equals(BUNGEE_CORD_CORRECTED_CHANNEL)) {
            return;
        }

        var recipient = Bukkit
                .getOnlinePlayers()
                .stream()
                .filter(manager::isNotFake)
                .findAny()
                .orElse(null);
        if (recipient == null) {
            return;
        }

        var data = packet.getData();
        var message = new byte[data.readableBytes()];
        data.getBytes(data.readerIndex(), message);
        recipient.sendPluginMessage(Main.getInstance(), BUNGEE_CORD_CHANNEL, message);
    }


    /**
     * 自定义延迟 (ping)
     * <p>1.20.1 的延迟还是 {@link ServerGamePacketListenerImpl} 上的字段 (没有 {@code latency()} 方法),
     * 因此这里通过反射写入; 原版每 30 秒会广播一次延迟, 写入后最迟 30 秒内会在 Tab 列表生效</p>
     *
     * @param ping      延迟 (毫秒), 负数表示不修改
     * @param broadcast 该版本无法立即广播, 忽略该参数
     */
    @Override
    public void setPing(int ping, boolean broadcast) {
        if (ping < 0) {
            return;
        }

        try {
            var field = ServerGamePacketListenerImpl.class.getDeclaredField("latency");
            field.setAccessible(true);
            field.setInt(this, ping);
        } catch (ReflectiveOperationException e) {
            Main.getInstance().getLogger().warning("Failed to set the ping: " + e.getMessage());
        }
    }

}
