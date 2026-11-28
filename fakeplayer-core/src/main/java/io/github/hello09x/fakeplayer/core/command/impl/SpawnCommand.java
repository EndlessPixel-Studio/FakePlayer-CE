package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.*;

import com.google.common.base.Throwables;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.command.exception.CommandException;
import io.github.hello09x.devtools.command.exception.HandleCommandException;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.command.Permission;
import io.github.hello09x.fakeplayer.core.entity.FakeplayerTicker;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.util.Mth;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class SpawnCommand extends AbstractCommand {

  private static final DateTimeFormatter REMOVE_AT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

  private static String toLocationString(@NotNull Location location) {
    return location.getWorld().getName()
        + ": "
        + StringUtils.joinWith(
            ", ",
            Mth.floor(location.getX(), 0.5),
            Mth.floor(location.getY(), 0.5),
            Mth.floor(location.getZ(), 0.5));
  }

  /** 创建假人 */
  @HandleCommandException
  public void spawn(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    var name = (String) args.get("name");
    if (name != null && (name.isEmpty() || name.equals("-"))) {
      // "-" 让只有 spawn.location 权限的玩家也能跳过名称参数去指定坐标
      name = null;
    }
    if (name != null && !sender.hasPermission(Permission.spawnName)) {
      throw new CommandException(translatable("fakeplayer.command.spawn.error.no-permission-name"));
    }
    this.spawn(sender, name, this.resolveSpawnpoint(sender, args), null);
  }

  /** 用指定 UUID 创建假人 */
  @HandleCommandException
  public void spawnWithUuid(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    var name = (String) args.get("name");
    var raw = (String) args.get("uuid");
    UUID uuid;
    try {
      uuid = UUID.fromString(Objects.requireNonNull(raw).trim());
    } catch (IllegalArgumentException | NullPointerException e) {
      LangUtils.send(
          sender,
          translatable("fakeplayer.spawn.error.uuid.invalid", text(String.valueOf(raw), GOLD))
              .color(RED));
      return;
    }

    this.spawn(sender, name, this.resolveSpawnpoint(sender, args), uuid);
  }

  private @NotNull Location resolveSpawnpoint(
      @NotNull CommandSender sender, @NotNull CommandArguments args) {
    var world = (World) args.get("world");
    var location = (Location) args.get("location");
    if ((world != null || location != null) && !sender.hasPermission(Permission.spawnLocation)) {
      throw new CommandException(
          translatable("fakeplayer.command.spawn.error.no-permission-location"));
    }
    if (world != null && location != null) {
      return new Location(world, location.getX(), location.getY(), location.getZ());
    }
    if (world != null) {
      // 只给了世界时用该世界的出生点, 不能静默忽略世界
      return world.getSpawnLocation().clone();
    }
    return sender instanceof Player p
        ? p.getLocation().clone()
        : Bukkit.getWorlds().get(0).getSpawnLocation().clone();
  }

  /** 普通 /fp spawn 使用配置里的寿命; /fp spawnjson 走 spawnWithOptions */
  private void spawn(
      @NotNull CommandSender sender,
      @Nullable String name,
      @NotNull Location spawnpoint,
      @Nullable UUID uuid) {
    this.spawnWithOptions(sender, name, spawnpoint, uuid, null);
  }

  public void spawnWithOptions(
      @NotNull CommandSender sender,
      @Nullable String name,
      @NotNull Location spawnpoint,
      @Nullable UUID uuid,
      @Nullable Long lifespanMillis) {
    var effectiveLifespan =
        lifespanMillis != null
            ? lifespanMillis
            : Optional.ofNullable(config.getLifespan())
                .map(Duration::toMillis)
                .orElse(FakeplayerTicker.NON_REMOVE_AT);
    var removedAt =
        effectiveLifespan == FakeplayerTicker.NON_REMOVE_AT
            ? null
            : LocalDateTime.now().plus(effectiveLifespan, ChronoUnit.MILLIS);
    manager
        .spawnAsync(sender, name, spawnpoint, effectiveLifespan, uuid)
        .thenAcceptAsync(
            player -> {
              if (player == null) {
                return;
              }
              Component message;
              if (removedAt == null) {
                message =
                    translatable(
                            "fakeplayer.command.spawn.success.without-lifespan",
                            text(player.getName(), WHITE),
                            text(toLocationString(spawnpoint), WHITE))
                        .color(GRAY);
              } else {
                message =
                    translatable(
                            "fakeplayer.command.spawn.success.with-lifespan",
                            text(player.getName(), WHITE),
                            text(toLocationString(spawnpoint), WHITE),
                            text(REMOVE_AT_FORMATTER.format(removedAt)))
                        .color(GRAY);
              }
              Schedulers.runFor(
                  Main.getInstance(),
                  sender,
                  () -> {
                    LangUtils.send(sender, message);
                    if (sender instanceof Player p && manager.countByCreator(sender) == 1) {
                      // 有些命令在有假人的时候才会显示, 因此需要强制刷新一下
                      p.updateCommands();
                    }
                  });
            })
        .exceptionally(
            e -> {
              if (Throwables.getRootCause(e) instanceof CommandException ce) {
                Schedulers.runFor(
                    Main.getInstance(), sender, () -> LangUtils.send(sender, ce.component()));
              } else {
                Schedulers.runFor(
                    Main.getInstance(),
                    sender,
                    () ->
                        LangUtils.send(
                            sender, translatable("fakeplayer.command.spawn.error.unknown", RED)));
                log.severe(Throwables.getStackTraceAsString(e));
              }
              return null;
            });
  }
}
