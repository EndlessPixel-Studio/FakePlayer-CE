package io.github.hello09x.fakeplayer.v1_21.spi;

import io.github.hello09x.fakeplayer.core.util.Reflections;

import io.github.hello09x.fakeplayer.api.spi.NMSEntity;
import lombok.Getter;
import net.minecraft.world.entity.Entity;

import org.jetbrains.annotations.NotNull;

public class NMSEntityImpl implements NMSEntity {

    @Getter
    private final Entity handle;

    public NMSEntityImpl(@NotNull org.bukkit.entity.@NotNull Entity entity) {
        this.handle = (Entity) Reflections.getHandle(entity);
    }


}
