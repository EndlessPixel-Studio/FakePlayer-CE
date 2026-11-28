# AuthMe

检测到 AuthMe 启用后，插件反射调用其 API 的 `forceLogin` / `forceRegister` 等方法，把假人标记为已登录。

## 行为

- 不需要给假人配置密码，也不用执行登录命令。
- 兼容逻辑会适配不同 AuthMe 版本的 API 签名。
- 假人退出时清理登录状态。

## 配置

无需配置。若你的 AuthMe 版本较新或做了改造，API 反射失败时可在日志中看到告警，此时改用 [命令模板自动登录](/guide/config/config-advanced) 兜底。

::: tip
不要把假人设为 OP。假人执行控制台命令时权限等同该假人。
:::
