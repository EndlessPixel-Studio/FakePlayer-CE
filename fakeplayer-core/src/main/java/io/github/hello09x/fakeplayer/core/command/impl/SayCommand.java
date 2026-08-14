package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.translatable;

@Singleton
public class SayCommand extends AbstractCommand {

    private final ActionManager actionManager;

    @Inject
    public SayCommand(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    /**
     * 让假人发送聊天消息。支持一次性 / 持续 / 间隔执行。
     *
     * @param action  执行方式 (once / continuous / interval / stop)
     * @param setting 执行设置 (含 message)
     */
    public @NotNull CommandExecutor say(@NotNull ActionSetting setting) {
        return (sender, args) -> {
            var message = (String) args.get("message");
            var fake = super.getFakeplayer(sender, args);
            var copy = setting.clone();
            copy.message = message;
            actionManager.setAction(fake, ActionType.SAY, copy);
            if (!setting.equals(ActionSetting.once()) || sender instanceof ConsoleCommandSender) {
                sender.sendMessage(translatable("fakeplayer.command.generic.success"));
            }
        };
    }

}
