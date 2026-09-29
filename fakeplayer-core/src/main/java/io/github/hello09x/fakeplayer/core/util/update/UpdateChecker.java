package io.github.hello09x.fakeplayer.core.util.update;

import com.google.gson.Gson;
import io.github.hello09x.devtools.core.version.InvalidVersionException;
import io.github.hello09x.devtools.core.version.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.regex.Pattern;

public class UpdateChecker {

    /**
     * 本项目的版本号格式, 例如 {@code fp.build13}
     */
    private final static Pattern BUILD_NUMBER = Pattern.compile("fp\\.build(\\d+)");

    private final static Gson gson = new Gson();

    private final String author;

    private final String repository;

    public UpdateChecker(@NotNull String author, @NotNull String repository) {
        this.author = author;
        this.repository = repository;
    }

    /**
     * 比较版本号。
     * <p>本项目使用 {@code fp.buildN} 格式, 因此直接比较 N 的大小;
     * 若任意一侧不是该格式, 则回退到通用的版本号比较。</p>
     *
     * @param local  当前版本号
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

    public @NotNull Release getLastRelease() throws IOException, InterruptedException {
        var url = String.format("https://api.github.com/repos/%s/%s/releases/latest", this.author, this.repository);
        var client = HttpClient.newHttpClient();
        var request = HttpRequest.newBuilder()
                                 .uri(URI.create(url))
                                 // GitHub API 要求携带 User-Agent, 否则会返回 403
                                 .header("User-Agent", "FakePlayer-CE-UpdateChecker")
                                 .header("Accept", "application/vnd.github+json")
                                 .GET()
                                 .build();

        var response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (response.statusCode() != 200) {
            throw new IllegalStateException("Not 200 response: " + response.statusCode() + ": " + response.body());
        }

        var body = response.body();
        return gson.fromJson(body, Release.class);
    }


}
