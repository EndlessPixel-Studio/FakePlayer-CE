package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class VersionCommand extends AbstractCommand {

  /** 输出插件、服务端与 Java 版本, 方便报错时对照 */
  public void version(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    var meta = Main.getInstance().getPluginMeta();
    LangUtils.send(
        sender, translatable("fakeplayer.command.version.plugin", GRAY, text(meta.getVersion())));
    LangUtils.send(
        sender,
        translatable(
            "fakeplayer.command.version.server",
            GRAY,
            text("%s %s".formatted(Bukkit.getName(), Bukkit.getServer().getVersion()))));
    LangUtils.send(
        sender,
        translatable(
            "fakeplayer.command.version.java",
            GRAY,
            text(String.valueOf(System.getProperty("java.version")))));
    LangUtils.send(sender, translatable("fakeplayer.command.version.update-hint", DARK_GRAY));
  }
}
