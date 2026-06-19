package io.github.hello09x.fakeplayer.v1_20_1.spi;

import io.github.hello09x.fakeplayer.core.util.Reflections;

import io.github.hello09x.fakeplayer.api.spi.NMSNetwork;
import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.v1_20_1.network.FakeConnection;
import io.github.hello09x.fakeplayer.v1_20_1.network.FakeServerGamePacketListenerImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public class NMSNetworkImpl implements NMSNetwork {

    private final @NotNull FakeConnection connection;

    private NMSServerGamePacketListener serverGamePacketListener;

    public NMSNetworkImpl(
            @NotNull InetAddress address
    ) {
        this.connection = new FakeConnection(address);
    }

    @Override
    public @NotNull NMSServerGamePacketListener placeNewPlayer(@NotNull Server server, @NotNull Player player) {
        var handle = (ServerPlayer) Reflections.getHandle(player);
        var mcServer = (MinecraftServer) Reflections.getHandle(server);
        try {
            var method = MinecraftServer.class.getMethod("placeNewPlayer", net.minecraft.network.Connection.class, ServerPlayer.class);
            method.invoke(mcServer, this.connection, handle);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var listener = new FakeServerGamePacketListenerImpl(
                mcServer,
                this.connection,
                handle
        );
        this.serverGamePacketListener = listener;
        handle.connection = listener;
        return listener;
    }

    @NotNull
    @Override
    public NMSServerGamePacketListener getServerGamePacketListener() throws IllegalStateException {
        if (this.serverGamePacketListener == null) {
            throw new IllegalStateException("not initialized");
        }
        return this.serverGamePacketListener;
    }

}
