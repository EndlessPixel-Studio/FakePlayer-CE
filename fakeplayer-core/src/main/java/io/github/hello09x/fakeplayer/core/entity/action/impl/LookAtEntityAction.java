package io.github.hello09x.fakeplayer.core.entity.action.impl;

import io.github.hello09x.fakeplayer.api.spi.Action;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import io.github.hello09x.fakeplayer.core.util.LookUtils;
import java.util.Collection;
import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LookAtEntityAction implements Action {

  @NotNull private final NMSServerPlayer player;

  public LookAtEntityAction(@NotNull NMSServerPlayer player) {
    this.player = player;
  }

  @Override
  public boolean tick() {
    var bukkitPlayer = player.getPlayer();
    var entities =
        bukkitPlayer.getLocation().getNearbyEntitiesByType(Damageable.class, 4.5, 4.5, 4.5);
    if (entities.isEmpty()) {
      return false;
    }

    var entity = this.getNearestEntity(bukkitPlayer, entities);
    if (entity == null) {
      return false;
    }

    LookUtils.lookAt(player, aimPoint(entity));
    player.resetLastActionTime();
    return true;
  }

  /**
   * 视线落点: 活着的实体看向眼睛, 其余看向碰撞箱中心。
   *
   * <p>直接看向 {@code getLocation()} 是脚底坐标, 近距离时俯仰角会明显偏低。
   */
  private static @NotNull Location aimPoint(@NotNull Entity entity) {
    if (entity instanceof LivingEntity living) {
      return living.getEyeLocation();
    }
    var center = entity.getBoundingBox().getCenter();
    return new Location(entity.getWorld(), center.getX(), center.getY(), center.getZ());
  }

  @Override
  public void inactiveTick() {}

  @Override
  public void stop() {}

  private @Nullable Entity getNearestEntity(
      @NotNull Player player, @NotNull Collection<? extends Entity> entities) {
    if (entities.isEmpty()) {
      return null;
    }
    // 以自身位置为圆心的查询会把自己也查出来 (距离 0), 必须排除,
    // 否则假人会一直「看自己」, 表现为朝一个固定角度发呆
    var selfId = player.getUniqueId();
    var loc = player.getLocation();
    Entity nearest = null;
    double distance = Double.MAX_VALUE;
    for (var entity : entities) {
      if (!entity.isValid() || entity.getUniqueId().equals(selfId)) {
        continue;
      }
      var d = loc.distanceSquared(entity.getLocation());
      if (d < distance) {
        nearest = entity;
        distance = d;
      }
    }
    return nearest;
  }
}
