package io.github.hello09x.fakeplayer.core.listener;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.compat.login.LoginCompatManager;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.repository.FakeplayerAuthRepository;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author tanyaofei
 * @since 2024/8/7
 */
@Singleton
public class FakeplayerLifecycleListener implements Listener {

  private final FakeplayerManager manager;
  private final FakeplayerConfig config;
  private final FakeplayerAuthRepository authRepository;
  private final LoginCompatManager loginCompatManager;

  // K:假人UUID V:创建者名称; Optional.empty 表示创建者已被先行清理, 但仍需 cleanup
  private final Map<UUID, Optional<String>> pendingFakeQuits = new ConcurrentHashMap<>();

  @Inject
  public FakeplayerLifecycleListener(
      FakeplayerManager manager,
      FakeplayerConfig config,
      FakeplayerAuthRepository authRepository,
      LoginCompatManager loginCompatManager) {
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

    Schedulers.entityLater(
        Main.getInstance(),
        player,
        20,
        () -> {
          if (player.isOnline()) {
            manager.dispatchCommands(player, config.getAfterSpawnCommands());
            manager.issueCommands(player, config.getSelfCommands());
            this.autoLogin(player);
          }
        });
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
  public void onPostQuit(@NotNull PlayerQuitEvent event) {
    var player = event.getPlayer();
    if (this.manager.isNotFake(player)) {
      // Not a fake player
      return;
    }
    // 无论创建者是否存在都要登记: 保证 onAfterQuit 一定会执行 cleanup,
    // 否则 loginCompatManager.exempt() 的豁免标记会残留
    var creatorName = this.manager.getCreatorName(player);
    pendingFakeQuits.put(player.getUniqueId(), Optional.ofNullable(creatorName));
    manager.dispatchCommands(player, config.getPostQuitCommands());
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
  public void onAfterQuit(@NotNull PlayerQuitEvent event) {
    var player = event.getPlayer();
    var uuid = player.getUniqueId();
    if (!pendingFakeQuits.containsKey(uuid)) {
      return;
    }
    Schedulers.globalLater(
        Main.getInstance(),
        1,
        () -> {
          // 原子取出: 同一假人连续触发两次退出事件时, 仅第一次派发 after-quit 命令
          var creatorName = pendingFakeQuits.remove(uuid);
          try {
            if (creatorName != null) {
              creatorName.ifPresent(
                  cn ->
                      manager.dispatchCommands(
                          new FakeplayerManager.DispatchCommandArgs(
                              player.getName(), uuid.toString(), cn),
                          config.getAfterQuitCommands()));
            }
          } finally {
            loginCompatManager.cleanup(player);
          }
        });
  }

  /**
   * 假人生成后自动注册并登录
   *
   * <p>仅当 {@code auto-login} 开启且数据库中存在该假人的密码时生效。 命令模板中的 {@code %password%} 会被替换为数据库中的密码, 其余 {@code
   * %p %u %c} 由派发逻辑替换。
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
      // 创建者可能是控制台等非 Player 的 CommandSender, 其本身无"在线"概念,
      // 能接收消息即应发送; 仅当创建者是 Player 且已离线时跳过, 避免 sendMessage 抛异常
      if (creator != null && (!(creator instanceof Player p) || p.isOnline())) {
        creator.sendMessage(
            translatable(
                "fakeplayer.command.password.generated", text(player.getName()), text(password)));
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
   * @param length 密码长度
   * @return 随机密码; {@code length <= 0} 时返回空串
   */
  private @NotNull String generateRandomPassword(@NotNull String charset, int length) {
    if (length <= 0) {
      return "";
    }
    var rnd = ThreadLocalRandom.current();
    var sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
      sb.append(charset.charAt(rnd.nextInt(charset.length())));
    }
    return sb.toString();
  }
}
