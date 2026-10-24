# vendors 依赖说明

本目录存放无法从稳定 Maven 源获取的编译期依赖 (compileOnly, 不会打进最终产物)。

## placeholderapi-2.12.3.jar

- 来源: https://github.com/PlaceholderAPI/PlaceholderAPI/releases/download/2.12.3/PlaceholderAPI-2.12.3.jar
- 官方 Maven 仓库 (`repo.helpch.at`, 由 `repo.extendedclip.com` 重定向而来) 为单点且故障频繁,
  502 时会导致整个构建中断, 因此直接使用此 jar。
- License: GPL-3.0 (PlaceholderAPI)。仅作编译期引用, 不随插件分发。
- 升级: 到上述 Releases 页面下载新版本, 替换本文件, 并同步修改
  `fakeplayer-core/build.gradle.kts` 中的 `compileOnly(files(...))` 路径。
