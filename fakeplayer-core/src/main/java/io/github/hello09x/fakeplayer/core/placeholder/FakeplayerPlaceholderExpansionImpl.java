package io.github.hello09x.fakeplayer.core.placeholder;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.translation.PluginTranslator;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tanyaofei
 * @since 2024/8/15
 **/
@Singleton
public class FakeplayerPlaceholderExpansionImpl extends PlaceholderExpansion implements FakeplayerPlaceholderExpansion {

    private final FakeplayerManager manager;
    private final ActionManager actionManager;
    private final PluginTranslator translator;

    @Inject
    public FakeplayerPlaceholderExpansionImpl(FakeplayerManager manager, ActionManager actionManager, PluginTranslator translator) {
        this.manager = manager;
        this.actionManager = actionManager;
        this.translator = translator;
    }

    @Override
    public @NotNull String getIdentifier() {
        // 固定为 "fakeplayer"，避免因插件名（如 FakePlayer-CE）变化导致占位符前缀变为
        // fakeplayer-ce_xxx 而与原文档/社区一致的 fakeplayer_xxx 不一致（tanyaofei #172）
        return "fakeplayer";
    }

    @Override
    public @NotNull String getAuthor() {
        return Iterables.getFirst(Main.getInstance().getPluginMeta().getAuthors(), "hello09x");
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        // /papi parse --null fakeplayer_total
        if (params.equalsIgnoreCase("total")) {
            return String.valueOf(manager.getSize());
        }

        // /papi parse CONSOLE_1 fakeplayer_creator
        if (params.equalsIgnoreCase("creator") && player != null && manager.isFake(player)) {
            return Optional.ofNullable(manager.getCreatorName(player)).orElse(params);
        }

        // papi parse CONSOLE_1 fakeplayer_actions
        if (params.equalsIgnoreCase("actions") && player != null && manager.isFake(player)) {
            return actionManager.getActiveActions(player).stream().map(Enum::name).collect(Collectors.joining("|"));
        }

        // papi parse CONSOLE_1 fakeplayer_actions_translated
        if (params.equalsIgnoreCase("actions_translated") && player != null && manager.isFake(player)) {
            return actionManager.getActiveActions(player).stream().map(actionType -> {
                var format = translator.translate(actionType.translationKey(), null);
                return format == null ? "" : format.format(new Object[0]);
            }).collect(Collectors.joining("|"));
        }

        return params;

    }

    @EventHandler
    public void unregister(@NotNull PluginDisableEvent event) {
        if (event.getPlugin() == Main.getInstance()) {
            this.unregister();
        }
    }
}