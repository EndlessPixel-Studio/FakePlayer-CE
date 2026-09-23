package io.github.hello09x.fakeplayer.core.compat.login;

import com.google.inject.Singleton;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 登录插件兼容管理器。
 * <p>探测当前已启用的登录插件，并逐个对刚生成的假人执行 {@link LoginCompat#exempt(Player)}。</p>
 */
@Singleton
public class LoginCompatManager {

    private final List<LoginCompat> compats = List.of(
            new CatSeedLoginCompat()
    );

    /**
     * 对假人执行所有可用登录插件的豁免。
     *
     * @param fakePlayer 刚生成、已加入服务器的假人
     */
    public void exempt(@NotNull Player fakePlayer) {
        for (LoginCompat compat : compats) {
            if (isEnabled(compat.pluginName())) {
                compat.exempt(fakePlayer);
            }
        }
    }

    private boolean isEnabled(@NotNull String pluginName) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }

    /**
     * 插件启用时回调：对当前已启用的登录插件执行启动期检查/警告。
     */
    public void onEnable() {
        for (LoginCompat compat : compats) {
            if (isEnabled(compat.pluginName())) {
                compat.onEnable();
            }
        }
    }

}
