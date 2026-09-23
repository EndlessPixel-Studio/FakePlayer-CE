package io.github.hello09x.fakeplayer.core.compat.login;

import io.github.hello09x.fakeplayer.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * AuthMe (AuthMeReloaded) 兼容实现。
 * <p>AuthMe 通过 {@code fr.xephi.authme.api.v3.AuthMeApi} 提供放行接口：未登录玩家会被冻结移动/交互、拦截命令并超时踢出。
 * 对假人调用 {@code forceLogin}（已注册）或 {@code forceRegister(..., autoLogin=true)}（未注册）即可将其标记为已登录，
 * 无需手动注册 / 登录命令。</p>
 * <p>通过目标插件自身的类加载器加载其类，避免编译期依赖（softdepend 方式接入）。</p>
 */
public class AuthMeCompat implements LoginCompat {

    private static final String PLUGIN_NAME = "AuthMe";
    private static final String API_CLASS = "fr.xephi.authme.api.v3.AuthMeApi";

    @Override
    public @NotNull String pluginName() {
        return PLUGIN_NAME;
    }

    @Override
    public void exempt(@NotNull Player fakePlayer) {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
            if (plugin == null || !plugin.isEnabled()) {
                return;
            }
            // 通过目标插件自身的类加载器加载其类（无编译期依赖）
            ClassLoader classLoader = plugin.getClass().getClassLoader();
            Class<?> apiClass = classLoader.loadClass(API_CLASS);
            Object api = apiClass.getMethod("getInstance").invoke(null);
            if (api == null) {
                // AuthMe 尚未完全初始化
                return;
            }

            String name = fakePlayer.getName();
            boolean registered = (boolean) apiClass.getMethod("isRegistered", String.class).invoke(api, name);
            if (registered) {
                apiClass.getMethod("forceLogin", Player.class).invoke(api, fakePlayer);
            } else {
                // 未注册：用随机密码注册并直接登录（autoLogin=true）
                String password = "fp-" + UUID.randomUUID() + "-fp";
                try {
                    apiClass.getMethod("forceRegister", Player.class, String.class, boolean.class)
                           .invoke(api, fakePlayer, password, true);
                } catch (NoSuchMethodException ignored) {
                    // 旧版本无 autoLogin 重载：注册后再 forceLogin
                    apiClass.getMethod("forceRegister", Player.class, String.class)
                           .invoke(api, fakePlayer, password);
                    apiClass.getMethod("forceLogin", Player.class).invoke(api, fakePlayer);
                }
            }
        } catch (Throwable t) {
            Main.getInstance().getLogger().warning(
                    "无法将假人 " + fakePlayer.getName() + " 标记为已登录 (AuthMe): " + t.getMessage());
        }
    }

}
