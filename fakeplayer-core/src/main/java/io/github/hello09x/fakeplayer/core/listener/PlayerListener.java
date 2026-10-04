package io.github.hello09x.fakeplayer.core.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.jetbrains.annotations.NotNull;

@Singleton
public class PlayerListener implements Listener {

    private final FakeplayerManager manager;

    @Inject
    public PlayerListener(FakeplayerManager manager) {
        this.manager = manager;
    }

    /**
     * 玩家蹲伏时取消假人骑乘
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onSneak(@NotNull PlayerToggleSneakEvent event) {
        var player = event.getPlayer();
        var passengers = player.getPassengers();
        if (passengers.isEmpty()) {
            return;
        }

        for (var passenger : passengers) {
            if (!(passenger instanceof Player target)) {
                continue;
            }
            if (manager.isFake(target)) {
                player.removePassenger(target);
            }
        }
    }

    /**
     * 无敌模式: 开启无敌的假人不应受到伤害, 包括创造模式玩家的攻击。
     * <p>原版 {@code Entity#isInvulnerableTo()} 的条件里带 {@code !source.isCreativePlayer()},
     * 即**创造模式玩家的攻击被显式放行** (方便在创造模式下打无敌实体)。因此只靠
     * {@link Player#setInvulnerable(boolean)} 的话, 管理员在创造模式下打假人依然会掉血,
     * 看起来就是「无敌模式无效」(实体标记其实已经设上了)。</p>
     * <p>这里只拦「直接造成伤害的创造模式玩家」; 原版本就该绕过无敌的来源
     * (虚空、{@code /kill} 等) 不受影响。</p>
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInvulnerableFakeDamaged(@NotNull EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)
                || !victim.isInvulnerable()
                || !manager.isFake(victim)) {
            return;
        }

        if (event.getDamageSource().getDirectEntity() instanceof Player damager
                && damager.getGameMode() == GameMode.CREATIVE) {
            event.setCancelled(true);
        }
    }

}
