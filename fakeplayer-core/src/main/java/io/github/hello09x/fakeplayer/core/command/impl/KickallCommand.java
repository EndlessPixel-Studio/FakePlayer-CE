package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class KickallCommand extends AbstractCommand {

    /**
     * 移除服务器所有假人
     */
    public void kickall(@NotNull CommandSender sender, @NotNull CommandArguments args) {
        manager.removeAll("Command kickall");
    }

}
