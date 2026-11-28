<div align="center">

<img src=".github/assets/logo.png" alt="FakePlayer CE" width="200">

# FakePlayer CE（社群版）

[English](README.md) | [简体中文](README_zh.md) | 繁體中文 | [日本語](README_ja.md)

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Hangar](https://img.shields.io/badge/Hangar-FakePlayer--CE-1f7ae0)](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

---

> **FakePlayer CE** 是 [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer) 的社群維護分支：受 [Carpet-Mod](https://github.com/gnembon/fabric-carpet) 啟發，在伺服器端生成對伺服器完全透明的假人玩家，並以 Gradle 多模組架構做到 **Minecraft `1.20.1` ~ `26.3` 單一 Jar 檔相容**。

⭐ 如果你喜歡 FakePlayer CE，給它點個 Star 吧！這對專案的成長非常有幫助。

## 目錄

- [⚠️ 社群版聲明](#️-社群版聲明)
- [快速上手](#快速上手)
- [功能特性](#功能特性)
- [執行前置依賴](#執行前置依賴)
- [下載](#下載)
- [設定檔說明](#設定檔說明)
- [資料庫](#資料庫)
- [AI 聊天](#ai-聊天)
- [指令清單](#指令清單)
- [HTTP 管理介面](#http-管理介面)
- [安全說明](#安全說明)
- [個人化設定](#個人化設定)
- [權限分組說明](#權限分組說明)
- [佔位符變數](#佔位符變數)
- [自訂本地化翻譯](#自訂本地化翻譯)
- [登入插件相容與聊天事件](#登入插件相容與聊天事件)
- [常見問題](#常見問題)
  - [斷開連線：PacketEvents 2.0 failed to inject](#斷開連線packetevents-20-failed-to-inject)
  - [假人不被怪物攻擊](#假人不被怪物攻擊)
  - [假人一段時間後自動掉線](#假人一段時間後自動掉線)
- [Star 趨勢](#star-趨勢)
- [貢獻者](#貢獻者)

## ⚠️ 社群版聲明

- 本倉庫是**獨立的社群維護分支**，不由原作者發布、維護或背書。

## 快速上手

1. 安裝 [CommandAPI](https://commandapi.jorel.dev) 到 `plugins/`
2. 把 `fakeplayer-fp.buildX.jar` 放進 `plugins/`
3. 啟動伺服器一次，把 `config.tmpl.yml` 重新命名為 `config.yml`
4. 執行 `/fp spawn`

## 功能特性

- 生成對伺服器完全透明的假人玩家，適用於區塊常駐載入
- 支援原版與插件指令管控（傳送、封禁、背包編輯等）
- 完整操控假人行為：移動、跳躍、攻擊、挖掘等，支援週期性自動化
- 每位玩家擁有獨立的個人化預設設定範本
- **單一 Jar 多版本**：一個 Jar 覆蓋 `1.20.1 ~ 26.3`，NMS 程式碼按版本隔離
- **HTTP 管理介面**：遠端查詢狀態、生成 / 移除假人，控制動作、朝向、背包與指令
- **登入插件相容**：AuthMe / CatSeedLogin / LibreLogin 免設定免登入

## 執行前置依賴

- [Paper](https://papermc.io) 或 [Purpur](http://purpurmc.org) 伺服器端
- [CommandAPI](https://commandapi.jorel.dev) 前置插件（**請勿使用 `10.0.0`**；**MC 26.3 需 `12.1.0` 及以上**）

## 下載

- **穩定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推薦）
- **Hangar**：[hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
- **最新建置**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 任一次成功的執行 → Artifacts → `fakeplayer-dist`

## 設定檔說明

首次載入只會生成範本 `config.tmpl.yml`，需手動重新命名為 `config.yml` 後才會生效。範例見 [config.yml](fakeplayer-core/src/main/resources/config.yml)。

**依權限設定假人上限**：在 `spawn-limit` 下設定如 `vip: 10`，並給玩家 `fakeplayer.spawn.limit.vip` 權限；命中多個權限時取最大值，OP 不受限。

## 資料庫

資料預設存在 SQLite（`plugins/FakePlayer-CE/data.db`）。想用 MySQL，先建一個空庫，在 `config.yml` 裡把 `database.type` 改成 `mysql` 並填好 `database.mysql`，然後重新啟動。

## AI 聊天

`/fp aichat` 讓假人透過 OpenAI 相容介面產生聊天內容。首次使用需在 `plugins/FakePlayer-CE/ai-chat.yml` 填入 `api` 與 `api-key`（或設定 `api-key-env` 指定的環境變數），把 `enabled` 改為 `true`，再執行 `/fp reload`。

| 指令 | 說明 |
|---|---|
| `/fp aichat public [once\|continuous\|interval <tick>] [假人]` | 發到公共聊天 |
| `/fp aichat private <玩家> [once\|continuous\|interval <tick>] [假人]` | 只發給指定玩家 |
| `/fp aichat stop [假人]` | 停止持續發言 |
| `/fp aichat model [名稱]` | 查看或切換 AI 模型（僅 OP） |

可在 `ai-chat.yml` 的 `models:` 下設定多個模型；`auto-reply` 也可在該檔案中開啟。

## 指令清單

凡是假人名稱參數都可以用 `-a` 一次操作範圍內所有假人；`select`、`spawn`/`spawnuuid`、`rename` 除外。

| 指令 | 權限節點 |
|------|---------|
| /fp spawn | fakeplayer.command.spawn |
| /fp spawnjson | fakeplayer.command.spawn.json |
| /fp spawnuuid（已淘汰） | fakeplayer.command.spawn.uuid |
| /fp kick / kickall | fakeplayer.command.kick / OP |
| /fp kill / killall | fakeplayer.command.kill / OP |
| /fp select / selection | fakeplayer.command.select / .selection |
| /fp list / distance | fakeplayer.command.list / .distance |
| /fp drop / dropstack / dropinv | fakeplayer.command.drop / .dropstack / .dropinv |
| /fp skin / invsee / enderchest | fakeplayer.command.skin / .invsee / .enderchest |
| /fp sleep / wakeup / autosleep | fakeplayer.command.sleep / .wakeup / .autosleep |
| /fp status / respawn | fakeplayer.command.status / .respawn |
| /fp tp / tphere / tps | fakeplayer.command.tp / .tphere / .tps |
| /fp set / setping / config | fakeplayer.command.set / .setping / .config |
| /fp expme | fakeplayer.command.expme |
| /fp attack / mine / use / jump | fakeplayer.command.attack / .mine / .use / .jump |
| /fp stop / turn / look / move | fakeplayer.command.stop / .turn / .look / .move |
| /fp goto / gotostop / ride | fakeplayer.command.goto / .goto / .ride |
| /fp sneak / sprint / swap / hold | fakeplayer.command.sneak / .sprint / .swap / .hold |
| /fp cmd / say / aichat | fakeplayer.command.cmd / .say / .aichat |
| /fp password / changepassword | fakeplayer.command.password / .changepassword |
| /fp reload / update / version / lang | OP / OP / - / - |

```text
/fp spawnjson {"name":"Bot_1","world":"world","x":0,"y":64,"z":0,"lifespan_seconds":600}
```

## HTTP 管理介面

選用的 HTTP 介面（預設關閉），用於 Web 面板與自動化腳本。

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1
  port: 3253
  token: ""
  allow-get: true
  interface:
    list: true
```

權杖只走 `Authorization: Bearer <token>` 請求標頭；參數走 query，`GET` / `POST` 均可。

| 介面 | 說明 |
|---|---|
| `/list`、`/status`、`/info` | 查詢假人與伺服器狀態 |
| `/spawn`、`/kick`、`/kill`、`/respawn` | 生命週期 |
| `/say`、`/aichat`、`/aichat/stop`、`/action`、`/stop` | 聊天與動作 |
| `/teleport`、`/look`、`/hold`、`/swap`、`/goto`、`/setping`、`/sneak`、`/sprint` | 移動與狀態 |
| `/reload`、`/cmd`、`/kickall`、`/killall`、`/sayall` | 管理 |

## 安全說明

驗證先於介面開關檢查，內建按 IP 限流與選擇性 `allowed-hosts` 校驗。

- 介面**沒有 TLS**，只在可信網路或反向代理之後暴露。
- `/cmd` 以假人身份執行指令，**不要把假人設為 OP**。
- 建議 `host` 用 `127.0.0.1`，並在防火牆層面限制來源。

## 個人化設定

每位玩家可儲存專屬預設值，修改後**下次生成假人時生效**：`/fp config list` 檢視，`/fp config set <項> <值>` 修改。

| 設定項 | 說明 |
|--------|------|
| `collidable` | 是否開啟碰撞箱 |
| `invulnerable` | 是否開啟無敵模式 |
| `wolverine` | 是否開啟自動回血 |
| `look_at_entity` | 自動看向周邊可攻擊實體 |
| `pickup_items` | 是否開啟物品拾取 |
| `skin` | 是否預設使用建立者造型 |
| `replenish` | 自動補充消耗的物品 |
| `replace_tools` | GCA 風格的工具替換 |
| `autofish` | 是否開啟自動釣魚 |
| `aichat_fake_inventory` | 是否允許 AI 讀取假人背包 |
| `aichat_questioner_inventory` | 是否允許 AI 讀取提問玩家背包 |
| `simulation_distance` | 假人的模擬距離，`-1` 沿用世界設定 |

## 權限分組說明

每條指令都有獨立權限節點，便捷分組見 `plugin.yml`。

## 佔位符變數

安裝 PlaceholderAPI 後可使用 `%fakeplayer_total%`、`%fakeplayer_list%`、`%fakeplayer_isfake%`，以及 `%fakeplayer_name%`、`%fakeplayer_world%`、`%fakeplayer_health%` 等假人屬性。

## 自訂本地化翻譯

內建 `en` / `zh` / `zh_tw` / `zh_hk` / `ja`。改 `config.yml` 的 `i18n.locale` 後 `/fp reload`；玩家可用 `/fp lang <代碼>` 單獨覆蓋。

## 登入插件相容與聊天事件

裝有 AuthMe / CatSeedLogin / LibreLogin 時，假人會自動標記為已登入，無需設定。`/fp say` 會觸發正常聊天事件，格式化插件可正常運作。

## 常見問題

### 斷開連線：PacketEvents 2.0 failed to inject

在 `config.yml` 設定 `prevent-kicking: ALWAYS`。

### 假人不被怪物攻擊

假人預設無敵。執行 `/fp config set invulnerable false` 後才會受傷。

### 假人一段時間後自動掉線

通常是登入插件在踢未登入玩家。上面三個插件已內建相容；其他插件可用 `auto-login.command` 指令模板兜底。

## Star 趨勢

[![Star History Chart](https://api.star-history.com/svg?repos=EndlessPixel-Studio/FakePlayer-CE&type=Date)](https://star-history.com/#EndlessPixel-Studio/FakePlayer-CE&Date)

## 貢獻者

[![Contributors](https://contrib.rocks/image?repo=EndlessPixel-Studio/FakePlayer-CE)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/graphs/contributors)
