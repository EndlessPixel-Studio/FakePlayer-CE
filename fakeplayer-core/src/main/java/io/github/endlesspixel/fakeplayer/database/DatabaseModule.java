package io.github.endlesspixel.fakeplayer.database;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import javax.sql.DataSource;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/** 按 config.yml 的 database 段构造数据源, 取代 devtools 的 DatabaseModule (它只读 datasource 段) */
public class DatabaseModule extends AbstractModule {

  @Provides
  @Singleton
  public @NotNull DatabaseType databaseType(@NotNull FakeplayerConfig config) {
    return config.getDatabaseType();
  }

  @Provides
  @Singleton
  public @NotNull DataSource dataSource(@NotNull FakeplayerConfig config) {
    var hikari = new HikariConfig();
    hikari.setPoolName("FakePlayer-CE");
    hikari.setDriverClassName(config.getDatabaseDriver());
    hikari.setJdbcUrl(config.getDatabaseUrl());
    if (config.getDatabaseUsername() != null) {
      hikari.setUsername(config.getDatabaseUsername());
    }
    if (config.getDatabasePassword() != null) {
      hikari.setPassword(config.getDatabasePassword());
    }
    hikari.setMaximumPoolSize(config.getDatabaseMaxPoolSize());
    hikari.setConnectionTimeout(config.getDatabaseConnectionTimeout());
    return new HikariDataSource(hikari);
  }

  @Provides
  @Singleton
  public @NotNull JdbcTemplate jdbcTemplate(
      @NotNull Plugin plugin, @NotNull DataSource dataSource) {
    return new JdbcTemplate(plugin, dataSource);
  }
}
