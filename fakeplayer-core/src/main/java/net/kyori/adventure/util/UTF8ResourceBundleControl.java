package net.kyori.adventure.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

/**
 * UTF-8 capable {@link ResourceBundle.Control}.
 *
 * <p>Older versions of adventure shipped this class; newer versions (4.x) removed it in favour of
 * {@link UTF8ResourceBundle}. Devtools still references it at runtime, so we re-provide it here to
 * stay compatible with servers that ship adventure 4.x (e.g. Leaf 26.2) without shading a whole
 * adventure build.</p>
 */
public class UTF8ResourceBundleControl extends ResourceBundle.Control {

    @Override
    public ResourceBundle newBundle(
        final String baseName,
        final Locale locale,
        final String format,
        final ClassLoader loader,
        final boolean reload
    ) throws IllegalAccessException, InstantiationException, IOException {
        final String bundleName = toBundleName(baseName, locale);
        final String resourceName = toResourceName(bundleName, "properties");
        ResourceBundle bundle = null;
        InputStream stream = null;

        if (reload) {
            final URL url = loader.getResource(resourceName);
            if (url != null) {
                final URLConnection connection = url.openConnection();
                if (connection != null) {
                    connection.setUseCaches(false);
                    stream = connection.getInputStream();
                }
            }
        } else {
            stream = loader.getResourceAsStream(resourceName);
        }

        if (stream != null) {
            try (final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                bundle = new PropertyResourceBundle(reader);
            }
        }

        return bundle;
    }
}
