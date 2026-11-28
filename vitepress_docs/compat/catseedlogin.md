# CatSeedLogin

检测到 CatSeedLogin 启用后，插件通过其 API 把假人写入已登录集合。

## 行为

- 不执行 `/login` 或 `/reg` 命令，也不需要给假人设置密码。
- 假人生成后自动处于已登录状态，可以正常移动和交互。
- 假人退出时清理对应兼容标记，避免残留。

## 配置

无需配置。若你同时启用了 `auto-login.command`，两套机制互相独立，API 免登录优先；一般不需要再开命令模板。

## 排查

如果假人仍被登录插件拦截：

1. 确认 CatSeedLogin 已启用，且 `plugin.yml` 的 `softdepend` 顺序未被破坏。
2. 查看启动日志中是否有兼容加载失败的信息。
3. 临时改用 [命令模板自动登录](/guide/config/config-advanced) 兜底。
