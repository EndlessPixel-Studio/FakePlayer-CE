package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.i18n.LangOverrides;
import java.util.StringJoiner;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class LangCommand extends AbstractCommand {

  private final LangOverrides langOverrides;

  @Inject
  public LangCommand(LangOverrides langOverrides) {
    this.langOverrides = langOverrides;
  }

  /**
   * 查看 / 切换自己的插件显示语言
   *
   * <p>不传参数时列出当前语言与可用语言; {@code reset} 回到客户端 (或配置) 的默认语言
   */
  public void lang(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    var raw = (String) args.getOptional("code").orElse(null);
    if (raw == null) {
      this.show(sender);
      return;
    }

    if (raw.equalsIgnoreCase("reset")) {
      var previous = this.langOverrides.getOverride(sender);
      this.langOverrides.reset(sender);
      this.send(
          sender,
          translatable(
              previous == null
                  ? "fakeplayer.command.lang.already-default"
                  : "fakeplayer.command.lang.reset",
              GRAY,
              text(previous != null ? previous.toLanguageTag() : "")));
      return;
    }

    var code = this.langOverrides.resolve(raw);
    if (code == null) {
      this.send(
          sender,
          translatable("fakeplayer.command.lang.unknown", RED, text(raw), text(this.joined())));
      return;
    }

    if (!this.langOverrides.set(sender, LangOverrides.toLocale(code))) {
      // 控制台没有客户端语言, 覆盖只对玩家有意义
      this.send(sender, translatable("fakeplayer.command.lang.console-unsupported", RED));
      return;
    }
    this.send(sender, translatable("fakeplayer.command.lang.switched", GRAY, text(code)));
  }

  private void show(@NotNull CommandSender sender) {
    this.send(
        sender,
        translatable(
            "fakeplayer.command.lang.current",
            GRAY,
            text(this.langOverrides.getLocale(sender).toLanguageTag())));
    this.send(sender, translatable("fakeplayer.command.lang.available", GRAY, text(this.joined())));
    this.send(sender, translatable("fakeplayer.command.lang.usage", DARK_GRAY));
  }

  private @NotNull String joined() {
    var joiner = new StringJoiner(", ");
    this.langOverrides.available().forEach(joiner::add);
    return joiner.toString();
  }

  /** 语言相关的反馈必须按目标语言渲染, 不能沿用客户端语言 */
  private void send(@NotNull CommandSender sender, @NotNull Component component) {
    var locale = this.langOverrides.getLocale(sender);
    sender.sendMessage(GlobalTranslator.render(component, locale));
  }
}
