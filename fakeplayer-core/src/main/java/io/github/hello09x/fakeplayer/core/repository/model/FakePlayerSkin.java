package io.github.hello09x.fakeplayer.core.repository.model;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * @author tanyaofei
 * @since 2024/8/8
 */
public record FakePlayerSkin(
    @NotNull UUID playerId, @NotNull UUID creatorId, @NotNull UUID targetId) {}
