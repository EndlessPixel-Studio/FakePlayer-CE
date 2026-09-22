package io.github.hello09x.fakeplayer.core.manager.invsee;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

/**
 * @author tanyaofei
 * @since 2024/8/12
 **/
public interface InvseeManager extends Listener {

    /**
     * 打开假人的背包
     */
    boolean invsee(@NotNull Player viewer, @NotNull Player whom);

    /**
     * 打开假人的末影箱
     */
    boolean enderchest(@NotNull Player viewer, @NotNull Player whom);

}
