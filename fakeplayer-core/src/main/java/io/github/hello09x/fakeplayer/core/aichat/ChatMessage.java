package io.github.hello09x.fakeplayer.core.aichat;

import org.jetbrains.annotations.NotNull;

/** 一条对话消息 (role: system / user / assistant) */
public record ChatMessage(@NotNull String role, @NotNull String content) {

  public static final String ROLE_SYSTEM = "system";

  public static final String ROLE_USER = "user";

  public static final String ROLE_ASSISTANT = "assistant";
}
