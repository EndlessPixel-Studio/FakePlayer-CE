package io.github.endlesspixel.fakeplayer.core.aichat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** AI 聊天配置, 独立存放于 {@code ai-chat.yml} */
@Getter
@Singleton
public class AiChatConfig {

  private static final Logger log = Main.getInstance().getLogger();

  private static final String FILE_NAME = "ai-chat.yml";
  private static final String DEFAULT_MODEL_NAME = "default";
  private static final Pattern ACTIVE_MODEL_LINE =
      Pattern.compile("(?m)^active-model:\\s*[^\\r\\n]*$");

  private static final String DEFAULT_SYSTEM_PROMPT =
      """
      你是 Minecraft 服务器中的一名玩家, 名字是 {name}。
      说话简短、自然、口语化, 每次只说一到两句话, 不要使用括号描述动作, 不要提到自己是 AI。
      """;

  private boolean enabled;

  private String api;

  private String apiKey;

  private String apiKeyEnv;

  private String model;

  private String activeModel;

  private final Map<String, AiChatModel> models = new LinkedHashMap<>();

  private boolean inventoryContextFakeplayer;

  private boolean inventoryContextQuestioner;

  private double temperature;

  private double topP;

  private double presencePenalty;

  private double frequencyPenalty;

  private int maxTokens;

  private int maxContext;

  private int recentChatLines;

  private boolean autoReplyEnabled;

  private double autoReplyChance;

  private int autoReplyCooldownSeconds;

  private int autoReplyMinLength;

  private int autoReplyMaxLength;

  private boolean allowContinuous;

  private int timeoutSeconds;

  private int intervalTicks;

  private String systemPrompt;

  private String turnPrompt;

  private String privateFormat;

  @Inject
  public AiChatConfig() {
    this.reload();
  }

  public void reload() {
    var folder = Main.getInstance().getDataFolder();
    var file = new File(folder, FILE_NAME);
    if (!file.isFile()) {
      folder.mkdirs();
      try {
        Main.getInstance().saveResource(FILE_NAME, false);
      } catch (IllegalArgumentException e) {
        log.warning("缺少内置资源 " + FILE_NAME + ", AI 聊天将不可用");
        this.enabled = false;
        return;
      }
    }

    var yaml = YamlConfiguration.loadConfiguration(file);
    this.enabled = yaml.getBoolean("enabled", false);
    this.api = trimTrailingSlash(yaml.getString("api", "https://api.openai.com/v1"));
    this.apiKey = yaml.getString("api-key", "");
    this.apiKeyEnv = yaml.getString("api-key-env", "FAKEPLAYER_AI_KEY");
    this.model = yaml.getString("model", "gpt-4o");
    this.activeModel = yaml.getString("active-model", DEFAULT_MODEL_NAME);
    this.inventoryContextFakeplayer = yaml.getBoolean("inventory-context.fakeplayer", false);
    this.inventoryContextQuestioner = yaml.getBoolean("inventory-context.questioner", false);
    this.temperature = yaml.getDouble("temperature", 1.0D);
    this.topP = yaml.getDouble("top-p", 1.0D);
    this.presencePenalty = yaml.getDouble("presence-penalty", 0.0D);
    this.frequencyPenalty = yaml.getDouble("frequency-penalty", 0.0D);
    this.maxTokens = yaml.getInt("max-tokens", 0);
    this.maxContext = Math.max(0, yaml.getInt("max-context", 20));
    this.recentChatLines = Math.max(0, yaml.getInt("recent-chat-lines", 0));
    this.autoReplyEnabled = yaml.getBoolean("auto-reply.enabled", false);
    this.autoReplyChance = Math.max(0D, Math.min(1D, yaml.getDouble("auto-reply.chance", 0.15D)));
    this.autoReplyCooldownSeconds = Math.max(0, yaml.getInt("auto-reply.cooldown-seconds", 90));
    this.autoReplyMinLength = Math.max(1, yaml.getInt("auto-reply.min-length", 4));
    this.autoReplyMaxLength =
        Math.max(this.autoReplyMinLength, yaml.getInt("auto-reply.max-length", 120));
    this.allowContinuous = yaml.getBoolean("allow-continuous", false);
    this.timeoutSeconds = Math.max(1, yaml.getInt("timeout-seconds", 30));
    this.intervalTicks = Math.max(20, yaml.getInt("interval-ticks", 200));
    this.systemPrompt = yaml.getString("system-prompt", DEFAULT_SYSTEM_PROMPT);
    this.turnPrompt = yaml.getString("turn-prompt", "说一句话。");
    this.privateFormat = yaml.getString("private-format", "<gray>[{name} -> 你] {message}</gray>");

    this.loadModels(yaml);
    this.validate();
  }

  private void loadModels(@NotNull YamlConfiguration yaml) {
    this.models.clear();
    var section = yaml.getConfigurationSection("models");
    if (section != null) {
      for (var key : section.getKeys(false)) {
        var entry = section.getConfigurationSection(key);
        if (entry == null) {
          continue;
        }
        this.models.put(
            key,
            new AiChatModel(
                key,
                stringValue(entry, "model", this.model),
                trimTrailingSlash(stringValue(entry, "api", this.api)),
                stringValue(entry, "api-key-env", this.apiKeyEnv),
                stringValue(entry, "api-key", this.apiKey)));
      }
    }
    if (this.models.isEmpty()) {
      // 旧配置没有 models 段, 顶层的 model/api/api-key 仍然就是唯一可用的模型
      this.models.put(
          DEFAULT_MODEL_NAME,
          new AiChatModel(DEFAULT_MODEL_NAME, this.model, this.api, this.apiKeyEnv, this.apiKey));
    }
  }

  private static @NotNull String stringValue(
      @NotNull ConfigurationSection section, @NotNull String path, @Nullable String fallback) {
    var value = section.getString(path);
    return value == null ? (fallback == null ? "" : fallback) : value;
  }

  private void validate() {
    if (!this.enabled) {
      return;
    }
    var active = this.resolveActiveModel();
    if (active == null) {
      log.warning("ai-chat.yml 已启用但没有可用模型, AI 聊天将保持关闭");
      this.enabled = false;
      return;
    }
    if (active.api().isBlank()) {
      log.warning("ai-chat.yml 已启用但 api 为空, AI 聊天将保持关闭");
      this.enabled = false;
      return;
    }
    if (active.model().isBlank()) {
      log.warning("ai-chat.yml 已启用但 model 为空, AI 聊天将保持关闭");
      this.enabled = false;
      return;
    }
    if (active.resolveApiKey() == null) {
      log.warning(
          "ai-chat.yml 已启用但未配置密钥, 请填写 api-key 或设置 " + active.apiKeyEnv() + " 环境变量, 否则请求会失败");
    }
  }

  public @Nullable AiChatModel resolveActiveModel() {
    var selected = this.models.get(this.activeModel);
    if (selected != null) {
      return selected;
    }
    var fallback = this.models.get(DEFAULT_MODEL_NAME);
    return fallback != null ? fallback : this.models.values().stream().findFirst().orElse(null);
  }

  public @NotNull List<String> getModelNames() {
    return List.copyOf(this.models.keySet());
  }

  /** 由管理员切换当前模型, 结果写回配置文件以免重启后丢失 */
  public boolean selectModel(@NotNull String name) {
    if (!this.models.containsKey(name) || !this.persistActiveModel(name)) {
      return false;
    }
    this.activeModel = name;
    return true;
  }

  private boolean persistActiveModel(@NotNull String name) {
    var file = new File(Main.getInstance().getDataFolder(), FILE_NAME);
    try {
      var text = Files.readString(file.toPath(), StandardCharsets.UTF_8);
      var escaped = name.replace("\\", "\\\\").replace("\"", "\\\"");
      var replacement = Matcher.quoteReplacement("active-model: \"" + escaped + "\"");
      var updated = ACTIVE_MODEL_LINE.matcher(text).replaceFirst(replacement);
      if (updated.equals(text)) {
        log.warning(FILE_NAME + " 缺少 active-model 项, 模型切换只在本次运行有效");
        return true;
      }
      Files.writeString(file.toPath(), updated, StandardCharsets.UTF_8);
      return true;
    } catch (IOException e) {
      log.warning("无法把模型选择写入 " + FILE_NAME + ": " + e.getMessage());
      return false;
    }
  }

  private static @NotNull String trimTrailingSlash(@NotNull String url) {
    var trimmed = url.trim();
    return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
  }
}
