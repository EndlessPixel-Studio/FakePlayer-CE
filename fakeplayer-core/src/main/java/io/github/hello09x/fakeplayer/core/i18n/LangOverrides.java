package io.github.hello09x.fakeplayer.core.i18n;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import io.github.hello09x.fakeplayer.core.Main;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 每个发送者自己的显示语言。
 *
 * <p>默认跟随客户端语言 (玩家) 或 {@code i18n.locale} (控制台), 这里保存按人的覆盖, 落在 {@code lang.yml} 里, 重启后仍然有效。
 */
@Singleton
public final class LangOverrides {

  /** 内置语言, 与 message 目录下的 properties 对应 */
  private static final List<String> BUILT_IN = List.of("en", "zh", "zh_cn", "zh_tw", "zh_hk", "ja");

  private final File file;
  private final Map<UUID, Locale> overrides = new ConcurrentHashMap<>();

  @Inject
  public LangOverrides() {
    this.file = new File(Main.getInstance().getDataFolder(), "lang.yml");
    this.load();
  }

  /** 该发送者实际生效的语言 */
  public @NotNull Locale getLocale(@NotNull CommandSender sender) {
    var override = this.getOverride(sender);
    return override != null ? override : TranslatorUtils.getLocale(sender);
  }

  /** 该发送者手动设置的语言, 没有设置时返回 {@code null} */
  public @Nullable Locale getOverride(@NotNull CommandSender sender) {
    return sender instanceof Player player ? this.overrides.get(player.getUniqueId()) : null;
  }

  /**
   * 设置发送者的显示语言
   *
   * @return 是否与之前不同
   */
  public boolean set(@NotNull CommandSender sender, @NotNull Locale locale) {
    if (!(sender instanceof Player player)) {
      return false;
    }
    var changed = !Objects.equals(this.overrides.put(player.getUniqueId(), locale), locale);
    this.save();
    return changed;
  }

  /**
   * 清除发送者的显示语言, 回到客户端 / 配置默认值
   *
   * @return 之前是否设置过
   */
  public boolean reset(@NotNull CommandSender sender) {
    if (!(sender instanceof Player player)) {
      return false;
    }
    var removed = this.overrides.remove(player.getUniqueId()) != null;
    if (removed) {
      this.save();
    }
    return removed;
  }

  /** 可用的语言: 内置的 + 用户自己丢进 message 目录的 */
  public @NotNull List<String> available() {
    var codes = new LinkedHashSet<String>();
    for (var code : BUILT_IN) {
      if (exists(code)) {
        codes.add(code);
      }
    }
    var custom = new File(Main.getInstance().getDataFolder(), "message");
    var files =
        custom.listFiles(
            (dir, name) -> name.startsWith("message_") && name.endsWith(".properties"));
    if (files != null) {
      for (var file : files) {
        var fileName = file.getName();
        var code =
            fileName.substring("message_".length(), fileName.length() - ".properties".length());
        if (!code.isBlank()) {
          codes.add(code);
        }
      }
    }
    return List.copyOf(codes);
  }

  /**
   * 把用户输入的代码解析成语言代码, 大小写与 {@code _} / {@code -} 不敏感
   *
   * @return 可用的语言代码, 不认识时返回 {@code null}
   */
  public @Nullable String resolve(@NotNull String input) {
    var normalized = input.trim().replace('-', '_').toLowerCase(Locale.ROOT);
    for (var code : this.available()) {
      if (code.toLowerCase(Locale.ROOT).equals(normalized)) {
        return code;
      }
    }
    return null;
  }

  public static @NotNull Locale toLocale(@NotNull String code) {
    return Locale.forLanguageTag(code.replace('_', '-'));
  }

  private boolean exists(@NotNull String code) {
    var name =
        code.equals("en")
            ? "message/message.properties"
            : "message/message_" + code + ".properties";
    return Main.getInstance().getClass().getClassLoader().getResource(name) != null;
  }

  private void load() {
    if (!this.file.isFile()) {
      return;
    }
    var config = YamlConfiguration.loadConfiguration(this.file);
    for (var key : config.getKeys(false)) {
      try {
        this.overrides.put(UUID.fromString(key), toLocale(config.getString(key, "")));
      } catch (RuntimeException ignored) {
        // 跳过坏行, 不影响其他设置
      }
    }
  }

  private void save() {
    var config = new YamlConfiguration();
    this.overrides.forEach((uuid, locale) -> config.set(uuid.toString(), locale.toLanguageTag()));
    try {
      Main.getInstance().getDataFolder().mkdirs();
      config.save(this.file);
    } catch (Exception e) {
      Main.getInstance().getLogger().warning("Could not save lang.yml: " + e.getMessage());
    }
  }

  /** 所有可选值, 供命令注册时做 TAB 补全 */
  public @NotNull List<String> suggestions() {
    var codes = new ArrayList<String>(this.available());
    codes.add("reset");
    return codes;
  }
}
