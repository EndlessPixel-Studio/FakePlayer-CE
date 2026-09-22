# 构建指南

English | 简体中文 | [繁體中文](BUILD_zh_TW.md)

本项目使用 **paperweight-userdev** Gradle 插件自动处理 NMS 依赖，无需手动运行 BuildTools。

## 前置条件

- **JDK 25**（用于编译 Paper 1.21+ 的 NMS 代码）
- 可用的网络连接（首次构建会下载 Paper 服务端 jar）

## 快速构建

```bash
./gradlew build --no-daemon
```

产物位于：

```
fakeplayer-dist/build/libs/fakeplayer-fp.buildX.jar
```

## 工作原理

每个版本模块（如 `fakeplayer-v1_21_6`）通过 `paperweight.paperDevBundle()` 声明所需的 Paper dev bundle，
插件会自动完成：

1. 下载对应版本的 Paper 服务端 jar
2. 对 NMS 类进行重映射与反混淆
3. 以 compile-only 依赖的形式提供这些类

因此无需运行 BuildTools 或维护本地 maven 仓库，即可获得完整的 NMS 源码访问能力。
