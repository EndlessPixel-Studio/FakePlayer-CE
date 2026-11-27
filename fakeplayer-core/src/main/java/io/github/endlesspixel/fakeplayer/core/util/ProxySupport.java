package io.github.endlesspixel.fakeplayer.core.util;

import io.github.hello09x.fakeplayer.core.Main;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

/**
 * 代理端 (BungeeCord / Velocity) 检测
 *
 * <p>只看 {@code spigot.yml} 的 {@code settings.bungeecord} 会漏掉 Velocity 现代转发: 这种部署下该开关通常是 {@code
 * false}, 代理信息写在 {@code config/paper-global.yml} 的 {@code proxies.velocity.enabled}。漏判的后果是 {@code
 * follow-quiting} 把「玩家跨服」 当成「玩家下线」, 十几秒后清掉他名下的假人。
 */
public final class ProxySupport {

  private ProxySupport() {}

  /**
   * 当前服务器是否挂在代理端后面
   *
   * @return 处于 BungeeCord 模式或 Velocity 现代转发时为 {@code true}
   */
  public static boolean isProxied() {
    return isLegacyBungee() || isVelocityForwarding();
  }

  /** 传统 BungeeCord 模式, 由 {@code spigot.yml} 的 {@code settings.bungeecord} 开关决定 */
  private static boolean isLegacyBungee() {
    try {
      return Bukkit.getServer().spigot().getSpigotConfig().getBoolean("settings.bungeecord", false);
    } catch (Throwable e) {
      return false;
    }
  }

  /**
   * Velocity 现代转发, 由 {@code proxies.velocity.enabled} 决定
   *
   * <p>1.20.5 起 Paper 把全局配置挪到了 {@code config/paper-global.yml}, 旧版本是 {@code paper.yml}, 两个都看。
   */
  private static boolean isVelocityForwarding() {
    var root = serverDirectory();
    for (var path : new String[] {"config/paper-global.yml", "paper.yml"}) {
      var file = new File(root, path);
      if (!file.isFile()) {
        continue;
      }
      try (var reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
        if (isVelocityEnabled(new Yaml().load(reader))) {
          return true;
        }
      } catch (Throwable e) {
        Main.getInstance()
            .getLogger()
            .fine("Cannot read " + path + " for proxy detection: " + e.getMessage());
      }
    }
    return false;
  }

  /**
   * 服务端根目录。
   *
   * <p>不能直接用相对路径: 相对的是 JVM 的工作目录, 从服务器目录之外启动时 ({@code java -jar /srv/mc/paper.jar} 或面板 / systemd 没写
   * {@code WorkingDirectory}) 会读不到 {@code config/paper-global.yml}。
   *
   * @return 世界容器目录 (通常即服务端根目录); 取不到时退回进程工作目录
   */
  private static @NotNull File serverDirectory() {
    try {
      var container = Bukkit.getWorldContainer();
      if (container != null) {
        return container;
      }
    } catch (Throwable ignored) {
      // 没有运行中的服务端 (例如离线测试) 时退回工作目录
    }
    return new File(".");
  }

  private static boolean isVelocityEnabled(Object root) {
    if (!(root instanceof Map<?, ?> map)) {
      return false;
    }
    if (!(map.get("proxies") instanceof Map<?, ?> proxies)) {
      return false;
    }
    if (!(proxies.get("velocity") instanceof Map<?, ?> velocity)) {
      return false;
    }
    return Boolean.TRUE.equals(velocity.get("enabled"));
  }
}
