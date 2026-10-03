package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import io.github.hello09x.devtools.core.utils.ComponentUtils;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.github.hello09x.fakeplayer.core.manager.feature.FakeplayerFeatureManager;
import io.github.hello09x.fakeplayer.core.repository.model.Feature;
import net.kyori.adventure.text.format.Style;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.kyori.adventure.text.Component.*;
import static net.kyori.adventure.text.JoinConfiguration.separator;
import static net.kyori.adventure.text.event.ClickEvent.runCommand;
import static net.kyori.adventure.text.format.NamedTextColor.*;
import static net.kyori.adventure.text.format.TextDecoration.UNDERLINED;

@Singleton
public class ConfigCommand extends AbstractCommand {

    private final FakeplayerFeatureManager featureManager;

    @Inject
    public ConfigCommand(FakeplayerFeatureManager featureManager) {
        this.featureManager = featureManager;
    }

    /**
     * 设置配置
     * <p>玩家默认设置自己的; op 与控制台可以通过 {@code [player]} 指定其他玩家</p>
     */
    public void setConfig(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var feature = (Feature) Objects.requireNonNull(args.get("feature"));
        var target = this.getTarget(sender, args);
        if (target == null) {
            throw CommandAPI.failWithString(ComponentUtils.toString(
                    translatable("fakeplayer.command.config.set.error.missing-player"),
                    TranslatorUtils.getLocale(sender)
            ));
        }

        if (!target.equals(sender) && !sender.isOp()) {
            throw CommandAPI.failWithString(ComponentUtils.toString(
                    translatable("fakeplayer.command.config.set.error.no-permission"),
                    TranslatorUtils.getLocale(sender)
            ));
        }

        // 离线玩家查不了权限; 能走到这里的离线目标只可能是 op / 控制台指定的 (上面的检查已保证)
        if (target instanceof Player onlineTarget && !feature.testPermissions(onlineTarget)) {
            throw CommandAPI.failWithString(ComponentUtils.toString(
                    translatable("fakeplayer.command.config.set.error.target-no-permission"),
                    TranslatorUtils.getLocale(sender)
            ));
        }

        var option = (String) Objects.requireNonNull(args.get("option"));
        featureManager.setFeature(target, feature, option);
        sender.sendMessage(translatable(
                "fakeplayer.command.config.set.success",
                translatable(feature.translationKey(), GOLD),
                text(option, WHITE)
        ).color(GRAY));
    }

    /**
     * 获取配置
     * <p>玩家默认查看自己的; op 与控制台可以通过 {@code [player]} 指定其他玩家;
     * 控制台未指定玩家时展示的即全局默认值</p>
     */
    public void listConfig(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var target = this.getTarget(sender, args);
        if (target != null && !target.equals(sender) && !sender.isOp()) {
            throw CommandAPI.failWithString(ComponentUtils.toString(
                    translatable("fakeplayer.command.config.set.error.no-permission"),
                    TranslatorUtils.getLocale(sender)
            ));
        }

        var userConfigs = target == null ? null : featureManager.getUserConfigs(target.getUniqueId());
        var suffix = (target == null || target.equals(sender) || target.getName() == null) ? "" : " " + target.getName();
        CompletableFuture.runAsync(() -> {
            var features = userConfigs == null
                    ? featureManager.getFeatures(sender)
                    : featureManager.getFeatures(sender, userConfigs);
            var lines = features.values().stream().map(feature -> textOfChildren(
                    translatable(feature.key(), GOLD),
                    text(": ", GRAY),
                    join(separator(space()), feature.key().getOptions().stream().map(option -> {
                        var style = option.equals(feature.value())
                                ? Style.style(GREEN, UNDERLINED)
                                : Style.style(GRAY);
                        return text("[" + option + "]").style(style).clickEvent(
                                runCommand("/fp config set " + feature.key() + " " + option + suffix)
                        );
                    }).toList())
            )).toList();
            var message = join(separator(newline()), lines);
            Schedulers.runFor(Main.getInstance(), sender, () -> sender.sendMessage(message));
        });
    }

    /**
     * 解析目标玩家
     * <p>未指定 {@code [player]} 时: 玩家发送者为自己, 控制台为 {@code null} (即没有个人配置, 使用默认值)</p>
     */
    private @Nullable OfflinePlayer getTarget(@NotNull CommandSender sender, @NotNull CommandArguments args) {
        var target = (OfflinePlayer) args.getOptional("player").orElse(null);
        if (target != null) {
            return target;
        }
        return sender instanceof Player player ? player : null;
    }

}
