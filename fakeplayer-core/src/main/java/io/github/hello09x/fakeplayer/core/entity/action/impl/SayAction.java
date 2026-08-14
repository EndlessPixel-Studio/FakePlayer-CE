package io.github.hello09x.fakeplayer.core.entity.action.impl;

import io.github.hello09x.fakeplayer.api.spi.Action;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SayAction implements Action {

    @NotNull
    private final NMSServerPlayer player;

    @Nullable
    private final String message;

    public SayAction(@NotNull NMSServerPlayer player, @Nullable String message) {
        this.player = player;
        this.message = message;
    }

    @Override
    public boolean tick() {
        if (message != null && !message.isEmpty()) {
            player.chat(message);
        }
        player.resetLastActionTime();
        return true;
    }

    @Override
    public void inactiveTick() {
        // SAY 无持续状态, 无需处理
    }

    @Override
    public void stop() {
        // SAY 无持续状态, 无需处理
    }
}
