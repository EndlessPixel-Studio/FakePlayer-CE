package io.github.hello09x.fakeplayer.core.entity.action.impl;

import io.github.hello09x.fakeplayer.api.spi.Action;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.util.WalkUtils;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.translatable;

/**
 * 走向目标坐标。到位、无法继续或执行停止时结束。
 */
public class GotoAction implements Action {

    /**
     * 临时提高跨步高度, 使假人能平滑走上一格高的台阶与坑洞边缘
     */
    private static final float STEP_HEIGHT = 1.25F;
    private static final float DEFAULT_STEP_HEIGHT = 0.6F;
    private static final int STUCK_TICKS = 25;
    private static final double ARRIVE_HORIZONTAL = 0.3;
    private static final double ARRIVE_VERTICAL = 0.6;
    private static final double SPEED = 0.22;
    private static final double WATER_SPEED = 0.16;
    private static final double JUMP_VELOCITY = 0.42;

    private final @NotNull NMSServerPlayer nms;
    private final @NotNull Player player;
    private final @NotNull ActionSetting setting;

    private int stuckTick;
    private @Nullable Location lastLocation;

    public GotoAction(@NotNull NMSServerPlayer nms, @NotNull Player player, @NotNull ActionSetting setting) {
        this.nms = nms;
        this.player = player;
        this.setting = setting;
        this.lastLocation = null;
        var attribute = player.getAttribute(Attribute.GENERIC_STEP_HEIGHT);
        if (attribute != null && attribute.getBaseValue() < STEP_HEIGHT) {
            attribute.setBaseValue(STEP_HEIGHT);
        }
    }

    @Override
    public boolean tick() {
        var targetX = setting.targetX;
        var targetY = setting.targetY;
        var targetZ = setting.targetZ;
        if (targetX == null || targetY == null || targetZ == null) {
            // 未设置目标坐标 (如通过 /action 接口直接派发), 静默结束
            setting.remains = 0;
            reset();
            return true;
        }

        var location = player.getLocation();
        var dx = targetX - location.getX();
        var dy = targetY - location.getY();
        var dz = targetZ - location.getZ();
        var horizontal = Math.sqrt(dx * dx + dz * dz);

        if (horizontal < ARRIVE_HORIZONTAL && Math.abs(dy) < ARRIVE_VERTICAL) {
            finish();
            return true;
        }

        // 位置几乎不变超过 1.25s 视为卡住, 传送兜底
        if (lastLocation != null && lastLocation.getWorld() == location.getWorld() && lastLocation.distanceSquared(location) < 0.0004) {
            stuckTick++;
        } else {
            stuckTick = 0;
        }
        lastLocation = location.clone();
        if (stuckTick >= STUCK_TICKS) {
            stuckTick = 0;
            var stand = WalkUtils.findStandLocation(player, location.getWorld().getBlockAt(targetX.intValue(), targetY.intValue(), targetZ.intValue()));
            if (stand != null) {
                player.teleportAsync(stand);
            }
            return false;
        }

        lookAt(location, dx, dy, dz);

        var inWater = player.isInWater() || location.getBlock().isLiquid();
        var speed = inWater ? WATER_SPEED : SPEED;
        var vx = horizontal > 0.001 ? dx / horizontal * speed : 0;
        var vz = horizontal > 0.001 ? dz / horizontal * speed : 0;

        if (inWater) {
            nms.setJumping(false);
            var vy = dy > 0.5 ? 0.08 : dy < -0.5 ? -0.08 : Math.min(player.getVelocity().getY(), 0);
            move(vx, vy, vz);
            return false;
        }

        var stepX = horizontal > 0.001 ? dx / horizontal : 0;
        var stepZ = horizontal > 0.001 ? dz / horizontal : 0;
        var front = location.clone().add(stepX * 0.7, 0, stepZ * 0.7);
        var frontBlock = front.getBlock();
        var frontAbove = front.clone().add(0, 1, 0).getBlock();
        var obstacle = frontBlock.getType().isSolid() && frontAbove.getType().isAir();

        if (obstacle && nms.onGround()) {
            nms.jumpFromGround();
            nms.setJumping(true);
            move(vx, JUMP_VELOCITY, vz);
        } else {
            nms.setJumping(false);
            move(vx, player.getVelocity().getY(), vz);
        }
        return false;
    }

    private void lookAt(Location location, double dx, double dy, double dz) {
        if (dx * dx + dz * dz <= 0.0001) {
            return;
        }
        var yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        var pitch = (float) Math.toDegrees(Math.atan2(-dy, Math.sqrt(dx * dx + dz * dz)));
        player.setRotation(yaw, pitch);
    }

    private void move(double vx, double vy, double vz) {
        var vector = new Vector(vx, vy, vz);
        nms.setDeltaMovement(vector);
        player.setVelocity(vector);
    }

    private void finish() {
        // 由 BaseActionTicker 在下一 tick 走 inactiveTick 并移除
        setting.remains = 0;
        reset();
        notifyCreator("fakeplayer.command.goto.arrived");
    }

    private void notifyCreator(String key) {
        var manager = Main.getInjector().getInstance(FakeplayerManager.class);
        var creator = manager.getCreator(player);
        if (creator != null) {
            creator.sendMessage(translatable(key));
        }
    }

    private void reset() {
        nms.setJumping(false);
        nms.setDeltaMovement(new Vector(0, 0, 0));
        player.setVelocity(new Vector(0, 0, 0));
        // 不能依赖 previousStepHeight: /fp stop 会更换 ticker, 新实例读到的是已被抬高的值
        var attribute = player.getAttribute(Attribute.GENERIC_STEP_HEIGHT);
        if (attribute != null && attribute.getBaseValue() > DEFAULT_STEP_HEIGHT) {
            attribute.setBaseValue(DEFAULT_STEP_HEIGHT);
        }
    }

    @Override
    public void inactiveTick() {
        reset();
    }

    @Override
    public void stop() {
        reset();
    }
}
