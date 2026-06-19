package io.github.hello09x.fakeplayer.v1_21_11.spi;

import io.github.hello09x.fakeplayer.core.util.Reflections;

import com.mojang.authlib.GameProfile;
import io.github.hello09x.devtools.core.utils.WorldUtils;
import io.github.hello09x.fakeplayer.api.spi.NMSServer;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import org.bukkit.Bukkit;
import org.bukkit.Server;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NMSServerImpl implements NMSServer {


    @Getter
    private final MinecraftServer handle;

    public NMSServerImpl(@NotNull Server server) {
        this.handle = (MinecraftServer) Reflections.getHandle(server);
    }

    @Override
    public @NotNull NMSServerPlayer newPlayer(@NotNull UUID uuid, @NotNull String name) {
        var server = new NMSServerImpl(Bukkit.getServer()).getHandle();
        var handle = new ServerPlayer(
                new NMSServerImpl(Bukkit.getServer()).getHandle(),
                new NMSServerLevelImpl(WorldUtils.getMainWorld()).getHandle(),
                new GameProfile(uuid, name),
                ClientInformation.createDefault()
        );
        server.getPlayerList().playerIo.load(handle.nameAndId()).ifPresent(nbt -> {
            var valueInput = TagValueInput.create(
                    ProblemReporter.DISCARDING,
                    server.registryAccess(),
                    nbt
            );
            handle.load(valueInput);
        });
        return new NMSServerPlayerImpl(handle.getBukkitEntity());
    }
}