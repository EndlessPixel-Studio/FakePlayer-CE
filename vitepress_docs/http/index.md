# HTTP 管理接口

HTTP 接口用于 Web 面板和自动化脚本远程查询状态、生成/移除假人、驱动动作。默认关闭。

## 启用

```yaml
http-admin:
  enabled: false
  host: 0.0.0.0
  port: 3253
  token: ''
```

- Undertow 依赖会在开启后自动下载到 `plugins/FakePlayer-CE/libs/`。
- `token` 留空时插件会随机生成并在启动日志中打印。
- `host` 建议填 `127.0.0.1`；只有需要远程访问时才监听 `0.0.0.0` 并配合反代 TLS。

## 调用方式

- 鉴权只认请求头：`Authorization: Bearer <token>`，**不支持** `?token=`。
- 参数一律走 URL query；`GET` / `POST` 都可以。
- 成功返回 `{"status":"success", ...}`，失败返回 `{"status":"failure","msg":"..."}`。
- 批量接口额外返回 `count`。

```
curl -H "Authorization: Bearer test-secret-token" http://127.0.0.1:3253/list
```

## 接口分类

- [信息类](/http/endpoints-info)：`/list`、`/status`、`/info`
- [生命周期](/http/endpoints-lifecycle)：`/spawn`、`/kick`、`/kill`、`/respawn`、`/aichat/stop`
- [动作与移动](/http/endpoints-action)：`/say`、`/aichat`、`/action`、`/stop`、`/teleport`、`/look`、`/hold`、`/swap`、`/goto`、`/setping`、`/sneak`、`/sprint`
- [高危接口](/http/endpoints-admin)：`/reload`、`/cmd`、`/kickall`、`/killall`、`/sayall`

## 安全机制

插件内置：鉴权先于接口开关、令牌常量时间比较、日志脱敏、按 IP 限流与锁定、可选 Host/Origin 白名单、CORS 开关、`Cache-Control: no-store`。

你仍需负责：不要直接把无 TLS 的管理端口暴露到公网；关闭不需要的接口；不要用 `/cmd` 执行不可信命令；保管好 token。详见 [安全说明](/contributing/issues)。
