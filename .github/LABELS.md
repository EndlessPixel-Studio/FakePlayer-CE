# Label 规范

仓库 label 分 **五个维度**，目标是列表里一眼看出「类型 + 哪个功能 + 哪块代码 + 优先级 + 是否被处理」，不必打开正文。

## 维度与规则

| 维度 | 标签 | 规则 |
|---|---|---|
| **类型**（issue 必贴，单选） | `bug` `enhancement` `documentation` `dependency` `breaking-change` `wontfix` `duplicate` `invalid` | 每个 issue 恰好一个。`help wanted` 可额外叠加，表示欢迎社区参与 |
| **功能域**（issue 多选 1-2 个） | `spawn` `action` `command` `login` `appearance` `config` `locale` `compat` `http-api` `placeholder` | 标记 issue 属于哪个功能领域（信息量最大的维度，切勿合并成一个大杂烩）。PR 仅 `config`/`locale`/`http-api`/`placeholder` 由 labeler 按路径自动贴，其余靠模块标签说明改哪儿 |
| **模块**（多选） | `core` `api` `nms` `dist` | 按涉及的代码模块贴；PR 由 `labeler.yml` 按文件路径自动贴，无需手工 |
| **优先级**（可选，仅维护者） | `priority-high` `priority-medium` `priority-low` | 紧急/阻塞用 `priority-high`；普通事项不贴（不贴=普通）。`medium`/`low` 用于排序参考 |
| **状态**（自动） | `needs-triage` | 新建 issue/PR 由 `workflows/triage.yml` 自动贴上；issue 一旦补上「功能域」标签（=已人工分类）即自动移除 |

## 功能域速查表

| 标签 | 范围 |
|---|---|
| `spawn` | 假人生命周期：生成 / 移除 / 踢出 / 杀死 / 名称 |
| `action` | 动作与假人行为：say / mine / attack / jump / look / move / ride / 无敌 / 寻路(goto) |
| `command` | 命令系统：help / config / 参数与 TAB 补全 / 命令报错 |
| `login` | 登录插件兼容与自动登录：AuthMe / LibreLogin / 密码 |
| `appearance` | 外观：skin / ping / tablist / invsee / enderchest |
| `config` | 配置项本身：新增 / 默认值 / 重排 |
| `locale` | 本地化：消息文案 / 多语言 / 帮助文本语言 |
| `compat` | 版本与服务端兼容：MC 版本支持 / Folia / Leaf / NMS 差异 |
| `http-api` | HTTP Admin 管理接口 |
| `placeholder` | PlaceholderAPI 变量（%fakeplayer_*%） |

## 自动贴标机制

- **PR**：`workflows/auto-label-pr.yml` + `labeler.yml` 按变更文件路径自动贴模块标签与 `documentation` / `dependency`，先清理后重算，无需手工。
- **issue**：`workflows/triage.yml` 在 `opened` 时贴 `needs-triage`；维护者补上「功能域」标签后自动移除 `needs-triage`。类型与功能域由提交者/维护者手工贴。
- **dependabot PR**：自动贴 `dependency`。
