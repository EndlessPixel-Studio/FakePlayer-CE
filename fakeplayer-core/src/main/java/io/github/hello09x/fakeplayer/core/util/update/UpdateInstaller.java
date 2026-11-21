package io.github.hello09x.fakeplayer.core.util.update;

import io.github.hello09x.fakeplayer.core.Main;
import java.nio.file.Path;
import org.jetbrains.annotations.NotNull;

/**
 * 「下载更新包」这件事的公共部分。
 *
 * <p>下载目标固定是服务器的 {@code plugins/update} 目录: Paper 系核心 (Paper / Purpur / Folia) 会在下次启动时 把该目录里的 jar
 * 覆盖到 {@code plugins/} 下, 于是更新只需要「下载 + 重启」。
 */
public final class UpdateInstaller {

  private UpdateInstaller() {}

  /**
   * 更新包目录, 即插件目录下的 {@code update}。
   *
   * <p>以插件自身的数据目录反推, 这样即使服务端换了插件目录也能对准。
   *
   * @return 更新包目录
   */
  public static @NotNull Path updateDirectory() {
    var dataFolder = Main.getInstance().getDataFolder();
    var plugins = dataFolder.getParentFile();
    if (plugins == null) {
      return Path.of("plugins", "update");
    }
    return plugins.toPath().resolve("update");
  }

  /**
   * 当前服务端是否会处理 {@code plugins/update} 目录。
   *
   * @return Paper 系核心返回 {@code true}
   */
  public static boolean isSupportedByServer() {
    try {
      // Paper 独有的类, 原版 CraftBukkit / Spigot 没有
      Class.forName("io.papermc.paper.plugin.provider.classloader.PaperPluginClassLoader");
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }
}
