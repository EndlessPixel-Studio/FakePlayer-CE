package io.github.hello09x.fakeplayer.core.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.compat.login.LoginCompatManager;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.repository.FakeplayerAuthRepository;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import static net.kyori.adventure.text.Component.text;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author tanyaofei
 * @since 2024/8/7
 **/
@Singleton
public class FakeplayerLifecycleListener implements Listener {

    private final FakeplayerManager manager;
    private final FakeplayerConfig config;
    private final FakeplayerAuthRepository authRepository;
    private final LoginCompatManager loginCompatManager;

    @Inject
    public FakeplayerLifecycleListener(FakeplayerManager manager, FakeplayerConfig config, FakeplayerAuthRepository authRepository, LoginCompatManager loginCompatManager) {
        this.manager = manager;
        this.config = config;
        this.authRepository = authRepository;
        this.loginCompatManager = loginCompatManager;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPostSpawn(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (this.manager.isNotFake(player)) {
            // Not a fake player
            return;
        }

        loginCompatManager.exempt(player);
        manager.dispatchCommands(player, config.getPostSpawnCommands());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAfterSpawn(@NotNull PlayerJoinEvent event) {
        var player = event.getPlayer();
        if (this.manager.isNotFake(player)) {
            // Not a fake player
            return;
        }

        Schedulers.entityLater(Main.getInstance(), player, 20, () -> {
            if (player.isOnline()) {
                manager.dispatchCommands(player, config.getAfterSpawnCommands());
                manager.issueCommands(player, config.getSelfCommands());
                this.autoLogin(player);
            }
        });
    }

    //K:假人UUID V:创建者名称
    private final Map<UUID, String> pendingFakeQuits = new ConcurrentHashMap<>();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPostQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        if (this.manager.isNotFake(player)) {
            // Not a fake player
            return;
        }
        pendingFakeQuits.put(player.getUniqueId(), this.manager.getCreatorName(player));
        manager.dispatchCommands(player, config.getPostQuitCommands());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onAfterQuit(@NotNull PlayerQuitEvent event) {
        var player = event.getPlayer();
        var uuid = player.getUniqueId();
        if (!pendingFakeQuits.containsKey(uuid)) return;
        Schedulers.globalLater(Main.getInstance(), 1, () -> {
            try {
                manager.dispatchCommands(new FakeplayerManager.DispatchCommandArgs(player.getName(),uuid.toString(),pendingFakeQuits.get(uuid)), config.getAfterQuitCommands());
            } finally {
                loginCompatManager.cleanup(player);
                pendingFakeQuits.remove(uuid);
            }
        });
    }

    /**
     * 假人生成后自动注册并登录
     * <p>仅当 {@code auto-login} 开启且数据库中存在该假人的密码时生效。
     * 命令模板中的 {@code %password%} 会被替换为数据库中的密码, 其余 {@code %p %u %c} 由派发逻辑替换。</p>
     */
    private void autoLogin(@NotNull Player player) {
        if (!config.isAutoLogin()) {
            return;
        }

        var password = authRepository.selectByName(player.getName());
        if (password == null || password.isBlank()) {
            var charset = config.getRandomPasswordCharset();
            if (charset == null || charset.isBlank()) {
                return;
            }
            password = generateRandomPassword(charset, config.getRandomPasswordLength());
            authRepository.saveOrUpdate(player.getName(), password);
            var creator = this.manager.getCreator(player);
            if (creator != null) {
                creator.sendMessage(text("已为假人 " + player.getName() + " 随机生成登录密码: " + password));
            }
        }

        var commands = new ArrayList<String>();
        var register = config.getRegisterCommand();
        if (register != null && !register.isBlank()) {
            commands.add(register.replace("%password%", password));
        }
        var login = config.getLoginCommand();
        if (login != null && !login.isBlank()) {
            commands.add(login.replace("%password%", password));
        }

        if (!commands.isEmpty()) {
            manager.issueCommands(player, commands);
        }
    }

    /**
     * 从指定字符集中生成随机密码
     *
     * @param charset 候选字符集
     * @param length  密码长度
     * @return 随机密码
     */
    private @NotNull String generateRandomPassword(@NotNull String charset, int length) {
        var rnd = ThreadLocalRandom.current();
        var sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(charset.charAt(rnd.nextInt(charset.length())));
        }
        return sb.toString();
    }
}
