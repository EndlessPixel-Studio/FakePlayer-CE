package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.repository.FakeplayerAuthRepository;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@Singleton
public class PasswordCommand extends AbstractCommand {

    @Inject
    private FakeplayerAuthRepository authRepository;

    /**
     * 设置假人的登录密码, 用于生成后自动登录
     */
    public void setPassword(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var target = super.getFakeplayer(sender, args);
        var password = (String) args.get("password");
        if (password == null || password.isBlank()) {
            sender.sendMessage(translatable("fakeplayer.command.password.error.empty", NamedTextColor.RED));
            return;
        }

        authRepository.saveOrUpdate(target.getName(), password);
        sender.sendMessage(translatable("fakeplayer.command.password.success", text(target.getName())).color(NamedTextColor.GREEN));
    }

}
