package io.github.hello09x.fakeplayer.v26_3.action;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.component.SwingAnimation;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;


public class AttackAction extends TraceAction {

    private final ServerPlayer player;

    public AttackAction(ServerPlayer player) {
        super(player);
        this.player = player;
    }


    @Override
    public boolean tick() {
        var hit = this.getTarget();
        if (hit == null) {
            return false;
        }

        if (hit.getType() != HitResult.Type.ENTITY) {
            return false;
        }

        var entityHit = (EntityHitResult) hit;
        player.attack(entityHit.getEntity());
        // 26.3: swing(InteractionHand, SwingAnimation, boolean)
        player.swing(InteractionHand.MAIN_HAND, SwingAnimation.DEFAULT, true);
        player.resetAttackStrengthTicker();
        player.resetLastActionTime();
        return true;
    }

    @Override
    public void inactiveTick() {

    }

    @Override
    public void stop() {

    }


}
