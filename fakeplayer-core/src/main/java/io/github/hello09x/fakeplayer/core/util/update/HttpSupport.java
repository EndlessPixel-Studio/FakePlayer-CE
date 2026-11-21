package io.github.hello09x.fakeplayer.core.util.update;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.jetbrains.annotations.NotNull;

/**
 * 更新检查 / 下载用的最小 HTTP 封装。
 *
 * <p>用 {@link HttpURLConnection} 而不是 {@code java.net.http.HttpClient}, 因为后者关闭主机名校验只能靠全局系统属性,
 * 而这里需要「按配置在单个连接上忽略证书校验」(部分内网镜像 / 代理的证书链不完整)。
 */
final class HttpSupport {

  private static final String USER_AGENT = "FakePlayer-CE-UpdateChecker";

  private HttpSupport() {}

  /**
   * 打开一个 GET 连接。
   *
   * @param url 地址
   * @param timeout 连接与读取超时
   * @param ignoreSslErrors 是否跳过证书与主机名校验
   * @return 连接
   * @throws IOException 打开失败
   */
  static @NotNull HttpURLConnection open(
      @NotNull URL url, @NotNull Duration timeout, boolean ignoreSslErrors) throws IOException {
    var connection = (HttpURLConnection) url.openConnection();
    var millis = (int) Math.min(Integer.MAX_VALUE, timeout.toMillis());
    connection.setConnectTimeout(millis);
    connection.setReadTimeout(millis);
    connection.setRequestProperty("User-Agent", USER_AGENT);
    connection.setRequestProperty("Accept", "application/vnd.github+json");
    connection.setInstanceFollowRedirects(true);

    if (ignoreSslErrors && connection instanceof HttpsURLConnection https) {
      https.setSSLSocketFactory(trustAllFactory());
      // 校验跳过 CA 之后主机名也一并放行, 否则证书链问题解决了还是连不上
      https.setHostnameVerifier((hostname, session) -> true);
    }
    return connection;
  }

  private static @NotNull SSLSocketFactory trustAllFactory() throws IOException {
    try {
      var context = SSLContext.getInstance("TLS");
      context.init(null, new TrustManager[] {TRUST_ALL}, new SecureRandom());
      return context.getSocketFactory();
    } catch (GeneralSecurityException e) {
      throw new IOException("Failed to create a trust-all SSL context", e);
    }
  }

  /** 不做任何校验的信任管理器 */
  private static final X509TrustManager TRUST_ALL =
      new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
          // 有意为之: 见 update-ignore-ssl-errors 配置说明
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
          // 有意为之: 见 update-ignore-ssl-errors 配置说明
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      };
}
