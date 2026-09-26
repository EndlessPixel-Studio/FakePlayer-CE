package io.github.hello09x.fakeplayer.core.util;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

/**
 * Folia 兼容的调度工具。
 *
 * <p>Paper 1.20+ 在普通 Paper 上也提供了 Folia 的调度 API（全局/区域/实体/异步），
 * 非 Folia 时自动落到主线程，因此这里统一使用这套 API，无需 {@code isFolia()}
 * 分支，也避免传统 {@code Bukkit.getScheduler()} 在 Folia 上抛
 * {@code UnsupportedOperationException} 的问题。</p>
 *
 * <p>线程上下文选择：</p>
 * <ul>
 *   <li>{@link #global}：与具体实体/坐标无关的服务器级任务。</li>
 *   <li>{@link #entity}：实体绑定任务（会跟随实体传送）；操作某个玩家/假人必须走这里。</li>
 *   <li>{@link #at}：按坐标的区域任务。</li>
 *   <li>{@link #async}：异步任务（数据库/网络等），不得触碰实体或世界状态。</li>
 * </ul>
 */
public final class Schedulers {

    private Schedulers() {
    }

    // ------------------------------------------------------------------ global

    public static @NotNull ScheduledTask global(@NotNull Plugin plugin, @NotNull Runnable task) {
        return Bukkit.getGlobalRegionScheduler().run(plugin, $ -> task.run());
    }

    public static @NotNull ScheduledTask globalLater(@NotNull Plugin plugin, long delayTicks, @NotNull Runnable task) {
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, $ -> task.run(), Math.max(1, delayTicks));
    }

    public static @NotNull ScheduledTask globalTimer(@NotNull Plugin plugin, long initialDelayTicks, long periodTicks, @NotNull Runnable task) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, $ -> task.run(), Math.max(1, initialDelayTicks), Math.max(1, periodTicks));
    }

    // ------------------------------------------------------------------- entity

    /** @return 调度成功返回任务句柄；实体已被移除时返回 {@code null}（与 {@code EntityScheduler} 一致）。 */
    public static @Nullable ScheduledTask entity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task) {
        return entity.getScheduler().run(plugin, $ -> task.run(), null);
    }

    public static @Nullable ScheduledTask entityLater(@NotNull Plugin plugin, @NotNull Entity entity, long delayTicks, @NotNull Runnable task) {
        return entity.getScheduler().runDelayed(plugin, $ -> task.run(), null, Math.max(1, delayTicks));
    }

    public static @Nullable ScheduledTask entityTimer(@NotNull Plugin plugin, @NotNull Entity entity, long initialDelayTicks, long periodTicks, @NotNull Runnable task) {
        return entity.getScheduler().runAtFixedRate(plugin, $ -> task.run(), null, Math.max(1, initialDelayTicks), Math.max(1, periodTicks));
    }

    /**
     * 在 owner 所属线程执行任务: 若 owner 是实体(如 Player)则用其实体调度器, 否则退回全局区域。
     * <p>用于「向某个玩家/控制台回执」这类场景。</p>
     */
    public static @NotNull ScheduledTask runFor(@NotNull Plugin plugin, @Nullable Object owner, @NotNull Runnable task) {
        if (owner instanceof Entity entity) {
            var scheduled = entity(plugin, entity, task);
            if (scheduled != null) {
                return scheduled;
            }
        }
        return global(plugin, task);
    }

    // --------------------------------------------------------------------- region

    public static @NotNull ScheduledTask at(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable task) {
        return Bukkit.getRegionScheduler().run(plugin, location, $ -> task.run());
    }

    // ---------------------------------------------------------------------- async

    public static @NotNull ScheduledTask async(@NotNull Plugin plugin, @NotNull Runnable task) {
        return Bukkit.getAsyncScheduler().runNow(plugin, $ -> task.run());
    }

    public static @NotNull ScheduledTask asyncLater(@NotNull Plugin plugin, long delayTicks, @NotNull Runnable task) {
        return Bukkit.getAsyncScheduler().runDelayed(plugin, $ -> task.run(), Math.max(1, delayTicks) * 50L, TimeUnit.MILLISECONDS);
    }

    public static @NotNull ScheduledTask asyncTimer(@NotNull Plugin plugin, long initialDelayTicks, long periodTicks, @NotNull Runnable task) {
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, $ -> task.run(), Math.max(1, initialDelayTicks) * 50L, Math.max(1, periodTicks) * 50L, TimeUnit.MILLISECONDS);
    }

    // ------------------------------------------------------- blocking call helpers
    // 语义等价于 devtools 的 SchedulerUtils: 在目标线程执行并阻塞等待结果, 返回 CompletableFuture。
    // blocker 由闭包捕获调用线程, 由被调度的任务在 finally 中 unpark。

    public static @NotNull CompletableFuture<Void> callGlobal(@NotNull Plugin plugin, @NotNull Runnable task) {
        return callGlobal(plugin, (Callable<Void>) () -> {
            task.run();
            return null;
        });
    }

    public static <T> @NotNull CompletableFuture<T> callGlobal(@NotNull Plugin plugin, @NotNull Callable<T> task) {
        return callGlobal(plugin, task, ForkJoinPool.commonPool());
    }

    public static <T> @NotNull CompletableFuture<T> callGlobal(@NotNull Plugin plugin, @NotNull Callable<T> task, @NotNull Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            var blocker = Thread.currentThread();
            var exception = new AtomicReference<Throwable>();
            var value = new AtomicReference<T>();
            Bukkit.getGlobalRegionScheduler().run(plugin, $ -> {
                try {
                    value.set(task.call());
                } catch (Throwable e) {
                    exception.set(e);
                } finally {
                    LockSupport.unpark(blocker);
                }
            });
            LockSupport.park(blocker);
            if (exception.get() != null) {
                throw new CompletionException(exception.get());
            }
            return value.get();
        }, executor);
    }

    public static @NotNull CompletableFuture<Void> callEntity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task) {
        return callEntity(plugin, entity, (Callable<Void>) () -> {
            task.run();
            return null;
        });
    }

    public static <T> @NotNull CompletableFuture<T> callEntity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Callable<T> task) {
        return CompletableFuture.supplyAsync(() -> {
            var blocker = Thread.currentThread();
            var exception = new AtomicReference<Throwable>();
            var value = new AtomicReference<T>();
            var scheduled = entity.getScheduler().run(plugin, $ -> {
                try {
                    value.set(task.call());
                } catch (Throwable e) {
                    exception.set(e);
                } finally {
                    LockSupport.unpark(blocker);
                }
            }, null);
            if (scheduled != null) {
                LockSupport.park(blocker);
            }
            if (exception.get() != null) {
                throw new CompletionException(exception.get());
            }
            return value.get();
        });
    }

    public static @NotNull CompletableFuture<Void> callAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        return callAsync(plugin, () -> {
            task.run();
            return null;
        });
    }

    public static <T> @NotNull CompletableFuture<T> callAsync(@NotNull Plugin plugin, @NotNull Supplier<T> task) {
        return callAsync(plugin, task, ForkJoinPool.commonPool());
    }

    public static <T> @NotNull CompletableFuture<T> callAsync(@NotNull Plugin plugin, @NotNull Supplier<T> task, @NotNull Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            var blocker = Thread.currentThread();
            var exception = new AtomicReference<Throwable>();
            var value = new AtomicReference<T>();
            Bukkit.getAsyncScheduler().runNow(plugin, $ -> {
                try {
                    value.set(task.get());
                } catch (Throwable e) {
                    exception.set(e);
                } finally {
                    LockSupport.unpark(blocker);
                }
            });
            LockSupport.park(blocker);
            if (exception.get() != null) {
                throw new CompletionException(exception.get());
            }
            return value.get();
        }, executor);
    }
}
