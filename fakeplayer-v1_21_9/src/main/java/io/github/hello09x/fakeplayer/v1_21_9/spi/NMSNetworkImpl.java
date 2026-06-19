package io.github.hello09x.fakeplayer.v1_21_9.spi;

import io.github.hello09x.fakeplayer.core.util.Reflections;
import io.github.hello09x.fakeplayer.api.spi.NMSNetwork;
import io.github.hello09x.fakeplayer.api.spi.NMSServerGamePacketListener;
import io.github.hello09x.fakeplayer.v1_21_9.network.FakeConnection;
import io.github.hello09x.fakeplayer.v1_21_9.network.FakeServerGamePacketListenerImpl;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

public class NMSNetworkImpl implements NMSNetwork {

    @NotNull
    private final FakeConnection connection;

    private NMSServerGamePacketListener serverGamePacketListener;

    public NMSNetworkImpl(
            @NotNull InetAddress address
    ) {
        this.connection = new FakeConnection(address);
    }

    @NotNull
    @Override
    public NMSServerGamePacketListener placeNewPlayer(
            @NotNull Server server,
            @NotNull Player player
    ) {
        var handle = (ServerPlayer) Reflections.getHandle(player);
        var profile = player.getPlayerProfile();
        var gameProfile = new com.mojang.authlib.GameProfile(profile.getUniqueId(), profile.getName());
        var cookie = CommonListenerCookie.createInitial(gameProfile, false);
        var mcServer = (MinecraftServer) Reflections.getHandle(server);
        try {
            var method = MinecraftServer.class.getMethod("placeNewPlayer", net.minecraft.network.Connection.class, ServerPlayer.class, CommonListenerCookie.class);
            method.invoke(mcServer, this.connection, handle, cookie);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        var listener = new FakeServerGamePacketListenerImpl(
                mcServer,
                this.connection,
                handle,
                cookie
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
