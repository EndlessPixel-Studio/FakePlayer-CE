package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.core.utils.ComponentUtils;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.manager.invsee.InvseeManager;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@Singleton
public class InvseeCommand extends AbstractCommand {

  private final InvseeManager invseeManager;

  @Inject
  public InvseeCommand(InvseeManager invseeManager) {
    this.invseeManager = invseeManager;
  }

  /** 查看背包 */
  public void invsee(@NotNull Player sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var fake = super.getFakeplayer(sender, args);
    if (!Objects.equals(sender.getLocation().getWorld(), fake.getLocation().getWorld())) {
      throw CommandAPI.failWithString(
          ComponentUtils.toString(
              translatable("fakeplayer.command.invsee.error.not-the-same-world"),
              LangUtils.getLocale(sender)));
    }
    invseeManager.invsee(sender, fake);
  }
}
