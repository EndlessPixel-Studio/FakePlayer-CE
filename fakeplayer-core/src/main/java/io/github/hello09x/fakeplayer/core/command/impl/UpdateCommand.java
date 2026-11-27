package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.endlesspixel.fakeplayer.config.ConfigUpdater;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.util.update.UpdateChecker;
import io.github.hello09x.fakeplayer.core.util.update.UpdateInstaller;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class UpdateCommand extends AbstractCommand {

  private @NotNull UpdateChecker checker() {
    return new UpdateChecker(
        "EndlessPixel-Studio", "FakePlayer-CE", config.isUpdateIgnoreSslErrors());
  }

  /** 检查是否有新版本; 网络请求放到异步线程, 结果回给命令执行者 */
  public void check(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    LangUtils.send(sender, translatable("fakeplayer.command.update.check.checking", GRAY));

    var current = Main.getInstance().getPluginMeta().getVersion();
    CompletableFuture.runAsync(
        () -> {
          try {
            var result = this.checker().check(current);
            if (!result.outdated()) {
              LangUtils.send(
                  sender,
                  translatable(
                      "fakeplayer.command.update.check.latest", GRAY, text(result.current())));
              return;
            }

            LangUtils.send(
                sender,
                translatable(
                    "fakeplayer.command.update.check.available",
                    GRAY,
                    text(result.latest() == null ? "" : result.latest()),
                    text(result.current())));
            if (config.isUpdateDownload()) {
              this.download(sender, result, this.checker());
              return;
            }
            if (result.url() != null) {
              LangUtils.send(
                  sender,
                  translatable(
                      "fakeplayer.command.update.check.download", GRAY, text(result.url())));
            }
          } catch (Throwable e) {
            LangUtils.send(
                sender,
                translatable(
                    "fakeplayer.command.update.check.failed",
                    RED,
                    text(String.valueOf(e.getMessage()))));
          }
        });
  }

  /** 把模板新增、用户还没有的配置项追加进 config.yml; 既有值与注释保持不变, 结构冲突只提示, 改写前备份 config.yml.bak。 */
  public void config(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    try {
      var result = ConfigUpdater.update(Main.getInstance());
      if (!result.exists()) {
        LangUtils.send(sender, translatable("fakeplayer.command.update.config.missing", RED));
        return;
      }

      if (result.changed()) {
        LangUtils.send(
            sender,
            translatable(
                "fakeplayer.command.update.config.added",
                GRAY,
                text(result.added().size()),
                text(result.templateVersion()),
                text(String.valueOf(result.backup()))));
      } else {
        LangUtils.send(
            sender,
            translatable(
                "fakeplayer.command.update.config.uptodate", GRAY, text(result.templateVersion())));
      }

      if (!result.conflicts().isEmpty()) {
        LangUtils.send(
            sender,
            translatable(
                "fakeplayer.command.update.config.conflict",
                RED,
                text(result.conflicts().size()),
                text(String.join(", ", result.conflicts()))));
      }

      if (result.changed()) {
        LangUtils.send(sender, translatable("fakeplayer.command.update.config.reload", GRAY));
      }
    } catch (Throwable e) {
      LangUtils.send(
          sender,
          translatable(
              "fakeplayer.command.update.config.failed",
              RED,
              text(String.valueOf(e.getMessage()))));
    }
  }

  /** 把更新包下载到 plugins/update, 由服务端核心在下次启动时替换 */
  private void download(
      @NotNull CommandSender sender,
      @NotNull UpdateChecker.Result result,
      @NotNull UpdateChecker checker) {
    try {
      var target =
          checker.download(
              result.release(),
              UpdateInstaller.updateDirectory(),
              Duration.ofSeconds(config.getUpdateDownloadTimeoutSeconds()));
      LangUtils.send(
          sender,
          translatable(
              "fakeplayer.command.update.download.success",
              GRAY,
              text(String.valueOf(result.latest())),
              text(target.toString())));
      if (!UpdateInstaller.isSupportedByServer()) {
        LangUtils.send(sender, translatable("fakeplayer.command.update.download.unsupported", RED));
      }
    } catch (Throwable e) {
      LangUtils.send(
          sender,
          translatable(
              "fakeplayer.command.update.download.failed",
              RED,
              text(String.valueOf(e.getMessage()))));
    }
  }
}
