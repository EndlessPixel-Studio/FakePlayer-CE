# LibreLogin / LibreLoginNext

检测到 LibreLogin 或 LibreLoginNext 后，插件通过其 API 为假人创建临时账号并标记登录。

## 行为

- 不执行命令，也不需要你手动配置密码。
- 假人使用独立账号，不与真实玩家数据混用。
- 假人退出时清理临时状态。

## 配置

无需配置。若 API 调用失败，可改用命令模板：

```yaml
auto-login:
  command:
    enabled: true
    register: '/reg %password% %password%'
    login: '/login %password%'
```

密码用 `/fp password <假人> <密码>` 设置，或配置 `random-password` 自动生成。
