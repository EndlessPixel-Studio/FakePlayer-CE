package io.github.hello09x.fakeplayer.core.command.impl;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.executors.CommandArguments;
import io.github.hello09x.fakeplayer.core.Main;
import io.github.hello09x.fakeplayer.core.i18n.LangUtils;
import io.github.hello09x.fakeplayer.core.manager.action.ActionManager;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

@Singleton
public class SysInfoCommand extends AbstractCommand {

  private static final Gson gson =
      new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

  private static final DateTimeFormatter FILE_NAME =
      DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

  /** 与堆 / 元空间 / 直接内存相关的启动参数 */
  private static final List<String> MEMORY_FLAGS =
      List.of(
          "-Xms",
          "-Xmx",
          "-Xmn",
          "-Xss",
          "-XX:MaxHeapSize",
          "-XX:InitialHeapSize",
          "-XX:MaxMetaspaceSize",
          "-XX:MetaspaceSize",
          "-XX:MaxDirectMemorySize",
          "-XX:SoftMaxHeapSize",
          "-XX:NewSize",
          "-XX:MaxNewSize");

  @Inject private ActionManager actionManager;

  /** 把 CPU / 内存 / 磁盘 / TPS 与假人状态写进插件目录下的 json 文件, 方便贴到性能问题里 */
  public void sysinfo(@NotNull CommandSender sender, @NotNull CommandArguments args) {
    try {
      var folder = new File(Main.getInstance().getDataFolder(), "sysinfo");
      Files.createDirectories(folder.toPath());

      var file = new File(folder, LocalDateTime.now().format(FILE_NAME) + ".json");
      Files.writeString(file.toPath(), gson.toJson(this.collect()), StandardCharsets.UTF_8);

      LangUtils.send(
          sender, translatable("fakeplayer.command.sysinfo.success", GRAY, text(file.getPath())));
    } catch (Throwable e) {
      LangUtils.send(
          sender,
          translatable(
              "fakeplayer.command.sysinfo.failed", RED, text(String.valueOf(e.getMessage()))));
    }
  }

  private @NotNull Map<String, Object> collect() {
    var root = new LinkedHashMap<String, Object>();
    root.put("generated_at", Instant.now().toString());
    root.put("plugin", map("version", Main.getInstance().getPluginMeta().getVersion()));
    root.put("server", this.server());
    root.put("java", this.java());
    root.put("jvm", this.jvm());
    root.put("cpu", this.cpu());
    root.put("memory", this.memory());
    root.put("disk", this.disk());
    root.put("tick", this.tick());
    root.put("fakeplayers", this.fakeplayers());
    return root;
  }

  private @NotNull Map<String, Object> server() {
    return map(
        "implementation", Bukkit.getName(),
        "version", Bukkit.getServer().getVersion(),
        "bukkit_version", Bukkit.getBukkitVersion(),
        "os", "%s %s".formatted(os("os.name"), os("os.version")).trim());
  }

  private @NotNull Map<String, Object> java() {
    return map(
        "version", os("java.version"),
        "vendor", os("java.vendor"),
        "vendor_version", os("java.vendor.version"),
        "vm", os("java.vm.name"),
        "vm_version", os("java.vm.version"),
        "home", os("java.home"),
        "bits", os("sun.arch.data.model"));
  }

  /**
   * 只挑内存与 GC 相关的启动参数。
   *
   * <p>整份参数里可能带 {@code -D} 形式的密码 / 令牌, 而这个文件是要贴到公开 issue 里的, 所以不做全量导出, 只给出这一类, 其余只报数量。
   */
  private @NotNull Map<String, Object> jvm() {
    var jvm = new LinkedHashMap<String, Object>();

    var args = this.safely(() -> ManagementFactory.getRuntimeMXBean().getInputArguments());
    var all = args == null ? List.<String>of() : List.copyOf(args);

    var memoryFlags = pick(all, flag -> MEMORY_FLAGS.stream().anyMatch(flag::startsWith));
    var gcFlags =
        pick(
            all,
            flag -> flag.contains("GC") || flag.toLowerCase(Locale.ROOT).startsWith("-xlog:gc"));

    var categorized =
        all.stream().filter(flag -> memoryFlags.contains(flag) || gcFlags.contains(flag)).count();

    jvm.put("memory_flags", memoryFlags);
    jvm.put("gc_flags", gcFlags);
    jvm.put("other_arg_count", (int) (all.size() - categorized));
    jvm.put("gc_collectors", this.gcCollectors());

    var uptime = this.safely(() -> ManagementFactory.getRuntimeMXBean().getUptime());
    if (uptime != null) {
      jvm.put("uptime_seconds", uptime / 1000);
    }
    return jvm;
  }

  private @NotNull List<String> gcCollectors() {
    var names = new ArrayList<String>();
    try {
      for (var gc : ManagementFactory.getGarbageCollectorMXBeans()) {
        names.add(gc.getName());
      }
    } catch (RuntimeException ignored) {
      // 拿不到就留空
    }
    return names;
  }

  private static @NotNull List<String> pick(
      @NotNull List<String> args, @NotNull Predicate<String> predicate) {
    return args.stream().filter(predicate).toList();
  }

  private @NotNull Map<String, Object> cpu() {
    var cpu = new LinkedHashMap<String, Object>();
    cpu.put("model", this.cpuModel());
    cpu.put("cores", Runtime.getRuntime().availableProcessors());
    cpu.put("arch", os("os.arch"));

    var load = ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage();
    if (load >= 0) {
      cpu.put("load_average", round(load));
    }
    return cpu;
  }

  /** CPU 型号没有标准 Java API: Linux 读 /proc/cpuinfo, Windows 读环境变量, 都拿不到时退回架构名 */
  private @NotNull String cpuModel() {
    var cpuinfo = Path.of("/proc/cpuinfo");
    if (Files.isReadable(cpuinfo)) {
      try (var lines = Files.lines(cpuinfo)) {
        var model =
            lines
                .filter(line -> line.startsWith("model name") || line.startsWith("Hardware"))
                .map(line -> line.substring(line.indexOf(':') + 1).trim())
                .filter(line -> !line.isEmpty())
                .findFirst();
        if (model.isPresent()) {
          return model.get();
        }
      } catch (IOException | RuntimeException ignored) {
        // 落到下面的兜底
      }
    }

    var identifier = System.getenv("PROCESSOR_IDENTIFIER");
    if (identifier != null && !identifier.isBlank()) {
      return identifier.trim();
    }
    return os("os.arch");
  }

  private @NotNull Map<String, Object> memory() {
    var runtime = Runtime.getRuntime();
    var memory = new LinkedHashMap<String, Object>();
    memory.put("jvm_max_mb", mb(runtime.maxMemory()));
    memory.put("jvm_total_mb", mb(runtime.totalMemory()));
    memory.put("jvm_used_mb", mb(runtime.totalMemory() - runtime.freeMemory()));

    // 物理内存只有 com.sun.management 的实现提供, 精简 JRE 上可能没有
    var bean = ManagementFactory.getOperatingSystemMXBean();
    if (bean instanceof com.sun.management.OperatingSystemMXBean os) {
      try {
        memory.put("system_total_mb", mb(os.getTotalMemorySize()));
        memory.put("system_free_mb", mb(os.getFreeMemorySize()));
      } catch (RuntimeException ignored) {
        // 拿不到就只保留 JVM 数据
      }
    }
    return memory;
  }

  private @NotNull Map<String, Object> disk() {
    var folder = Main.getInstance().getDataFolder();
    var disk = new LinkedHashMap<String, Object>();
    disk.put("path", folder.getAbsolutePath());
    try {
      var store = Files.getFileStore(folder.toPath());
      var total = store.getTotalSpace();
      var free = store.getUsableSpace();
      disk.put("total_gb", gb(total));
      disk.put("used_gb", gb(total - free));
      disk.put("free_gb", gb(free));
    } catch (IOException | RuntimeException e) {
      disk.put("error", String.valueOf(e.getMessage()));
    }
    return disk;
  }

  private @NotNull Map<String, Object> tick() {
    var tick = new LinkedHashMap<String, Object>();

    var tps = this.safely(() -> Bukkit.getServer().getTPS());
    if (tps != null && tps.length >= 3) {
      tick.put("tps_1m", round(tps[0]));
      tick.put("tps_5m", round(tps[1]));
      tick.put("tps_15m", round(tps[2]));
    }

    // Folia 等实现可能不支持平均值, 退回到自己算最近若干 tick 的均值
    var mspt = this.safely(Bukkit.getServer()::getAverageTickTime);
    if (mspt == null) {
      var times = this.safely(Bukkit.getServer()::getTickTimes);
      if (times != null && times.length > 0) {
        mspt = Arrays.stream(times).average().orElse(0) / 1_000_000.0;
      }
    }
    if (mspt != null) {
      tick.put("mspt", round(mspt));
    }
    return tick;
  }

  private @NotNull Map<String, Object> fakeplayers() {
    var items = new ArrayList<Map<String, Object>>();
    for (var player : manager.getAll()) {
      var item = new LinkedHashMap<String, Object>();
      item.put("name", player.getName());

      try {
        var location = player.getLocation();
        var world = location.getWorld();
        item.put("world", world == null ? null : world.getName());
        item.put(
            "location",
            map(
                "x", round(location.getX()),
                "y", round(location.getY()),
                "z", round(location.getZ())));
      } catch (RuntimeException ignored) {
        // 玩家正被移除时读取位置可能失败, 不代表整体失败
      }

      try {
        var settings = actionManager.getSettings(player);
        var actions = new ArrayList<Map<String, Object>>();
        settings.forEach(
            (type, setting) ->
                actions.add(
                    map(
                        "type", type.name(),
                        "remaining", setting.remains,
                        "interval", setting.interval)));
        item.put("busy", !actions.isEmpty());
        item.put("actions", actions);
      } catch (RuntimeException e) {
        item.put("busy", null);
        item.put("actions", List.of());
      }

      items.add(item);
    }
    return map("count", items.size(), "items", items);
  }

  private <T> T safely(@NotNull Supplier<T> supplier) {
    try {
      return supplier.get();
    } catch (Throwable e) {
      return null;
    }
  }

  private static @NotNull String os(@NotNull String key) {
    return Objects.toString(System.getProperty(key), "");
  }

  private static long mb(long bytes) {
    return bytes / 1024 / 1024;
  }

  private static double gb(long bytes) {
    return round(bytes / 1024.0 / 1024 / 1024);
  }

  private static double round(double value) {
    return Math.round(value * 100) / 100.0;
  }

  private static @NotNull Map<String, Object> map(Object... pairs) {
    var map = new LinkedHashMap<String, Object>();
    for (var i = 0; i + 1 < pairs.length; i += 2) {
      map.put(String.valueOf(pairs[i]), pairs[i + 1]);
    }
    return map;
  }
}
