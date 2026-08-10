package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Singleton
public class SayCommand extends AbstractCommand {

    /**
     * 让假人发送聊天信息
     */
    public void say(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var fake = getFakeplayer(sender, args);
        var message = Objects.requireNonNull((String) args.get("message"));

        bridge.fromPlayer(fake).chat(message);
    }

}
