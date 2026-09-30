package io.github.hello09x.fakeplayer.core.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.Nullable;

/**
 * 定期让假人的自定义延迟小幅波动, 让 Tab 列表里的延迟看起来更自然
 */
@Singleton
public class FakeplayerPingSetter {

    private final FakeplayerConfig config;

    private final FakeplayerList fakeplayers;

    private @Nullable ScheduledTask task;

    @Inject
    public FakeplayerPingSetter(FakeplayerConfig config, FakeplayerList fakeplayers) {
        this.config = config;
        this.fakeplayers = fakeplayers;
    }

    /**
     * (重新)启动延迟波动任务, 未开启 {@code custom-ping-dynamic} 时不执行任何任务
     * <p>波动只修改内存中的数值、不额外发包, 因此放在全局调度器上执行即可</p>
     */
    public void restart() {
        this.stop();

        if (!config.isCustomPingDynamic() || config.getCustomPing().isEmpty()) {
            return;
        }

        var interval = config.getCustomPingDynamicInterval();
        this.task = Schedulers.globalTimer(Main.getInstance(), interval, interval, this::run);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void run() {
        for (var fake : fakeplayers.getAll()) {
            fake.updateDynamicPing();
        }
    }

}
