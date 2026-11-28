package io.github.endlesspixel.fakeplayer.core.aichat;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 一个可选的 AI 模型条目, 接口与密钥可以单独覆盖 */
public record AiChatModel(
    @NotNull String name,
    @NotNull String model,
    @NotNull String api,
    @NotNull String apiKeyEnv,
    @NotNull String apiKey) {

  public @Nullable String resolveApiKey() {
    if (!this.apiKeyEnv.isBlank()) {
      var fromEnv = System.getenv(this.apiKeyEnv.trim());
      if (fromEnv != null && !fromEnv.isBlank()) {
        return fromEnv.trim();
      }
    }
    return this.apiKey.isBlank() ? null : this.apiKey.trim();
  }

  public @NotNull String getCompletionsUrl() {
    return this.api + "/chat/completions";
  }
}
