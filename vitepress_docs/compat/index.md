# 联动插件

FakePlayer-CE 对登录插件、皮肤插件、PlaceholderAPI 和外部 Java API 提供兼容。

## 登录插件

安装下列插件后，兼容逻辑自动启用，无需配置：

- [CatSeedLogin](/compat/catseedlogin)
- [AuthMe](/compat/authme)
- [LibreLogin / LibreLoginNext](/compat/librelogin)

这些兼容通过**插件 API** 把假人标记为已登录，不执行命令、不需要密码。若登录插件没有兼容，可改用 [命令模板自动登录](/guide/config/config-advanced)。

## 其他联动

| 插件 / 功能 | 说明 |
|---|---|
| SkinsRestorer | 安装后 `/fp skin` 走其 API 设置并应用皮肤 |
| PlaceholderAPI | 提供 `%fakeplayer_*%` 占位符，见 [占位符变量](/placeholders) |
| OpenInv | `invsee-implement: AUTO` 时自动使用其背包实现 |
| Velocity | 代理服同步人数与 MOTD 时可设置 `ping-passthrough: "ALL"` |
| CommandAPI | 必需前置，提供命令注册与参数解析 |

## 聊天事件兼容

同时监听 `AsyncPlayerChatEvent` 与 `AsyncChatEvent`，兼容不同版本的聊天插件。假人自己的发言不会触发自动接话，避免自问自答。
