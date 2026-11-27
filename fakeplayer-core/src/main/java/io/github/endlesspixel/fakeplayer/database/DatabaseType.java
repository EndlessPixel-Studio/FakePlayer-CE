package io.github.endlesspixel.fakeplayer.database;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.jetbrains.annotations.NotNull;

/** 支持的数据库类型, 以及两种方言的 SQL 差异 */
public enum DatabaseType {
  SQLITE,
  MYSQL;

  public static @NotNull DatabaseType parse(String raw) {
    if (raw == null || raw.isBlank()) {
      return SQLITE;
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "mysql", "mariadb" -> MYSQL;
      default -> SQLITE;
    };
  }

  public boolean isMysql() {
    return this == MYSQL;
  }

  /** 自增主键: SQLite 的 autoincrement 只能跟 integer primary key, MySQL 用 auto_increment */
  public @NotNull String autoIncrementPrimaryKey() {
    return this == MYSQL
        ? "int not null auto_increment primary key"
        : "integer not null primary key autoincrement";
  }

  /** 可做索引/主键的字符串列: MySQL 的 text 不能直接当主键, 必须带长度 */
  public @NotNull String varchar(int length) {
    return "varchar(" + length + ")";
  }

  /** 普通文本列, 两边都叫 text */
  public @NotNull String text() {
    return "text";
  }

  /** MySQL 不支持 create index if not exists, 建索引要容忍重复 */
  public boolean supportsIndexIfNotExists() {
    return this == SQLITE;
  }

  public @NotNull String createUniqueIndex(
      @NotNull String name, @NotNull String table, @NotNull List<String> columns) {
    var cols = String.join(", ", columns);
    var prefix = this == SQLITE ? "create unique index if not exists " : "create unique index ";
    return prefix + name + " on " + table + " (" + cols + ")";
  }

  /** upsert: SQLite 用 insert or replace, MySQL 用 on duplicate key update */
  public @NotNull String upsert(
      @NotNull String table, @NotNull List<String> columns, @NotNull List<String> keys) {
    var cols = String.join(", ", columns);
    var marks = String.join(", ", Collections.nCopies(columns.size(), "?"));
    if (this == SQLITE) {
      return "insert or replace into " + table + " (" + cols + ") values (" + marks + ")";
    }

    var updates =
        columns.stream()
            .filter(column -> !keys.contains(column))
            .map(column -> column + " = values(" + column + ")")
            .toList();
    var suffix = updates.isEmpty() ? "" : " on duplicate key update " + String.join(", ", updates);
    return "insert into " + table + " (" + cols + ") values (" + marks + ")" + suffix;
  }
}
