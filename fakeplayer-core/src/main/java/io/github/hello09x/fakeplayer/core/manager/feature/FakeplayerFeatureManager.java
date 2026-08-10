package io.github.hello09x.fakeplayer.core.manager.feature;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.hello09x.fakeplayer.core.config.FakeplayerConfig;
import io.github.hello09x.fakeplayer.core.entity.Fakeplayer;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerList;
import io.github.hello09x.fakeplayer.core.repository.UserConfigRepository;
import io.github.hello09x.fakeplayer.core.repository.model.Feature;
import io.github.hello09x.fakeplayer.core.repository.model.UserConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class FakeplayerFeatureManager {

    private final UserConfigRepository repository;
    private final FakeplayerConfig config;
    private final FakeplayerList fakeplayerList;

    @Inject
    public FakeplayerFeatureManager(UserConfigRepository repository, FakeplayerConfig config, FakeplayerList fakeplayerList) {
        this.repository = repository;
        this.config = config;
        this.fakeplayerList = fakeplayerList;
    }

    private @NotNull String getDefaultOption(@NotNull Feature key) {
        return Optional.ofNullable(config.getDefaultFeatures().get(key)).filter(option -> key.getOptions().contains(option)).orElse(key.getDefaultOption());
    }

    public @NotNull FeatureInstance getFeature(@NotNull Player player, @NotNull Feature key) {
        if (!key.testPermissions(player)) {
            return new FeatureInstance(key, this.getDefaultOption(key));
        }

        String value = Optional.ofNullable(repository.selectByPlayerIdAndKey(player.getUniqueId(), key))
                               .map(UserConfig::value)
                               .orElseGet(() -> this.getDefaultOption(key));

        return new FeatureInstance(key, value);
    }

    public @NotNull Map<Feature, FeatureInstance> getFeatures(@NotNull CommandSender sender) {
        var playerId = sender instanceof Player player ? player.getUniqueId() : null;
        return this.getFeatures(sender, this.getUserConfigs(playerId));
    }

    public @NotNull Map<Feature, UserConfig> getUserConfigs(@Nullable UUID playerId) {
        if (playerId == null) {
            return Collections.emptyMap();
        }
        return repository.selectByPlayerId(playerId).stream().collect(Collectors.toMap(UserConfig::key, Function.identity()));
    }

    public @NotNull Map<Feature, FeatureInstance> getFeatures(
            @NotNull CommandSender sender,
            @NotNull Map<Feature, UserConfig> userConfigs
    ) {
        var configs = new LinkedHashMap<Feature, FeatureInstance>(Feature.values().length, 1.0F);
        for (var key : Feature.values()) {
            String value;
            if (!key.testPermissions(sender)) {
                value = this.getDefaultOption(key);
            } else {
                value = Optional.ofNullable(userConfigs.get(key)).map(UserConfig::value).orElseGet(() -> this.getDefaultOption(key));
            }
            configs.put(key, new FeatureInstance(key, value));
        }

        return configs;
    }

    public void setFeature(@NotNull Player player, @NotNull Feature key, @NotNull String value) {
        this.repository.saveOrUpdate(new UserConfig(
                null,
                player.getUniqueId(),
                key,
                value
        ));

        // 同时实时应用到该创建者已生成的在线假人，避免修改配置后需重新生成才生效
        if (key.hasModifier()) {
            for (var fake : this.fakeplayerList.getAll()) {
                if (fake.getCreator() instanceof Player creator && creator.getUniqueId().equals(player.getUniqueId())) {
                    key.getModifier().accept(fake.getPlayer(), value);
                }
            }
        }
    }

}
