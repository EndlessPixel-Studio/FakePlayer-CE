package io.github.hello09x.fakeplayer.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.zaxxer.hikari.HikariDataSource;
import dev.jorel.commandapi.CommandAPI;
import io.github.endlesspixel.fakeplayer.core.aichat.AiChatListener;
import io.github.endlesspixel.fakeplayer.core.database.DatabaseModule;
import io.github.endlesspixel.fakeplayer.core.http.HttpAdminService;
import io.github.endlesspixel.fakeplayer.core.util.library.LibraryManager;
import io.github.hello09x.devtools.command.CommandModule;
import io.github.hello09x.devtools.core.TranslationModule;
import io.github.hello09x.devtools.core.translation.TranslationConfig;
import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import io.github.hello09x.devtools.core.utils.Exceptions;
import io.github.hello09x.fakeplayer.core.command.CommandRegistry;
import io.github.hello09x.fakeplayer.core.compat.login.LoginCompatManager;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.listener.FakeplayerLifecycleListener;
import io.github.hello09x.fakeplayer.core.listener.FakeplayerListener;
import io.github.hello09x.fakeplayer.core.listener.PlayerListener;
import io.github.hello09x.fakeplayer.core.listener.SpawnTeleportGuard;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerAutofishManager;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerAutosleepManager;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerPingSetter;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerReplenishManager;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerRestoreManager;
import io.github.hello09x.fakeplayer.core.manager.WildFakeplayerManager;
import io.github.hello09x.fakeplayer.core.manager.invsee.InvseeManager;
import io.github.hello09x.fakeplayer.core.placeholder.FakeplayerPlaceholderExpansion;
import io.github.hello09x.fakeplayer.core.repository.UsedIdRepository;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import io.github.hello09x.fakeplayer.core.util.update.UpdateChecker;
import io.github.hello09x.fakeplayer.core.util.update.UpdateInstaller;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import javax.sql.DataSource;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class Main extends JavaPlugin {

  @Getter private static Main instance;

  private Injector injector;
  private FakeplayerManager fakeplayerManager;
  private UsedIdRepository usedIdRepository;
  private boolean stopped;

  /** 已经提示过的新版本, 用于周期检查时去重 */
  private volatile String notifiedVersion;

  private long loadAt;

  @Override
  public void onLoad() {
    loadAt = System.currentTimeMillis();
    instance = this;
  }

  @Override
  public void onEnable() {
    stopped = false;
    try {
      LibraryManager.ensureEnabledFeatures(getConfig());
    } catch (RuntimeException e) {
      // 下载失败不应让整个插件禁用; 对应功能会自己记录不可用
      getLogger().severe("Failed to download optional libraries: " + e);
    }
    injector =
        Guice.createInjector(
            new FakeplayerModule(),
            new CommandModule(),
            new DatabaseModule(),
            new TranslationModule(
                new TranslationConfig(
                    "message/message", TranslatorUtils.getDefaultLocale(Main.getInstance()))));
    fakeplayerManager = injector.getInstance(FakeplayerManager.class);
    usedIdRepository = injector.getInstance(UsedIdRepository.class);
    getServer()
        .getPluginManager()
        .registerEvents(injector.getInstance(FakeplayerListener.class), this);

    injector.getInstance(CommandRegistry.class).register();
    {
      var messenger = getServer().getMessenger();
      messenger.registerIncomingPluginChannel(
          this, "BungeeCord", injector.getInstance(WildFakeplayerManager.class));
      messenger.registerOutgoingPluginChannel(this, "BungeeCord");
    }

    {
      var manager = getServer().getPluginManager();
      manager.registerEvents(injector.getInstance(WildFakeplayerManager.class), this);
      manager.registerEvents(injector.getInstance(PlayerListener.class), this);
      manager.registerEvents(injector.getInstance(FakeplayerLifecycleListener.class), this);
      manager.registerEvents(injector.getInstance(FakeplayerAutofishManager.class), this);
      manager.registerEvents(injector.getInstance(FakeplayerReplenishManager.class), this);
      manager.registerEvents(injector.getInstance(InvseeManager.class), this);
      manager.registerEvents(injector.getInstance(AiChatListener.class), this);
      manager.registerEvents(injector.getInstance(SpawnTeleportGuard.class), this);
    }

    injector.getInstance(LoginCompatManager.class).onEnable();
    injector.getInstance(FakeplayerPingSetter.class).restart();

    {
      var placeholderExpansion = injector.getInstance(FakeplayerPlaceholderExpansion.class);
      if (placeholderExpansion != null) {
        if (placeholderExpansion.register()) {
          getServer().getPluginManager().registerEvents(placeholderExpansion, this);
          getLogger().info("Successfully registered PlaceholderExpansion");
        }
      }
    }

    injector.getInstance(HttpAdminService.class).start();

    {
      var restoreManager = injector.getInstance(FakeplayerRestoreManager.class);
      getServer().getPluginManager().registerEvents(restoreManager, this);
      restoreManager.start();
    }

    injector.getInstance(FakeplayerAutosleepManager.class).start();

    var config = injector.getInstance(FakeplayerConfig.class);
    if (config.isCheckForUpdates()) {
      checkForUpdatesAsync();

      // 长期不重启的服务器也要能看到新版本; 间隔为 0 时保持旧行为, 只在开服检查一次
      var hours = config.getUpdateCheckIntervalHours();
      if (hours > 0) {
        var period = (long) hours * 60 * 60 * 20;
        Schedulers.asyncTimer(this, period, period, this::checkForUpdatesAsync);
      }
    }

    getLogger().info("Enabled in %d ms".formatted(System.currentTimeMillis() - loadAt));
  }

  public void checkForUpdatesAsync() {
    CompletableFuture.runAsync(
        () -> {
          var meta = this.getPluginMeta();
          // 本项目的版本号为 fp.buildN 格式, 因此检查本仓库自己的 GitHub Releases
          var checker =
              new UpdateChecker(
                  "EndlessPixel-Studio",
                  "FakePlayer-CE",
                  injector.getInstance(FakeplayerConfig.class).isUpdateIgnoreSslErrors());
          try {
            var result = checker.check(meta.getVersion());
            var latest = result.latest();
            if (!result.outdated() || latest == null) {
              return;
            }

            // 周期检查时同一个版本只提示一次, 否则更新日志会按间隔反复刷
            if (latest.equals(this.notifiedVersion)) {
              return;
            }
            this.notifiedVersion = latest;

            var log = getLogger();
            log.info("New version: %s (current: %s)".formatted(latest, result.current()));
            log.info("Address: " + (result.url() != null ? result.url() : meta.getWebsite()));
            log.info("Update Log");
            var body = result.body();
            if (body != null) {
              for (var line : body.split("\n")) {
                log.info("\t" + line);
              }
            }

            this.downloadUpdateIfConfigured(checker, result);
          } catch (Throwable e) {
            getLogger().warning("Error on checking for updates: " + e.getMessage());
          }
        });
  }

  /**
   * 按配置把更新包下载到 {@code plugins/update}。
   *
   * <p>同一个新版本只会走一次 (调用方已用 {@code notifiedVersion} 去重)。
   *
   * @param checker 更新检查器
   * @param result 检查结果
   */
  private void downloadUpdateIfConfigured(
      @NotNull UpdateChecker checker, @NotNull UpdateChecker.Result result) {
    var config = injector.getInstance(FakeplayerConfig.class);
    if (!config.isUpdateDownload()) {
      return;
    }

    var log = getLogger();
    try {
      var target =
          checker.download(
              result.release(),
              UpdateInstaller.updateDirectory(),
              Duration.ofSeconds(config.getUpdateDownloadTimeoutSeconds()));
      log.info(
          "Downloaded %s to %s, restart the server to apply it".formatted(result.latest(), target));
      if (!UpdateInstaller.isSupportedByServer()) {
        log.warning(
            "This server does not process the plugins/update folder automatically,"
                + " move the jar to plugins/ manually");
      }
    } catch (Throwable e) {
      log.warning("Failed to download the update: " + e.getMessage());
    }
  }

  @Override
  public void onDisable() {
    shutdownResources();
  }

  public void shutdownResources() {
    if (stopped) {
      return;
    }
    stopped = true;
    {
      Exceptions.suppress(this, () -> CommandAPI.unregister("fp", true));
      Exceptions.suppress(this, () -> CommandAPI.unregister("fakeplayer", true));
    }
    Exceptions.suppress(this, () -> injector.getInstance(HttpAdminService.class).stop());
    Exceptions.suppress(
        this, () -> injector.getInstance(FakeplayerRestoreManager.class).saveSnapshot());

    if (fakeplayerManager != null) {
      Exceptions.suppress(this, fakeplayerManager::onDisable);
      fakeplayerManager = null;
    }
    if (injector != null) {
      Exceptions.suppress(this, () -> injector.getInstance(LoginCompatManager.class).cleanupAll());
    }
    if (usedIdRepository != null) {
      Exceptions.suppress(this, usedIdRepository::onDisable);
      usedIdRepository = null;
    }
    {
      Exceptions.suppress(
          this,
          () -> {
            var messenger = getServer().getMessenger();
            messenger.unregisterIncomingPluginChannel(this);
            messenger.unregisterOutgoingPluginChannel(this);
          });
    }
    if (injector != null) {
      // 关掉连接池, 否则 reload 场景下 MySQL 连接会一直挂着
      Exceptions.suppress(
          this,
          () -> {
            var dataSource = injector.getInstance(DataSource.class);
            if (dataSource instanceof HikariDataSource hikari) {
              hikari.close();
            }
          });
    }
  }

  public static @NotNull Injector getInjector() {
    return instance.injector;
  }
}
