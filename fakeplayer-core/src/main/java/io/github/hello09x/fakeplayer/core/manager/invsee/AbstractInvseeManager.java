package io.github.hello09x.fakeplayer.core.manager.invsee;

import io.github.hello09x.devtools.core.utils.ComponentUtils;
import io.github.hello09x.fakeplayer.core.command.Permission;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * @author tanyaofei
 * @since 2024/8/12
 **/
public abstract class AbstractInvseeManager implements InvseeManager {

    protected final FakeplayerManager manager;
    protected final FakeplayerList fakeplayerList;
    protected final FakeplayerConfig config;

    protected AbstractInvseeManager(FakeplayerManager manager, FakeplayerList fakeplayerList, FakeplayerConfig config) {
        this.manager = manager;
        this.fakeplayerList = fakeplayerList;
        this.config = config;
    }

    @Override
    public boolean invsee(@NotNull Player viewer, @NotNull Player whom) {
        var fp = fakeplayerList.getByUUID(whom.getUniqueId());
        if (fp == null) {
            return false;
        }
        if (!viewer.hasPermission(Permission.invsee) && !viewer.isOp() && !fp.isCreatedBy(viewer) && !config.isAllowNonOpOpenInv()) {
            viewer.sendMessage(translatable("fakeplayer.command.invsee.error.no-permission"));
            return false;
        }
        var view = this.openInventory(viewer, whom);
        if (view == null) {
            return false;
        }
        whom.getLocation().getWorld().playSound(
                whom.getLocation(),
                Sound.BLOCK_CHEST_OPEN,
                SoundCategory.BLOCKS,
                0.3F, 1.0F
        );
        return true;
    }

    protected abstract @Nullable InventoryView openInventory(@NotNull Player viewer, @NotNull Player whom);

    /**
     * 打开假人的末影箱, 权限判定与 {@link #invsee(Player, Player)} 保持一致
     */
    @Override
    public boolean enderchest(@NotNull Player viewer, @NotNull Player whom) {
        var fp = fakeplayerList.getByUUID(whom.getUniqueId());
        if (fp == null) {
            return false;
        }
        if (!viewer.hasPermission(Permission.enderchest)
                && !viewer.isOp()
                && !fp.isCreatedBy(viewer)
                && !config.isAllowNonOpOpenInv()) {
            viewer.sendMessage(translatable("fakeplayer.command.enderchest.error.no-permission"));
            return false;
        }
        var view = this.openEnderChest(viewer, whom);
        if (view == null) {
            return false;
        }
        whom.getLocation().getWorld().playSound(
                whom.getLocation(),
                Sound.BLOCK_ENDER_CHEST_OPEN,
                SoundCategory.BLOCKS,
                0.3F, 1.0F
        );
        return true;
    }

    /**
     * 打开假人的末影箱。假人始终在线, 直接取 {@link Player#getEnderChest()} 即可,
     * 两种实现 (原生 / OpenInv) 通用, 需要时可以覆盖
     */
    protected @Nullable InventoryView openEnderChest(@NotNull Player viewer, @NotNull Player whom) {
        var view = viewer.openInventory(whom.getEnderChest());
        if (view != null) {
            view.setTitle(ComponentUtils.toString(translatable(
                    "fakeplayer.manager.enderchest.title",
                    text(whom.getName())
            ), viewer.locale()));
        }
        return view;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void rightClickToInvsee(@NotNull PlayerInteractAtEntityEvent event) {
        if (!((event.getRightClicked()) instanceof Player whom)) {
            return;
        }

        // 潜行右键打开末影箱, 直接右键打开背包
        if (event.getPlayer().isSneaking()) {
            this.enderchest(event.getPlayer(), whom);   // fakeplayer check here
            return;
        }

        this.invsee(event.getPlayer(), whom);   // fakeplayer check here
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void fixDragInventory(@NotNull InventoryDragEvent event) {
        var top = event.getView().getTopInventory();
        if (top.getType() == InventoryType.PLAYER && top.getHolder() instanceof Player whom && manager.isFake(whom)) {
            if (event.getNewItems().keySet().stream().anyMatch(slot -> slot > 35)) {    // > 35 表示从假人背包拖动到玩家背包, 这种操作会出现问题
                event.setCancelled(true);
            }
        }
    }
}
