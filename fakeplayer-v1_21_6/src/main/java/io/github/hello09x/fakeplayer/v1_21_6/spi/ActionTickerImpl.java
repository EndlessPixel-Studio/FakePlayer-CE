package io.github.hello09x.fakeplayer.v1_21_6.spi;

import io.github.hello09x.fakeplayer.core.util.Reflections;


import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionTicker;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.entity.action.BaseActionTicker;
import io.github.hello09x.fakeplayer.v1_21_6.action.AttackAction;
import io.github.hello09x.fakeplayer.v1_21_6.action.MineAction;
import io.github.hello09x.fakeplayer.v1_21_6.action.UseAction;

import org.bukkit.entity.Player;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;

public class ActionTickerImpl extends BaseActionTicker implements ActionTicker {

    public ActionTickerImpl(@NotNull NMSBridge nms, @NotNull Player player, @NotNull ActionType action, @NotNull ActionSetting setting) {
        super(nms, player, action, setting);
        if (this.action == null) {
            this.action = switch (action) {
                case ATTACK -> new AttackAction((ServerPlayer) Reflections.getHandle(player));
                case MINE -> new MineAction((ServerPlayer) Reflections.getHandle(player));
                case USE -> new UseAction((ServerPlayer) Reflections.getHandle(player));
                case JUMP, LOOK_AT_NEAREST_ENTITY, DROP_INVENTORY, DROP_STACK, DROP_ITEM ->
                        throw new UnsupportedOperationException();
            };
        }
    }

}
