package io.github.hello09x.fakeplayer.v1_21_5.network;

import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.network.FakeChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.util.logging.Logger;

public class FakeConnection extends Connection {

    private final static Logger log = Main.getInstance().getLogger();
    private final FakeplayerManager manager = Main.getInjector().getInstance(FakeplayerManager.class);

    public FakeConnection(@NotNull InetAddress address) {
        super(PacketFlow.SERVERBOUND);
        this.channel = new FakeChannel(null, address);
        this.address = this.channel.remoteAddress();
        Connection.configureSerialization(this.channel.pipeline(), PacketFlow.SERVERBOUND, false, null);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public void send(Packet<?> packet, @Nullable PacketSendListener listener) {
        // 假人没有真实客户端, 主动回应 keepalive, 否则会被判定超时踢出 (Folia 会 tick 连接)
        if (packet instanceof ClientboundKeepAlivePacket keepAlive
                && this.getPacketListener() instanceof ServerGamePacketListenerImpl gameListener) {
            try {
                gameListener.handleKeepAlive(new ServerboundKeepAlivePacket(keepAlive.getId()));
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public void send(Packet<?> packet) { this.send(packet, null); }

}