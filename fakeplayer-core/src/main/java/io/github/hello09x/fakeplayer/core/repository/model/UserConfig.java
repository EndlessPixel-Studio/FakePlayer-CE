package io.github.hello09x.fakeplayer.core.repository.model;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;

public record UserConfig(
    Integer id, @NotNull UUID playerId, @NotNull Feature key, @NotNull String value) {}
