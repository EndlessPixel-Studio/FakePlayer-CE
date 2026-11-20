package io.github.hello09x.fakeplayer.core.database;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/** 把 data.db (SQLite) 里的数据搬到当前配置的 MySQL 库 */
@Singleton
public class DatabaseMigrator {

  /** 表名 -> 冲突判定用的列, 决定 upsert 的 on duplicate key 部分 */
  private static final Map<String, List<String>> TABLES = new LinkedHashMap<>();

  static {
    TABLES.put("fake_player_profile", List.of("id"));
    TABLES.put("fakeplayer_auth", List.of("name"));
    TABLES.put("fakeplayer_skin", List.of("player_id", "creator_id"));
    TABLES.put("user_config", List.of("player_id", "`key`"));
  }

  private final JdbcTemplate jdbc;
  private final DatabaseType dialect;
  private final FakeplayerConfig config;

  @Inject
  public DatabaseMigrator(JdbcTemplate jdbc, DatabaseType dialect, FakeplayerConfig config) {
    this.jdbc = jdbc;
    this.dialect = dialect;
    this.config = config;
  }

  public @NotNull Report migrate() throws SQLException {
    if (!this.dialect.isMysql()) {
      throw new IllegalStateException("database.type is not mysql");
    }

    var report = new Report();
    try (var source = DriverManager.getConnection(this.config.getSqliteUrl())) {
      for (var entry : TABLES.entrySet()) {
        var table = entry.getKey();
        if (!this.tableExists(source, table)) {
          continue;
        }
        report.rows.put(table, this.copyTable(source, table, entry.getValue()));
      }
    }
    return report;
  }

  private boolean tableExists(@NotNull java.sql.Connection connection, @NotNull String table)
      throws SQLException {
    try (var statement = connection.createStatement();
        var rs =
            statement.executeQuery(
                "select name from sqlite_master where type = 'table' and name = '" + table + "'")) {
      return rs.next();
    }
  }

  /** 逐行读出来再 upsert, 列名按结果集元数据走, 这样两边表结构有新增列也不会漏 */
  private int copyTable(
      @NotNull java.sql.Connection source, @NotNull String table, @NotNull List<String> keys)
      throws SQLException {
    try (var statement = source.createStatement();
        var rs = statement.executeQuery("select * from " + table)) {
      var meta = rs.getMetaData();
      var columns = this.columnsOf(meta);
      var sql = this.dialect.upsert(table, columns, keys);

      var count = 0;
      while (rs.next()) {
        var values = new Object[columns.size()];
        for (var i = 0; i < columns.size(); i++) {
          values[i] = rs.getObject(i + 1);
        }
        this.jdbc.update(sql, values);
        count++;
      }
      return count;
    }
  }

  private @NotNull List<String> columnsOf(@NotNull ResultSetMetaData meta) throws SQLException {
    var columns = new ArrayList<String>(meta.getColumnCount());
    for (var i = 1; i <= meta.getColumnCount(); i++) {
      var column = meta.getColumnLabel(i);
      // MySQL 侧 key / value 是保留字, 统一加反引号
      columns.add(
          "key".equalsIgnoreCase(column) || "value".equalsIgnoreCase(column)
              ? "`" + column + "`"
              : column);
    }
    return columns;
  }

  /** 迁移结果: 表名 -> 行数 */
  public record Report(@NotNull Map<String, Integer> rows) {
    public Report() {
      this(new LinkedHashMap<>());
    }

    public int total() {
      return this.rows.values().stream().mapToInt(Integer::intValue).sum();
    }
  }
}
