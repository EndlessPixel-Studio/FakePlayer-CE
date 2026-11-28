package io.github.endlesspixel.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.command.exception.HandleCommandException;
import io.github.hello09x.fakeplayer.core.command.Permission;
import io.github.hello09x.fakeplayer.core.command.impl.AbstractCommand;
import io.github.hello09x.fakeplayer.core.command.impl.SpawnCommand;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import java.time.Duration;
import java.util.Arrays;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 高级召唤入口: 用一个 JSON 对象承载所有可选参数。
 *
 * <p>基础用法仍然走 {@code /fp spawn}; 这里放不常用、但组合很多的参数。NBT 暂不支持 (#192), 未知字段一律拒绝。
 */
@Singleton
public class SpawnJsonCommand extends AbstractCommand {

  private final SpawnCommand spawnCommand;

  @Inject
  public SpawnJsonCommand(@NotNull SpawnCommand spawnCommand) {
    this.spawnCommand = spawnCommand;
  }

  /** 高级召唤 */
  @HandleCommandException
  public void spawn(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    final SpawnJsonRequest request;
    try {
      request = SpawnJsonRequest.parse(String.valueOf(args.get("json")));
    } catch (SpawnJsonRequest.Invalid e) {
      var components =
          Arrays.stream(e.args())
              .map(value -> (Component) text(abbreviate(String.valueOf(value)), GOLD))
              .toArray(Component[]::new);
      LangUtils.send(sender, translatable(e.key(), components).color(RED));
      return;
    }

    var name = request.name();
    if (name != null && name.isEmpty()) {
      name = null;
    }
    if (name != null && !sender.hasPermission(Permission.spawnName)) {
      this.deny(sender, "fakeplayer.command.spawn.json.error.no-permission", "name");
      return;
    }
    if ((request.world() != null || request.x() != null)
        && !sender.hasPermission(Permission.spawnLocation)) {
      this.deny(sender, "fakeplayer.command.spawn.json.error.no-permission", "location");
      return;
    }

    UUID uuid = null;
    if (request.uuid() != null) {
      if (name == null) {
        LangUtils.send(
            sender, translatable("fakeplayer.command.spawn.json.error.uuid-needs-name").color(RED));
        return;
      }
      if (!sender.hasPermission(Permission.spawnUUID)) {
        this.deny(sender, "fakeplayer.command.spawn.json.error.no-permission", "uuid");
        return;
      }
      try {
        uuid = UUID.fromString(request.uuid().trim());
      } catch (IllegalArgumentException e) {
        LangUtils.send(
            sender,
            translatable(
                    "fakeplayer.spawn.error.uuid.invalid", text(abbreviate(request.uuid()), GOLD))
                .color(RED));
        return;
      }
    }

    var spawnpoint =
        sender instanceof Player player
            ? player.getLocation().clone()
            : Bukkit.getWorlds().get(0).getSpawnLocation().clone();
    if (request.world() != null) {
      var world = Bukkit.getWorld(request.world());
      if (world == null) {
        LangUtils.send(
            sender,
            translatable(
                    "fakeplayer.command.spawn.json.error.world",
                    text(abbreviate(request.world()), GOLD))
                .color(RED));
        return;
      }
      spawnpoint.setWorld(world);
    }
    if (request.x() != null) {
      spawnpoint.setX(request.x());
      spawnpoint.setY(request.y());
      spawnpoint.setZ(request.z());
    }
    if (request.yaw() != null) {
      spawnpoint.setYaw(request.yaw());
    }
    if (request.pitch() != null) {
      spawnpoint.setPitch(request.pitch());
    }

    Long lifespanMillis =
        request.lifespanSeconds() == null
            ? null
            : Duration.ofSeconds(request.lifespanSeconds()).toMillis();
    this.spawnCommand.spawnWithOptions(sender, name, spawnpoint, uuid, lifespanMillis);
  }

  private void deny(@NotNull CommandSender sender, @NotNull String key, @NotNull String field) {
    LangUtils.send(sender, translatable(key, text(field, GOLD)).color(RED));
  }

  private static @NotNull String abbreviate(@NotNull String raw) {
    return raw.length() <= 120 ? raw : raw.substring(0, 120) + "...";
  }
}
