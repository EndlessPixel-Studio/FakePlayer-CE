# fp.build22 — 2026-11-28

## 主要功能

- **按 TPS 动态调整假人上限**：新增 `tps-adaptive` 段，可配置检测间隔、阈值与最低上限；TPS 低时按周期收紧玩家上限并移除最新召唤的超额假人。旧的 `kale-tps` 仍能作为阈值兼容读取。`config.yml` 版本 39。
- **新增 `/fp update config`**：把模板新增、当前配置缺失的项连同注释补进 `config.yml`，不改已有取值，改写前备份为 `config.yml.bak`。
- **`/fp version` 显示构建 commit**：便于区分版本号相同的正式包与 CI 测试包。

## 修复

- AI 回复中的行内 Markdown 样式在链接处不再断开，收尾标记不再漏进聊天栏。
- `/fp kick` 可以移除插件已丢失记录的幽灵假人。
- `/fp rename ... force` 校验失败时不再误删占名假人。
- 阻塞式调度助手增加超时，避免主线程永久挂起。
- 恢复快照改回主线程读取假人状态。
- HTTP `/teleport` 拒绝 `NaN` 与 `Infinity`。
- TAB 补全对假人名称大小写不敏感。
- `/fp update config` 保留原文件换行符。
- AI 请求发出前的异常不再让假人永久停止发言。
- 修正从服务端目录之外启动时 Velocity 转发漏判（#155）。
