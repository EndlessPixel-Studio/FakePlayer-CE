package io.github.hello09x.fakeplayer.core.util.update;

import com.google.gson.Gson;
import io.github.hello09x.devtools.core.version.InvalidVersionException;
import io.github.hello09x.devtools.core.version.Version;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdateChecker {

  /** 本项目的版本号格式, 例如 {@code fp.build13} */
  private static final Pattern BUILD_NUMBER = Pattern.compile("fp\\.build(\\d+)");

  private static final Gson gson = new Gson();

  private final String author;

  private final String repository;

  public UpdateChecker(@NotNull String author, @NotNull String repository) {
    this.author = author;
    this.repository = repository;
  }

  /**
   * 比较版本号。
   *
   * <p>本项目使用 {@code fp.buildN} 格式, 因此直接比较 N 的大小; 若任意一侧不是该格式, 则回退到通用的版本号比较。
   *
   * @param local 当前版本号
   * @param remote 远端版本号
   * @return 远端版本是否更新
   */
  public static boolean isNew(@NotNull String local, @NotNull String remote) {
    var localBuild = parseBuildNumber(local);
    var remoteBuild = parseBuildNumber(remote);
    if (localBuild != null && remoteBuild != null) {
      return localBuild < remoteBuild;
    }

    Version a, b;
    try {
      a = Version.parse(local);
      b = Version.parse(remote);
    } catch (InvalidVersionException e) {
      return false;
    }

    return a.compareTo(b) < 0;
  }

  /**
   * 解析 {@code fp.buildN} 中的 N
   *
   * @param version 版本号
   * @return N, 不符合该格式时返回 {@code null}
   */
  public static @Nullable Long parseBuildNumber(@NotNull String version) {
    var matcher = BUILD_NUMBER.matcher(version.trim().toLowerCase(Locale.ROOT));
    if (!matcher.find()) {
      return null;
    }

    try {
      return Long.parseLong(matcher.group(1));
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /**
   * 检查是否有更新
   *
   * @param current 当前版本号
   * @return 检查结果
   */
  public @NotNull Result check(@NotNull String current) throws IOException, InterruptedException {
    var release = this.getLastRelease();
    var latest = stripTagPrefix(release.getTagName());
    return new Result(
        current,
        latest,
        latest != null && isNew(current, latest),
        release.getHtmlUrl(),
        release.getBody(),
        release);
  }

  /**
   * 去掉 tag 上可能存在的 {@code v} 前缀
   *
   * @param tag tag, 例如 {@code v1.2.3} 或 {@code fp.build16}
   * @return 版本号, tag 为空时返回 {@code null}
   */
  public static @Nullable String stripTagPrefix(@Nullable String tag) {
    if (tag == null || tag.isBlank()) {
      return null;
    }
    return switch (tag.charAt(0)) {
      case 'v', 'V' -> tag.substring(1);
      default -> tag;
    };
  }

  public @NotNull Release getLastRelease() throws IOException, InterruptedException {
    var url =
        String.format(
            "https://api.github.com/repos/%s/%s/releases/latest", this.author, this.repository);
    var client = HttpClient.newHttpClient();
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            // GitHub API 要求携带 User-Agent, 否则会返回 403
            .header("User-Agent", "FakePlayer-CE-UpdateChecker")
            .header("Accept", "application/vnd.github+json")
            .GET()
            .build();

    var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    if (response.statusCode() != 200) {
      throw new IllegalStateException(
          "Not 200 response: " + response.statusCode() + ": " + response.body());
    }

    var body = response.body();
    return gson.fromJson(body, Release.class);
  }

  /**
   * 把发布页上的 jar 下载到指定目录。
   *
   * <p>用于 Paper 系的 {@code plugins/update} 机制: 核心在下次启动时会把该目录里的 jar 覆盖到 {@code plugins/} 下,
   * 省得管理员手动替换文件。
   *
   * @param release 发布信息
   * @param directory 目标目录 (通常是 {@code plugins/update})
   * @param timeout 下载超时时间
   * @return 写入的文件
   * @throws IOException 下载或写文件失败
   * @throws InterruptedException 线程被中断
   * @throws IllegalStateException 发布页上没有可下载的 jar
   */
  public @NotNull Path download(
      @NotNull Release release, @NotNull Path directory, @NotNull Duration timeout)
      throws IOException, InterruptedException {
    var asset = findJarAsset(release);
    var client =
        HttpClient.newBuilder()
            // GitHub 的附件地址会 302 到 objects.githubusercontent.com
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(timeout)
            .build();
    var request =
        HttpRequest.newBuilder()
            .uri(URI.create(asset.getBrowserDownloadUrl()))
            .header("User-Agent", "FakePlayer-CE-UpdateChecker")
            .timeout(timeout)
            .GET()
            .build();

    Files.createDirectories(directory);
    var target = directory.resolve(asset.getName());
    var temp = directory.resolve(asset.getName() + ".part");

    var response = client.send(request, HttpResponse.BodyHandlers.ofFile(temp));
    if (response.statusCode() != 200) {
      Files.deleteIfExists(temp);
      throw new IllegalStateException(
          "Not 200 response: " + response.statusCode() + " while downloading " + asset.getName());
    }

    // 先清掉目录里的旧版本, 否则核心会把两个版本都当成插件处理
    this.deleteOtherJars(directory, target);
    Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
    return target;
  }

  private @NotNull Release.Asset findJarAsset(@NotNull Release release) {
    var assets = release.getAssets();
    if (assets == null) {
      throw new IllegalStateException("The release has no assets");
    }
    return assets.stream()
        .filter(asset -> asset.getName() != null && asset.getName().endsWith(".jar"))
        .filter(asset -> asset.getBrowserDownloadUrl() != null)
        // 优先选本插件自己的 jar, 避免把校验文件之类的附件下下来
        .min(Comparator.comparingInt(asset -> asset.getName().startsWith("fakeplayer") ? 0 : 1))
        .orElseThrow(() -> new IllegalStateException("The release has no downloadable jar"));
  }

  private void deleteOtherJars(@NotNull Path directory, @NotNull Path keep) throws IOException {
    try (var files = Files.list(directory)) {
      for (var file : files.toList()) {
        var name = file.getFileName().toString();
        if (file.equals(keep) || !name.startsWith("fakeplayer") || !name.endsWith(".jar")) {
          continue;
        }
        Files.deleteIfExists(file);
      }
    }
  }

  /**
   * 一次更新检查的结果
   *
   * @param current 当前版本号
   * @param latest 远端最新版本号, 发布页没写 tag 时为 {@code null}
   * @param outdated 远端是否更新
   * @param url 发布页地址
   * @param body 更新日志
   * @param release 原始发布信息, 下载更新包时用
   */
  public record Result(
      @NotNull String current,
      @Nullable String latest,
      boolean outdated,
      @Nullable String url,
      @Nullable String body,
      @NotNull Release release) {}
}
