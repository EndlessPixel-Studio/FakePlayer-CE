package io.github.hello09x.fakeplayer.core.compat.skin;

import io.github.hello09x.fakeplayer.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SkinsRestorer 兼容（反射接入，不依赖其编译期 API）。
 * <p>按官方推荐流程：{@code SkinStorage.findOrCreateSkinData(name)} 解析 →
 * {@code PlayerStorage.setSkinIdOfPlayer(uuid, identifier)} 持久化 →
 * 平台 applier 的 {@code applySkin} 刷新外观。</p>
 * <p>查询与存储涉及网络/数据库 I/O，调用方必须在异步线程执行 {@link #resolve} 与
 * {@link #save}；{@link #apply} 需在实体线程执行（applier 自身会再调度档案刷新）。</p>
 */
public final class SkinsRestorerCompat {

    private static final Logger log = Main.getInstance().getLogger();

    private static volatile boolean initialized;
    private static volatile @Nullable SkinsRestorerCompat instance;

    private final Object api;
    private final Method getSkinStorage;
    private final Method getPlayerStorage;
    private final Method findOrCreateSkinData;
    private final Method setSkinIdOfPlayer;
    private final Method getSkinApplier;
    private final Method applySkin;
    private final Method getIdentifier;
    private final Method getProperty;

    private SkinsRestorerCompat(
            @NotNull Object api,
            @NotNull Method getSkinStorage,
            @NotNull Method getPlayerStorage,
            @NotNull Method findOrCreateSkinData,
            @NotNull Method setSkinIdOfPlayer,
            @NotNull Method getSkinApplier,
            @NotNull Method applySkin,
            @NotNull Method getIdentifier,
            @NotNull Method getProperty
    ) {
        this.api = api;
        this.getSkinStorage = getSkinStorage;
        this.getPlayerStorage = getPlayerStorage;
        this.findOrCreateSkinData = findOrCreateSkinData;
        this.setSkinIdOfPlayer = setSkinIdOfPlayer;
        this.getSkinApplier = getSkinApplier;
        this.applySkin = applySkin;
        this.getIdentifier = getIdentifier;
        this.getProperty = getProperty;
    }

    /**
     * @return 可用实例；SkinsRestorer 未安装/未启用/API 不兼容时返回 null
     */
    public static @Nullable SkinsRestorerCompat get() {
        if (!initialized) {
            synchronized (SkinsRestorerCompat.class) {
                if (!initialized) {
                    instance = create();
                    initialized = true;
                }
            }
        }
        return instance;
    }

    private static @Nullable SkinsRestorerCompat create() {
        var plugin = Bukkit.getPluginManager().getPlugin("SkinsRestorer");
        if (plugin == null || !plugin.isEnabled()) {
            return null;
        }
        try {
            var loader = plugin.getClass().getClassLoader();
            var provider = Class.forName("net.skinsrestorer.api.SkinsRestorerProvider", true, loader);
            var api = provider.getMethod("get").invoke(null);
            var root = Class.forName("net.skinsrestorer.api.SkinsRestorer", true, loader);
            var storageClass = Class.forName("net.skinsrestorer.api.storage.SkinStorage", true, loader);
            var playerStorageClass = Class.forName("net.skinsrestorer.api.storage.PlayerStorage", true, loader);
            var identifierClass = Class.forName("net.skinsrestorer.api.property.SkinIdentifier", true, loader);
            var propertyClass = Class.forName("net.skinsrestorer.api.property.SkinProperty", true, loader);
            var applierClass = Class.forName("net.skinsrestorer.api.skinapplier.SkinApplier", true, loader);
            // 解析结果类型: 优先新版的 InputDataResult, 兼容旧版 SkinData
            var resultClass = loadFirst(loader,
                    "net.skinsrestorer.api.property.InputDataResult",
                    "net.skinsrestorer.api.property.SkinData");

            return new SkinsRestorerCompat(
                    api,
                    root.getMethod("getSkinStorage"),
                    root.getMethod("getPlayerStorage"),
                    storageClass.getMethod("findOrCreateSkinData", String.class),
                    playerStorageClass.getMethod("setSkinIdOfPlayer", UUID.class, identifierClass),
                    root.getMethod("getSkinApplier", Class.class),
                    applierClass.getMethod("applySkin", Object.class, propertyClass),
                    resultClass.getMethod("getIdentifier"),
                    resultClass.getMethod("getProperty")
            );
        } catch (Throwable e) {
            log.log(Level.WARNING, "SkinsRestorer 兼容初始化失败, skin 命令将回退到 Mojang API", e);
            return null;
        }
    }

    private static Class<?> loadFirst(@NotNull ClassLoader loader, @NotNull String... names) throws ClassNotFoundException {
        ClassNotFoundException last = null;
        for (var name : names) {
            try {
                return Class.forName(name, true, loader);
            } catch (ClassNotFoundException e) {
                last = e;
            }
        }
        throw last == null ? new ClassNotFoundException() : last;
    }

    /**
     * 解析皮肤（网络请求，须在异步线程调用）。
     *
     * @param input 正版玩家名 / 自定义皮肤名 / URL
     * @return 解析结果；名称无效、查询失败或状态未知时返回 null
     */
    public @Nullable SkinResult resolve(@NotNull String input) {
        try {
            var storage = getSkinStorage.invoke(api);
            var result = (Optional<?>) findOrCreateSkinData.invoke(storage, input);
            if (result == null || result.isEmpty()) {
                return null;
            }
            var data = result.get();
            var identifier = getIdentifier.invoke(data);
            var property = getProperty.invoke(data);
            if (identifier == null || property == null) {
                return null;
            }
            return new SkinResult(identifier, property);
        } catch (Throwable e) {
            log.log(Level.WARNING, "SkinsRestorer 解析皮肤失败: " + input, unwrap(e));
            return null;
        }
    }

    /**
     * 持久化玩家选择的皮肤（数据库 I/O，须在异步线程调用）。
     *
     * @return 是否成功
     */
    public boolean save(@NotNull UUID playerId, @NotNull Object identifier) {
        try {
            var storage = getPlayerStorage.invoke(api);
            setSkinIdOfPlayer.invoke(storage, playerId, identifier);
            return true;
        } catch (Throwable e) {
            log.log(Level.WARNING, "SkinsRestorer 保存皮肤选择失败: " + playerId, unwrap(e));
            return false;
        }
    }

    /**
     * 应用皮肤到在线玩家（须在实体线程调用；applier 会自行调度档案刷新）。
     */
    public void apply(@NotNull Player player, @NotNull Object property) {
        try {
            var applier = getSkinApplier.invoke(api, Player.class);
            if (applier == null) {
                return;
            }
            applySkin.invoke(applier, player, property);
        } catch (Throwable e) {
            log.log(Level.WARNING, "SkinsRestorer 应用皮肤失败: " + player.getName(), unwrap(e));
        }
    }

    private static Throwable unwrap(@NotNull Throwable e) {
        return e instanceof java.lang.reflect.InvocationTargetException ite && ite.getCause() != null
                ? ite.getCause()
                : e;
    }

    /**
     * 解析结果：标识符用于持久化，属性用于即时刷新技术
     */
    public record SkinResult(@NotNull Object identifier, @NotNull Object property) {
    }
}
