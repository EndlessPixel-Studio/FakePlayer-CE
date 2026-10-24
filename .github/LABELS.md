# Label 规范

仓库共 **16 个 label**，分四个维度。贴标规则如下，新增 issue/PR 时遵守。

## 维度与规则

| 维度 | 标签 | 规则 |
|---|---|---|
| **类型**（issue 必贴，单选） | `bug` `enhancement` `documentation` `dependency` `breaking-change` `wontfix` `duplicate` `invalid` | 每个 issue 恰好一个。`help wanted` 可额外叠加表示欢迎社区参与 |
| **模块**（多选） | `core` `api` `nms` `dist` | 按涉及的代码模块贴；PR 由 `labeler.yml` 按文件路径自动贴，无需手工 |
| **功能域**（可选） | `feature` | **仅 issue 使用**，标记该 issue 属于某个功能领域。PR 不贴（PR 的模块标签已说明改哪儿） |
| **优先级**（可选） | `priority-high` | **仅维护者使用**，标记紧急/阻塞项。普通事项不贴（不贴=普通） |

## 已废弃（勿再用）

- `priority-medium`：中优先级即普通优先级，用「不贴」表达，2026-10-24 删除。
- 历史功能标签（`command` `config` `locale` `ci` `auto-login` `plugin-compat` `skin` `spawn` `action` `despawn` `tablist` `inventory` `autofish` `chunk-loader` `http-api` `placeholder` `replace-tools` `replenish` `permission` `database`）：2026-10-24 全部合并为 `feature`。

## 自动贴标机制

- **PR**：`workflows/auto-label-pr.yml` + `labeler.yml` 按变更文件路径自动贴模块标签与 `documentation` / `dependency`，先清理后重算，无需手工干预。
- **dependabot PR**：自动贴 `dependency`。
- **issue**：无自动贴标，由提交者/维护者按上表手工贴。
