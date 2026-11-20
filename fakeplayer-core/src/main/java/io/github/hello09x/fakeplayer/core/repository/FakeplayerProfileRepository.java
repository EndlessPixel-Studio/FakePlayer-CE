package io.github.hello09x.fakeplayer.core.repository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import io.github.hello09x.devtools.database.jdbc.rowmapper.BooleanRowMapper;
import io.github.hello09x.fakeplayer.core.database.DatabaseType;
import io.github.hello09x.fakeplayer.core.database.SchemaSupport;
import io.github.hello09x.fakeplayer.core.repository.model.FakePlayerProfile;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author tanyaofei
 * @since 2024/8/3
 */
@Singleton
public class FakeplayerProfileRepository {

  private final JdbcTemplate jdbc;
  private final DatabaseType dialect;

  @Inject
  public FakeplayerProfileRepository(JdbcTemplate jdbc, DatabaseType dialect) {
    this.jdbc = jdbc;
    this.dialect = dialect;
    this.initTables();
  }

  public void insert(@NotNull String name, @NotNull UUID uuid) {
    var sql = "INSERT INTO fake_player_profile (name, uuid) VALUES (?, ?)";
    jdbc.update(sql, name, uuid.toString());
  }

  public boolean existsByUUID(@NotNull UUID uuid) {
    var sql = "SELECT EXISTS(SELECT 1 FROM fake_player_profile WHERE uuid = ?)";
    return Boolean.TRUE.equals(jdbc.queryForObject(sql, new BooleanRowMapper(), uuid.toString()));
  }

  public @Nullable UUID selectUUIDByName(@NotNull String name) {
    return Optional.ofNullable(this.selectByName(name))
        .map(FakePlayerProfile::uuid)
        .map(UUID::fromString)
        .orElse(null);
  }

  public @Nullable FakePlayerProfile selectByName(@NotNull String name) {
    var sql = "SELECT * FROM fake_player_profile WHERE name = ?";
    return jdbc.queryForObject(sql, FakePlayerProfile.FakePlayerProfileRowMapper.instance, name);
  }

  private void initTables() {
    jdbc.execute(
        "create table if not exists fake_player_profile ("
            + "id "
            + dialect.autoIncrementPrimaryKey()
            + ", name "
            + dialect.varchar(32)
            + " not null, uuid "
            + dialect.varchar(36)
            + " not null)");

    SchemaSupport.createUniqueIndex(
        jdbc, dialect, "fake_player_profile_name", "fake_player_profile", List.of("name"));
    SchemaSupport.createUniqueIndex(
        jdbc, dialect, "fake_player_profile_uuid", "fake_player_profile", List.of("uuid"));
  }
}
