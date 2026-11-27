package io.github.endlesspixel.fakeplayer.core.database;

import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class SchemaSupport {

  private SchemaSupport() {}

  /** 建唯一索引; MySQL 没有 create index if not exists, 重复建索引的错误要吞掉 */
  public static void createUniqueIndex(
      @NotNull JdbcTemplate jdbc,
      @NotNull DatabaseType type,
      @NotNull String name,
      @NotNull String table,
      @NotNull List<String> columns) {
    try {
      jdbc.execute(type.createUniqueIndex(name, table, columns));
    } catch (RuntimeException e) {
      // SQLite 走的是 if not exists, 报错说明是真的失败
      if (type.supportsIndexIfNotExists()) {
        throw e;
      }
    }
  }
}
