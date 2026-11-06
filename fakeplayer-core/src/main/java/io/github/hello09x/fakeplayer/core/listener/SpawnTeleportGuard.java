package io.github.hello09x.fakeplayer.core.listener;

import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 出生过程中只允许传送到插件自己指定的落点。
 *
 * <p>其它插件（如 Essentials 的 spawn 处理）可能在假人刚进服时把它传送到别处， 这时生成流程算出的落点就失效了，因此在出生窗口内拒绝外来传送。
 */
@Singleton
public class SpawnTeleportGuard implements Listener {

  private final Map<UUID, Set<String>> allowed = new ConcurrentHashMap<>();

  /**
   * 记录本次出生允许的落点
   *
   * @param player 假人
   * @param locations 允许的落点；为 null 的项会被忽略
   */
  public void begin(@NotNull Player player, @Nullable Location @NotNull ... locations) {
    var keys = new HashSet<String>(locations.length);
    for (var location : locations) {
      if (location != null && location.getWorld() != null) {
        keys.add(key(location));
      }
    }
    this.allowed.put(player.getUniqueId(), keys);
  }

  /** 出生流程结束（无论成功失败）后解除限制 */
  public void end(@NotNull Player player) {
    this.allowed.remove(player.getUniqueId());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onTeleport(@NotNull PlayerTeleportEvent event) {
    var keys = this.allowed.get(event.getPlayer().getUniqueId());
    if (keys == null || keys.isEmpty()) {
      return;
    }

    var to = event.getTo();
    if (to == null || to.getWorld() == null) {
      return;
    }

    if (!keys.contains(key(to))) {
      event.setCancelled(true);
    }
  }

  private static String key(@NotNull Location location) {
    return location.getWorld().getName()
        + ':'
        + location.getBlockX()
        + ':'
        + location.getBlockY()
        + ':'
        + location.getBlockZ();
  }
}
