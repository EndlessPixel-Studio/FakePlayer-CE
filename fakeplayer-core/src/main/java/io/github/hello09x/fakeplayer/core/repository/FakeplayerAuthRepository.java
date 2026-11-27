package io.github.hello09x.fakeplayer.core.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.endlesspixel.fakeplayer.database.DatabaseType;
import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class FakeplayerAuthRepository {

  private final JdbcTemplate jdbc;
  private final DatabaseType dialect;

  @Inject
  public FakeplayerAuthRepository(JdbcTemplate jdbc, DatabaseType dialect) {
    this.jdbc = jdbc;
    this.dialect = dialect;
    this.initTables();
  }

  /**
   * 保存或更新指定假人的登录密码
   *
   * @param name 假人名称
   * @param password 登录密码
   */
  public void saveOrUpdate(@NotNull String name, @NotNull String password) {
    var sql = dialect.upsert("fakeplayer_auth", List.of("name", "password"), List.of("name"));
    jdbc.update(sql, name, password);
  }

  /**
   * 获取指定假人的登录密码
   *
   * @param name 假人名称
   * @return 密码, 不存在时返回 {@code null}
   */
  public @Nullable String selectByName(@NotNull String name) {
    var sql = "select password from fakeplayer_auth where name = ?";
    var result = jdbc.query(sql, (rs, rowNum) -> rs.getString("password"), name);
    return result.isEmpty() ? null : result.get(0);
  }

  /**
   * 删除指定假人的登录密码
   *
   * @param name 假人名称
   */
  public void deleteByName(@NotNull String name) {
    jdbc.update("delete from fakeplayer_auth where name = ?", name);
  }

  protected void initTables() {
    jdbc.execute(
        "create table if not exists fakeplayer_auth ("
            + "name "
            + dialect.varchar(64)
            + " not null primary key, "
            + "password "
            + dialect.varchar(255)
            + " not null)");
  }
}
