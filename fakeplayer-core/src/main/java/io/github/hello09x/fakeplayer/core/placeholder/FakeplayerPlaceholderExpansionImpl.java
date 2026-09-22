package io.github.hello09x.fakeplayer.core.placeholder;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.translation.PluginTranslator;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.constant.MetadataKeys;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tanyaofei
 * @since 2024/8/15
 **/
@Singleton
public class FakeplayerPlaceholderExpansionImpl extends PlaceholderExpansion implements FakeplayerPlaceholderExpansion {

    private final static String DEFAULT_SEPARATOR = ", ";

    private final static String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 1 tick = 50ms
     */
    private final static long TICK_MILLIS = 50L;

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
        return "1.1";
    }

    @Override
    public @Nullable String onPlaceholderRequest(@Nullable Player player, @NotNull String params) {
        var key = params.toLowerCase(Locale.ROOT);

        // 全局: /papi parse --null fakeplayer_total
        if (key.equals("total") || key.equals("list_size")) {
            return String.valueOf(manager.getSize());
        }

        // 全局: /papi parse --null fakeplayer_list
        if (key.equals("list")) {
            return manager.getAll()
                          .stream()
                          .map(Player::getName)
                          .collect(Collectors.joining(separator()));
        }

        // 全局: /papi parse --null fakeplayer_list_0_name
        if (key.startsWith("list_")) {
            var rest = key.substring("list_".length());
            var split = rest.indexOf('_');
            var indexPart = split < 0 ? rest : rest.substring(0, split);
            var attribute = split < 0 ? "name" : rest.substring(split + 1);
            var index = parseInt(indexPart);
            if (index == null) {
                return params;
            }
            var all = manager.getAll();
            if (index < 0 || index >= all.size()) {
                return "";
            }
            return Optional.ofNullable(attribute(all.get(index), attribute)).orElse(params);
        }

        if (player == null) {
            return params;
        }

        // 任意玩家: /papi parse <player> fakeplayer_isfake
        if (key.equals("isfake")) {
            return String.valueOf(manager.isFake(player));
        }

        if (!manager.isFake(player)) {
            return params;
        }

        // 假人: /papi parse <fake player> fakeplayer_name
        return Optional.ofNullable(attribute(player, key)).orElse(params);
    }

    /**
     * 取假人的某个属性, 无法识别时返回 null
     */
    private @Nullable String attribute(@NotNull Player fake, @NotNull String attribute) {
        return switch (attribute) {
            case "name" -> fake.getName();
            case "uuid" -> fake.getUniqueId().toString();
            case "creator", "spawner" -> manager.getCreatorName(fake);
            case "spawntime" -> spawnTime(fake);
            case "world" -> fake.getWorld().getName();
            case "x" -> String.valueOf(fake.getLocation().getBlockX());
            case "y" -> String.valueOf(fake.getLocation().getBlockY());
            case "z" -> String.valueOf(fake.getLocation().getBlockZ());
            case "health" -> String.valueOf(fake.getHealth());
            case "food" -> String.valueOf(fake.getFoodLevel());
            case "level" -> String.valueOf(fake.getLevel());
            case "gamemode" -> fake.getGameMode().name();
            case "actions" -> actions(fake, false);
            case "actions_translated" -> actions(fake, true);
            default -> null;
        };
    }

    /**
     * 当前进行中的动作
     */
    private @NotNull String actions(@NotNull Player fake, boolean translated) {
        return actionManager
                .getActiveActions(fake)
                .stream()
                .map(actionType -> {
                    if (!translated) {
                        return actionType.name();
                    }
                    var format = translator.translate(actionType.translationKey(), null);
                    return format == null ? "" : format.format(new Object[0]);
                })
                .collect(Collectors.joining("|"));
    }

    /**
     * 假人的生成时间。{@link MetadataKeys#SPAWNED_AT} 记录的是生成时的 tick, 换算回墙上时间
     */
    private @Nullable String spawnTime(@NotNull Player fake) {
        var tick = MetadataKeys.getSpawnedAt(fake);
        if (tick == null) {
            return null;
        }
        var millis = System.currentTimeMillis() - (Bukkit.getCurrentTick() - tick) * TICK_MILLIS;
        return formatTime(millis);
    }

    private @NotNull String formatTime(long millis) {
        var pattern = text("fakeplayer.placeholder.time-format", DEFAULT_TIME_FORMAT);
        DateTimeFormatter formatter;
        try {
            formatter = DateTimeFormatter.ofPattern(pattern);
        } catch (IllegalArgumentException e) {
            formatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
        }
        return formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault()));
    }

    private @NotNull String separator() {
        return text("fakeplayer.placeholder.separator", DEFAULT_SEPARATOR);
    }

    /**
     * 取一条翻译文本, 取不到时使用默认值
     */
    private @NotNull String text(@NotNull String key, @NotNull String defaultValue) {
        MessageFormat format;
        try {
            format = translator.translate(key, null);
        } catch (Exception e) {
            return defaultValue;
        }
        if (format == null) {
            return defaultValue;
        }
        var text = format.format(new Object[0]);
        return text.isBlank() ? defaultValue : text;
    }

    private static @Nullable Integer parseInt(@NotNull String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @EventHandler
    public void unregister(@NotNull PluginDisableEvent event) {
        if (event.getPlugin() == Main.getInstance()) {
            this.unregister();
        }
    }
}
