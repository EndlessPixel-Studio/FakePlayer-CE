package io.github.hello09x.fakeplayer.core.aichat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * 收集公共聊天, 供 AI 接话时参考
 */
@Singleton
public class AiChatListener implements Listener {

    private final AiChatManager aiChatManager;

    @Inject
    public AiChatListener(AiChatManager aiChatManager) {
        this.aiChatManager = aiChatManager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(@NotNull AsyncChatEvent event) {
        var message = PlainTextComponentSerializer.plainText().serialize(event.message()).trim();
        if (message.isEmpty()) {
            return;
        }
        this.aiChatManager.onPlayerChat(event.getPlayer(), message);
    }

}
