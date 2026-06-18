package io.github.hello09x.fakeplayer.core.manager.invsee;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.devtools.core.utils.ComponentUtils;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryView;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;

/**
 * 默认实现, 无法看到装备栏
 *
 * @author tanyaofei
 * @since 2024/8/12
 **/
@Singleton
public class SimpleInvseeManagerImpl extends AbstractInvseeManager {

    @Inject
    public SimpleInvseeManagerImpl(FakeplayerManager manager, FakeplayerList fakeplayerList) {
        super(manager, fakeplayerList);
    }

    @Override
    protected @Nullable InventoryView openInventory(@NotNull Player viewer, @NotNull Player whom) {
        var view = viewer.openInventory(whom.getInventory());
        if (view!= null) {
            view.setTitle(ComponentUtils.toString(translatable(
                    "fakeplayer.manager.inventory.title",
                    text(whom.getName())
            ), viewer.locale()));
        }
        return view;
    }


}