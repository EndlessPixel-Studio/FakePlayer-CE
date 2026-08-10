package io.github.hello09x.fakeplayer.core.util;

import io.github.hello09x.fakeplayer.api.spi.NMSServerPlayer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

/**
 * 朝向工具。
 *
 * <p>原本使用 {@code io.papermc.paper.entity.LookAnchor}（{@code Player#lookAt(Location, LookAnchor, LookAnchor)}）。
 * 部分混合服务端（如 Mohist）的 Paper 兼容层会将该 API 重映射到其内部类
 * {@code com.mohistmc.paper.math.Position}，而该实现在某些构建中缺失，
 * 导致插件在加载任何引用 {@code io.papermc.paper} 包的类时抛出 {@code NoClassDefFoundError}
 * （见 <a href="https://github.com/tanyaofei/minecraft-fakeplayer/issues/200">issue #200</a>）。
 *
 * <p>为避免对 {@code io.papermc.paper.*} 的编译期/链接期依赖，这里改用纯 NMS 角度计算，
 * 直接通过 {@link NMSServerPlayer#setYRot(float)} / {@link NMSServerPlayer#setXRot(float)} 设置朝向。
 */
public final class LookUtils {

    private LookUtils() {
    }

    /**
     * 让玩家看向指定坐标（使用眼睛锚点）。
     *
     * @param player 假人对应的 NMS 玩家
     * @param target 目标坐标
     */
    public static void lookAt(@NotNull NMSServerPlayer player, @NotNull Location target) {
        var bukkit = player.getPlayer();
        var eye = bukkit.getEyeLocation();

        var dx = target.getX() - eye.getX();
        var dy = target.getY() - eye.getY();
        var dz = target.getZ() - eye.getZ();

        var yaw = (float) Math.toDegrees(Math.atan2(dx, -dz));
        var pitch = (float) Math.toDegrees(-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));

        player.setYRot(yaw % 360);
        player.setXRot(Mth.clamp(pitch, -90, 90));
    }

}
