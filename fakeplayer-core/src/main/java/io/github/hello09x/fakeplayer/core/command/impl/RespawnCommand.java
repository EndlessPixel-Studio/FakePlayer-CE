package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

@Singleton
public class RespawnCommand extends AbstractCommand {

  /** 重生 */
  public void respawn(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var fake = super.getFakeplayer(sender, args, Entity::isDead);
    bridge.fromPlayer(fake).respawn();
    LangUtils.send(sender, translatable("fakeplayer.command.generic.success"));
  }
}
