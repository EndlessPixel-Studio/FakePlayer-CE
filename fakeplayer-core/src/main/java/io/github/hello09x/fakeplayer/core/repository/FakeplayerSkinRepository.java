package io.github.hello09x.fakeplayer.core.repository;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.database.jdbc.JdbcTemplate;
import io.github.hello09x.fakeplayer.core.database.DatabaseType;
import io.github.hello09x.fakeplayer.core.repository.model.FakePlayerSkin;
import io.github.hello09x.fakeplayer.core.repository.model.FakePlayerSkinRowMapper;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author tanyaofei
 * @since 2024/8/8
 */
@Singleton
public class FakeplayerSkinRepository {

  private final JdbcTemplate jdbc;
  private final DatabaseType dialect;

  @Inject
  public FakeplayerSkinRepository(JdbcTemplate jdbc, DatabaseType dialect) {
    this.jdbc = jdbc;
    this.dialect = dialect;
    this.initTables();
  }

  @CanIgnoreReturnValue
  public int insertOrUpdate(@NotNull FakePlayerSkin skin) {
    var sql =
        dialect.upsert(
            "fakeplayer_skin",
            List.of("player_id", "creator_id", "target_id"),
            List.of("player_id", "creator_id"));

    return jdbc.update(
        sql, skin.playerId().toString(), skin.creatorId().toString(), skin.targetId());
  }

  public @Nullable FakePlayerSkin selectByCreatorIdAndPlayerId(
      @NotNull UUID creatorId, @NotNull UUID playerId) {
    return jdbc.queryForObject(
        "select * from fakeplayer_skin where creator_id = ? and player_id = ?",
        FakePlayerSkinRowMapper.instance,
        creatorId.toString(),
        playerId.toString());
  }

  public void initTables() {
    var text = dialect.varchar(36);
    this.jdbc.execute(
        "create table if not exists fakeplayer_skin ("
            + "player_id "
            + text
            + " not null, "
            + "creator_id "
            + text
            + " not null, "
            + "target_id "
            + text
            + " not null, "
            + "constraint fakeplayer_skin_pk primary key (player_id, creator_id))");
  }
}
