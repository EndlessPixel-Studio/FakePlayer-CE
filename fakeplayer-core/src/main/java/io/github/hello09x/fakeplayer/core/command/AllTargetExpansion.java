package io.github.hello09x.fakeplayer.core.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.commandsenders.AbstractCommandSender;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import dev.jorel.commandapi.executors.ExecutionInfo;
import dev.jorel.commandapi.executors.ExecutorType;
import dev.jorel.commandapi.executors.NormalExecutor;
import dev.jorel.commandapi.executors.ResultingExecutor;
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/** name 参数为 -a 时, 把命令按发送者可见的假人逐个执行 */
@Singleton
public class AllTargetExpansion {

  private static final Set<String> EXCLUDED = Set.of("select", "spawn", "spawnuuid", "rename");

  private final FakeplayerManager manager;

  @Inject
  public AllTargetExpansion(FakeplayerManager manager) {
    this.manager = manager;
  }

  public void apply(@NotNull CommandAPICommand root) {
    this.walk(root);
  }

  private void walk(@NotNull CommandAPICommand command) {
    if (!EXCLUDED.contains(command.getName())
        && command.getArguments().stream().anyMatch(arg -> arg.getNodeName().equals("name"))) {
      this.wrap(command);
    }
    for (var sub : command.getSubcommands()) {
      this.walk(sub);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void wrap(@NotNull CommandAPICommand command) {
    var executor = command.getExecutor();
    if (executor == null) {
      return;
    }
    var normal = new ArrayList<NormalExecutor>();
    for (var delegate : executor.getNormalExecutors()) {
      normal.add(new NormalWrapper(delegate, this));
    }
    if (!normal.isEmpty()) {
      executor.setNormalExecutors((List) normal);
    }
    var resulting = new ArrayList<ResultingExecutor>();
    for (var delegate : executor.getResultingExecutors()) {
      resulting.add(new ResultingWrapper(delegate, this));
    }
    if (!resulting.isEmpty()) {
      executor.setResultingExecutors((List) resulting);
    }
  }

  private @NotNull List<ExecutionInfo> expanded(@NotNull ExecutionInfo info) {
    var args = info.args();
    if (!"-a".equals(args.getRaw("name"))) {
      return List.of(info);
    }
    var sender = (CommandSender) info.sender();
    var targets = sender.isOp() ? this.manager.getAll() : this.manager.getAll(sender);
    var original = args.get("name");
    var expanded = new ArrayList<ExecutionInfo>(targets.size());
    for (var fake : targets) {
      var values = new LinkedHashMap<>(args.argsMap());
      values.put("name", original instanceof String ? fake.getName() : fake.getPlayer());
      var raw = new LinkedHashMap<>(args.rawArgsMap());
      raw.put("name", fake.getName());
      expanded.add(
          new SimpleExecutionInfo(
              info.sender(),
              (AbstractCommandSender<?>) info.senderWrapper(),
              new CommandArguments(args.args(), values, args.rawArgs(), raw, args.fullInput())));
    }
    return expanded;
  }

  @SuppressWarnings("rawtypes")
  private static final class NormalWrapper implements NormalExecutor {

    private final NormalExecutor delegate;
    private final AllTargetExpansion owner;

    private NormalWrapper(NormalExecutor delegate, AllTargetExpansion owner) {
      this.delegate = delegate;
      this.owner = owner;
    }

    @Override
    public void run(ExecutionInfo info) throws WrapperCommandSyntaxException {
      for (var expanded : this.owner.expanded(info)) {
        this.delegate.run(expanded);
      }
    }

    @Override
    public ExecutorType getType() {
      return this.delegate.getType();
    }
  }

  @SuppressWarnings("rawtypes")
  private static final class ResultingWrapper implements ResultingExecutor {

    private final ResultingExecutor delegate;
    private final AllTargetExpansion owner;

    private ResultingWrapper(ResultingExecutor delegate, AllTargetExpansion owner) {
      this.delegate = delegate;
      this.owner = owner;
    }

    @Override
    public int run(ExecutionInfo info) throws WrapperCommandSyntaxException {
      var total = 0;
      for (var expanded : this.owner.expanded(info)) {
        total += this.delegate.run(expanded);
      }
      return total;
    }

    @Override
    public ExecutorType getType() {
      return this.delegate.getType();
    }
  }

  private record SimpleExecutionInfo(
      Object sender, AbstractCommandSender<?> senderWrapper, CommandArguments args)
      implements ExecutionInfo<Object, AbstractCommandSender<?>> {}
}
