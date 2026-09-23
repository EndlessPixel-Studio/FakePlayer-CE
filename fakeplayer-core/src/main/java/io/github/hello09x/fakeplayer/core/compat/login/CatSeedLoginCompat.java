package io.github.hello09x.fakeplayer.core.compat.login;

import io.github.hello09x.fakeplayer.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * CatSeedLogin 兼容实现。
 * <p>CatSeedLogin 通过 {@code LoginPlayerHelper.isLogin(name)} 判定玩家是否已登录：未登录则冻结移动/交互、
 * 拦截命令并超时踢出，其自动踢出任务同样认 {@code isLogin}。内部 {@code LoginPlayerHelper.add(LoginPlayer)}
 * 为 public static，可直接把假人标记为已登录，无需密码、数据库或命令白名单。</p>
 * <p>通过目标插件自身的类加载器加载其类，避免编译期依赖（softdepend 方式接入）。</p>
 */
public class CatSeedLoginCompat implements LoginCompat {

    private static final String PLUGIN_NAME = "CatSeedLogin";
    private static final String HELPER_CLASS = "cc.baka9.catseedlogin.bukkit.object.LoginPlayerHelper";
    private static final String MODEL_CLASS = "cc.baka9.catseedlogin.common.model.LoginPlayer";

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
            Class<?> loginPlayerClass = classLoader.loadClass(MODEL_CLASS);
            Class<?> helperClass = classLoader.loadClass(HELPER_CLASS);
            Object loginPlayer = loginPlayerClass.getConstructor(String.class, String.class)
                    .newInstance(fakePlayer.getName(), "");
            helperClass.getMethod("add", loginPlayerClass).invoke(null, loginPlayer);
        } catch (Throwable t) {
            Main.getInstance().getLogger().warning(
                    "无法将假人 " + fakePlayer.getName() + " 标记为已登录 (CatSeedLogin): " + t.getMessage());
        }
    }

    @Override
    public void onEnable() {
        try {
            Plugin plugin = Bukkit.getPluginManager().getPlugin(PLUGIN_NAME);
            if (plugin == null || !plugin.isEnabled()) {
                return;
            }
            ClassLoader classLoader = plugin.getClass().getClassLoader();
            Class<?> settingsClass = classLoader.loadClass("cc.baka9.catseedlogin.bukkit.Config$Settings");
            Field field = settingsClass.getField("CanTpSpawnLocation");
            if (field.getBoolean(null)) {
                Main.getInstance().getLogger().warning(
                        "检测到 CatSeedLogin 已启用且 CanTpSpawnLocation=true：假人加入时会被传送到 CatSeedLogin 的出生点，" +
                        "可能与 /fp spawn 指定的生成位置冲突。如不需要该行为，请在 CatSeedLogin/config.yml 中关闭 CanTpSpawnLocation。");
            }
        } catch (Throwable ignored) {
            // 读取失败仅跳过警告，不影响兼容本身
        }
    }

}
