package io.github.hello09x.fakeplayer.core.aichat;

import org.jetbrains.annotations.NotNull;

/**
 * 一条对话消息 (role: system / user / assistant)
 */
public record ChatMessage(@NotNull String role, @NotNull String content) {

    public final static String ROLE_SYSTEM = "system";

    public final static String ROLE_USER = "user";

    public final static String ROLE_ASSISTANT = "assistant";

}
