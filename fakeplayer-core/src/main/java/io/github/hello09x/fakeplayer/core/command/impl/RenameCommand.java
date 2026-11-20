package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.command.exception.CommandException;
import io.github.hello09x.devtools.command.exception.HandleCommandException;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.command.Permission;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerRestoreManager;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class RenameCommand extends AbstractCommand {

  @Inject private FakeplayerRestoreManager restoreManager;

  /** 重命名假人 */
  @HandleCommandException
  public void rename(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    var name = (String) args.get("name");
    var newName = (String) args.get("new-name");
    if (name == null || newName == null || newName.isBlank()) {
      return;
    }

    var force = args.get("force") != null;
    if (force && !sender.hasPermission(Permission.renameForce)) {
      LangUtils.send(sender, translatable("fakeplayer.command.rename.error.force-permission", RED));
      return;
    }

    // 被重命名的假人如果正好是当前选中的, 重建后要把选中状态指过去
    var selection = sender instanceof Player p ? manager.getSelection(p) : null;
    var wasSelected = selection != null && selection.getName().equalsIgnoreCase(name);

    manager
        .renameAsync(sender, name, newName, force)
        .thenAcceptAsync(
            player -> {
              if (player == null) {
                return;
              }
              Schedulers.runFor(
                  Main.getInstance(),
                  sender,
                  () -> {
                    LangUtils.send(
                        sender,
                        translatable(
                                "fakeplayer.command.rename.success",
                                text(name, WHITE),
                                text(player.getName(), WHITE))
                            .color(GRAY));
                    if (sender instanceof Player p) {
                      if (wasSelected) {
                        manager.setSelection(p, player);
                      }
                      p.updateCommands();
                    }
                    // 旧名称的快照已随下线写过一次, 这里补一次新名称的, 避免崩溃后恢复出旧名字
                    restoreManager.saveSnapshot();
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
                            sender, translatable("fakeplayer.command.rename.error.unknown", RED)));
                log.severe(Throwables.getStackTraceAsString(e));
              }
              return null;
            });
  }
}
