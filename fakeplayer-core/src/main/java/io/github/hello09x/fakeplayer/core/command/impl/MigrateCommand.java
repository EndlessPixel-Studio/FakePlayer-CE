package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.database.DatabaseMigrator;
import io.github.hello09x.fakeplayer.core.database.DatabaseType;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.util.Schedulers;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** 把 SQLite 的数据迁移到 MySQL; 必须在 database.type 已经是 mysql 时执行 */
@Singleton
public class MigrateCommand extends AbstractCommand {

  private final DatabaseMigrator migrator;
  private final DatabaseType dialect;

  @Inject
  public MigrateCommand(DatabaseMigrator migrator, DatabaseType dialect) {
    this.migrator = migrator;
    this.dialect = dialect;
  }

  public void migrate(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    if (!this.dialect.isMysql()) {
      LangUtils.send(sender, translatable("fakeplayer.command.migrate.not-mysql", RED));
      return;
    }

    LangUtils.send(sender, translatable("fakeplayer.command.migrate.started", GRAY));
    Schedulers.async(
        Main.getInstance(),
        () -> {
          try {
            var report = this.migrator.migrate();
            report
                .rows()
                .forEach(
                    (table, rows) -> log.info("Migrated %d row(s) from %s".formatted(rows, table)));
            this.reply(
                sender,
                translatable(
                    "fakeplayer.command.migrate.success",
                    GRAY,
                    text(String.valueOf(report.total()))));
          } catch (Exception e) {
            log.warning("Database migration failed: " + e);
            this.reply(
                sender,
                translatable(
                    "fakeplayer.command.migrate.failed",
                    RED,
                    text(String.valueOf(e.getMessage()))));
          }
        });
  }

  private void reply(@NotNull CommandSender sender, @NotNull Component message) {
    Schedulers.runFor(Main.getInstance(), sender, () -> LangUtils.send(sender, message));
  }
}
