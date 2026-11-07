package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;

import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class SelectCommand extends AbstractCommand {

  public void select(@NotNull Player sender, @NotNull CommandArguments args) {
    var target = this.getTargetNullable(sender, args);
    manager.setSelection(sender, target);
    if (target == null) {
      LangUtils.send(sender, translatable("fakeplayer.command.select.success.clear", GRAY));
    } else {
      LangUtils.send(
          sender,
          translatable("fakeplayer.command.select.success.selected", text(target.getName(), WHITE))
              .color(GRAY));
    }
  }

  public void selection(@NotNull Player sender, @NotNull CommandArguments args) {
    var selection = manager.getSelection(sender);
    if (selection == null) {
      LangUtils.send(sender, translatable("fakeplayer.command.selection.error.none", GRAY));
    } else {
      LangUtils.send(
          sender,
          translatable("fakeplayer.command.selection.success", text(selection.getName(), WHITE))
              .color(GRAY));
    }
  }
}
