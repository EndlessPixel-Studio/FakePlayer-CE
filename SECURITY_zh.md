# 安全说明

[English](SECURITY.md) | 简体中文 | [繁體中文](SECURITY_zh_TW.md) | [日本語](SECURITY_ja.md)

FakePlayer CE 会生成真实的服务器玩家，并提供可选的 HTTP 管理接口——配置不当等于把服务器控制权交给别人。本文说明支持范围、如何报告问题，以及怎样安全地运行本插件。

## 支持范围

只有最新发布的版本会收到安全修复，旧版本不再维护。请从 [GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases) 或 [Hangar](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE) 更新到最新的 `fp.buildN`。

| 版本 | 是否支持 |
|---|---|
| 最新发布的 `fp.buildN` | ✅ |
| 更早的版本 | ❌ |

## 报告漏洞

**不要为安全问题开公开 issue。**

请使用 GitHub 的私密报告：打开仓库的 [Security 标签页](https://github.com/EndlessPixel-Studio/FakePlayer-CE/security) → *Report a vulnerability*，生成的 advisory 只有维护者可见。

请附上：

- 插件版本（`/version FakePlayer-CE` 或 jar 文件名）
- 服务端类型与版本（Paper / Purpur / Leaf / Folia，Minecraft 版本）
- 复现步骤、预期结果与实际结果
- 相关配置（记得打码 token）与日志片段

我们会尽快确认收到。这是志愿者项目，请给几天时间。修复发布后，我们很乐意在 advisory 与更新日志里注明你的贡献。

## HTTP 管理接口

HTTP 接口**默认关闭**，且每个端点都有独立开关。启用后请留意以下几点。

### 插件已经做了什么

| 防护 | 行为 |
|---|---|
| 令牌认证 | 只接受 `Authorization: Bearer <token>`。**不支持 `?token=`**——查询串会进入代理日志与浏览器历史。`token` 留空时启动时生成随机值并打印到控制台。 |
| 鉴权先于路由 | 鉴权在端点开关之前判定，`401` / `403` 无法被用来探测哪些端点已启用。 |
| 常量时间比较 | 令牌用 `MessageDigest.isEqual` 比较，而非 `String.equals`。 |
| 日志脱敏 | 记录前会隐藏令牌并替换控制字符。 |
| 限流 | 按来源 IP 限流，连续鉴权失败后锁定。 |
| Host / Origin 白名单 | `allowed-hosts` 会拒绝 Host 或跨源 Origin 不匹配的请求，缓解 DNS rebinding。 |
| 禁止缓存 | 响应带 `Cache-Control: no-store`。 |

### 仍需你自己负责的部分

- **接口没有 TLS。** 它走的是明文 HTTP。请绑定 `127.0.0.1`，或放在负责 TLS 的反向代理之后，并在防火墙层面限制来源。
- **`/cmd` 以假人身份执行命令**，权限等同该假人。这是最危险的端点——不需要时请关掉 `http-admin.interface.cmd`，并且**绝不要给假人 OP**：任何能操纵该假人的人都会继承这些权限。
- **保管好令牌。** 把它当 root 密码对待。一旦出现在截图、日志或公开仓库里，立即更换。
- **只暴露需要的端点。** 每个端点都有 `http-admin.interface` 下的独立开关，用不到的请关掉。

### 加固清单

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1          # 只监听本机，或放在反向代理之后
  token: ""                # 留空会随机生成；建议自己设置一个足够长的随机值
  allowed-hosts: []        # 通过代理对外暴露时填你的域名
  rate-limit:
    requests-per-minute: 120
    auth-failures: 10
    lockout-seconds: 60
  interface:
    cmd: false             # 用不到的端点都关掉
```

如果接口要暴露到 `localhost` 之外，还应：

- 在代理层终止 TLS，并只允许代理的地址访问
- 在令牌之外再加一层认证（mTLS、Basic Auth 或 SSO 网关）
- 留意插件日志里反复出现的 `401` / `429`

## 范围

属于范围：插件本体（命令处理、HTTP 接口、`versions/` 下的 NMS 模块、登录插件兼容、更新检查、本仓库的 Hangar / GitHub 发布工作流）。

不属于范围：需要先攻陷服务器才能触发的问题、主动给假人 OP 权限、在不受支持的服务端版本上使用 `forced-execution: true`，以及按文档正常使用功能造成的破坏（例如 `/fp cmd`）。
