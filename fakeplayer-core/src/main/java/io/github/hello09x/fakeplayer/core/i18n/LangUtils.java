package io.github.hello09x.fakeplayer.core.i18n;

import io.github.hello09x.fakeplayer.core.Main;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * 按发送者的语言发送消息。
 *
 * <p>{@code sender.sendMessage(translatable(...))} 会按受众自己的语言渲染 (玩家是客户端语言, 控制台是 {@code
 * i18n.locale}), 无法覆盖。设置了 {@code /fp lang} 的消息在这里先用 {@link LangOverrides} 的语言渲染一遍再发。
 */
public final class LangUtils {

  private static @NotNull LangOverrides overrides() {
    return Main.getInjector().getInstance(LangOverrides.class);
  }

  /** 该发送者实际生效的语言 */
  public static @NotNull Locale getLocale(@NotNull CommandSender sender) {
    return overrides().getLocale(sender);
  }

  /** 发送消息, 尊重发送者的语言覆盖 */
  public static void send(@NotNull CommandSender sender, @NotNull Component component) {
    var override = overrides().getOverride(sender);
    sender.sendMessage(override == null ? component : GlobalTranslator.render(component, override));
  }

  private LangUtils() {}
}
