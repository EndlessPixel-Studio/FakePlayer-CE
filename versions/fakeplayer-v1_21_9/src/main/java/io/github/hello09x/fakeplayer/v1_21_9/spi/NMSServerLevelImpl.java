package io.github.hello09x.fakeplayer.v1_21_9.spi;

import io.github.hello09x.fakeplayer.core.util.Reflections;

import io.github.hello09x.fakeplayer.api.spi.NMSServerLevel;
import lombok.Getter;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.World;

import org.jetbrains.annotations.NotNull;

public class NMSServerLevelImpl implements NMSServerLevel {

    @Getter
    private final ServerLevel handle;

    public NMSServerLevelImpl(@NotNull World world) {
        this.handle = (ServerLevel) Reflections.getHandle(world);
    }

}
