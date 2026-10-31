package io.github.hello09x.fakeplayer.core.manager.action;

import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionTicker;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.api.spi.NMSBridge;
import io.github.hello09x.fakeplayer.core.Main;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class ActionManager {

    private final static Logger log = Main.getInstance().getLogger();

    private final Map<UUID, Map<ActionType, ActionTicker>> managers = new ConcurrentHashMap<>();

    private final NMSBridge bridge;


    @Inject
    public ActionManager(NMSBridge bridge) {
        this.bridge = bridge;
        // tick 由各假人的 FakeplayerTicker 在实体调度器上按玩家驱动 (Folia 兼容), 见 #tick(Player)
    }

    public boolean hasActiveAction(
            @NotNull Player player,
            @NotNull ActionType action
    ) {
        return Optional.ofNullable(this.managers.get(player.getUniqueId()))
                       .map(manager -> manager.get(action))
                       // -1 表示持续执行 (continuous) 的动作, 与 getActiveActions 的判定保持一致
                       .filter(ac -> ac.getSetting().remains > 0 || ac.getSetting().remains == -1)
                       .isPresent();
    }

    public @NotNull @Unmodifiable Set<ActionType> getActiveActions(@NotNull Player player) {
        var manager = this.managers.get(player.getUniqueId());
        if (manager == null || managers.isEmpty()) {
            return Collections.emptySet();
        }

        return manager.entrySet()
                      .stream()
                      .filter(action -> {
                          int remains = action.getValue().getSetting().remains;
                          return remains > 0 || remains == -1;
                      })
                      .map(Map.Entry::getKey)
                      .collect(Collectors.toSet());
    }

    /**
     * 复制当前活跃动作的配置, 供持久化 (如 restore-on-start) 使用。
     * <p>返回的是副本, 快照线程无需担心与 tick 线程竞争。</p>
     */
    public @NotNull Map<ActionType, ActionSetting> getSettings(@NotNull Player player) {
        var tickers = this.managers.get(player.getUniqueId());
        if (tickers == null || tickers.isEmpty()) {
            return Map.of();
        }

        var settings = new EnumMap<ActionType, ActionSetting>(ActionType.class);
        for (var entry : tickers.entrySet()) {
            var setting = entry.getValue().getSetting();
            if (setting.remains <= 0 && setting.remains != -1) {
                continue;
            }
            var copy = new ActionSetting(setting.maximum, setting.interval, setting.wait, setting.message);
            copy.remains = setting.remains;
            copy.targetX = setting.targetX;
            copy.targetY = setting.targetY;
            copy.targetZ = setting.targetZ;
            settings.put(entry.getKey(), copy);
        }
        return settings;
    }

    public void setAction(
            @NotNull Player player,
            @NotNull ActionType action,
            @NotNull ActionSetting setting
    ) {
        var managers = this.managers.computeIfAbsent(player.getUniqueId(), key -> new ConcurrentHashMap<>());
        managers.put(action, bridge.createAction(player, action, setting));
    }

    /**
     * 立即执行一次动作, 不经过 ticker
     * <p>同一 tick 内连续调用时, 走 ticker 会被后一次覆盖, 前一次永远不执行 (如连续 /fp say once 丢消息), 这里直接执行。</p>
     * <p>必须在假人所属的实体线程调用。</p>
     */
    public void executeOnce(
            @NotNull Player player,
            @NotNull ActionType action,
            @NotNull ActionSetting setting
    ) {
        var oneShot = setting.clone();
        oneShot.remains = 1;
        bridge.createAction(player, action, oneShot).tick();
    }

    public void stop(@NotNull Player player) {
        var managers = this.managers.get(player.getUniqueId());
        if (managers == null || managers.isEmpty()) {
            return;
        }

        for (var entry : managers.entrySet()) {
            // 原先把 ticker 直接和 ActionSetting 比 equals，类型不同恒 false，守卫一直没生效
            if (!entry.getValue().getSetting().equals(ActionSetting.stop())) {
                entry.setValue(bridge.createAction(player, entry.getKey(), ActionSetting.stop()));
            }
        }
    }

    /**
     * tick 指定假人的动作。
     * <p>由 {@link io.github.hello09x.fakeplayer.core.entity.FakeplayerTicker} 在假人所属区域线程驱动
     * (Folia 下不能跨区域线程操作实体)。</p>
     */
    public void tick(@NotNull Player player) {
        var tickers = this.managers.get(player.getUniqueId());
        if (tickers == null || tickers.isEmpty()) {
            return;
        }

        if (!player.isValid()) {
            // 假人下线或者死亡
            this.managers.remove(player.getUniqueId());
            for (var ticker : tickers.values()) {
                ticker.stop();
            }
            return;
        }

        tickers.values().removeIf(ticker -> {
            try {
                return ticker.tick();
            } catch (Throwable e) {
                log.warning(Throwables.getStackTraceAsString(e));
                return false;
            }
        });
        if (tickers.isEmpty()) {
            this.managers.remove(player.getUniqueId());
        }
    }

}