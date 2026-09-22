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
import org.bukkit.enchantments.Enchantment;
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    private final Set<ReplenishRequest> pendingContainerReturns = new HashSet<>();
    private final Map<ReplenishRequest, Boolean> pendingToolReplacements = new HashMap<>();

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
     * 设置假人是否自动替换低耐久工具
     */
    public void setReplaceTools(@NotNull Player target, boolean replaceTools) {
        if (!replaceTools) {
            target.removeMetadata(MetadataKeys.REPLACE_TOOLS, Main.getInstance());
        } else {
            target.setMetadata(MetadataKeys.REPLACE_TOOLS, new FixedMetadataValue(Main.getInstance(), true));
        }
    }

    /**
     * 判断假人是否自动替换低耐久工具
     */
    public boolean isReplaceTools(@NotNull Player target) {
        return target.hasMetadata(MetadataKeys.REPLACE_TOOLS);
    }

    /**
     * 消耗物品自动填装
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onItemUse(@NotNull PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        this.replenishIfSingleItem(player, event.getHand(), event.getItem(), true);
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
        var item = event.getBrokenItem();
        var slot = this.getHoldingHand(player, item);
        if (slot == null) {
            return;
        }

        if (slot == EquipmentSlot.HAND && this.isReplaceTools(player)) {
            this.replaceToolLater(player, slot, item, true);
            return;
        }

        if (!this.isReplenish(player)) {
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
        if (item == null) {
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
        if (maxDurability <= 0) {
            return;
        }

        var remainingDurability = maxDurability - damageable.getDamage() - event.getDamage();
        if (slot == EquipmentSlot.HAND
                && this.isReplaceTools(player)
                && (remainingDurability <= 0
                    || (this.hasMendingEnchant(held)
                        && remainingDurability <= this.config.getToolReplacementRemainingDurabilityThreshold()))) {
            this.replaceToolLater(player, slot, held, remainingDurability <= 0);
        }

        if (!this.isReplenish(player) || damageable.getDamage() + event.getDamage() < maxDurability) {
            return;
        }

        if (slot == EquipmentSlot.HAND && this.isReplaceTools(player)) {
            // Tool replacement handles the broken item and avoids racing the regular refill path.
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
        this.replenishLater(target, slot, item, false);
    }

    private void replenishLater(
            @NotNull Player target,
            @NotNull EquipmentSlot slot,
            @NotNull ItemStack item,
            boolean allowContainerReturn
    ) {
        var requires = item.clone();
        var request = new ReplenishRequest(target.getUniqueId(), slot);
        if (allowContainerReturn) {
            // PlayerInteractEvent may have queued the same request earlier in this tick.
            // Keep the stronger consume-event information even when the request is already pending.
            this.pendingContainerReturns.add(request);
        }
        if (!this.pendingReplenishments.add(request)) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            this.pendingReplenishments.remove(request);
            var allowRemainderStorage = this.pendingContainerReturns.remove(request);
            if (!target.isOnline()) {
                return;
            }
            var held = target.getInventory().getItem(slot);
            if (held != null && !held.getType().isAir() && held.getAmount() != 0) {
                if (allowRemainderStorage) {
                    this.replenishAfterContainerReturn(target, slot, requires, held);
                }
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
     * If consuming an item leaves its vanilla container remainder in hand, store that remainder
     * in the inventory and move a matching replacement item into the hand.
     */
    private boolean replenishAfterContainerReturn(
            @NotNull Player target,
            @NotNull EquipmentSlot slot,
            @NotNull ItemStack required,
            @NotNull ItemStack remainder
    ) {
        var expectedRemainder = this.getContainerRemainder(required.getType());
        if (expectedRemainder == null || remainder.getType() != expectedRemainder) {
            return false;
        }

        var inventory = target.getInventory();
        var storage = inventory.getStorageContents();
        var handIndex = inventory.getHeldItemSlot();
        var replacementSlot = -1;
        for (int i = storage.length - 1; i >= 0; i--) {
            if (i == handIndex) {
                continue;
            }
            var candidate = storage[i];
            if (candidate != null && this.isReplenishmentMatch(candidate, required)) {
                replacementSlot = i;
                break;
            }
        }
        if (replacementSlot < 0) {
            return false;
        }

        // Plan the container insertion first. If inventory storage cannot hold it, leave it in hand.
        var remainderToStore = remainder.clone();
        var plannedStorage = storage.clone();
        for (int i = 0; i < plannedStorage.length && remainderToStore.getAmount() > 0; i++) {
            if (i == handIndex || i == replacementSlot) {
                continue;
            }
            var existing = plannedStorage[i];
            if (existing == null || existing.getType().isAir() || !existing.isSimilar(remainderToStore)) {
                continue;
            }
            var movable = Math.min(remainderToStore.getAmount(), existing.getMaxStackSize() - existing.getAmount());
            if (movable > 0) {
                existing = existing.clone();
                existing.setAmount(existing.getAmount() + movable);
                plannedStorage[i] = existing;
                remainderToStore.setAmount(remainderToStore.getAmount() - movable);
            }
        }
        for (int i = 0; i < plannedStorage.length && remainderToStore.getAmount() > 0; i++) {
            if (i == handIndex || i == replacementSlot) {
                continue;
            }
            var existing = plannedStorage[i];
            if (existing != null && !existing.getType().isAir()) {
                continue;
            }
            var movable = Math.min(remainderToStore.getAmount(), remainderToStore.getMaxStackSize());
            var placed = remainderToStore.clone();
            placed.setAmount(movable);
            plannedStorage[i] = placed;
            remainderToStore.setAmount(remainderToStore.getAmount() - movable);
        }
        if (remainderToStore.getAmount() > 0) {
            return false;
        }

        var replacement = inventory.getItem(replacementSlot);
        if (replacement == null || !this.isReplenishmentMatch(replacement, required)) {
            return false;
        }
        for (int i = 0; i < plannedStorage.length; i++) {
            if (i != handIndex && i != replacementSlot && !java.util.Objects.equals(storage[i], plannedStorage[i])) {
                inventory.setItem(i, plannedStorage[i]);
            }
        }
        inventory.setItem(slot, replacement.clone());
        inventory.setItem(replacementSlot, null);
        return true;
    }

    @Nullable
    private Material getContainerRemainder(@NotNull Material consumedItem) {
        return switch (consumedItem) {
            case MILK_BUCKET -> Material.BUCKET;
            case MUSHROOM_STEW, RABBIT_STEW, BEETROOT_SOUP, SUSPICIOUS_STEW -> Material.BOWL;
            case HONEY_BOTTLE, POTION -> Material.GLASS_BOTTLE;
            default -> null;
        };
    }

    /**
     * Replenish a stack after a /fp drop action empties the main hand.
     */
    public void replenishAfterDrop(@NotNull Player target, @NotNull ItemStack droppedItem) {
        if (!this.isReplenish(target) || droppedItem.getType().isAir() || droppedItem.getAmount() <= 0) {
            return;
        }
        var held = target.getInventory().getItemInMainHand();
        if (held != null && !held.getType().isAir() && held.getAmount() > 0) {
            return;
        }
        this.replenishLater(target, EquipmentSlot.HAND, droppedItem);
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
        this.replenishFromNearbyChest(target, slot, item, false);
    }

    private void replenishFromNearbyChest(
            @NotNull Player target,
            @NotNull EquipmentSlot slot,
            @NotNull ItemStack item,
            boolean matchToolType
    ) {
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
                    if (replacement != null && (matchToolType
                            ? this.isToolReplacementCandidate(replacement, item)
                            : this.isReplenishmentMatch(replacement, item))) {
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
     * Apply GCA's default policy to a nearly worn main-hand Mending tool.
     */
    public void replaceWornTool(@NotNull Player target) {
        var item = target.getInventory().getItemInMainHand();
        if (item == null || item.getType().isAir() || !(item.getItemMeta() instanceof Damageable damageable)) {
            return;
        }
        if (!this.hasMendingEnchant(item)) {
            return;
        }

        var maxDurability = item.getType().getMaxDurability();
        if (maxDurability <= 0) {
            return;
        }

        var remainingDurability = maxDurability - damageable.getDamage();
        if (remainingDurability > this.config.getToolReplacementRemainingDurabilityThreshold()) {
            return;
        }

        this.replaceToolFromInventory(target, EquipmentSlot.HAND, item);
    }

    /**
     * Wait for the current damage event to finish before looking at the hand and swapping items.
     */
    private void replaceToolLater(
            @NotNull Player target,
            @NotNull EquipmentSlot slot,
            @NotNull ItemStack item,
            boolean broken
    ) {
        var required = item.clone();
        var request = new ReplenishRequest(target.getUniqueId(), slot);
        var existingRequest = this.pendingToolReplacements.putIfAbsent(request, broken);
        if (existingRequest != null) {
            if (broken && !existingRequest) {
                this.pendingToolReplacements.put(request, true);
            }
            return;
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            var wasBroken = Boolean.TRUE.equals(this.pendingToolReplacements.remove(request));
            if (!target.isOnline() || !this.isReplaceTools(target)) {
                return;
            }

            var held = target.getInventory().getItem(slot);
            var current = held == null || held.getType().isAir() ? null : held;
            if (current != null) {
                if (!this.isToolReplacementMatch(current, required)
                        || !(current.getItemMeta() instanceof Damageable damageable)) {
                    return;
                }
                var maxDurability = current.getType().getMaxDurability();
                if (maxDurability <= 0) {
                    return;
                }
                var remainingDurability = maxDurability - damageable.getDamage();
                if (wasBroken && remainingDurability <= 0) {
                    target.getInventory().setItem(slot, null);
                    current = null;
                } else if (wasBroken) {
                    // GCA keeps a currently valid replacement candidate in hand.
                    if (this.isToolReplacementCandidate(current, required)) {
                        return;
                    }
                } else if (!this.hasMendingEnchant(current)
                        || remainingDurability > this.config.getToolReplacementRemainingDurabilityThreshold()) {
                    return;
                }
            }

            if (!this.replaceToolFromInventory(target, slot, current == null ? required : current)
                    && current == null
                    && this.isReplenish(target)
                    && Optional.ofNullable(manager.getCreator(target))
                               .filter(creator -> creator.hasPermission(Permission.replenishFromChest))
                               .isPresent()) {
                this.replenishFromNearbyChest(target, slot, required, true);
            }
        }, 1);
    }

    /**
     * Use the first GCA-eligible item of the same type, then swap slots.
     */
    private boolean replaceToolFromInventory(
            @NotNull Player target,
            @NotNull EquipmentSlot slot,
            @NotNull ItemStack required
    ) {
        if (slot != EquipmentSlot.HAND || !(required.getItemMeta() instanceof Damageable)) {
            return false;
        }

        var inventory = target.getInventory();
        var storage = inventory.getStorageContents();
        var selectedSlot = inventory.getHeldItemSlot();
        for (int i = 0; i < storage.length; i++) {
            if (i == selectedSlot) {
                continue;
            }

            var replacement = inventory.getItem(i);
            if (!this.isToolReplacementCandidate(replacement, required)) {
                continue;
            }

            var worn = inventory.getItemInMainHand();
            inventory.setItem(i, worn == null || worn.getType().isAir() ? null : worn.clone());
            inventory.setItemInMainHand(replacement.clone());
            return true;
        }
        return false;
    }

    /**
     * 在指定交互确实消耗了单个物品时安排补货。
     */
    private void replenishIfSingleItem(
            @NotNull Player target,
            @Nullable EquipmentSlot slot,
            @Nullable ItemStack item
    ) {
        this.replenishIfSingleItem(target, slot, item, false);
    }

    private void replenishIfSingleItem(
            @NotNull Player target,
            @Nullable EquipmentSlot slot,
            @Nullable ItemStack item,
            boolean allowContainerReturn
    ) {
        if (!this.isReplenish(target) || slot == null || item == null || item.getAmount() != 1) {
            return;
        }

        this.replenishLater(target, slot, item, allowContainerReturn);
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
     * Carpet-style tool matching compares the item type and ignores item components such as enchantments.
     */
    private boolean isToolReplacementMatch(@NotNull ItemStack replacement, @NotNull ItemStack required) {
        return replacement.getType() == required.getType();
    }

    /**
     * GCA's default mode accepts every same-type non-Mending tool and only Mending tools
     * with more remaining durability than the configured threshold.
     */
    private boolean isToolReplacementCandidate(@Nullable ItemStack candidate, @NotNull ItemStack required) {
        if (candidate == null || candidate.getType().isAir()
                || !this.isToolReplacementMatch(candidate, required)
                || !(candidate.getItemMeta() instanceof Damageable damageable)) {
            return false;
        }
        if (!this.hasMendingEnchant(candidate)) {
            return true;
        }

        var maxDurability = candidate.getType().getMaxDurability();
        return maxDurability > 0
                && maxDurability - damageable.getDamage() > this.config.getToolReplacementRemainingDurabilityThreshold();
    }

    private boolean hasMendingEnchant(@NotNull ItemStack item) {
        return item.getEnchantmentLevel(Enchantment.MENDING) > 0;
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
