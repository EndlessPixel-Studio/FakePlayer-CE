package io.github.hello09x.fakeplayer.core.command;

import dev.jorel.commandapi.CommandAPICommand;
import io.github.hello09x.devtools.core.translation.TranslatorUtils;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/** Resolves command help text once with the locale configured by the plugin. */
final class CommandHelpLocalizer {

    private static final String BUNDLE_NAME = "message.message";
    private static final ResourceBundle.Control CONTROL =
            ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    private final Locale locale;
    private final ClassLoader dataFolderClassLoader;
    private final ClassLoader pluginClassLoader;
    private final ClassLoader[] classLoaders;

    CommandHelpLocalizer(Plugin plugin, Locale locale) {
        this.locale = locale;
        this.dataFolderClassLoader = TranslatorUtils.getDataFolderClassLoader(plugin);
        this.pluginClassLoader = TranslatorUtils.getJarClassLoader(plugin);
        this.classLoaders = new ClassLoader[]{dataFolderClassLoader, pluginClassLoader};
    }

    String translate(String key) {
        for (var classLoader : classLoaders) {
            try {
                var bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale, classLoader, CONTROL);
                if (bundle.containsKey(key)) {
                    return bundle.getString(key);
                }
            } catch (MissingResourceException ignored) {
                // Try the next resource location, then leave unknown keys unchanged.
            }
        }
        return key;
    }

    void localizeDescriptions(CommandAPICommand command) {
        for (var subcommand : command.getSubcommands()) {
            var description = subcommand.getShortDescription();
            if (description != null && !description.isEmpty()) {
                subcommand.withShortDescription(translate(description));
            }
            localizeDescriptions(subcommand);
        }
    }
}
