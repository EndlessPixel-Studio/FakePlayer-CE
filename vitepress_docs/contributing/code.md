# 代码与提交规范

## 项目结构

- `fakeplayer-api`：对外 SPI（NMS 桥接、动作类型等）
- `fakeplayer-core`：核心逻辑、命令、配置、HTTP、AI 与数据库
- `fakeplayer-dist`：把各模块组装成单一 jar
- `versions/fakeplayer-v*`：按 MC 版本隔离的 NMS 实现

`paper-api` 钉在 `1.21-R0.1-SNAPSHOT`，**不要升级**；高版本差异由各版本模块处理。

## 构建与验证

- JDK 25，`./gradlew build`
- 格式化：google-java-format 1.28.0，2 空格缩进、100 列
- 本地 gradle 不可用时可用 javac 做类型检查，但以 GitHub Actions CI 为准
- 涉及命令注册的改动要在测试服跑一次冒烟，确认没有 `BadLiteralException`

## 提交

```
feat: 新增 xxx
fix: 修复 xxx
docs: 补充 xxx
ci: 调整 xxx
chore: 清理 xxx
```

- 英文前缀 + 中文描述。
- 移植外部提交要保留原作者署名：`Co-authored-by: Name <email>`，尖括号不能省。
- 大功能拆成多次提交，CHANGELOG 条目跟着拆。

## CHANGELOG

- 只写影响用户/运行时行为的变化；构建工具链、死代码清理、重构不写。
- 每条以加粗短语开头，≤2 句、≤300 字符。
- 中英文条目数量与顺序一致。

## 配置与权限

- 新增配置项：`config.yml` 的 `version` 必须 +1。
- 新增命令：权限节点、五语文案、四语 README 都要同步。
- 按名字找假人时必须保留 op 分支：`sender.isOp() ? manager.get(name) : manager.get(sender, name)`。
