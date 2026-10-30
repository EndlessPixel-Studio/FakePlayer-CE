package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import io.github.hello09x.devtools.core.utils.ComponentUtils;
import io.github.hello09x.fakeplayer.core.aichat.AiChatConfig;
import io.github.hello09x.fakeplayer.core.aichat.AiChatManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@Singleton
public class AiChatCommand extends AbstractCommand {

    private final AiChatManager aiChat;

    private final AiChatConfig aiChatConfig;

    @Inject
    public AiChatCommand(AiChatManager aiChat, AiChatConfig aiChatConfig) {
        this.aiChat = aiChat;
        this.aiChatConfig = aiChatConfig;
    }

    /**
     * 发一句到公共聊天
     */
    public void publicOnce(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var fake = this.readyFakeplayer(sender, args);
        this.aiChat.say(fake, null);
        sender.sendMessage(translatable("fakeplayer.command.generic.success", GRAY));
    }

    /**
     * 发一句给指定玩家
     */
    public void privateOnce(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var fake = this.readyFakeplayer(sender, args);
        this.aiChat.say(fake, this.getTarget(args));
        sender.sendMessage(translatable("fakeplayer.command.generic.success", GRAY));
    }

    public void publicContinuous(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        this.checkContinuous(sender);
        var fake = this.readyFakeplayer(sender, args);
        this.aiChat.start(fake, null, getInterval(args));
        sender.sendMessage(translatable("fakeplayer.command.aichat.started", GRAY));
    }

    public void privateContinuous(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        this.checkContinuous(sender);
        var fake = this.readyFakeplayer(sender, args);
        this.aiChat.start(fake, this.getTarget(args), getInterval(args));
        sender.sendMessage(translatable("fakeplayer.command.aichat.started", GRAY));
    }

    public void stop(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var fake = super.getFakeplayer(sender, args);
        if (this.aiChat.stop(fake)) {
            sender.sendMessage(translatable("fakeplayer.command.aichat.stopped", GRAY));
        } else {
            sender.sendMessage(translatable("fakeplayer.command.aichat.not-running", GRAY));
        }
    }

    private @NotNull Player readyFakeplayer(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        if (!this.aiChatConfig.isEnabled()) {
            throw this.fail(sender, "fakeplayer.command.aichat.error.disabled");
        }
        if (this.aiChatConfig.resolveApiKey() == null) {
            throw this.fail(sender, "fakeplayer.command.aichat.error.missing-api-key");
        }
        return super.getFakeplayer(sender, args);
    }

    private void checkContinuous(@NotNull CommandSender sender) throws WrapperCommandSyntaxException {
        if (!this.aiChatConfig.isAllowContinuous()) {
            throw this.fail(sender, "fakeplayer.command.aichat.error.continuous-disabled");
        }
    }

    private @NotNull Player getTarget(@NotNull CommandArguments args) {
        return Objects.requireNonNull((Player) args.get("target"));
    }

    private static @Nullable Integer getInterval(@NotNull CommandArguments args) {
        return args.getOptional("ticks").map(value -> ((Number) value).intValue()).orElse(null);
    }

    private @NotNull WrapperCommandSyntaxException fail(@NotNull CommandSender sender, @NotNull String key) {
        return CommandAPI.failWithString(ComponentUtils.toString(
                translatable(key),
                TranslatorUtils.getLocale(sender)
        ));
    }

}
