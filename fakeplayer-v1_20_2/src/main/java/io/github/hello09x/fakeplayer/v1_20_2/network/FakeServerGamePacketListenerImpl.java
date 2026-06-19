package io.github.hello09x.fakeplayer.v1_20_2.network;

import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jetbrains.annotations.NotNull;

public class FakeServerGamePacketListenerImpl extends ServerGamePacketListenerImpl implements NMSServerGamePacketListener {

    private final FakeplayerManager manager = Main.getInjector().getInstance(FakeplayerManager.class);

    public FakeServerGamePacketListenerImpl(
            @NotNull MinecraftServer server,
            @NotNull Connection connection,
            @NotNull ServerPlayer player,
            @NotNull CommonListenerCookie cookie
    ) {
        super(server, connection, player, cookie);
        Bukkit.getMessenger().registerOutgoingPluginChannel(Main.getInstance(), StandardMessenger.validateAndCorrectChannel(BUNGEE_CORD_CHANNEL));
    }

    @Override
    public void send(Packet<?> packet) {
        if (packet instanceof ClientboundCustomPayloadPacket p) {
            this.handleCustomPayloadPacket(p);
        }
    }

    private void handleCustomPayloadPacket(@NotNull ClientboundCustomPayloadPacket packet) {
        String channel = null;
        try {
            var identifier = (net.minecraft.resources.ResourceLocation) packet.getClass().getMethod("getIdentifier").invoke(packet);
            channel = StandardMessenger.validateAndCorrectChannel(identifier.getNamespace() + ":" + identifier.getPath());
        } catch (Exception e) {
            try {
                var id = (net.minecraft.resources.ResourceLocation) packet.getClass().getMethod("getId").invoke(packet);
                channel = StandardMessenger.validateAndCorrectChannel(id.getNamespace() + ":" + id.getPath());
            } catch (Exception ignored) {
                return;
            }
        }

        if (!channel.equals(BUNGEE_CORD_CHANNEL)) {
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

        byte[] message = null;
        try {
            var data = (net.minecraft.network.FriendlyByteBuf) packet.getClass().getMethod("getData").invoke(packet);
            message = data.array();
        } catch (Exception e) {
            try {
                var data = (net.minecraft.network.FriendlyByteBuf) packet.getClass().getMethod("getData").invoke(packet);
                message = new byte[data.readableBytes()];
                data.getBytes(data.readerIndex(), message);
            } catch (Exception ignored) {
                return;
            }
        }

        recipient.sendPluginMessage(Main.getInstance(), channel, message);
    }

}
