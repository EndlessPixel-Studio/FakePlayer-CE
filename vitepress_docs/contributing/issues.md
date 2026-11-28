# 问题与反馈规范

## 提交 issue 前

- 先搜索已有 issue，确认不是重复问题。
- 提供插件版本 `fp.buildN`、服务端核心与 MC 版本、Java 版本。
- 写清复现步骤、期望行为与实际行为。
- 附上相关日志；性能问题请附 `/fp sysinfo` 生成的 JSON 内容。

## 安全漏洞

**不要开公开 issue**。请使用 GitHub 仓库的 Security 标签页 → *Report a vulnerability* 私密报告，并附上版本、服务端、复现步骤与打码后的配置/日志。

## HTTP 管理接口安全

插件内置 Bearer 鉴权、令牌常量时间比较、日志脱敏、IP 限流与锁定、Host/Origin 白名单、CORS 开关。你仍需要：

- 不要直接把无 TLS 的管理端口暴露到公网；绑 `127.0.0.1` 或放在反代后面。
- 关闭不需要的接口，尤其是 `/cmd`。
- 不要把假人设为 OP。
- 保管好 token，定期更换。

完整规则见 `docs/SECURITY_zh.md`。
