package io.github.hello09x.fakeplayer.core.entity;

import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerReplenishManager;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class FakeplayerTicker {

    private final static ActionManager actionManager = Main.getInjector().getInstance(ActionManager.class);

    public final static long NON_REMOVE_AT = -1;

    @NotNull
    private final Fakeplayer player;

    /**
     * 移除时间
     * <p>如果不需要定时移除则为 0</p>
     */
    private final long removeAt;

    /**
     * 是否是第一次 tick
     */
    private boolean firstTick;

    /**
     * 实体调度任务句柄 (Folia)
     */
    private volatile ScheduledTask task;

    public FakeplayerTicker(
            @NotNull Fakeplayer player,
            long lifespan
    ) {
        this.player = player;
        this.removeAt = lifespan > 0 ? System.currentTimeMillis() + lifespan : NON_REMOVE_AT;
        this.firstTick = true;
    }

    /**
     * 启动周期性 tick。
     * <p>使用实体调度器, 保证在假人所属区域线程执行 (Folia 兼容); 假人被移除后任务自动停止。</p>
     */
    public void run() {
        this.task = Schedulers.entityTimer(Main.getInstance(), this.player.getPlayer(), 0, 1, this::tick);
    }

    private void tick() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        if (this.removeAt != NON_REMOVE_AT && this.player.getTickCount() % 20 == 0 && System.currentTimeMillis() > removeAt) {
            Main.getInjector().getInstance(FakeplayerManager.class).remove(player.getName(), "lifespan ends");
            this.cancel();
            return;
        }

        var bukkitPlayer = this.player.getPlayer();

        // 周期性处理磨损工具，作为耐久事件之外的兜底检查。
        if (this.player.getTickCount() % 20 == 0) {
            var replenishManager = Main.getInjector().getInstance(FakeplayerReplenishManager.class);
            if (replenishManager.isReplaceTools(bukkitPlayer)) {
                replenishManager.replaceWornTool(bukkitPlayer);
            }
            if (replenishManager.isReplenish(bukkitPlayer)) {
                replenishManager.replenishTools(bukkitPlayer);
            }
        }

        // 真实的玩家是通过 ServerGamePacketListenerImpl#tick() 进行时刻运算的
        // 这个方法会修复第一次 tick 坐标错误的问题
        // 但是这个方法会导致强制修正坐标为客户端坐标, 然而假人的连接并不会发送任何坐标
        // 因此这里自行修复第一次 tick 的坐标, 并直接调用 ServerPlayer#doTick() 来进行时刻运算
        if (this.firstTick) {
            this.doFirstTick();
        } else {
            this.doTick();
            // 在假人所属区域线程上驱动其动作, 避免跨区域线程操作实体
            actionManager.tick(bukkitPlayer);
        }
    }

    private void cancel() {
        var task = this.task;
        if (task != null) {
            task.cancel();
        }
    }

    /**
     * 处理第一次 tick
     * <p>在这里在 {@link NMSServerPlayer#doTick()} 之后, 强行设置一次坐标解决被其他插件干预导致随机传送</p>
     * <p>似乎是 clearfog 或者 multiverse 插件导致的</p>
     */
    private void doFirstTick() {
        var handle = this.player.getHandle();
        var player = this.player.getPlayer();
        var x = handle.getX();
        var y = handle.getY();
        var z = handle.getZ();

        // 将本 tick 的移动取消
        handle.setXo(x);
        handle.setYo(y);
        handle.setZo(z);

        handle.doTick();

        // clearFog 插件会在第一次传送的时候改变了玩家的位置, 因此必须进行一次传送
        // Folia 下使用异步传送, 完成后再同步 NMS 坐标
        player.teleportAsync(new Location(player.getWorld(), x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch())).thenRun(() -> {
            handle.absMoveTo(x, y, z, player.getLocation().getYaw(), player.getLocation().getPitch());
            this.firstTick = false;
        });
    }

    private void doTick() {
        var handle = this.player.getHandle();
        handle.doTick();
    }

}
