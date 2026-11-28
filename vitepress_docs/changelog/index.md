# 版本变更史

FakePlayer-CE 的每个 `fp.buildN` 对应一个 GitHub Release 和同名 git tag。仓库根目录的 `CHANGELOG.md` 保留完整中英双语条目；本页提供中文完整归档，方便按版本查找破坏性变更。

## 阅读约定

- **破坏性变更**：升级前必须阅读，可能改变命令语义、配置结构或鉴权方式；
- **配置版本**：需要运行 `/fp update config` 补全新项；
- **未发布**：当前 `master` 上尚未进入正式 Release 的内容。

## fp.build23（未发布）

- 修复 `/fp spawn` 世界名解析与权限报错；
- 可选依赖改为按需下载，jar 体积从约 10.3 MiB 降到 2.6 MiB；
- `-a` 覆盖所有名称类命令；
- 修复 `/fp selection` 权限与「只给世界」的召唤；
- 新增按权限设置假人上限（`spawn-limit`，配置版本 42）；
- AI 聊天支持背包上下文（配置版本 41）与多模型；
- 新增 `/fp spawnjson`，支持 JSON 精确生成；
- 新增 `name-suffix`（配置版本 40）；
- 修复 1.21.x 重复移除问题。

## fp.build22（2026-11-28）

- 新增 TPS 自适应上限 `tps-adaptive`（配置版本 39）；
- 新增 `/fp update config`，可安全补齐配置缺失项；
- `/fp version` 显示 commit 短号；
- 修复 AI Markdown 行内样式；
- 修复 `/fp kick` 幽灵假人；
- `rename force` 权限校验；
- HTTP 拒绝 NaN / Infinity；
- TAB 补全大小写不敏感；
- 修复 Velocity 跨目录启动问题。

## fp.build21（2026-11-22）

- 修复 CommandAPI 12.1.0 下 `BadLiteralException` 导致无法启用的问题；
- 新增 `update-ignore-ssl-errors`；
- 新增 `update-download`（配置版本 38）；
- AI 响应支持 Markdown 转换；
- 假人可被打伤（26.1+ / 1.21.9+）；
- HTTP 增加 CORS 配置。

## fp.build20（2026-11-20）

- 帮助文本跟随客户端语言；
- 清理登录兼容层残留；
- 新增 `/fp autosleep`；
- 新增 MySQL 与 `/fp migrate-db`（配置版本 35）；
- HTTP 后端改为 Undertow；
- 新增「看向最近实体」；
- 发包异常兜底；
- 新增 `/fp rename`。

## fp.build19（2026-11-14）

- 完善 Velocity 检测（`paper-global.yml`）；
- 新增 `/fp lang`；
- 补全菜单说明；
- 修复 EasyBot 大小写问题；
- 新增击退处理；
- 移除即写快照；
- AI 请求增加重试。

## fp.build18（2026-11-06）

- 修复 build17 无法启用的问题，改用 `/fp gotostop`；
- 新增周期化更新检查 `update-check-interval-hours`，默认 24 小时（配置版本 34）。

## fp.build17（2026-11-06）

- 新增 `/fp sysinfo`、`/fp version`、`/fp update check`；
- 日语本地化（配置版本 33）；
- 文档四语化并发布到 Hangar；
- 新增 `/fp goto stop`；
- 修复 `-a` 对 OP 无效、LibreLogin 异步授权、生成时被传走、误操作他人假人、HTTP 线程和 `say` 消息丢失等问题。

## fp.build16（2026-10-31）

- 新增 `simulation_distance`（配置版本 29）；
- 恢复动作与特性快照；
- 新增 `death-message`（配置版本 30）；
- 新增 `/fp aichat` 与自动接话；
- HTTP 新增 `/aichat`（配置版本 31）；
- 新增 `/fp spawnuuid`；
- 新增成就支持（配置版本 32）。

## fp.build15（2026-10-24）

- SkinsRestorer 支持正版名；
- 自动登录配置归入 `auto-login`；
- HTTP 新增 5 个端点；
- 发布渠道统一到 GitHub Releases；
- 新增 `share-fakeplayer`（配置版本 28）；
- 修复 Nexo 共载译文问题；
- `/fp goto` 改为寻路实现。

## fp.build14（2026-10-16）

- 要求 Java 25；
- 无敌模式真正免伤，默认改回开启（配置版本 25）；
- 新增 `/fp help <子命令>`；
- 修复重复退出事件。

## fp.build13（2026-10-03）

- 支持 MC 26.3，需 CommandAPI 12.1.0+；
- 移除 `allow-commands`；
- 更新检查改查本仓库；
- 新增 `custom-ping`；
- 配置重组（配置版本 24）；
- `/fp config` 支持控制台；
- 修复 `/fp say` 一系列问题。

## fp.build12（2026-09-26）

- Folia 支持；
- CatSeedLogin / AuthMe / LibreLogin 登录兼容；
- `/fp say` 改走聊天事件；
- 新增 `follow-quiting-force`（配置版本 22）；
- 修复 AuthMe 短密码与 CatSeedLogin 类位置。

## fp.build11（2026-09-23）

- 新增 `/fp enderchest`；
- 扩充占位符；
- HTTP 从 5 个端点扩到 18 个；
- **破坏性**：token 只走请求头；
- HTTP 安全加固；
- 新增 `replace_tools` 与扩展 `replenish`。

## fp.build10（2026-09-12）

- 内置 HTTP 管理接口（5 个端点）；
- **破坏性**：`/fp kill` 改为真实杀死，移除假人改用 `/fp kick`。

## fp.build9（2026-09-11）

- 自动登录；
- 新增 `/fp password` / `/fp changepassword`；
- 随机密码；
- 新增 `forced-execution`；
- 修复占位符扩展标识为 `fakeplayer`。

## fp.build8（2026-09-11）

- `invsee` 权限控制；
- 特权模式复用真人数据；
- 补货前状态校验。

## fp.build7（2026-08-14）

- 修复 Leaf 26.2+ 聊天（未签名消息）；
- `/fp say` 改为动作命令；
- 修复幽灵假人。

## fp.build6（2026-08-13）

- 通过 NMS `PlayerList.remove` 真正移除假人；
- 兼容 Leaf 分支；
- 说明原版 `/kick` 对假人无效。

## fp.build5（2026-08-12）

- 升级 CommandAPI 12.0.0；
- 内置 Adventure；
- 修复 Leaf 上 `/fp spawn` 的 `ClassCastException`。

## fp.build4（2026-08-10）

- 特性改动立即作用于在线假人；
- 补货时修复磨损工具，默认关闭；
- Mohist 兼容。

## fp.build3（2026-08-10）

- 新增 `/fp say`；
- 修复聊天签名、reload / 停用崩溃、NMS 包与 BungeeCord 通道校验。

## fp.build2（2026-07-28）

- 支持 MC 26.1 / 26.2；
- 26.x 需要 Java 25，1.20 / 1.21 仍可用 Java 21+。

## fp.build1（2026-06-19）

- 首个社区版；
- Gradle Kotlin DSL + paperweight userdev（Mojang 映射）；
- 单 jar 覆盖 1.20.1–1.21.11，约 500 KB；
- 每个版本一个 NMS 模块；
- 要求 Java 21+、Paper 1.20.1+、CommandAPI 11.1.0+。
