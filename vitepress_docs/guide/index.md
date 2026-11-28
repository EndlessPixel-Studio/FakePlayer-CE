# 入门指南

这一页把「插件能做什么、支持什么环境、怎么装、装完怎么验证、出问题先看哪里」一次讲清楚。命令和配置的完整细节分别见 [命令完全手册](/commands/) 与 [配置完全手册](/guide/config/)。

## 1. 这个插件解决什么问题

FakePlayer-CE 是 `tanyaofei/minecraft-fakeplayer` 的社区维护分支。它在服务端创建一个「对服务器来说就是真实在线玩家」的实体：

- 参与区块加载与模拟，可用于让农场、刷石机、刷怪塔常驻运行；
- 可以执行游戏内命令、说话、走路、挖矿、攻击、钓鱼、睡觉、看向实体等动作；
- 可以接入 AI 聊天、登录插件、PlaceholderAPI、SkinsRestorer、OpenInv 等生态；
- 提供 HTTP 管理接口，供 Web 面板或自动化脚本调用；
- 支持 Folia，也支持 Bukkit/Paper/Spigot/Purpur/Leaf 等 Paper 系核心。

它不是客户端 Mod，也不是代理；所有逻辑都在服务端完成。假人没有真实玩家账号，也没有真实的客户端连接。

::: tip
假人是否「像真人」，取决于服务端、插件和版本。它本质上是一个经过 NMS 层特殊构造的玩家实体，因此部分依赖客户端包的插件会有兼容边界。
:::

## 2. 运行环境

| 项目 | 要求 |
|---|---|
| 服务端核心 | Paper / Purpur / Leaf / Spigot 等 Paper 系核心；Folia 可用 |
| Minecraft | 1.20.1 ~ 26.3 |
| Java | Java 25（1.20.x / 1.21.x 的部分旧构建可用 Java 21+，但当前构建以 25 为准） |
| 硬依赖 | CommandAPI（除 `10.0.0` 外多数版本可用；MC 26.3 需要 12.1.0+） |
| 软依赖 | OpenInv、PlaceholderAPI、CatSeedLogin、AuthMe、LibreLogin、LibreLoginNext、SkinsRestorer |
| 可选依赖 | Undertow、MySQL 驱动，按功能启用时自动下载 |

`plugin.yml` 声明 `api-version: 1.20`、`folia-supported: true`。仓库中 `versions/` 下有 22 个 NMS 版本模块，单个 jar 覆盖全部受支持版本，无需为不同 MC 版本换包。

### 2.1 为什么必须装 CommandAPI

插件的命令注册、参数类型、TAB 补全与帮助生成都依赖 CommandAPI。缺少它时插件无法完成注册，启动时会出现依赖缺失错误。不要把 CommandAPI 当成本插件自带组件；它是独立的 `plugins/` jar。

### 2.2 可选依赖的下载机制

下面两类依赖不打包进主 jar，以减小体积：

- `database.type: mysql` → 自动下载 MySQL 驱动；
- `http-admin.enabled: true` → 自动下载 Undertow。

下载位置是 `plugins/FakePlayer-CE/libs/<groupId>/<artifactId>/<version>/`。默认仓库为 Maven Central，可用 JVM 参数更换镜像：

```bash
-DLeaf.library-download-repo=https://maven.aliyun.com/repository/public
```

下载器只接受 `http`/`https` URL，校验 jar 首字节 `PK`，先写 `.part` 再原子移动，路径被限制在 `libs/` 内。下载失败不会禁用整个插件，但对应功能（HTTP 或 MySQL）会报错。

## 3. 安装步骤

1. 从 GitHub Releases、Hangar 项目页，或 GitHub Actions 成功运行的 `fakeplayer-dist` 制品下载 jar。
2. 把 CommandAPI jar 放进 `plugins/`。
3. 把 `fakeplayer-fp.buildX.jar` 放进 `plugins/`。
4. 启动一次服务器。插件会生成 `plugins/FakePlayer-CE/config.tmpl.yml`。
5. 把 `config.tmpl.yml` 复制或改名为 `config.yml`，按需要修改。
6. 重启服务器，或使用 `/fp reload` 重载；数据库类型、HTTP 端口等改动仍需重启。
7. 在游戏里执行 `/fp spawn`，验证假人是否生成。

::: warning
首次启动只生成模板 `config.tmpl.yml`，**模板本身不生效**。必须改名/复制为 `config.yml`。这样设计是为了升级时让你看到新增配置项，而不覆盖已有设置。
:::

### 3.1 目录结构

```text
plugins/
├── CommandAPI.jar
└── FakePlayer-CE/
    ├── config.tmpl.yml       # 模板，升级时更新
    ├── config.yml            # 实际生效的配置
    ├── ai-chat.yml           # AI 聊天配置，首次使用后生成
    ├── data.db               # SQLite 数据库（默认）
    ├── libs/                 # 按需下载的 Undertow / MySQL 驱动
    ├── snapshots/            # 崩溃恢复快照
    └── sysinfo/              # /fp sysinfo 输出
```

## 4. 升级与配置迁移

### 4.1 常规升级

直接覆盖插件 jar 即可。配置项新增时，模板会变化，但你的 `config.yml` 不会被覆盖。

用 OP 执行：

```text
/fp update config
```

它会：

- 对比 jar 内模板与现有 `config.yml`，只补「最浅层」缺失键；
- 保留已有值、注释和换行符；
- 把 `version` 更新到模板版本；
- 改写前备份为 `config.yml.bak`；
- 遇到结构冲突只报告，不覆盖；
- 写入前做 YAML 合法性与键存在性校验，失败则一个字都不写。

执行后按提示运行 `/fp reload`。

### 4.2 版本检查与自动下载

```text
/fp update check       # 检查 GitHub 上的 fp.buildN
/fp update config      # 补齐配置
```

`update-download: true` 时，新版本会下载到 `plugins/update/`，由 Paper 系核心下次启动替换。`update-ignore-ssl-errors: true` 会跳过证书校验，有中间人风险，默认关闭。

## 5. 创建第一个假人

假设默认配置，进入游戏后依次执行：

```text
/fp spawn
/fp list
/fp status
/fp say once 你好，世界
/fp stop
```

实际行为：

- `/fp spawn` 未给名字时，按 `name-template` / `name-prefix` / `name-suffix` 生成，默认通常是「创建者名_序号」；
- `/fp list` 列出假人；OP 看全服，普通玩家看自己创建的；
- `/fp status` 显示生命、饥饿、经验、模式和当前特性；
- `/fp say once ...` 让假人发一条消息；
- `/fp stop` 停止它正在进行的动作。

### 5.1 目标的选取规则

绝大多数命令都可以省略假人名字。解析优先级是：

1. 显式写了名字 → 用这个名字；
2. 没写名字 → 用 `/fp select` 选中的假人；
3. 还没选 → 如果你恰好只有 1 个假人，自动用它；
4. 0 个 → 提示没有假人；多个 → 提示需要指定名字。

::: warning
显式写了名字但没匹配到时，插件**不会**退回选中假人或唯一假人，而是直接报错。这是为了防止打错名字误操作别的假人。
:::

### 5.2 批量操作 `-a`

以假人名为参数的命令支持 `-a`，表示对发送者可见的所有假人各执行一次：

- OP / 控制台：全服假人；
- 普通玩家：自己创建的假人。

不支持 `-a` 的命令：`select`、`spawn`、`spawnuuid`、`rename`。

### 5.3 动作类命令的通用形态

`attack`、`mine`、`use`、`jump`、`drop`、`dropstack`、`dropinv` 等动作命令都支持：

```text
/fp attack            # 执行一次
/fp attack once       # 执行一次
/fp attack continuous # 每 tick 执行
/fp attack interval 10 # 每 10 tick 执行
/fp attack stop       # 停止该动作
```

不带子命令等价于 `once`。玩家执行 `once` 时通常没有成功提示，因为动作本身可见。

## 6. 卸载与数据

- 直接删除插件 jar 与 `plugins/FakePlayer-CE/` 目录即可卸载；
- SQLite 数据在 `data.db`，MySQL 数据在配置的数据库中；
- 想从 SQLite 迁到 MySQL：改 `database.type: mysql` 并填连接信息，重启后执行 `/fp migrate-db`（OP）。迁移是异步的，原 SQLite 文件保留；
- `config.yml` 不会随 jar 删除，除非你手动删除整个插件目录。

## 7. 首次部署检查清单

- CommandAPI 是否已安装且版本合适；
- `config.yml` 是否由模板改名而来，而不是只有 `config.tmpl.yml`；
- 玩家是否有 `fakeplayer.command.spawn` 权限；
- `player-limit` 是否为期望值（默认 1）；
- `follow-quiting` 是否会导致创建者下线后假人消失（默认 true）；
- 是否启用了登录插件联动（见 [联动插件](/compat/)）；
- 是否真的需要 HTTP 接口；如需要，请务必改 `host` 并设 `token`（见 [HTTP 管理接口](/http/)）。

## 8. 常见问题

<details>
<summary>执行 /fp spawn 提示没有权限？</summary>

根命令需要 `fakeplayer.command.spawn`。检查权限组插件配置。`fakeplayer.spawn` 聚合组包含常用生成/查看/管理权限，但不包含 OP 级命令。

</details>

<details>
<summary>假人为什么创建者一上线就被踢？</summary>

检查 `prevent-kicking`。默认 `ON_SPAWNING` 只在创建时防踢；如果登录插件在下一 tick 才踢人，需要改成 `ALWAYS`。

</details>

<details>
<summary>为什么 /fp respawn 用不了？</summary>

默认 `kick-on-dead: true`，假人死亡时会被移除，因此 `respawn` 守卫生效前就不可用了。要先关闭 `kick-on-dead`。

</details>

<details>
<summary>为什么普通玩家用不了 /fp select？</summary>

`needSelect` 要求 OP，或玩家的上限大于 1 且已经有假人。默认 `player-limit: 1` 时普通玩家不满足。

</details>

<details>
<summary>插件启动报缺少 Undertow / MySQL 驱动？</summary>

检查 `plugins/FakePlayer-CE/libs/` 是否有下载失败残留，确认服务器能访问 Maven Central（或配置的镜像），并检查 JVM 参数 `-DLeaf.library-download-repo`。

</details>

## 9. 接下来读哪里

- [配置完全手册](/guide/config/)：每一个配置项、默认值、依赖与踩坑点；
- [命令完全手册](/commands/)：完整命令树与目标选择规则；
- [HTTP 管理接口](/http/)：25 个端点、鉴权、限流与安全边界；
- [占位符变量](/placeholders)：所有 `%fakeplayer_*%` 变量；
- [联动插件](/compat/)：登录插件、皮肤、背包查看等生态；
- [版本变更史](/changelog/)：每个构建版本做了什么、有哪些破坏性变更。
