package io.github.endlesspixel.fakeplayer.core.util.library;

import io.github.hello09x.fakeplayer.core.Main;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import org.bukkit.configuration.Configuration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** 可选依赖按需下载到 plugins/FakePlayer-CE/libs, 不随插件 jar 分发 */
public final class LibraryManager {

  private static final Logger log = Main.getInstance().getLogger();
  private static final String REPO_PROPERTY = "Leaf.library-download-repo";
  private static final String DEFAULT_REPO = "https://repo1.maven.org/maven2";
  private static final Duration TIMEOUT = Duration.ofSeconds(30);
  private static final String SAFE_PART = "[A-Za-z0-9_.-]+";
  private static final Map<LibrarySet, LibraryClassLoader> loaders = new ConcurrentHashMap<>();

  private LibraryManager() {}

  /** 按配置决定需要哪些可选依赖; 必须在 Guice 创建任何使用可选依赖的对象之前调用 */
  public static void ensureEnabledFeatures(@NotNull Configuration config) {
    if ("mysql".equalsIgnoreCase(config.getString("database.type", "sqlite"))) {
      ensure(LibrarySet.MYSQL);
    }
    if (config.getBoolean("http-admin.enabled", false)) {
      ensure(LibrarySet.UNDERTOW);
    }
  }

  public static synchronized void ensure(@NotNull LibrarySet set) {
    if (loaders.containsKey(set)) {
      return;
    }
    var directory = libraryDirectory();
    try {
      Files.createDirectories(directory);
      var urls = new ArrayList<URL>();
      var pluginJar = pluginJarUrl();
      if (pluginJar != null) {
        urls.add(pluginJar);
      }
      for (var coordinate : set.coordinates()) {
        var file = resolveLibraryFile(directory, coordinate);
        if (!isJar(file)) {
          download(coordinate, file);
        }
        urls.add(file.toUri().toURL());
      }
      loaders.put(
          set,
          new LibraryClassLoader(
              urls.toArray(URL[]::new),
              Main.getInstance().getClass().getClassLoader(),
              set.prefixes(),
              set.exactClasses()));
      log.info("Loaded " + set + " libraries from " + directory);
    } catch (IOException | RuntimeException e) {
      log.severe("Failed to prepare " + set + " libraries: " + e);
      throw new IllegalStateException("Optional libraries unavailable: " + set, e);
    }
  }

  public static @NotNull ClassLoader loader(@NotNull LibrarySet set) {
    var loader = loaders.get(set);
    if (loader == null) {
      throw new IllegalStateException("Libraries not prepared: " + set);
    }
    return loader;
  }

  private static @NotNull Path libraryDirectory() {
    return Main.getInstance().getDataFolder().toPath().resolve("libs").toAbsolutePath().normalize();
  }

  /** 坐标全部写死在 {@link LibrarySet} 里, 仍然逐段校验, 防止将来有人把外部输入拼进来 */
  private static @NotNull Path resolveLibraryFile(
      @NotNull Path directory, @NotNull String coordinate) {
    var parts = coordinate.split(":");
    if (parts.length != 3) {
      throw new IllegalArgumentException("Invalid library coordinate: " + coordinate);
    }
    for (var part : parts) {
      if (part.isBlank() || !part.matches(SAFE_PART) || part.contains("..")) {
        throw new SecurityException("Unsafe library coordinate: " + coordinate);
      }
    }
    var group = parts[0];
    var artifact = parts[1];
    var version = parts[2];
    var relative =
        Path.of(group.replace('.', '/'), artifact, version, artifact + "-" + version + ".jar");
    var target = directory.resolve(relative).normalize();
    if (!target.startsWith(directory)) {
      throw new SecurityException("Library path escapes libs directory: " + coordinate);
    }
    return target;
  }

  private static @Nullable URL pluginJarUrl() {
    var source = Main.getInstance().getClass().getProtectionDomain().getCodeSource();
    return source == null ? null : source.getLocation();
  }

  private static boolean isJar(@NotNull Path file) {
    if (!Files.isRegularFile(file)) {
      return false;
    }
    try (var in = Files.newInputStream(file)) {
      return in.read() == 'P' && in.read() == 'K';
    } catch (IOException e) {
      return false;
    }
  }

  private static void download(@NotNull String coordinate, @NotNull Path file) throws IOException {
    var url = URI.create(repository() + "/" + coordinatePath(coordinate)).toURL();
    var connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout((int) TIMEOUT.toMillis());
    connection.setReadTimeout((int) TIMEOUT.toMillis());
    connection.setInstanceFollowRedirects(true);
    connection.setRequestProperty("User-Agent", "FakePlayer-CE-LibraryLoader");
    var status = connection.getResponseCode();
    if (status < 200 || status >= 300) {
      throw new IOException("HTTP " + status + " while downloading " + coordinate);
    }

    var part = file.resolveSibling(file.getFileName() + ".part");
    Files.createDirectories(file.getParent());
    try (InputStream in = connection.getInputStream()) {
      Files.copy(in, part, StandardCopyOption.REPLACE_EXISTING);
    } finally {
      connection.disconnect();
    }
    if (!isJar(part)) {
      Files.deleteIfExists(part);
      throw new IOException("Downloaded file is not a jar: " + coordinate);
    }
    try {
      Files.move(part, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException e) {
      Files.move(part, file, StandardCopyOption.REPLACE_EXISTING);
    }
    log.info("Downloaded " + coordinate + " to " + file);
  }

  private static @NotNull String coordinatePath(@NotNull String coordinate) {
    var parts = coordinate.split(":");
    return parts[0].replace('.', '/')
        + "/"
        + parts[1]
        + "/"
        + parts[2]
        + "/"
        + parts[1]
        + "-"
        + parts[2]
        + ".jar";
  }

  private static @NotNull String repository() {
    var configured = System.getProperty(REPO_PROPERTY, DEFAULT_REPO).trim();
    var uri = URI.create(configured);
    var scheme = uri.getScheme();
    if (!Set.of("http", "https").contains(scheme) || uri.getHost() == null) {
      throw new IllegalArgumentException("-D" + REPO_PROPERTY + " must be an http(s) URL");
    }
    return configured.endsWith("/") ? configured.substring(0, configured.length() - 1) : configured;
  }
}
