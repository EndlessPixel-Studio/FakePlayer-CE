package io.github.hello09x.fakeplayer.core.config;

import io.github.hello09x.devtools.core.config.PluginConfig;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 把用户的 config.yml 补齐到模板 (jar 内的 config.yml) 中缺失的配置项。
 *
 * <p>只追加缺失的键, 不改动用户已经写下的任何值; 同名但结构冲突 (一边是节, 另一边是叶子) 的键只报告, 同样不改动。改写之前先生成 config.yml.bak 备份,
 * 原始文件连同注释一起保留。
 */
public final class ConfigUpdater {

  private static final String VERSION_KEY = "version";

  private static final Pattern VERSION_LINE =
      Pattern.compile("(?m)^version:[ \\t]*(-?\\d+)[ \\t]*$");

  private ConfigUpdater() {}

  /**
   * @param exists 用户的 config.yml 是否存在
   * @param changed 本次是否改写了 config.yml
   * @param templateVersion 模板的 version
   * @param added 实际追加的最浅路径 (其子项不再单独列出)
   * @param conflicts 与模板结构冲突、被保留原样的路径
   * @param backup 备份文件路径, 未改写时为 null
   */
  public record Result(
      boolean exists,
      boolean changed,
      int templateVersion,
      @NotNull List<String> added,
      @NotNull List<String> conflicts,
      @Nullable Path backup) {}

  /**
   * 补齐 config.yml 缺失项。
   *
   * @param plugin 提供 dataFolder 与 jar 内模板的插件实例
   * @return 补齐结果
   * @throws IOException 读写配置文件失败, 或 jar 内缺少模板
   * @throws InvalidConfigurationException config.yml 或模板不是合法 YAML
   */
  public static @NotNull Result update(@NotNull Plugin plugin)
      throws IOException, InvalidConfigurationException {
    var target = new File(plugin.getDataFolder(), PluginConfig.CONFIG_FILE_NAME).toPath();
    if (!Files.isRegularFile(target)) {
      return new Result(false, false, 0, List.of(), List.of(), null);
    }

    var template = load(plugin.getResource(PluginConfig.CONFIG_FILE_NAME), "template");
    var user = load(Files.readString(target, StandardCharsets.UTF_8), "config.yml");

    var missing = new ArrayList<String>();
    var conflicted = new ArrayList<String>();
    for (var key : template.getKeys(true)) {
      if (VERSION_KEY.equals(key)) {
        continue;
      }
      if (!user.contains(key)) {
        missing.add(key);
      } else if (template.isConfigurationSection(key) != user.isConfigurationSection(key)) {
        conflicted.add(key);
      }
    }

    // 只补最浅的缺失节点, 否则一个节会连同它下面的子孙被重复追加
    var added = shallowest(missing);
    var conflicts = shallowest(excludeDescendants(conflicted, added));

    var templateVersion = template.getInt(VERSION_KEY, 0);
    var userVersion = user.getInt(VERSION_KEY, 0);
    var changed = !added.isEmpty() || templateVersion != userVersion;
    if (!changed) {
      return new Result(true, false, templateVersion, List.of(), conflicts, null);
    }

    var backup = target.resolveSibling(target.getFileName() + ".bak");
    Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);

    var text = replaceVersion(Files.readString(target, StandardCharsets.UTF_8), templateVersion);
    if (!added.isEmpty()) {
      text = text + System.lineSeparator() + render(template, added, templateVersion);
    }
    Files.writeString(target, text, StandardCharsets.UTF_8);

    return new Result(true, true, templateVersion, added, conflicts, backup);
  }

  private static @NotNull YamlConfiguration load(@Nullable String raw, @NotNull String name)
      throws InvalidConfigurationException {
    if (raw == null) {
      throw new InvalidConfigurationException("Missing " + name);
    }
    var config = new YamlConfiguration();
    config.loadFromString(raw);
    return config;
  }

  private static @NotNull YamlConfiguration load(@Nullable InputStream raw, @NotNull String name)
      throws IOException, InvalidConfigurationException {
    if (raw == null) {
      throw new IOException("Missing " + name + " in the plugin jar");
    }
    try (raw) {
      return load(new String(raw.readAllBytes(), StandardCharsets.UTF_8), name);
    }
  }

  /** 丢弃祖先已经缺失的路径, 只保留最浅的一层。 */
  private static @NotNull List<String> shallowest(@NotNull List<String> paths) {
    var result = new ArrayList<String>();
    for (var path : paths) {
      var covered =
          paths.stream().anyMatch(other -> !other.equals(path) && path.startsWith(other + "."));
      if (!covered) {
        result.add(path);
      }
    }
    return result;
  }

  /** 一个路径的祖先已经被补进配置文件时, 它自身不再算作冲突。 */
  private static @NotNull List<String> excludeDescendants(
      @NotNull List<String> paths, @NotNull List<String> ancestors) {
    var result = new ArrayList<String>();
    for (var path : paths) {
      var covered = ancestors.stream().anyMatch(ancestor -> path.startsWith(ancestor + "."));
      if (!covered) {
        result.add(path);
      }
    }
    return result;
  }

  private static @NotNull String replaceVersion(@NotNull String raw, int version) {
    var matcher = VERSION_LINE.matcher(raw);
    if (matcher.find()) {
      return matcher.replaceFirst(Matcher.quoteReplacement(VERSION_KEY + ": " + version));
    }
    return VERSION_KEY + ": " + version + System.lineSeparator() + raw;
  }

  private static @NotNull String render(
      @NotNull YamlConfiguration template, @NotNull List<String> paths, int version) {
    var addition = new YamlConfiguration();
    for (var path : paths) {
      if (template.isConfigurationSection(path)) {
        copySection(template.getConfigurationSection(path), addition.createSection(path));
      } else {
        addition.set(path, template.get(path));
      }
    }

    var separator = System.lineSeparator();
    var header =
        "# ----------------------------------------------------------------------"
            + separator
            + "# Options below were appended by /fp update config (template v"
            + version
            + "),"
            + separator
            + "# existing options and comments above are left untouched."
            + separator
            + "# 以下配置项由 /fp update config 自动补齐 (模板 v"
            + version
            + "),"
            + separator
            + "# 上方的既有配置与注释均未被改动。"
            + separator
            + "# ----------------------------------------------------------------------"
            + separator;
    return header + addition.saveToString();
  }

  private static void copySection(
      @NotNull ConfigurationSection source, @NotNull ConfigurationSection target) {
    for (var key : source.getKeys(false)) {
      if (source.isConfigurationSection(key)) {
        copySection(source.getConfigurationSection(key), target.createSection(key));
      } else {
        target.set(key, source.get(key));
      }
    }
  }
}
