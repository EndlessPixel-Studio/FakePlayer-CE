package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandExecutor;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.Range;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Singleton
public class MoveCommand extends AbstractCommand {

    private final Map<UUID, ScheduledTask> stopTasks = new HashMap<>();

    /**
     * 假人移动
     */
    public CommandExecutor move(@Range(from = 0, to = 1) float forward, @Range(from = 0, to = 1) float strafing) {
        return (sender, args) -> {
            var fake = getFakeplayer(sender, args);
            var handle = bridge.fromPlayer(fake);
            float vel = fake.isSneaking() ? 0.3F : 1.0F;
            if (forward != 0.0F) {
                handle.setZza(vel * forward);
            }
            if (strafing != 0.0F) {
                handle.setXxa(vel * strafing);
            }

            var task = stopTasks.remove(fake.getUniqueId());
            if (task != null && !task.isCancelled()) {
                task.cancel();
            }

            var fakeId = fake.getUniqueId();
            var ref = new AtomicReference<ScheduledTask>();
            var stopping = Schedulers.entityLater(Main.getInstance(), fake.getPlayer(), fake.isSprinting() ? 40 : 20, () -> {
                handle.setXxa(0);
                handle.setZza(0);
                // 仅移除本次调度对应的任务, 避免误删后续重新发起的停止任务
                stopTasks.remove(fakeId, ref.get());
            });
            if (stopping != null) {
                ref.set(stopping);
                this.stopTasks.put(fakeId, stopping);
            }
        };
    }


}
