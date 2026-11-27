package io.github.hello09x.fakeplayer.core.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.endlesspixel.fakeplayer.database.DatabaseType;
import io.github.endlesspixel.fakeplayer.database.SchemaSupport;
import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import io.github.hello09x.fakeplayer.core.repository.model.Feature;
import io.github.hello09x.fakeplayer.core.repository.model.UserConfig;
import io.github.hello09x.fakeplayer.core.repository.model.UserConfigRowMapper;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Singleton
public class UserConfigRepository {

  private final JdbcTemplate jdbc;
  private final DatabaseType dialect;

  @Inject
  public UserConfigRepository(JdbcTemplate jdbc, DatabaseType dialect) {
    this.jdbc = jdbc;
    this.dialect = dialect;
    this.initTables();
  }

  public @Nullable UserConfig selectByPlayerIdAndKey(
      @NotNull UUID playerId, @NotNull Feature feature) {
    var sql = "select * from user_config where player_id = ? and `key` = ?";
    return jdbc.queryForObject(
        sql, UserConfigRowMapper.instance, playerId.toString(), feature.name());
  }

  public int saveOrUpdate(@NotNull UserConfig config) {
    var sql =
        dialect.upsert(
            "user_config", List.of("player_id", "`key`", "`value`"), List.of("player_id", "`key`"));

    return jdbc.update(sql, config.playerId().toString(), config.key().name(), config.value());
  }

  public @NotNull List<UserConfig> selectByPlayerId(@NotNull UUID playerId) {
    var sql = "select * from user_config where player_id = ?";
    return jdbc.query(sql, UserConfigRowMapper.instance, playerId.toString());
  }

  protected void initTables() {
    jdbc.execute(
        "create table if not exists user_config ("
            + "id "
            + dialect.autoIncrementPrimaryKey()
            + ", player_id "
            + dialect.varchar(36)
            + " not null, `key` "
            + dialect.text()
            + " not null, `value` "
            + dialect.text()
            + " not null)");

    SchemaSupport.createUniqueIndex(
        jdbc,
        dialect,
        "table_name_player_id_key_uindex",
        "user_config",
        List.of("player_id", "`key`"));
  }
}
