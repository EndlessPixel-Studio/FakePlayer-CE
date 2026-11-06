package io.github.hello09x.fakeplayer.core;

import com.google.inject.Guice;
import com.google.inject.Injector;
import dev.jorel.commandapi.CommandAPI;
import io.github.hello09x.devtools.command.CommandModule;
import io.github.hello09x.devtools.core.TranslationModule;
import io.github.hello09x.devtools.core.translation.TranslationConfig;
import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import io.github.hello09x.devtools.core.utils.Exceptions;
import io.github.hello09x.devtools.database.DatabaseModule;
import io.github.hello09x.fakeplayer.core.aichat.AiChatListener;
import io.github.hello09x.fakeplayer.core.command.CommandRegistry;
import io.github.hello09x.fakeplayer.core.compat.login.LoginCompatManager;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.http.HttpAdminService;
import io.github.hello09x.fakeplayer.core.listener.FakeplayerLifecycleListener;
import io.github.hello09x.fakeplayer.core.listener.FakeplayerListener;
import io.github.hello09x.fakeplayer.core.listener.PlayerListener;
import io.github.hello09x.fakeplayer.core.listener.SpawnTeleportGuard;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerAutofishManager;
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
import java.util.concurrent.CompletableFuture;
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
          var checker = new UpdateChecker("EndlessPixel-Studio", "FakePlayer-CE");
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
          } catch (Throwable e) {
            getLogger().warning("Error on checking for updates: " + e.getMessage());
          }
        });
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
  }

  public static @NotNull Injector getInjector() {
    return instance.injector;
  }
}
