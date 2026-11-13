package io.github.hello09x.fakeplayer.core.aichat;

import org.jetbrains.annotations.NotNull;

/** 一条对话消息 (role: system / user / assistant) */
public record ChatMessage(@NotNull String role, @NotNull String content) {

  public static final String ROLE_SYSTEM = "system";

  public static final String ROLE_USER = "user";

  public static final String ROLE_ASSISTANT = "assistant";

  public ChatMessage {
    if (role == null || role.isBlank()) {
      throw new IllegalArgumentException("role must not be blank");
    }
    if (content == null) {
      throw new IllegalArgumentException("content must not be null");
    }
  }

  public static @NotNull ChatMessage system(@NotNull String content) {
    return new ChatMessage(ROLE_SYSTEM, content);
  }

  public static @NotNull ChatMessage user(@NotNull String content) {
    return new ChatMessage(ROLE_USER, content);
  }

  public static @NotNull ChatMessage assistant(@NotNull String content) {
    return new ChatMessage(ROLE_ASSISTANT, content);
  }
}
