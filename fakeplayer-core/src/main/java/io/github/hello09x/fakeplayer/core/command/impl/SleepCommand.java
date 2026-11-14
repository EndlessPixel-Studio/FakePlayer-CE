package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.core.utils.BlockUtils;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerAutosleepManager;
import org.bukkit.block.data.type.Bed;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class SleepCommand extends AbstractCommand {

  private final FakeplayerAutosleepManager autosleep;

  @Inject
  public SleepCommand(FakeplayerAutosleepManager autosleep) {
    this.autosleep = autosleep;
  }

  /** 睡觉 */
  public void sleep(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var fake = getFakeplayer(sender, args);
    var bed =
        BlockUtils.getNearbyBlock(
            fake.getLocation(),
            4,
            block -> {
              if (!(block.getBlockData() instanceof Bed data)) {
                return false;
              }

              return !data.isOccupied() && data.getPart() == Bed.Part.HEAD;
            });
    if (bed == null) {
      return;
    }

    fake.sleep(bed.getLocation(), false);
  }

  /** 开启自动睡觉 */
  public void autosleepOn(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    this.autosleep.setAutosleep(getFakeplayer(sender, args), true);
    LangUtils.send(sender, translatable("fakeplayer.command.autosleep.enabled", GRAY));
  }

  /** 关闭自动睡觉 */
  public void autosleepOff(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    this.autosleep.setAutosleep(getFakeplayer(sender, args), false);
    LangUtils.send(sender, translatable("fakeplayer.command.autosleep.disabled", GRAY));
  }

  /** 起床 */
  public void wakeup(@NotNull CommandSender sender, @NotNull CommandArguments args)
      throws WrapperCommandSyntaxException {
    var target = getFakeplayer(sender, args);
    if (!target.isSleeping()) {
      return;
    }

    target.wakeup(true);
  }
}
