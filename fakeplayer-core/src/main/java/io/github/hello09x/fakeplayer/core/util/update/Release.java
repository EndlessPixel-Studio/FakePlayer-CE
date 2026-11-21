package io.github.hello09x.fakeplayer.core.util.update;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Release {

  /** GitHub tag name */
  @SerializedName("tag_name")
  private String tagName;

  /** GitHub release content */
  private String body;

  /** GitHub release page */
  @SerializedName("html_url")
  private String htmlUrl;

  /** 发布页上的附件 (jar 等) */
  private List<Asset> assets;

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Asset {

    /** 文件名, 例如 {@code fakeplayer-fp.build20.jar} */
    private String name;

    /** 直接下载地址 */
    @SerializedName("browser_download_url")
    private String browserDownloadUrl;

    /** 字节数 */
    private long size;
  }
}
