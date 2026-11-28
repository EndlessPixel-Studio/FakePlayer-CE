package io.github.endlesspixel.fakeplayer.core.util.library;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

/** 只对可选依赖自身使用子优先加载, 其余类仍交给插件类加载器 */
final class LibraryClassLoader extends URLClassLoader {

  private final Set<String> prefixes;
  private final Set<String> exactClasses;

  LibraryClassLoader(
      @NotNull URL[] urls,
      @NotNull ClassLoader parent,
      @NotNull Set<String> prefixes,
      @NotNull Set<String> exactClasses) {
    super(urls, parent);
    this.prefixes = prefixes;
    this.exactClasses = exactClasses;
  }

  private boolean childFirst(@NotNull String name) {
    if (this.exactClasses.contains(name)) {
      return true;
    }
    for (var prefix : this.prefixes) {
      if (name.startsWith(prefix + ".")) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected Class<?> loadClass(@NotNull String name, boolean resolve)
      throws ClassNotFoundException {
    if (!this.childFirst(name)) {
      return super.loadClass(name, resolve);
    }
    var loaded = this.findLoadedClass(name);
    if (loaded == null) {
      try {
        loaded = this.findClass(name);
      } catch (ClassNotFoundException e) {
        loaded = super.loadClass(name, false);
      }
    }
    if (resolve) {
      this.resolveClass(loaded);
    }
    return loaded;
  }
}
