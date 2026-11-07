package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import java.util.Objects;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class SetPingCommand extends AbstractCommand {

  /** 设置假人在 Tab 列表里显示的延迟 (ping) */
  public void setPing(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var fake = super.getFakeplayer(sender, args);
    var ping = (int) Objects.requireNonNull(args.get("ping"));

    var target = Main.getInjector().getInstance(FakeplayerList.class).getByUUID(fake.getUniqueId());
    if (target == null) {
      return;
    }

    target.setPing(ping);
    LangUtils.send(
        sender,
        translatable(
            "fakeplayer.command.setping.success",
            text(target.getName()),
            text(Integer.toString(ping))));
  }
}
