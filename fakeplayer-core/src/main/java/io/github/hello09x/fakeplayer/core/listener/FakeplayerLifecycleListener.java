package io.github.hello09x.fakeplayer.core.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanyaofei
 * @since 2024/8/7
 **/
@Singleton
public class FakeplayerLifecycleListener implements Listener {

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;

    @Inject
    public FakeplayerLifecycleListener(FakeplayerManager manager, FakeplayerConfig config) {
        this.manager = manager;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPostSpawn(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (this.manager.isNotFake(player)) {
            // Not a fake player
            return;
        }

        manager.dispatchCommands(player, config.getPostSpawnCommands());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAfterSpawn(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (this.manager.isNotFake(player)) {
            // Not a fake player
            return;
        }

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (player.isOnline()) {
                manager.dispatchCommands(player, config.getAfterSpawnCommands());
                manager.issueCommands(player, config.getSelfCommands());
            }
        }, 20);
    }

    //K:假人UUID V:创建者名称
    private final Map<UUID, String> pendingFakeQuits = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPostQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (this.manager.isNotFake(player)) {
            // Not a fake player
            return;
        }
        pendingFakeQuits.put(player.getUniqueId(), this.manager.getCreatorName(player));
        manager.dispatchCommands(player, config.getPostQuitCommands());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAfterQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();
        if (!pendingFakeQuits.containsKey(uuid)) return;
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            try {
                manager.dispatchCommands(new FakeplayerManager.DispatchCommandArgs(player.getName(),uuid.toString(),pendingFakeQuits.get(uuid)), config.getAfterQuitCommands());
            } finally {
                pendingFakeQuits.remove(uuid);
            }
        }, 1);
    }
}