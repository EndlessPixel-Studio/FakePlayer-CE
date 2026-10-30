package io.github.hello09x.fakeplayer.core.aichat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.logging.Logger;

/**
 * AI 聊天配置, 独立存放于 {@code ai-chat.yml}
 */
@Getter
@Singleton
public class AiChatConfig {

    private final static Logger log = Main.getInstance().getLogger();

    private final static String FILE_NAME = "ai-chat.yml";

    private final static String DEFAULT_SYSTEM_PROMPT = """
            你是 Minecraft 服务器中的一名玩家, 名字是 {name}。
            说话简短、自然、口语化, 每次只说一到两句话, 不要使用括号描述动作, 不要提到自己是 AI。
            """;

    private boolean enabled;

    private String api;

    private String apiKey;

    private String apiKeyEnv;

    private String model;

    private double temperature;

    private double topP;

    private double presencePenalty;

    private double frequencyPenalty;

    private int maxTokens;

    private int maxContext;

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
        this.temperature = yaml.getDouble("temperature", 1.0D);
        this.topP = yaml.getDouble("top-p", 1.0D);
        this.presencePenalty = yaml.getDouble("presence-penalty", 0.0D);
        this.frequencyPenalty = yaml.getDouble("frequency-penalty", 0.0D);
        this.maxTokens = yaml.getInt("max-tokens", 0);
        this.maxContext = Math.max(0, yaml.getInt("max-context", 20));
        this.timeoutSeconds = Math.max(1, yaml.getInt("timeout-seconds", 30));
        this.intervalTicks = Math.max(20, yaml.getInt("interval-ticks", 200));
        this.systemPrompt = yaml.getString("system-prompt", DEFAULT_SYSTEM_PROMPT);
        this.turnPrompt = yaml.getString("turn-prompt", "说一句话。");
        this.privateFormat = yaml.getString("private-format", "<gray>[{name} -> 你] {message}</gray>");

        this.validate();
    }

    private void validate() {
        if (!this.enabled) {
            return;
        }
        if (this.api == null || this.api.isBlank()) {
            log.warning("ai-chat.yml 已启用但 api 为空, AI 聊天将保持关闭");
            this.enabled = false;
            return;
        }
        if (this.model == null || this.model.isBlank()) {
            log.warning("ai-chat.yml 已启用但 model 为空, AI 聊天将保持关闭");
            this.enabled = false;
            return;
        }
        if (this.resolveApiKey() == null) {
            log.warning("ai-chat.yml 已启用但未配置密钥, 请填写 api-key 或设置 "
                    + this.apiKeyEnv + " 环境变量, 否则请求会失败");
        }
    }

    /**
     * 实际可用的密钥: 优先环境变量 (配置了 api-key-env 时), 否则读 api-key
     */
    public @Nullable String resolveApiKey() {
        if (this.apiKeyEnv != null && !this.apiKeyEnv.isBlank()) {
            var fromEnv = System.getenv(this.apiKeyEnv.trim());
            if (fromEnv != null && !fromEnv.isBlank()) {
                return fromEnv;
            }
        }
        return this.apiKey == null || this.apiKey.isBlank() ? null : this.apiKey.trim();
    }

    public @NotNull String getCompletionsUrl() {
        return this.api + "/chat/completions";
    }

    private static @NotNull String trimTrailingSlash(@NotNull String url) {
        var trimmed = url.trim();
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }

}
