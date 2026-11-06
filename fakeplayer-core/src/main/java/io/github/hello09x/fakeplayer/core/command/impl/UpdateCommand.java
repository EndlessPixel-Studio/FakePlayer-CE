package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.util.update.UpdateChecker;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class UpdateCommand extends AbstractCommand {

  private static final UpdateChecker checker =
      new UpdateChecker("EndlessPixel-Studio", "FakePlayer-CE");

  /** 检查是否有新版本; 网络请求放到异步线程, 结果回给命令执行者 */
  public void check(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    sender.sendMessage(translatable("fakeplayer.command.update.check.checking", GRAY));

    var current = Main.getInstance().getPluginMeta().getVersion();
    CompletableFuture.runAsync(
        () -> {
          try {
            var result = checker.check(current);
            if (!result.outdated()) {
              sender.sendMessage(
                  translatable(
                      "fakeplayer.command.update.check.latest", GRAY, text(result.current())));
              return;
            }

            sender.sendMessage(
                translatable(
                    "fakeplayer.command.update.check.available",
                    GRAY,
                    text(result.latest() == null ? "" : result.latest()),
                    text(result.current())));
            if (result.url() != null) {
              sender.sendMessage(
                  translatable(
                      "fakeplayer.command.update.check.download", GRAY, text(result.url())));
            }
          } catch (Throwable e) {
            sender.sendMessage(
                translatable(
                    "fakeplayer.command.update.check.failed",
                    RED,
                    text(String.valueOf(e.getMessage()))));
          }
        });
  }
}
