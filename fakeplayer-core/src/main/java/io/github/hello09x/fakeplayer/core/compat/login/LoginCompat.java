package io.github.hello09x.fakeplayer.core.compat.login;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;

/**
 * 登录插件兼容抽象。
 * <p>主流登录插件（AuthMe / CatSeedLogin 等）在玩家未登录时会冻结移动/交互、拦截命令并在超时后踢出。
 * 实现本接口可在假人生成时将其标记为已登录或豁免，使假人正常工作。</p>
 */
public interface LoginCompat {

    /**
     * @return 目标登录插件的 softdepend 名称（用于动态探测是否启用）
     */
    @NotNull
    String pluginName();

    /**
     * 在触发登录插件的预登录事件前准备假人账号。默认无需处理。
     *
     * @param fakePlayer 尚未加入服务器的假人
     * @param address 假人的内部连接地址
     * @return 是否可以继续生成
     */
    default boolean prepare(@NotNull Player fakePlayer, @NotNull InetAddress address) {
        return true;
    }

    /**
     * 登录插件处理完预登录事件后同步其玩家缓存。默认无需处理。
     *
     * @param fakePlayer 尚未加入服务器的假人
     * @return 是否可以继续生成
     */
    default boolean afterPreLogin(@NotNull Player fakePlayer) {
        return true;
    }

    /**
     * 清理该兼容层为假人临时创建的登录资料。默认无需处理。
     */
    default void cleanup(@NotNull Player fakePlayer) {
    }

    /**
     * 插件关闭时清理所有临时登录资料。默认无需处理。
     */
    default void cleanupAll() {
    }

    /**
     * 将假人标记为已登录 / 豁免该登录插件的拦截。
     *
     * @param fakePlayer 刚生成、已加入服务器的假人
     */
    void exempt(@NotNull Player fakePlayer);

    /**
     * 插件启用时回调，用于打印兼容性相关的启动警告。默认空实现。
     */
    default void onEnable() {
    }

}
