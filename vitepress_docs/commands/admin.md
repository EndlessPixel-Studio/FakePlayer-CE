# 管理与运维

## /fp kickall / /fp killall

- 权限：OP
- 无参数，作用于服务器全部假人

## /fp reload

- 权限：OP
- 重载插件配置与翻译；数据库类型和 HTTP 端口仍需重启

## /fp migrate-db

- 权限：OP
- 异步把 SQLite 的 `data.db` 四张表迁移到当前 MySQL 库
- 需要 `database.type: mysql`，改完重启后执行

## /fp update check

- 权限：OP
- 检查 GitHub 是否有新版本；`update-download: true` 时会下载到 `plugins/update/`

## /fp update config

- 权限：OP
- 把模板新增、当前 `config.yml` 缺失的配置项补进去
- 已有取值与注释不变，改写前备份为 `config.yml.bak`

## /fp version

- 权限：所有玩家
- 显示插件、服务端与 Java 版本，以及构建该 jar 的 commit

## /fp sysinfo

- 权限：OP
- 导出 CPU、内存、磁盘、TPS 与假人状态为 JSON，写入 `plugins/FakePlayer-CE/sysinfo/`
- 报告性能问题时可直接附上该文件内容

## /fp lang

- 权限：所有玩家
- 切换自己的显示语言，优先于客户端语言

## 权限分组

| 分组 | 包含内容 |
|---|---|
| `fakeplayer.spawn` | 生成、踢出、列表、选择、基础状态与配置 |
| `fakeplayer.tp` | `tp` / `tphere` / `tps` |
| `fakeplayer.action` | `attack` / `mine` / `use` / `move` / `look` / `sneak` / `sprint` 等动作与特性权限 |
| `fakeplayer.basic` | 普通玩家常用权限的组合，不含高危命令 |

> 不要把假人设为 OP：`/fp cmd` 以假人身份执行控制台命令，权限等同该假人。
