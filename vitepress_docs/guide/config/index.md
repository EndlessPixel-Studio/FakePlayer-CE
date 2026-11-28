# 插件配置文件

插件有两个主要配置文件：

| 文件 | 用途 |
|---|---|
| `config.yml` | 插件核心行为：数量、命名、数据库、特性、命令钩子、更新、HTTP 接口 |
| `ai-chat.yml` | AI 聊天：接口、模型、提示词、上下文与回复节奏 |

首次启动只生成 `config.tmpl.yml` / `ai-chat.yml` 模板，改名后生效。

## config.yml 章节

- [基础设置](/guide/config/config-basic)：数量上限、命名、寿命、跟随下线、IP 共享
- [数据库与依赖](/guide/config/config-database)：SQLite / MySQL、迁移、libs 按需下载
- [特性与性能](/guide/config/config-features)：默认特性、补货、工具替换、TPS 保护
- [高级设置](/guide/config/config-advanced)：命令钩子、自动登录、兼容性、更新
- [AI 聊天配置](/guide/config/ai-chat)：`ai-chat.yml` 全部选项

## 修改后生效

部分配置支持 `/fp reload` 热重载（例如 AI 聊天、TPS 限制），数据库类型和 HTTP 端口等需要重启服务器。配置模板顶部有 `version: N`，新增配置项时会递增；如果日志提示配置过期，可用 `/fp update config` 补齐缺失项。
