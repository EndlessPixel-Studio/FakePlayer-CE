package io.github.hello09x.fakeplayer.core.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * 假人行走相关的落点计算
 */
public final class WalkUtils {

    /**
     * 目标周围 2 格内的候选偏移, 按近到远排序
     */
    private static final int[][] OFFSETS = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {-1, -1}, {1, -1}, {-1, 1},
            {2, 0}, {-2, 0}, {0, 2}, {0, -2},
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {-1, 2}, {1, -2}, {-1, -2}
    };

    private WalkUtils() {
    }

    /**
     * 在目标方块周围寻找可站立的落点 (地面有支撑, 且脚与头部空间可通过)
     *
     * @param player 假人, 用于优先选择与当前落脚高度相近的位置
     * @param target 目标方块
     * @return 落点, 目标周围不存在可站立位置时返回 null
     */
    public static @Nullable Location findStandLocation(@Nullable Player player, @Nullable Block target) {
        if (target == null || player == null) {
            return null;
        }
        var world = target.getWorld();
        var targetCenter = target.getLocation().add(0.5, 0.5, 0.5);
        var playerLoc = player.getLocation();

        var best = (Location) null;
        var bestScore = Double.MAX_VALUE;
        for (var offset : OFFSETS) {
            var sx = target.getX() + offset[0];
            var sz = target.getZ() + offset[1];
            var startY = Math.min(target.getY() + 3, world.getMaxHeight() - 2);
            var minY = Math.max(target.getY() - 4, world.getMinHeight());
            for (var sy = startY; sy >= minY; sy--) {
                var ground = world.getBlockAt(sx, sy, sz);
                var feet = world.getBlockAt(sx, sy + 1, sz);
                var head = world.getBlockAt(sx, sy + 2, sz);
                var groundOk = ground.getType().isSolid() || ground.isLiquid();
                var spaceOk = passable(feet) && passable(head);
                if (groundOk && spaceOk) {
                    var candidate = new Location(world, sx + 0.5, sy + 1, sz + 0.5);
                    // 只接受目标附近 (水平距离平方 <= 14) 的落点, 同高度优先
                    var score = candidate.distanceSquared(targetCenter);
                    if (score > 14) {
                        break;
                    }
                    if (Math.abs(candidate.getY() - playerLoc.getY()) > 1.5) {
                        score += 100;
                    }
                    if (score < bestScore) {
                        bestScore = score;
                        best = candidate;
                    }
                    break;
                }
            }
        }
        return best;
    }

    /**
     * 方块是否可以穿过 (空气或液体)
     */
    public static boolean passable(Block block) {
        return block.isEmpty() || block.isLiquid();
    }

    /**
     * 该世界坐标是否在可活动的高度范围内
     */
    public static boolean withinHeight(World world, double y) {
        return y >= world.getMinHeight() && y <= world.getMaxHeight() - 2;
    }
}
