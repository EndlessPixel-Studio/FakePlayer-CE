package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.StringJoiner;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

@Singleton
public class KillCommand extends AbstractCommand {

    /**
     * 杀死假人 (真实死亡, 而非移除)
     */
    public void kill(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var fakes = super.getFakeplayers(sender, args);

        var names = new StringJoiner(", ");
        for (var fake : fakes) {
            if (fake.isDead()) {
                continue;
            }
            var name = fake.getName();
            fake.setHealth(0D);
            names.add(name);
        }

        if (names.length() == 0) {
            sender.sendMessage(translatable("fakeplayer.command.kill.error.non-killed", GRAY));
            return;
        }

        sender.sendMessage(textOfChildren(
                translatable("fakeplayer.command.kill.success.killed", GRAY),
                space(),
                text(names.toString())
        ));
    }

}
