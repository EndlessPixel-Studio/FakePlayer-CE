package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import java.util.StringJoiner;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class KickCommand extends AbstractCommand {

  /** 踢出假人 (从服务器移除) */
  public void kick(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var fakes = super.getFakeplayers(sender, args);

    if (fakes.isEmpty()) {
      LangUtils.send(sender, translatable("fakeplayer.command.kick.error.non-removed", GRAY));
      return;
    }

    var names = new StringJoiner(", ");
    for (var fake : fakes) {
      if (manager.remove(fake.getName(), "command kick")) {
        names.add(fake.getName());
      }
    }
    LangUtils.send(
        sender,
        textOfChildren(
            translatable("fakeplayer.command.kick.success.removed", GRAY),
            space(),
            text(names.toString())));
  }
}
