package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class HoldCommand extends AbstractCommand {

  public void hold(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var fake = super.getFakeplayer(sender, args);
    var slot = (int) Objects.requireNonNull(args.get("slot"));
    fake.getInventory().setHeldItemSlot(slot - 1);
  }
}
