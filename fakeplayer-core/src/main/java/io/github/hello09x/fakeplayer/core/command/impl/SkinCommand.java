package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.compat.skin.SkinsRestorerCompat;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerSkinManager;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import org.apache.commons.lang3.mutable.MutableInt;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@Singleton
public class SkinCommand extends AbstractCommand {

    private final Map<CommandSender, MutableInt> spams = new HashMap<>();

    private final FakeplayerSkinManager manager;

    @Inject
    public SkinCommand(FakeplayerSkinManager manager) {
        this.manager = manager;
        Schedulers.globalTimer(Main.getInstance(), 0, 1, () -> {
            spams.entrySet().removeIf(counter -> counter.getValue().decrementAndGet() <= 0);
        });
    }

    /**
     * 复制皮肤
     */
    public void skin(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var fake = getFakeplayer(sender, args);
        var player = Objects.requireNonNull((OfflinePlayer) args.get("player"));

        // 装有 SkinsRestorer 时优先用它: 可按正版玩家名解析皮肤并持久化到其存储
        var compat = SkinsRestorerCompat.get();
        if (compat != null && player.getName() != null) {
            this.applyWithSkinsRestorer(sender, fake, player, compat);
            return;
        }

        if (manager.useSkin(fake, player)) {
            manager.rememberSkin(sender, fake, player);
            return;
        }

        // 限制请求数, 防止 mojang api 限流
        if (!sender.isOp() && spams.computeIfAbsent(sender, k -> new MutableInt()).getValue() != 0) {
            sender.sendMessage(translatable("fakeplayer.command.skin.error.too-many-operations", RED));
            return;
        }

        try {
            this.manager.useOnlineSkinAsync(fake, player)
                        .thenAcceptAsync(success -> {
                            manager.rememberSkin(sender, fake, player);
                            Schedulers.entity(Main.getInstance(), fake.getPlayer(), () -> {
                                if (success) {
                                    fake.sendMessage(translatable("fakeplayer.command.generic.success"));
                                }
                            });
                        });
        } finally {
            if (!sender.isOp()) {
                spams.put(sender, new MutableInt(1200));
            }
        }
    }

    /**
     * SkinsRestorer 路径: 异步解析并持久化, 再在实体线程应用外观
     */
    private void applyWithSkinsRestorer(
            @NotNull CommandSender sender,
            @NotNull Player fake,
            @NotNull OfflinePlayer from,
            @NotNull SkinsRestorerCompat compat
    ) {
        // 与兜底路径共用冷却, 避免频繁触发网络请求
        if (!sender.isOp() && spams.computeIfAbsent(sender, k -> new MutableInt()).getValue() != 0) {
            sender.sendMessage(translatable("fakeplayer.command.skin.error.too-many-operations", RED));
            return;
        }
        if (!sender.isOp()) {
            spams.put(sender, new MutableInt(1200));
        }

        var name = from.getName();
        var plugin = Main.getInstance();
        Schedulers.async(plugin, () -> {
            var result = compat.resolve(name);
            if (result == null) {
                Schedulers.runFor(plugin, sender, () -> sender.sendMessage(
                        translatable("fakeplayer.command.skin.error.not-found", RED, text(name))));
                return;
            }

            compat.save(fake.getUniqueId(), result.identifier());
            manager.rememberSkin(sender, fake, from);

            Schedulers.entity(plugin, fake, () -> compat.apply(fake, result.property()));
            Schedulers.runFor(plugin, sender, () -> sender.sendMessage(
                    translatable("fakeplayer.command.skin.success", GRAY, text(name))));
        });
    }

}
