package io.github.endlesspixel.fakeplayer.core.manager;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * 根据最近一分钟的平均 TPS 动态调整每位玩家的假人上限。
 *
 * <p>参考 xiplugin/FakePlayerPlus 的同名功能：TPS 低于阈值时每隔一个周期把上限降 1，并移除创建者名下最新召唤的超额假人；TPS
 * 恢复后每周期降价 1，直到回到配置的上限。减少量只影响「该玩家上限」，不修改配置文件。
 */
@Singleton
public class TpsAdaptiveLimitManager {

  private static final Logger log = Main.getInstance().getLogger();

  private final FakeplayerConfig config;
  private final Provider<FakeplayerManager> manager;
  private final ScheduledExecutorService executor;

  private volatile int reduction = 0;
  private ScheduledFuture<?> task;

  @Inject
  public TpsAdaptiveLimitManager(
      @NotNull FakeplayerConfig config, @NotNull Provider<FakeplayerManager> manager) {
    this.config = config;
    this.manager = manager;
    ThreadFactory factory =
        runnable -> {
          var thread = new Thread(runnable, "fakeplayer-tps-limit");
          thread.setDaemon(true);
          return thread;
        };
    this.executor = Executors.newSingleThreadScheduledExecutor(factory);
  }

  /** 启动定时器；配置未启用时不注册任何任务 */
  public void start() {
    this.reschedule();
  }

  /** 配置重载后按新参数重新注册定时器 */
  public void reload() {
    this.reduction = 0;
    this.reschedule();
  }

  /** 插件停用时停止定时器并恢复上限 */
  public void shutdown() {
    this.reduction = 0;
    if (this.task != null) {
      this.task.cancel(false);
      this.task = null;
    }
    this.executor.shutdownNow();
  }

  /** 当前从每位玩家上限中扣减的数量 */
  public int getReduction() {
    return this.reduction;
  }

  /** 扣除动态减少量后的玩家上限，至少为配置的下限 */
  public int effectiveLimit() {
    return Math.max(
        this.config.getTpsAdaptiveMinCount(),
        this.config.getPlayerLimit() - this.reduction);
  }

  private void reschedule() {
    if (this.task != null) {
      this.task.cancel(false);
      this.task = null;
    }
    if (!this.config.isTpsAdaptiveEnabled()) {
      return;
    }
    var interval = Math.max(1, this.config.getTpsAdaptiveInterval());
    this.task =
        this.executor.scheduleWithFixedDelay(
            this::checkSafely, interval, interval, TimeUnit.SECONDS);
  }

  private void checkSafely() {
    try {
      this.check();
    } catch (Throwable e) {
      // 定时任务里抛异常会让它永久停止, 后续再也不检查 TPS
      log.warning("TPS adaptive limit check failed: " + e);
    }
  }

  private void check() {
    var tps = Bukkit.getServer().getTPS()[0];
    if (tps >= this.config.getTpsAdaptiveThreshold()) {
      if (this.reduction > 0) {
        this.reduction--;
        var tpsText = text(String.valueOf(tps), GOLD);
        var limitText = text(this.effectiveLimit(), GOLD);
        var message =
            translatable("fakeplayer.tps-adaptive.limit-recovered", tpsText, limitText)
                .color(GREEN);
        Schedulers.global(Main.getInstance(), () -> this.notifyPlayers(message));
      }
      return;
    }

    var maxReduction =
        Math.max(0, this.config.getPlayerLimit() - this.config.getTpsAdaptiveMinCount());
    if (this.reduction >= maxReduction) {
      return;
    }
    this.reduction++;
    var limit = this.effectiveLimit();
    var tpsText = text(String.valueOf(tps), GOLD);
    var limitText = text(limit, GOLD);

    Schedulers.global(
        Main.getInstance(),
        () -> {
          for (Player player : Bukkit.getOnlinePlayers()) {
            if (this.manager.get().isFake(player)) {
              continue;
            }
            LangUtils.send(
                player,
                translatable("fakeplayer.tps-adaptive.limit-decreased", tpsText, limitText)
                    .color(RED));
            List<Player> fakes = this.manager.get().getAll(player);
            for (var i = fakes.size() - 1; i >= limit; i--) {
              this.manager.get().remove(fakes.get(i).getName(), "TPS adaptive limit");
            }
          }
        });
  }

  private void notifyPlayers(net.kyori.adventure.text.Component message) {
    for (Player player : Bukkit.getOnlinePlayers()) {
      if (this.manager.get().isFake(player)) {
        continue;
      }
      LangUtils.send(player, message);
    }
  }
}
