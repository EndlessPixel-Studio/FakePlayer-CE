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
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Singleton
public class ActionManager {

    private final static Logger log = Main.getInstance().getLogger();

    private final Map<UUID, Map<ActionType, ActionTicker>> managers = new HashMap<>();

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
                       .filter(ac -> ac.getSetting().remains > 0)
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

    public void setAction(
            @NotNull Player player,
            @NotNull ActionType action,
            @NotNull ActionSetting setting
    ) {
        var managers = this.managers.computeIfAbsent(player.getUniqueId(), key -> new HashMap<>());
        managers.put(action, bridge.createAction(player, action, setting));
    }

    public void stop(@NotNull Player player) {
        var managers = this.managers.get(player.getUniqueId());
        if (managers == null || managers.isEmpty()) {
            return;
        }

        for (var entry : managers.entrySet()) {
            if (!entry.getValue().equals(ActionSetting.stop())) {
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