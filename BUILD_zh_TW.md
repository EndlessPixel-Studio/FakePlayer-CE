# 建置指南

[English](BUILD.md) | [简体中文](BUILD_zh.md) | 繁體中文

本專案使用 **paperweight-userdev** Gradle 外掛自動處理 NMS 依賴，無需手動執行 BuildTools。

## 前置條件

- **JDK 25**（用於編譯 Paper 1.21+ 的 NMS 程式碼）
- 可用的網路連線（首次建置會下載 Paper 伺服器端 jar）

## 快速建置

```bash
./gradlew build --no-daemon
```

產物位於：

```
fakeplayer-dist/build/libs/fakeplayer-fp.buildX.jar
```

## 運作原理

每個版本模組（如 `fakeplayer-v1_21_6`）透過 `paperweight.paperDevBundle()` 宣告所需的 Paper dev bundle，
外掛會自動完成：

1. 下載對應版本的 Paper 伺服器端 jar
2. 對 NMS 類別進行重新對應與反混淆
3. 以 compile-only 依賴的形式提供這些類別

因此無需執行 BuildTools 或維護本機 maven 儲存庫，即可取得完整的 NMS 原始碼存取能力。
