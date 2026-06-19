package io.github.hello09x.fakeplayer.v1_21_9.network;

import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.DiscardedPayload;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Logger;

public class FakeServerGamePacketListenerImpl extends ServerGamePacketListenerImpl implements NMSServerGamePacketListener {

    private final FakeplayerManager manager = Main.getInjector().getInstance(FakeplayerManager.class);
    private final static Logger log = Main.getInstance().getLogger();

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
        } else if (packet instanceof ClientboundSetEntityMotionPacket p) {
            this.handleClientboundSetEntityMotionPacket(p);
        }
    }

    public void handleClientboundSetEntityMotionPacket(@NotNull ClientboundSetEntityMotionPacket packet) {
        if (packet.getId() == this.player.getId() && this.player.hurtMarked) {
            Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                this.player.hurtMarked = true;
                try {
                    var xa = (double) packet.getClass().getMethod("getXa").invoke(packet);
                    var ya = (double) packet.getClass().getMethod("getYa").invoke(packet);
                    var za = (double) packet.getClass().getMethod("getZa").invoke(packet);
                    this.player.lerpMotion(new net.minecraft.world.phys.Vec3(xa, ya, za));
                } catch (Exception e) {
                    try {
                        var x = (double) packet.getClass().getMethod("getX").invoke(packet);
                        var y = (double) packet.getClass().getMethod("getY").invoke(packet);
                        var z = (double) packet.getClass().getMethod("getZ").invoke(packet);
                        this.player.lerpMotion(new net.minecraft.world.phys.Vec3(x, y, z));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }

    private void handleCustomPayloadPacket(@NotNull ClientboundCustomPayloadPacket packet) {
        var payload = packet.payload();
        var resourceLocation = payload.type().id();
        var channel = resourceLocation.getNamespace() + ":" + resourceLocation.getPath();

        if (!channel.equals(BUNGEE_CORD_CORRECTED_CHANNEL)) {
            return;
        }

        if (!(payload instanceof DiscardedPayload p)) {
            return;
        }

        var recipient = Bukkit
                .getOnlinePlayers()
                .stream()
                .filter(manager::isNotFake)
                .findAny()
                .orElse(null);

        if (recipient == null) {
            log.warning("Failed to forward a plugin message cause non real players in the server");
            return;
        }

        var data = p.data();
        recipient.sendPluginMessage(Main.getInstance(), BUNGEE_CORD_CHANNEL, data);
    }

}
