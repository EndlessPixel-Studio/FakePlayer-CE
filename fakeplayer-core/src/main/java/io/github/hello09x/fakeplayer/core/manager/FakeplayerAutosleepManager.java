package io.github.hello09x.fakeplayer.core.manager;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.utils.BlockUtils;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.constant.MetadataKeys;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import org.bukkit.World;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

/** 夜晚时让开启了自动睡觉的假人自己找床上床 */
@Singleton
public class FakeplayerAutosleepManager {

  /** 主世界夜晚的时间区间 (tick) */
  private static final long NIGHT_START = 12542L;

  private static final long NIGHT_END = 23458L;
  private static final long CHECK_INTERVAL_TICKS = 100L;
  private static final int BED_RANGE = 4;

  private final FakeplayerManager manager;

  @Inject
  public FakeplayerAutosleepManager(FakeplayerManager manager) {
    this.manager = manager;
  }

  /** 注册周期检查任务; 是否记录由每个假人的标记决定, 因此任务始终运行 */
  public void start() {
    Schedulers.globalTimer(
        Main.getInstance(), CHECK_INTERVAL_TICKS, CHECK_INTERVAL_TICKS, this::tick);
  }

  public boolean isAutosleep(@NotNull Player fake) {
    return fake.hasMetadata(MetadataKeys.AUTOSLEEP);
  }

  public void setAutosleep(@NotNull Player target, boolean autosleep) {
    if (autosleep) {
      target.setMetadata(MetadataKeys.AUTOSLEEP, new FixedMetadataValue(Main.getInstance(), true));
    } else {
      target.removeMetadata(MetadataKeys.AUTOSLEEP, Main.getInstance());
    }
  }

  /** 遍历走全局区域线程, 判断与上床都回到假人自己的区域线程 (Folia 兼容) */
  private void tick() {
    for (var fake : this.manager.getAll()) {
      if (!fake.isOnline()) {
        continue;
      }
      Schedulers.entity(
          Main.getInstance(),
          fake,
          () -> {
            if (this.isAutosleep(fake) && !fake.isSleeping()) {
              this.sleepIfNight(fake);
            }
          });
    }
  }

  private void sleepIfNight(@NotNull Player fake) {
    var world = fake.getWorld();
    if (world.getEnvironment() != World.Environment.NORMAL) {
      return;
    }
    var time = world.getTime();
    if (time < NIGHT_START || time > NIGHT_END) {
      return;
    }

    var bed =
        BlockUtils.getNearbyBlock(
            fake.getLocation(),
            BED_RANGE,
            block -> {
              if (!(block.getBlockData() instanceof Bed data)) {
                return false;
              }
              return !data.isOccupied() && data.getPart() == Bed.Part.HEAD;
            });
    if (bed == null) {
      return;
    }

    fake.sleep(bed.getLocation(), false);
  }
}
