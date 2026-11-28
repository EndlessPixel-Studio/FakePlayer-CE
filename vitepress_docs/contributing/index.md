# 贡献指南

欢迎提交 Issue、改进文档、修复 Bug 或实现新功能。本页汇总了代码、提交、文档、翻译、Issue 和 PR 的约定，细节以仓库根目录的 `docs/CONTRIBUTING*.md`、`docs/SECURITY_zh.md` 和 `docs/CODE_OF_CONDUCT*.md` 为准。

## 1. 先决定用 Issue 还是直接 PR

| 场景 | 做法 |
|---|---|
| 报告 Bug、提出需求、讨论设计 | 开 Issue，先搜索去重 |
| 文档错别字、小修、加配置项、单文件修复 | 可以直接推 `master` |
| 较大功能、跨模块改动、行为变化 | 新建分支并开 PR，让 CI 完整跑一遍 |
| 安全问题 | **不要开公开 Issue**，走 GitHub Security 页 *Report a vulnerability* |

### 1.1 Bug 报告应包含

- `fp.buildN` 版本号；
- 服务端核心与 MC 版本；
- Java 版本；
- 精确复现步骤；
- 预期行为与实际行为；
- 相关日志；
- 性能问题附 `/fp sysinfo` 生成的 JSON。

### 1.2 安全问题

附上版本、服务端、复现步骤，以及打码后的配置与日志。不要公开披露可利用细节。

## 2. 分支与 PR 流程

1. 从 `master` 新建分支，例如 `feat/xxx`、`fix/xxx`、`docs/xxx`；
2. 小改动可直接推 `master`，但功能改动建议走 PR；
3. PR 描述写清「改了什么、为什么改、怎么验证」；
4. CI 会自动跑 Build / Check / Format / label；
5. `format.yml` 会在任意分支 push 后自动格式化并以 `github-actions[bot]` 提交回分支；
6. PR 只校验格式，不对 PR 分支自动提交；
7. 合并前确认 CI 全绿。

::: warning
不要跳过 hooks，不要强制推送到 `master`。涉及上游移植时保留原始作者信息。
:::

## 3. 提交规范

前缀使用英文，描述使用中文：

```text
feat: 新增 AI 多模型配置
fix: 修复 /fp spawn 世界名解析
docs: 补齐中文文档内容
refactor: 拆分 HTTP 服务
ci: 调整工作流触发条件
chore: 更新构建脚本
```

- 一个提交只做一件事；
- 拆分功能时同步拆分 CHANGELOG 条目；
- body 中引用 Issue 或上游提交；
- 移植外部提交必须保留 `Co-authored-by: Name <email>`，尖括号不可省略。

## 4. 代码规范

- 格式：google-java-format，2 空格缩进，100 列；提交前跑 `./gradlew spotlessApply`；
- 注释只解释「为什么」，不要写类头罗列式 javadoc 或逐字段注释；
- 优先使用 `Schedulers`、`Reflections`、`ActionManager` 等已有抽象；
- 实体操作必须在所属实体线程执行，Folia 下尤其重要；
- 面向用户的文本必须走翻译键，不要硬编码中文或英文；
- 命令注册改动要在测试服冒烟，确认没有 `BadLiteralException`；
- 本地没有 Gradle 时可以用 javac 做类型检查，但以 CI 结果为准。

### 4.1 项目结构硬约束

- `fakeplayer-core` 不直接碰 NMS；
- 版本相关代码放 `versions/fakeplayer-vX`；
- `fakeplayer-core` 与 `fakeplayer-api` 的 `paper-api` 钉在 `1.21-R0.1-SNAPSHOT`，禁止升级，否则 1.20.x 会失效；
- 构建需要 JDK 25；
- 没有单元测试，验证靠全模块编译 + 真实服务器手测；
- 可用 git 忽略的 `test-server/` 放置测试用 jar 与 CommandAPI。

### 4.2 权限相关约定

OP 和控制台可以按名字操作任意假人，普通玩家只能操作自己召唤的。任何按名字解析假人的新代码都必须保留这个分支：

```java
sender.isOp() ? manager.get(name) : manager.get(sender, name)
```

## 5. CHANGELOG 约定

- 中英条目数量与顺序一致；
- 只写用户可感知变化：功能、修复、破坏性变更、配置/权限变更；
- 构建脚本、CI、重构、死代码清理不写；
- 每条以加粗短语开头，不超过 2 句且不超过 300 字符；
- 细节放 Release notes；
- 未发布版本写 `## fp.buildN - (Not published | 未发布)`，发布时改成日期。

修改 `config.yml` 时必须把顶部 `version` +1，并在 CHANGELOG 登记。新增用户可见文案必须补齐五份 `message/*.properties`。

## 6. 文档与翻译规范

- 四份 README（`README.md`、`README_zh.md`、`README_zh_TW.md`、`README_ja.md`）结构保持一致；
- 新增命令、配置项或权限时四份 README 同步；
- `docs/BUILD*.md` 同理；
- 推送 `README.md` 到 `master` 会自动重发 Hangar 项目页；
- Vitepress 站点先写中文，再补其他语言；
- 新增页面要同步修改 `vitepress_docs/.vitepress/config.mts` 的侧边栏；
- 检查相对链接，运行 `npm run build` 确认没有死链；
- 命令参数和配置名必须与代码一致，不要照抄旧版 README。

### 6.1 新增语言

1. 添加 `message_<语言>_<地区>.properties`；
2. 在 `config.yml` 的 `i18n.locale` 注释里登记；
3. 更新四份 README 的语言列表；
4. 补 Vitepress 对应 locale 目录。

## 7. 行为准则

Issue、PR、讨论与提交信息都适用行为准则：尊重他人，不骚扰，不人身攻击，不泄露隐私。详见 `docs/CODE_OF_CONDUCT*.md`。

## 8. 贡献者清单

项目由 EndlessPixel Studio 与社区贡献者共同维护。完整贡献者名单以 GitHub 仓库 Contributors 页面为准。感谢所有提交 Issue、翻译、复现问题和写代码的贡献者。
