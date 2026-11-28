# 下载安装插件

## 下载地址

发布物为单一 `fakeplayer-fp.buildX.jar`，放入服务端 `plugins/` 目录并重启即可（需先装 CommandAPI）。

- **稳定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推荐）
- **Hangar**：[hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)，可在网页端直接下载与更新
- **最新构建**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 任一次成功的运行 → Artifacts → `fakeplayer-dist`

## 环境要求

- 服务端：Paper / Purpur / Leaf 等 Paper 系核心，覆盖 MC `1.20.1 ~ 26.3`
- Java：25
- 前置插件：[CommandAPI](https://commandapi.jorel.dev) `12.1.0+`

## 安装步骤

1. 把 CommandAPI 的 jar 放进服务端的 `plugins/` 目录。
2. 把 FakePlayer-CE 的 jar 放进 `plugins/`。
3. 启动一次服务器，插件会生成 `plugins/FakePlayer-CE/config.tmpl.yml` 模板。
4. 把模板复制/重命名为 `config.yml`，按需修改后重启或执行 `/fp reload`。
5. 执行 `/fp spawn` 创建第一个假人。

> 首次启动只生成 `config.tmpl.yml`，这是为了升级时能看到新增配置而不会覆盖你已有的设置。

## 可选依赖不打包

插件 jar 不再内置 HTTP 服务器（Undertow）和 MySQL 驱动。只有当你开启 `http-admin.enabled` 或设置 `database.type: mysql` 时，插件才会在启动时下载依赖到：

```
plugins/FakePlayer-CE/libs/
```

下载源默认为 Maven Central。如果你的网络访问 Maven Central 慢或失败，可以在 JVM 参数里指定镜像：

```
-DLeaf.library-download-repo=https://maven.aliyun.com/repository/public
```

详见 [数据库与依赖下载](/guide/config/config-database) 和 [Leaf 官方文档](https://www.leafmc.one/zh/docs/config/jvm-flags#-dleaflibrary-download-repo)。

## 升级

把新版本 jar 覆盖旧文件即可。升级后如果配置模板新增了选项，可用 `/fp update config` 把缺失项补进现有 `config.yml`，已有取值和注释不会被覆盖，原文件会备份为 `config.yml.bak`。
