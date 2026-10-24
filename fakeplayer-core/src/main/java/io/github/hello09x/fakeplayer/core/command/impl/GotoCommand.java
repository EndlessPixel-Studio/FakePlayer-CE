package io.github.hello09x.fakeplayer.core.command.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.devtools.command.exception.HandleCommandException;
import io.github.hello09x.fakeplayer.api.spi.ActionSetting;
import io.github.hello09x.fakeplayer.api.spi.ActionType;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import io.github.hello09x.fakeplayer.core.util.WalkUtils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.YELLOW;

/**
 * 让假人前往指定坐标
 */
@Singleton
public class GotoCommand extends AbstractCommand {

    /**
     * 距离过远通常是误操作 (如少打负号), 直接拒绝
     */
    private static final double MAX_DISTANCE = 1024;

    private final ActionManager actionManager;

    @Inject
    public GotoCommand(ActionManager actionManager) {
        this.actionManager = actionManager;
    }

    @HandleCommandException
    public void goTo(@NotNull CommandSender sender, @NotNull CommandArguments args) throws WrapperCommandSyntaxException {
        var input = (Location) args.get("location");
        var fake = getFakeplayer(sender, args);
        var world = fake.getWorld();
        // location 参数的默认世界是发送者所在世界, 统一替换为假人所在世界
        var target = new Location(world, input.getX(), input.getY(), input.getZ());

        if (!WalkUtils.withinHeight(world, target.getY())) {
            sender.sendMessage(translatable("fakeplayer.command.goto.error.height", RED));
            return;
        }
        if (!world.getWorldBorder().isInside(target)) {
            sender.sendMessage(translatable("fakeplayer.command.goto.error.unreachable", RED));
            return;
        }
        if (target.distance(fake.getLocation()) > MAX_DISTANCE) {
            sender.sendMessage(translatable("fakeplayer.command.goto.error.too-far", RED));
            return;
        }

        var stand = WalkUtils.findStandLocation(fake, target.getBlock());
        if (stand == null) {
            sender.sendMessage(translatable("fakeplayer.command.goto.error.unreachable", RED));
            return;
        }

        var setting = ActionSetting.continuous();
        setting.targetX = stand.getX();
        setting.targetY = stand.getY();
        setting.targetZ = stand.getZ();
        actionManager.setAction(fake, ActionType.GOTO, setting);

        sender.sendMessage(translatable(
                "fakeplayer.command.goto.started",
                YELLOW,
                text(coordinate(stand.getX()), GOLD),
                text(coordinate(stand.getY()), GOLD),
                text(coordinate(stand.getZ()), GOLD)
        ));
    }

    private static String coordinate(double value) {
        return String.format(Locale.ROOT, "%.1f", value);
    }
}
