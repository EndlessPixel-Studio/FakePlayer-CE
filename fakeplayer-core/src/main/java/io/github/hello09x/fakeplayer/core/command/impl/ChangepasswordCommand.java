package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.repository.FakeplayerAuthRepository;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

@Singleton
public class ChangepasswordCommand extends AbstractCommand {

    @Inject
    private FakeplayerAuthRepository authRepository;

    /**
     * 修改假人的登录密码, 校验旧密码后同步到登录插件与数据库
     */
    public void change(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var target = super.getFakeplayer(sender, args);
        var oldInput = (String) args.get("old");
        var newPassword = (String) args.get("new");

        if (newPassword == null || newPassword.isBlank()) {
            sender.sendMessage(translatable("fakeplayer.command.changepassword.error.empty", NamedTextColor.RED));
            return;
        }

        var stored = authRepository.selectByName(target.getName());
        if (stored == null) {
            sender.sendMessage(translatable("fakeplayer.command.changepassword.error.not-set", NamedTextColor.RED));
            return;
        }

        // 旧密码可省略: 省略时直接使用数据库中已保存的密码, 方便忘记随机密码时重置
        String oldPassword;
        if (oldInput == null || oldInput.isBlank()) {
            oldPassword = stored;
        } else if (!stored.equals(oldInput)) {
            sender.sendMessage(translatable("fakeplayer.command.changepassword.error.wrong-old", NamedTextColor.RED));
            return;
        } else {
            oldPassword = oldInput;
        }

        var template = config.getChangePasswordCommand();
        authRepository.saveOrUpdate(target.getName(), newPassword);
        var cmd = template.replace("%oldpassword%", oldPassword).replace("%newpassword%", newPassword);
        manager.issueCommands(target, List.of(cmd));
        sender.sendMessage(translatable("fakeplayer.command.changepassword.success", text(target.getName())).color(NamedTextColor.GREEN));
    }

}
