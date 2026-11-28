# 联动插件

FakePlayer-CE 通过软依赖与运行时反射与常见生态插件联动。所有软依赖都是可选的：装了就自动启用，没装也不影响核心功能。

## 1. 依赖概览

| 插件 | 作用 | 是否需配置 |
|---|---|---|
| CommandAPI | 命令注册、参数解析、TAB 补全 | 必需 |
| PlaceholderAPI | `%fakeplayer_*%` 变量 | 否 |
| OpenInv | 增强 `/fp invsee` / `/fp enderchest` | 否 |
| SkinsRestorer | `/fp skin` 支持正版名 | 否 |
| CatSeedLogin | 假人免登录 | 否 |
| AuthMe / AuthMeReloaded | 假人免登录 | 否 |
| LibreLogin / LibreLoginNext | 假人免登录（Paper） | 否 |
| Velocity | Ping 透传与跨服识别 | 视代理配置 |

`plugin.yml` 中这些是 `softdepend`，不是 `depend`。插件只在目标插件存在且已启用时执行兼容逻辑。

## 2. 登录插件免登录

### 2.1 统一生命周期

`LoginCompatManager` 按固定顺序注册 4 个兼容层：

1. CatSeedLogin；
2. AuthMe；
3. LibreLogin；
4. LibreLoginNext。

调用时机：

| 阶段 | 方法 | 说明 |
|---|---|---|
| 预登录前 | `prepare` | 失败则中止生成并报错 |
| 预登录后 | `afterPreLogin` | 同步缓存，满足后续事件假设 |
| 加入服务器后 | `exempt` | 在 `post-spawn-commands` 前授予登录态 |
| 退出时 | `cleanup` | 清理临时账号/登录态，在 `finally` 中执行 |
| 插件禁用时 | `cleanupAll` | 等待所有待处理清理任务 |

### 2.2 CatSeedLogin

- 通过目标插件自身的类加载器反射 `LoginPlayerHelper.add(LoginPlayer)`；
- 构造 `new LoginPlayer(name, "")`，把假人写入登录名单；
- 不执行命令，不需要密码；
- 兼容原版、ReCatSeedLogin ≥2.0.0 及旧 fork；
- 启动时若检测到 `Config$Settings.CanTpSpawnLocation=true`，会警告该设置会把假人传送到 CatSeedLogin 出生点，与 `/fp spawn` 指定位置冲突。

反射失败只记警告，不会禁用插件。

### 2.3 AuthMe / AuthMeReloaded

- 反射 `AuthMeApi.getInstance()`；
- 已注册：`forceLogin(fake)`；
- 未注册：用 `SecureRandom` 生成 16 位字母数字密码，`forceRegister(fake, password, true)`；
- 兼容旧签名：无三参重载时退化为两参 `forceRegister` + `forceLogin`；
- 若 `isAuthenticated` 仍为假，再补一次 `forceLogin`；
- 假人退出时清理登录状态。

16 位长度是为了避开 AuthMe 默认 `passwordMaxLength=30` 对过长密码的静默拒绝。

### 2.4 LibreLogin / LibreLoginNext

同一个适配器通过不同包名和 provider 类同时支持两者：

- `xyz.kyngs.librelogin` / `getLibreLogin`；
- `xyz.miguvt.libreloginnext` / `getLibreLoginNext`。

流程：

1. `prepare`：查数据库用户；不存在时用假人 UUID 或随机 UUID 创建未注册用户，记入 `createdUsers`；
2. `afterPreLogin`：把用户对象镜像到 Paper 监听器的 `readOnlyUserCache`；
3. `exempt`：在假人所属实体线程同步授权，避免 FastLogin 之类插件看到「已登录却未授权」；
4. `cleanup`：仅当用户名未变且 `isRegistered=false` 时删除，避免误删真人账号；
5. `cleanupAll`：等待所有清理任务完成。

日志前缀是 `LibreLogin compatibility (<插件名>):`。

### 2.5 反射失败时的兜底

如果登录插件 API 变动导致反射失败，可以改用命令模板登录（见 [配置完全手册](/guide/config/)）：

```yaml
auto-login:
  command:
    enabled: true
    register: /reg %password% %password%
    login: /login %password%
```

密码用 `/fp password` 设置，或配置 `random-password.charset` 自动生成。

## 3. SkinsRestorer

`/fp skin <player> [name]` 在检测到 SkinsRestorer 时可以使用正版名作为皮肤来源。没有 SkinsRestorer 时使用插件自身的皮肤缓存逻辑。

::: warning
不要把假人设为 OP。皮肤/登录兼容层会在服务端以玩家身份执行部分逻辑，过高权限会带来安全风险。
:::

## 4. PlaceholderAPI

扩展标识固定为 `fakeplayer`。全部变量见 [占位符变量](/placeholders)。插件禁用时会自动注销扩展，不会残留失效占位符。

## 5. OpenInv 与背包查看

- `invsee-implement: AUTO`：自动检测 OpenInv；
- `invsee-implement: SIMPLE`：使用内置实现，不能查看/编辑装备栏；
- 创建者本人默认可以查看自己假人的背包；
- 其他人需要 `fakeplayer.command.invsee`（或 `enderchest`）权限，或开启 `allow-non-op-open-inv`。

## 6. Velocity / 代理联动

- `paper-global.yml` 中的 Velocity 检测用于识别代理转发；
- `custom-ping` 可模拟 Tab 列表延迟；
- `ping-passthrough: "ALL"` 之类的代理配置会影响延迟显示；
- 跨目录启动时需要注意代理密钥文件位置。

## 7. 聊天事件兼容

`/fp say` 同时触发 `AsyncPlayerChatEvent` 与 `AsyncChatEvent`，提高与其他聊天插件的兼容性。假人自己的发言不会触发 AI 自动接话，避免自问自答。

## 8. 联动排错清单

| 现象 | 排查方向 |
|---|---|
| 登录插件仍然踢假人 | 检查兼容层启动日志、`prevent-kicking`、插件是否真的启用 |
| CatSeedLogin 把假人传走 | 关闭 `CanTpSpawnLocation` |
| AuthMe 未注册成功 | 检查 API 版本；必要时用命令模板 |
| LibreLogin 显示已登录但未授权 | 确认没有 FastLogin 之类的插件干扰同步授权 |
| `/fp skin` 找不到皮肤 | 检查 SkinsRestorer 是否启用、名称是否正确 |
| 占位符不生效 | 检查 PlaceholderAPI 与扩展标识 `fakeplayer` |
| 背包查看被拒 | 同世界要求、创建者归属、`allow-non-op-open-inv` |
