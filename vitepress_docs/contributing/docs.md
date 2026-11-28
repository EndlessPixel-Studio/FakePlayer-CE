# 文档与翻译规范

## 文档语言

- 仓库根 README 与 `docs/` 下的社区文档均有四语版本：`README.md` / `_zh` / `_zh_TW` / `_ja`。
- 消息文案有五份：`message.properties` / `_zh` / `_zh_TW` / `_zh_HK` / `_ja`，非英文用 `\uXXXX` 转义。
- 新增配置项或命令时，四语 README 与五语消息要一起改。

## 新文档站

本 Vitepress 站点位于 `vitepress_docs/`，按语言分目录：

```
vitepress_docs/
  index.md          # 简体中文
  guide/
  en/
  zh_TW/
  ja/
```

先写中文，审核后再补其他语言。侧边栏在 `.vitepress/config.mts`，新增页面要同步更新。

## 检查

- 改完 Markdown 要检查相对链接，避免指向不存在的文件。
- `npm run build` 会检查站内死链，提交前跑一次。
- 命令行参数、配置项名称必须与代码一致；不要照抄旧文档。
