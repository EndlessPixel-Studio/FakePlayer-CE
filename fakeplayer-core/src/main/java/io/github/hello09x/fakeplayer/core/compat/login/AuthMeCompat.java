package io.github.hello09x.fakeplayer.core.compat.login;

import io.github.hello09x.fakeplayer.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.security.SecureRandom;
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
                // 未注册：用符合 AuthMe 密码策略的随机密码注册并直接登录（autoLogin=true）。
                // 注意 AuthMe 默认 passwordMaxLength=30，过长的密码会被注册流水线静默拒绝，
                // 因此这里生成长度受限的纯字母数字密码。
                String password = randomPassword();
                try {
                    apiClass.getMethod("forceRegister", Player.class, String.class, boolean.class)
                           .invoke(api, fakePlayer, password, true);
                } catch (NoSuchMethodException ignored) {
                    // 旧版本无 autoLogin 重载：注册后再 forceLogin
                    apiClass.getMethod("forceRegister", Player.class, String.class)
                           .invoke(api, fakePlayer, password);
                    apiClass.getMethod("forceLogin", Player.class).invoke(api, fakePlayer);
                }
                // 兜底：forceRegister(autoLogin) 在某些版本不会自动登录，未认证时再补一次 forceLogin
                if (!(boolean) apiClass.getMethod("isAuthenticated", Player.class).invoke(api, fakePlayer)) {
                    apiClass.getMethod("forceLogin", Player.class).invoke(api, fakePlayer);
                }
            }
        } catch (Throwable t) {
            Main.getInstance().getLogger().warning(
                    "无法将假人 " + fakePlayer.getName() + " 标记为已登录 (AuthMe): " + t.getMessage());
        }
    }

    /**
     * 生成符合 AuthMe 默认密码策略的随机密码。
     * <p>AuthMe 默认 {@code passwordMaxLength=30}、{@code allowedPasswordCharacters=[!-~]*}，
     * 此处使用 16 位纯字母数字串，既满足最小长度也远小于最大长度，避免被注册流水线静默拒绝。</p>
     */
    private static String randomPassword() {
        final String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var rnd = new SecureRandom();
        var sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
