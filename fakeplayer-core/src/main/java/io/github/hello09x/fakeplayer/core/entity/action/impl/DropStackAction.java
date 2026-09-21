package io.github.hello09x.fakeplayer.core.entity.action.impl;

import io.github.hello09x.fakeplayer.api.spi.Action;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import io.github.hello09x.fakeplayer.core.repository.model.Singletons;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class DropStackAction implements Action {

    @NotNull
    private final NMSServerPlayer player;
    @NotNull
    private final Player bukkitPlayer;

    public DropStackAction(@NotNull NMSServerPlayer player, @NotNull Player bukkitPlayer) {
        this.player = player;
        this.bukkitPlayer = bukkitPlayer;
    }


    @Override
    public boolean tick() {
        var droppedItem = this.bukkitPlayer.getInventory().getItemInMainHand().clone();
        player.drop(true);
        if (this.isMainHandEmpty()) {
            Singletons.replenishManager.get().replenishAfterDrop(this.bukkitPlayer, droppedItem);
        }
        player.resetLastActionTime();
        return true;
    }

    private boolean isMainHandEmpty() {
        ItemStack held = this.bukkitPlayer.getInventory().getItemInMainHand();
        return held == null || held.getType().isAir() || held.getAmount() <= 0;
    }

    @Override
    public void inactiveTick() {

    }

    @Override
    public void stop() {

    }

}
