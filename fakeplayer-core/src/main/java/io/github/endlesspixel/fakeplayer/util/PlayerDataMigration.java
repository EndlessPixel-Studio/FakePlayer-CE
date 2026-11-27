package io.github.endlesspixel.fakeplayer.util;

import io.github.hello09x.fakeplayer.core.Main;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * 把玩家数据文件从一个 UUID 迁移到另一个 UUID。
 *
 * <p>重命名假人时会换一个 UUID, 而背包 / 末影箱 / 成就 / 统计都按 UUID 存在世界目录里, 不搬就会丢。
 *
 * <p>目录结构随版本变过, 这里两种都试一遍 (存在才搬):
 *
 * <ul>
 *   <li>1.20 ~ 1.21.x: {@code <world>/playerdata|advancements|stats/<uuid>.{dat,json}}
 *   <li>26.x: {@code <world>/players/data|advancements|stats/<uuid>.{dat,json}}
 * </ul>
 */
public final class PlayerDataMigration {

  private static final Logger log = Main.getInstance().getLogger();

  private static final List<String> DATA_DIRS = List.of("playerdata", "data");

  private static final List<String> JSON_DIRS = List.of("advancements", "stats");

  private PlayerDataMigration() {}

  /**
   * 迁移世界目录下属于 {@code from} 的玩家数据到 {@code to}。
   *
   * <p>只在文件存在时才搬, 目标文件已存在则覆盖。失败只记日志, 不影响重命名本身。
   *
   * @param world 世界 (数据总是存在主世界目录下, 调用方应传主世界)
   * @param from 旧 UUID
   * @param to 新 UUID
   */
  public static void migrate(@NotNull World world, @NotNull UUID from, @NotNull UUID to) {
    if (from.equals(to)) {
      return;
    }

    var folder = world.getWorldFolder().toPath();
    // 老版本直接放在世界目录下, 26.x 起多了一层 players/
    for (var root : List.of(folder, folder.resolve("players"))) {
      for (var dir : DATA_DIRS) {
        move(root.resolve(dir), from, to, ".dat");
        move(root.resolve(dir), from, to, ".dat_old");
      }
      for (var dir : JSON_DIRS) {
        move(root.resolve(dir), from, to, ".json");
      }
    }
  }

  private static void move(
      @NotNull Path folder, @NotNull UUID from, @NotNull UUID to, @NotNull String suffix) {
    var source = folder.resolve(from + suffix);
    if (!Files.isRegularFile(source)) {
      return;
    }
    try {
      Files.move(
          source,
          folder.resolve(to + suffix),
          StandardCopyOption.REPLACE_EXISTING,
          StandardCopyOption.ATOMIC_MOVE);
    } catch (IOException e) {
      // 跨文件系统时 ATOMIC_MOVE 会失败, 退回普通移动
      try {
        Files.move(source, folder.resolve(to + suffix), StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e2) {
        log.warning("Failed to migrate player data " + source + " -> " + to + suffix + ": " + e2);
      }
    }
  }
}
