package io.github.hello09x.fakeplayer.core.config;

import io.github.hello09x.devtools.core.config.PluginConfig;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
 *
 * <p>追加是**按文本插入**而不是重新序列化: 重新序列化会丢掉模板里的双语注释, 而且只能把整节写回文件末尾 —— 对于「父节已存在、模板新增了子项」的情况,
 * 那会在文件里造出两个同名顶层键, SnakeYAML 取最后一个, 等于把用户那一节的取值全部重置成默认值。
 */
public final class ConfigUpdater {

  private static final String VERSION_KEY = "version";

  /** {@code version: 35} / {@code version: "35"} / {@code version: 35 # 手改过} 都要能认出来 */
  private static final Pattern VERSION_LINE =
      Pattern.compile("(?m)^[ \\t]*version[ \\t]*:[ \\t]*[\"']?-?\\d+[\"']?[^\\r\\n]*$");

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
   * @throws IOException 读写配置文件失败, 或 jar 内缺少模板, 或生成的内容不是合法 YAML
   * @throws InvalidConfigurationException config.yml 或模板不是合法 YAML
   */
  public static @NotNull Result update(@NotNull Plugin plugin)
      throws IOException, InvalidConfigurationException {
    var target = new File(plugin.getDataFolder(), PluginConfig.CONFIG_FILE_NAME).toPath();
    if (!Files.isRegularFile(target)) {
      return new Result(false, false, 0, List.of(), List.of(), null);
    }

    var templateText = readTemplate(plugin);
    var userText = Files.readString(target, StandardCharsets.UTF_8);
    var template = load(templateText);
    var user = load(userText);

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
    if (added.isEmpty() && templateVersion == userVersion) {
      return new Result(true, false, templateVersion, List.of(), conflicts, null);
    }

    var backup = target.resolveSibling(target.getFileName() + ".bak");
    Files.copy(target, backup, StandardCopyOption.REPLACE_EXISTING);

    var updated = replaceVersion(userText, templateVersion);
    if (!added.isEmpty()) {
      updated = insert(userText, updated, templateText, added, templateVersion);
    }

    // 安全网: 生成的内容必须仍是合法 YAML, 且每个待补的键都真的出现了, 否则一个字都不写
    var check = load(updated);
    for (var path : added) {
      if (!check.contains(path)) {
        throw new IOException("Generated config.yml does not contain '" + path + "', aborted");
      }
    }

    Files.writeString(target, updated, StandardCharsets.UTF_8);
    return new Result(true, true, templateVersion, added, conflicts, backup);
  }

  private static @NotNull String readTemplate(@NotNull Plugin plugin) throws IOException {
    try (var in = plugin.getResource(PluginConfig.CONFIG_FILE_NAME)) {
      if (in == null) {
        throw new IOException("Missing " + PluginConfig.CONFIG_FILE_NAME + " in the plugin jar");
      }
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static @NotNull YamlConfiguration load(@NotNull String raw)
      throws InvalidConfigurationException {
    var config = new YamlConfiguration();
    config.loadFromString(raw);
    return config;
  }

  /** 丢弃祖先已经缺失的路径, 只保留最浅的一层。 */
  private static @NotNull List<String> shallowest(@NotNull List<String> paths) {
    var result = new ArrayList<String>();
    for (var path : paths) {
      var covered = paths.stream().anyMatch(other -> !other.equals(path) && isUnder(path, other));
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
      var covered = ancestors.stream().anyMatch(ancestor -> isUnder(path, ancestor));
      if (!covered) {
        result.add(path);
      }
    }
    return result;
  }

  private static boolean isUnder(@NotNull String path, @NotNull String ancestor) {
    return path.startsWith(ancestor + ".");
  }

  /**
   * 把 version 改写为模板版本, 保留行尾注释。
   *
   * <p>这里只替换第一个 {@code version:} 行。之前用的正则是「整行只有数字」, 用户写成 {@code version: 30 # 手改过} 时会匹配失败,
   * 于是往文件头再插一行 —— 文件里出现两个 {@code version}, SnakeYAML 取最后一个, 结果版本永远升不上去, 每次执行都多插一行。
   */
  private static @NotNull String replaceVersion(@NotNull String raw, int version) {
    var matcher = VERSION_LINE.matcher(raw);
    if (!matcher.find()) {
      return VERSION_KEY + ": " + version + lineSeparatorOf(raw) + raw;
    }
    var suffix =
        matcher.group().replaceFirst("^[ \\t]*version[ \\t]*:[ \\t]*[\"']?-?\\d+[\"']?", "");
    return matcher.replaceFirst(Matcher.quoteReplacement(VERSION_KEY + ": " + version + suffix));
  }

  /**
   * 把缺失的配置项按文本插入到 config 里。
   *
   * <p>顶层缺失项追加到文件末尾; 节内缺失项插到父节的最后一个子项之后 —— 这样才能保留模板里的注释, 也不会因为重复顶层键而让整节配置失效。
   *
   * @param userText 改写后的用户配置
   * @param withVersion 已经改过 version 的用户配置
   * @param templateText 模板原文
   * @param added 待补路径
   * @param version 模板版本, 仅用于追加区块的说明
   * @return 新的配置文本
   */
  private static @NotNull String insert(
      @NotNull String userText,
      @NotNull String withVersion,
      @NotNull String templateText,
      @NotNull List<String> added,
      int version)
      throws IOException {
    var separator = lineSeparatorOf(userText);
    var lines = new ArrayList<>(List.of(withVersion.split("\r?\n", -1)));
    var templateLines = List.of(templateText.split("\r?\n", -1));

    var appended = new ArrayList<String>();
    for (var path : added) {
      var parent = parentOf(path);
      var slice = slice(templateLines, path);
      if (slice.isEmpty()) {
        throw new IOException("Cannot locate '" + path + "' in the template, aborted");
      }

      if (parent.isEmpty()) {
        appended.addAll(slice);
        continue;
      }

      // 节内新增: 插到父节末尾, 缩进按「用户父节 / 模板父节」的差值平移
      var parentLine = findKeyLine(lines, parent);
      var templateParentLine = templateKeyLine(templateLines, parent);
      if (parentLine < 0 || templateParentLine < 0) {
        throw new IOException("Cannot locate the section of '" + path + "' in config.yml, aborted");
      }
      var delta = indentOf(lines.get(parentLine)) - indentOf(templateLines.get(templateParentLine));
      var reindented = new ArrayList<String>();
      for (var line : slice) {
        if (line.isBlank() || delta == 0) {
          reindented.add(line);
          continue;
        }
        if (delta < 0) {
          reindented.add(line.length() >= -delta ? line.substring(-delta) : line.strip());
        } else {
          reindented.add(" ".repeat(delta) + line);
        }
      }
      var insertAt = blockEnd(lines, parentLine);
      lines.addAll(insertAt, reindented);
    }

    var builder = new StringBuilder(String.join(separator, lines));
    if (!appended.isEmpty()) {
      if (!builder.isEmpty() && !builder.toString().endsWith(separator)) {
        builder.append(separator);
      }
      builder
          .append(separator)
          .append(header(version, separator))
          .append(String.join(separator, appended));
    }
    return builder.toString();
  }

  /** 说明区块的换行符必须跟着目标文件走, 否则在 LF 文件里混进 CRLF, 编辑器会显示成空行或 ^M */
  private static @NotNull String header(int version, @NotNull String separator) {
    return String.join(
            separator,
            "# ----------------------------------------------------------------------",
            "# Options below were appended by /fp update config (template v" + version + "),",
            "# existing options and comments above are left untouched.",
            "# 以下配置项由 /fp update config 自动补齐 (模板 v" + version + "),",
            "# 上方的既有配置与注释均未被改动。",
            "# ----------------------------------------------------------------------")
        + separator;
  }

  @Nullable
  private static String parentOf(@NotNull String path) {
    var dot = path.lastIndexOf('.');
    return dot < 0 ? "" : path.substring(0, dot);
  }

  /** 取模板里某条路径的原文片段, 连同紧贴其上的注释行。 */
  private static @NotNull List<String> slice(
      @NotNull List<String> templateLines, @NotNull String path) {
    var keyLine = findKeyLine(templateLines, path);
    if (keyLine < 0) {
      return List.of();
    }

    var indent = indentOf(templateLines.get(keyLine));
    var start = keyLine;
    while (start > 0) {
      var previous = templateLines.get(start - 1);
      var trimmed = previous.trim();
      if (trimmed.isEmpty() || !trimmed.startsWith("#") || indentOf(previous) != indent) {
        break;
      }
      start--;
    }
    return templateLines.subList(start, blockEnd(templateLines, keyLine));
  }

  /** 返回路径所在的键行下标, 找不到返回 -1。 */
  private static int findKeyLine(@NotNull List<String> lines, @NotNull String path) {
    var index = -1;
    var indent = -1;
    for (var segment : path.split("\\.")) {
      var found = -1;
      for (var i = index + 1; i < lines.size(); i++) {
        var raw = lines.get(i);
        var trimmed = raw.trim();
        if (trimmed.isEmpty() || trimmed.startsWith("#")) {
          continue;
        }
        var currentIndent = indentOf(raw);
        if (index < 0) {
          if (currentIndent != 0) {
            continue;
          }
        } else if (currentIndent <= indent) {
          break;
        }
        if (trimmed.matches(Pattern.quote(segment) + "[ \\t]*:.*")) {
          found = i;
          break;
        }
      }
      if (found < 0) {
        return -1;
      }
      index = found;
      indent = indentOf(lines.get(found));
    }
    return index;
  }

  private static int templateKeyLine(@NotNull List<String> templateLines, @NotNull String path) {
    return findKeyLine(templateLines, path);
  }

  /** 某个键所在块的结束下标 (不含), 块尾的空行会被去掉。 */
  private static int blockEnd(@NotNull List<String> lines, int keyLine) {
    var indent = indentOf(lines.get(keyLine));
    var end = keyLine + 1;
    var i = keyLine + 1;
    while (i < lines.size()) {
      var raw = lines.get(i);
      if (!raw.isBlank()) {
        if (indentOf(raw) <= indent) {
          break;
        }
        end = i + 1;
      }
      i++;
    }
    return end;
  }

  private static int indentOf(@NotNull String line) {
    var i = 0;
    while (i < line.length() && line.charAt(i) == ' ') {
      i++;
    }
    return i;
  }

  private static @NotNull String lineSeparatorOf(@NotNull String text) {
    return text.contains("\r\n") ? "\r\n" : "\n";
  }
}
