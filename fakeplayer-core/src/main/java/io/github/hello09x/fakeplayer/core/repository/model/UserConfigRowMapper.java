package io.github.hello09x.fakeplayer.core.repository.model;

import io.github.hello09x.devtools.database.jdbc.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author tanyaofei
 * @since 2024/7/27
 */
public class UserConfigRowMapper implements RowMapper<UserConfig> {

  public static final UserConfigRowMapper instance = new UserConfigRowMapper();

  @Override
  public @Nullable UserConfig mapRow(@NotNull ResultSet rs, int rowNum) throws SQLException {
    return new UserConfig(
        rs.getInt("id"),
        UUID.fromString(rs.getString("player_id")),
        Feature.valueOf(rs.getString("key")),
        rs.getString("value"));
  }
}
