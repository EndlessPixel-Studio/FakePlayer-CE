# fp.build21 — 2026-11-22

## 修复与兼容

- **修复 CommandAPI 12.1.0 下插件无法启用（#150）**：`/fp rename` 的可选 `force` 参数缺少字面量，注册命令时抛 `BadLiteralException`；现已按正确方式传入。
- **更新检查与下载可跳过证书校验**：`update-ignore-ssl-errors: true` 只对这两个请求跳过 CA 与主机名校验，默认关闭。
- **更新包可自动下载**：`update-download: true` 时下载到 `plugins/update/`，由 Paper 系核心在重启时替换。`config.yml` 版本 38。

## 功能改进

- **AI 回复 Markdown 转换**：加粗/斜体/删除线/行内代码映射成 Adventure 样式，标题转加粗，列表转 `•`，链接可点击；私聊模板不再让模型输出注入 MiniMessage 标签。
- **假人可被攻击、会掉血**：26.1+ 与 1.21.9 让假人主动报告客户端已加载；1.21.11 暂无对应钩子。
- **HTTP 管理接口支持 CORS**：`http-admin.cors.enabled` 默认关闭；开启后可配置allowed-origins 并应答预检请求。
