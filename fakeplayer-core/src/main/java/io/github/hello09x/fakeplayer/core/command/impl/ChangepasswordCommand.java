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

@Singleton
public class ChangepasswordCommand extends AbstractCommand {

    @Inject
    private FakeplayerAuthRepository authRepository;

    /**
     * 修改假人的登录密码, 校验旧密码后同步到登录插件与数据库
     */
    public void change(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var target = super.getFakeplayer(sender, args);
        var oldPassword = (String) args.get("old");
        var newPassword = (String) args.get("new");

        if (newPassword == null || newPassword.isBlank()) {
            sender.sendMessage(text("新密码不能为空").color(NamedTextColor.RED));
            return;
        }

        var stored = authRepository.selectByName(target.getName());
        if (stored == null) {
            sender.sendMessage(text("该假人尚未设置密码, 请先使用 /fp password 设置").color(NamedTextColor.RED));
            return;
        }

        if (!stored.equals(oldPassword)) {
            sender.sendMessage(text("旧密码不正确").color(NamedTextColor.RED));
            return;
        }

        var template = config.getChangePasswordCommand();
        authRepository.saveOrUpdate(target.getName(), newPassword);
        var cmd = template.replace("%oldpassword%", oldPassword).replace("%newpassword%", newPassword);
        manager.issueCommands(target, List.of(cmd));
        sender.sendMessage(text("已为假人 " + target.getName() + " 修改登录密码").color(NamedTextColor.GREEN));
    }

}
