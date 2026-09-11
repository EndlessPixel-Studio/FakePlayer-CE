package io.github.hello09x.fakeplayer.core.manager;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.utils.BlockUtils;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.command.Permission;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.constant.MetadataKeys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * @author tanyaofei
 * @since 2024/8/11
 **/
@Singleton
public class FakeplayerReplenishManager implements Listener {

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;
    private final Set<ReplenishRequest> pendingReplenishments = new HashSet<>();

    @Inject
    public FakeplayerReplenishManager(FakeplayerManager manager, FakeplayerConfig config) {
        this.manager = manager;
        this.config = config;
    }

    /**
     * 设置假人是否自动填装
     *
     * @param target    假人
     * @param replenish 是否自动补货
     */
    public void setReplenish(@NotNull Player target, boolean replenish) {
        if (!replenish) {
            target.removeMetadata(MetadataKeys.REPLENISH, Main.getInstance());
        } else {
            target.setMetadata(MetadataKeys.REPLENISH, new FixedMetadataValue(Main.getInstance(), true));
        }
    }

    /**
     * 判断假人是否自动补货
     *
     * @param target 假人
     * @return 是否自动补货
     */
    public boolean isReplenish(@NotNull Player target) {
        return target.hasMetadata(MetadataKeys.REPLENISH);
    }

    /**
     * 消耗物品自动填装
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemUse(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        this.replenishIfSingleItem(player, event.getHand(), event.getItem());
    }

    /**
     * 普通方块或空气交互自动填装
     *
     * <p>铲子将泥土变为土径、骨粉催熟等行为不会触发
     * {@link PlayerItemConsumeEvent}，但会在交互后消耗手上的物品。</p>
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        this.replenishIfSingleItem(event.getPlayer(), event.getHand(), event.getItem());
    }

    /**
     * 实体交互自动填装
     *
     * <p>喂食动物等行为使用独立的实体交互事件。</p>
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInteractEntity(@NotNull PlayerInteractEntityEvent event) {
        var player = event.getPlayer();
        var slot = event.getHand();
        this.replenishIfSingleItem(player, slot, slot == null ? null : player.getInventory().getItem(slot));
    }

    /**
     * 放置方块自动填装
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBlockPlace(@NotNull BlockPlaceEvent event) {
        this.replenishIfSingleItem(event.getPlayer(), event.getHand(), event.getItemInHand());
    }

    /**
     * 物品损坏自动填装
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemBreak(@NotNull PlayerItemBreakEvent event) {
        var player = event.getPlayer();
        if (!this.isReplenish(player)) {
            return;
        }

        var item = event.getBrokenItem();
        var slot = this.getHoldingHand(player, item);
        if (slot == null) {
            return;
        }

        this.replenishLater(player, slot, item);
    }

    /**
     * 工具即将损坏时自动填装
     *
     * <p>{@link PlayerItemBreakEvent} 在不同服务端实现中的触发时机可能位于手上物品
     * 被移除前后。利用耐久事件在物品仍然位于玩家手上时记录补货请求，可以避免无法识别手
     * 部位的问题。</p>
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemDamage(@NotNull PlayerItemDamageEvent event) {
        var player = event.getPlayer();
        var item = event.getItem();
        if (!this.isReplenish(player) || item == null) {
            return;
        }

        var slot = this.getHoldingHand(player, item);
        if (slot == null) {
            return;
        }

        var held = player.getInventory().getItem(slot);
        if (held == null || !(held.getItemMeta() instanceof Damageable damageable)) {
            return;
        }

        var maxDurability = held.getType().getMaxDurability();
        if (maxDurability <= 0 || damageable.getDamage() + event.getDamage() < maxDurability) {
            return;
        }

        this.replenishLater(player, slot, held);
    }

    /**
     * 发射投掷物, 如扔喷溅型药水 自动填装
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onProjectileLaunch(@NotNull PlayerLaunchProjectileEvent event) {
        var player = event.getPlayer();
        if (!this.isReplenish(player)) {
            return;
        }

        var item = event.getItemStack();
        var slot = this.getHoldingHand(player, item);
        this.replenishIfSingleItem(player, slot, item);
    }

    /**
     * 在下一 tick 填装物品
     *
     * @param target 玩家
     * @param slot   填充位置
     * @param item   要填充的物品
     */
    public void replenishLater(@NotNull Player target, @NotNull EquipmentSlot slot, @NotNull ItemStack item) {
        var requires = item.clone();
        item = null;    // 以防下面的代码用到了这个值
        var request = new ReplenishRequest(target.getUniqueId(), slot);
        if (!this.pendingReplenishments.add(request)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            this.pendingReplenishments.remove(request);
            if (!target.isOnline()) {
                return;
            }
            var held = target.getInventory().getItem(slot);
            if (held != null && !held.getType().isAir() && held.getAmount() != 0) {
                return;
            }

            if (!this.replenishFromInventory(target, slot, requires)) {
                if (Optional.ofNullable(manager.getCreator(target))
                            .filter(creator -> creator.hasPermission(Permission.replenishFromChest))
                            .isPresent()
                ) {
                    this.replenishFromNearbyChest(target, slot, requires);
                }
            }

        }, 1);  // delay 1 是因为要等手上的物品在此 tick 消耗完
    }

    /**
     * 从背包里补货
     *
     * @param target 假人
     * @param slot   补充到哪只手
     * @param item   需要补货的物品
     * @return 是否补货了
     */
    private boolean replenishFromInventory(@NotNull Player target, @NotNull EquipmentSlot slot, @NotNull ItemStack item) {
        var inv = target.getInventory();
        for (int i = inv.getSize() - 1; i >= 0; i--) {
            var replacement = inv.getItem(i);
            if (replacement != null && this.isReplenishmentMatch(replacement, item)) {
                inv.setItem(slot, replacement);
                inv.setItem(i, null);
                return true;
            }
        }
        return false;
    }

    /**
     * 从附近的箱子里补货
     *
     * @param target 假人
     * @param slot   补充到哪只手
     * @param item   需要补货的物品
     */
    public void replenishFromNearbyChest(@NotNull Player target, @NotNull EquipmentSlot slot, @NotNull ItemStack item) {
        var blocks = BlockUtils.getNearbyBlocks(target.getLocation(), 4, Material.CHEST);
        for (var block : blocks) {
            var openEvent = new PlayerInteractEvent(
                    target,
                    Action.RIGHT_CLICK_BLOCK,
                    target.getInventory().getItemInOffHand(),
                    block,
                    BlockFace.NORTH
            );
            if (!openEvent.callEvent()) {
                // 无法打开箱子
                continue;
            }

            if (target.openInventory(((Chest) block.getState()).getBlockInventory()) == null) {
                continue;
            }

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
                if (!target.isOnline()) {
                    return;
                }
                var view = target.getOpenInventory();
                if (view == null) {
                    return;
                }
                var inv = view.getTopInventory();
                if (inv.getType() != InventoryType.CHEST) {
                    // 被其他插件取消了, 变成打开自己的背包了
                    target.closeInventory(InventoryCloseEvent.Reason.PLAYER);
                    return;
                }
                for (int i = inv.getSize() - 1; i >= 0; i--) {
                    var replacement = inv.getItem(i);
                    if (replacement != null && this.isReplenishmentMatch(replacement, item)) {
                        var event = new InventoryClickEvent(
                                view,
                                InventoryType.SlotType.CONTAINER,
                                i,
                                ClickType.SHIFT_LEFT,
                                InventoryAction.MOVE_TO_OTHER_INVENTORY
                        );
                        if (!event.callEvent()) {
                            // 无法操作箱子
                            break;
                        }

                        target.getInventory().setItem(slot, replacement);
                        inv.setItem(i, null);
                        break;
                    }
                }
                target.closeInventory(InventoryCloseEvent.Reason.PLAYER);
            }, 20);
            return;
        }
    }

    /**
     * 自动修复磨损的工具
     * <p>工具靠耐久消耗, 而耐久下降时不会触发任何事件, 因此无法被 {@link #replenishLater} 处理.
     * 这里周期性检查双手持有物品的磨损程度, 超过配置阈值时直接修复 (把 Damageable 的 damage 归零).</p>
     *
     * @param target 假人
     */
    public void replenishTools(@NotNull Player target) {
        var threshold = this.config.getReplenishToolsDurabilityThreshold();
        if (threshold <= 0) {
            return;
        }

        for (var slot : new EquipmentSlot[]{EquipmentSlot.HAND, EquipmentSlot.OFF_HAND}) {
            var item = target.getInventory().getItem(slot);
            if (item == null || item.getType().isAir() || !(item.getItemMeta() instanceof Damageable damageable)) {
                continue;
            }

            var maxDurability = item.getType().getMaxDurability();
            if (maxDurability <= 0) {
                continue;
            }

            var damaged = damageable.getDamage();
            if (damaged <= 0) {
                continue;
            }

            var wornPercent = damaged * 100 / maxDurability;
            if (wornPercent < threshold) {
                continue;
            }

            damageable.setDamage(0);
            item.setItemMeta(damageable);
            target.getInventory().setItem(slot, item);
        }
    }

    /**
     * 在指定交互确实消耗了单个物品时安排补货。
     */
    private void replenishIfSingleItem(
            @NotNull Player target,
            @Nullable EquipmentSlot slot,
            @Nullable ItemStack item
    ) {
        if (!this.isReplenish(target) || slot == null || item == null || item.getAmount() != 1) {
            return;
        }

        this.replenishLater(target, slot, item);
    }

    /**
     * 判断两个物品是否可以互相补充。
     *
     * <p>工具的耐久值会随着使用变化，但补货时新工具不应因为耐久不同而被排除。</p>
     */
    private boolean isReplenishmentMatch(@NotNull ItemStack replacement, @NotNull ItemStack required) {
        if (replacement.isSimilar(required)) {
            return true;
        }

        if (!(replacement.getItemMeta() instanceof Damageable)
                || !(required.getItemMeta() instanceof Damageable)) {
            return false;
        }

        var replacementCopy = replacement.clone();
        var requiredCopy = required.clone();
        var replacementMeta = (Damageable) replacementCopy.getItemMeta();
        var requiredMeta = (Damageable) requiredCopy.getItemMeta();
        replacementMeta.setDamage(0);
        requiredMeta.setDamage(0);
        replacementCopy.setItemMeta(replacementMeta);
        requiredCopy.setItemMeta(requiredMeta);
        return replacementCopy.isSimilar(requiredCopy);
    }

    /**
     * 获取玩家哪只手持有对应的物品
     *
     * @param player 玩家
     * @param item   对应的物品
     * @return 哪只手
     */
    private @Nullable EquipmentSlot getHoldingHand(@NotNull Player player, @NotNull ItemStack item) {
        var inv = player.getInventory();
        if (item.equals(inv.getItemInMainHand())) {
            return EquipmentSlot.HAND;
        } else if (item.equals(inv.getItemInOffHand())) {
            return EquipmentSlot.OFF_HAND;
        } else {
            return null;
        }
    }

    private record ReplenishRequest(@NotNull UUID playerId, @NotNull EquipmentSlot slot) {
    }

}
