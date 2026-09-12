package io.github.hello09x.fakeplayer.core.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.entity.FakeplayerTicker;
import io.github.hello09x.fakeplayer.core.util.OfflineCreator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * 记录当前在线的假人, 并在服务器下次启动时自动恢复。
 * <p>开启 {@code restore-on-start} 后:
 * <ul>
 *     <li>周期性把在线假人 (名称 / 创建者 / 位置) 写入快照文件, 关服前再写一次</li>
 *     <li>服务器启动完成的 {@link ServerLoadEvent} 后, 按快照逐个异步重建假人</li>
 * </ul>
 */
@Singleton
public class FakeplayerRestoreManager implements Listener {

    private final static Logger log = Main.getInstance().getLogger();

    private final static String FILE_NAME = "restore-fakeplayers.txt";
    private final static String SEPARATOR = "|";
    private final static long SNAPSHOT_INTERVAL_TICKS = 20L * 30;

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;

    @Inject
    public FakeplayerRestoreManager(FakeplayerManager manager, FakeplayerConfig config) {
        this.manager = manager;
        this.config = config;
    }

    /**
     * 注册周期性快照任务 (插件启用时调用)
     * <p>任务始终注册, 是否记录由 {@link #saveSnapshot()} 内的配置开关决定, 以便 /fp reload 后动态生效。</p>
     */
    public void start() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(
                Main.getInstance(),
                this::saveSnapshot,
                SNAPSHOT_INTERVAL_TICKS,
                SNAPSHOT_INTERVAL_TICKS
        );
    }

    /**
     * 将当前在线假人 (名称 / 创建者 / 位置) 写入快照文件
     */
    public void saveSnapshot() {
        if (!config.isRestoreOnStart()) {
            return;
        }
        var lines = new ArrayList<String>();
        for (var player : manager.getAll()) {
            var creator = manager.getCreatorName(player);
            if (creator == null) {
                continue;
            }
            var location = player.getLocation();
            lines.add(String.join(SEPARATOR,
                    player.getName(),
                    creator,
                    location.getWorld().getName(),
                    String.valueOf(location.getX()),
                    String.valueOf(location.getY()),
                    String.valueOf(location.getZ()),
                    String.valueOf(location.getYaw()),
                    String.valueOf(location.getPitch())
            ));
        }
        this.write(lines);
    }

    /**
     * 服务器启动完成后, 按快照自动恢复假人
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerLoad(@NotNull ServerLoadEvent event) {
        if (event.getType() != ServerLoadEvent.LoadType.STARTUP) {
            return;
        }
        if (!config.isRestoreOnStart()) {
            return;
        }
        var entries = this.read();
        if (entries.isEmpty()) {
            return;
        }

        log.info("Restoring %d fake player(s) from the last session...".formatted(entries.size()));
        var lifespan = Optional.ofNullable(config.getLifespan()).map(Duration::toMillis).orElse(FakeplayerTicker.NON_REMOVE_AT);

        // 逐个重建需要等待, 放到异步线程串行执行 (与 /spawn 的异步生成流程一致)
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> {
            var restored = 0;
            for (var entry : entries) {
                try {
                    var location = this.resolveLocation(entry);
                    var player = manager.spawnAsync(new OfflineCreator(entry.creator()), entry.name(), location, lifespan)
                                         .get(30, TimeUnit.SECONDS);
                    if (player != null) {
                        restored++;
                        log.info("Restored fake player %s (creator: %s)".formatted(entry.name(), entry.creator()));
                    }
                } catch (Exception e) {
                    log.warning("Failed to restore fake player %s: %s".formatted(entry.name(), e.getMessage()));
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
            log.info("Restore finished: %d/%d fake player(s) restored".formatted(restored, entries.size()));
        });
    }

    private @NotNull Location resolveLocation(@NotNull Entry entry) {
        var world = Bukkit.getWorld(entry.world());
        if (world == null) {
            world = Bukkit.getWorlds().get(0);
        }
        return new Location(world, entry.x(), entry.y(), entry.z(), entry.yaw(), entry.pitch());
    }

    private void write(@NotNull List<String> lines) {
        try {
            var folder = Main.getInstance().getDataFolder();
            if (!folder.exists() && !folder.mkdirs()) {
                return;
            }
            var tmp = folder.toPath().resolve(FILE_NAME + ".tmp");
            var target = folder.toPath().resolve(FILE_NAME);
            Files.writeString(tmp, String.join("\n", lines), StandardCharsets.UTF_8);
            Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            log.warning("Failed to save fake player restore list: " + e);
        }
    }

    private @NotNull List<Entry> read() {
        var file = Main.getInstance().getDataFolder().toPath().resolve(FILE_NAME);
        if (!Files.isRegularFile(file)) {
            return List.of();
        }
        try {
            var entries = new ArrayList<Entry>();
            for (var line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                var entry = this.parse(line);
                if (entry != null) {
                    entries.add(entry);
                }
            }
            return entries;
        } catch (IOException e) {
            log.warning("Failed to read fake player restore list: " + e);
            return List.of();
        }
    }

    private @Nullable Entry parse(@NotNull String line) {
        var parts = line.trim().split("\\" + SEPARATOR);
        if (parts.length < 8 || parts[0].isBlank() || parts[1].isBlank()) {
            return null;
        }
        try {
            return new Entry(
                    parts[0],
                    parts[1],
                    parts[2],
                    Double.parseDouble(parts[3]),
                    Double.parseDouble(parts[4]),
                    Double.parseDouble(parts[5]),
                    Float.parseFloat(parts[6]),
                    Float.parseFloat(parts[7])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record Entry(
            @NotNull String name,
            @NotNull String creator,
            @NotNull String world,
            double x,
            double y,
            double z,
            float yaw,
            float pitch
    ) {
    }

}
