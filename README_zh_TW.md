# FakePlayer CE（社群版）

<div align="center">

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.2-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

[English](README.md) | [简体中文](README_zh.md) | 繁體中文

---

> **FakePlayer CE** 是以 FakePlayer 原專案為基礎的社群維護分支，透過 Gradle 多模組架構重構，實現了對 Minecraft `1.20.1` 至 `26.2` 全版本的**單一 Jar 檔相容**。

## ⚠️ 社群版聲明

本倉庫為 **FakePlayer CE（Community Edition）** —— 一個獨立的社群維護分支，**並非 FakePlayer 官方原版**。

- 本專案**不由原作者發布、維護或背書**，係基於開源協議二次開發的社群版本。
- 核心目標：重構並擴展至 Minecraft `1.20.1` ~ `26.2` 全版本跨版本相容，單一 Jar 檔通吃。
- 所有 Bug、功能需求、問題回饋**請僅提交至本倉庫**，切勿提交至上游原作者倉庫。

---

## 概述

FakePlayer 是一款受 [Carpet-Mod](https://github.com/gnembon/fabric-carpet) 啟發的伺服器端假人插件，可在 Minecraft 伺服器上生成高度擬真的虛擬玩家。本社群版在原版基礎上擴展了多版本相容能力，並持續跟進維護。

📺 [觀看展示影片](https://youtu.be/NePaDz-P5nI)

## 功能特性

- 生成對伺服器完全透明的假人玩家，適用於區塊常駐載入
- 完美支援原版及插件指令管控（傳送、封禁、背包編輯等）
- 完整操控假人行為：移動、跳躍、攻擊、挖掘等，支援週期性自動化
- 每位玩家擁有獨立的個人化預設配置範本

### FakePlayer CE 專屬增強

| 增強項 | 說明 |
|---|---|
| **單 Jar 多版本** | 一個通用 Jar 覆蓋 MC `1.20.1 ~ 26.2`，無需分版本下載 |
| **Gradle Kotlin DSL 建置** | 從 Maven 遷移至現代化 Gradle 多模組工程架構 |
| **NMS 版本隔離** | 各版本 NMS 程式碼獨立封裝，降低未來 MC 版本適配成本 |
| **持續相容維護** | 持續跟進 Paper/Purpur 最新版本相容性修復 |
| **HTTP 管理介面** | 內建輕量 HTTP 介面，可遠端查詢狀態、生成 / 移除假人，並控制其動作、朝向、背包與指令 |

## 執行前置依賴

- [Paper](https://papermc.io) 或 [Purpur](http://purpurmc.org) 核心伺服器端
- [CommandAPI](https://commandapi.jorel.dev) 前置插件（**請勿使用 `10.0.0` 版本**）

## 下載

FakePlayer CE 以**單一通用 jar**（`fakeplayer-fp.buildX.jar`）形式發布，覆蓋 Minecraft `1.20.1 ~ 26.2` 全版本。

### 1. 穩定版發布（推薦）

從 GitHub Releases 頁面下載最新穩定建置：

- 📦 [Release 發布下載](https://github.com/EndlessPixel/FakePlayer-CE/releases)

每個 Release 均包含預先建置的 `fakeplayer-fp.buildX.jar`，將其放入伺服器端的 `plugins/` 目錄即可。

### 2. 自動建置（CI / 每次提交）

每次 push 與 Pull Request 都會透過 GitHub Actions 觸發 Gradle 建置。可在任意一次成功的建置執行中，於 **Artifacts（產物）** 區域取得最新 jar：

- 🔧 [GitHub Actions 自動建置](https://github.com/EndlessPixel/FakePlayer-CE/actions)

開啟某次建置執行紀錄，向下捲動到 **Artifacts**，下載 `fakeplayer-dist` 產物（內含 `fakeplayer-fp.buildX.jar`）。此類建置適合在穩定版發布前搶先測試最新修復。

> 下載完成後，將 `fakeplayer-fp.buildX.jar` 放入伺服器端 `plugins/` 目錄並重新啟動伺服器。需先安裝 CommandAPI 前置插件。

## 設定檔說明

插件首次載入時僅生成範本檔案 `config.tmpl.yml`，需手動重新命名為 `config.yml` 後才會生效。此範本機制可讓你在升級時直觀預覽新增設定項，避免覆寫既有設定。

[檢視設定檔範例](fakeplayer-core/src/main/resources/config.yml)

## 指令清單
| 指令 | 功能說明 | 權限節點 | 備註 |
|------|---------|---------|------|
| /fp spawn | 建立假人 | fakeplayer.command.spawn | |
| /fp kick | 踢出假人（從伺服器移除） | fakeplayer.command.kick | |
| /fp kickall | 踢出伺服器所有假人 | OP | |
| /fp kill | 殺死假人（真實死亡，可能掉落物品） | fakeplayer.command.kill | |
| /fp killall | 殺死伺服器所有假人 | OP | |
| /fp select | 設定預設操作假人 | fakeplayer.command.select | 建立多個假人後可用 |
| /fp selection | 檢視目前選中的假人 | fakeplayer.command.selection | 建立多個假人後可用 |
| /fp list | 列出所有線上假人 | fakeplayer.command.list | |
| /fp distance | 檢視與假人間距 | fakeplayer.command.distance | |
| /fp drop | 假人丟棄手中單個物品 | fakeplayer.command.drop | |
| /fp dropstack | 假人丟棄手中整組物品 | fakeplayer.command.dropstack | |
| /fp dropinv | 假人清空全部背包物品 | fakeplayer.command.dropinv | |
| /fp skin | 複製其他玩家造型 | fakeplayer.command.skin | 離線玩家複製存在 60 秒冷卻 |
| /fp invsee | 開啟假人背包介面 | fakeplayer.command.invsee | 對假人按右鍵可觸發同等效果 |
| /fp enderchest | 開啟假人終界箱 | fakeplayer.command.enderchest | 別名 `/fp ec`；對假人潛行按右鍵可觸發同等效果，需與假人處於同一世界 |
| /fp sleep | 假人進入睡覺狀態 | fakeplayer.command.sleep | |
| /fp wakeup | 喚醒睡覺中的假人 | fakeplayer.command.wakeup | |
| /fp status | 檢視假人目前狀態 | fakeplayer.command.status | |
| /fp respawn | 復活已死亡假人 | fakeplayer.command.respawn | 僅關閉「假人死亡踢出」設定時可用 |
| /fp tp | 傳送到假人位置 | fakeplayer.command.tp | |
| /fp tphere | 將假人傳送至自身位置 | fakeplayer.command.tphere | |
| /fp tps | 與假人互換位置 | fakeplayer.command.tps | |
| /fp set | 修改單一假人獨立設定 | fakeplayer.command.set | |
| /fp config | 修改自身建立假人的預設設定 | fakeplayer.command.config | |
| /fp expme | 提取假人經驗值至自身 | fakeplayer.command.expme | |
| /fp attack | 假人發起攻擊 | fakeplayer.command.attack | |
| /fp mine | 假人挖掘方塊 | fakeplayer.command.mine | |
| /fp use | 假人互動 / 放置方塊 / 使用物品 | fakeplayer.command.use | |
| /fp jump | 假人跳躍 | fakeplayer.command.jump | |
| /fp stop | 終止假人所有動作 | fakeplayer.command.stop | |
| /fp turn | 假人原地轉向 | fakeplayer.command.turn | |
| /fp look | 假人看向指定座標 | fakeplayer.command.look | |
| /fp move | 假人定向移動 | fakeplayer.command.move | |
| /fp ride | 假人騎乘實體 | fakeplayer.command.ride | |
| /fp sneak | 假人進入潛行模式 | fakeplayer.command.sneak | |
| /fp sprint | 假人疾跑 | fakeplayer.command.sprint | |
| /fp swap | 切換主手副手物品 | fakeplayer.command.swap | |
| /fp hold | 切換快捷欄指定格子物品 | fakeplayer.command.hold | |
| /fp cmd | 讓假人執行控制台指令 | fakeplayer.command.cmd | |
| /fp say | 讓假人發送聊天訊息 | fakeplayer.command.say | |
| /fp password | 設定假人登入密碼（存入插件資料庫） | fakeplayer.command.password | 需搭配 auto-login 使用 |
| /fp changepassword | 修改假人登入密碼（舊密碼可省略，留空則使用已儲存密碼） | fakeplayer.command.changepassword | |
| /fp reload | 重新載入插件設定檔 | OP | |

## HTTP 管理介面

FakePlayer CE 內建一個選用的輕量 HTTP 介面，用於遠端管理假人（對接 Web 面板、自動化腳本等）。此功能**預設關閉**，基於 JDK 內建 HTTP 伺服器實作，無需額外依賴。

在 `config.yml` 中啟用：

```yaml
http-admin:
  enabled: true          # 是否啟用 HTTP 介面
  host: 0.0.0.0          # 監聽位址
  port: 3253             # 監聽連接埠
  token: ""              # 驗證權杖；留空時啟動會自動產生隨機權杖並輸出到控制台
  allow-get: true        # 是否允許 GET；設為 false 後僅接受 POST
  allowed-hosts: []      # Host / Origin 白名單，留空不校驗；設定後可緩解 DNS rebinding
  rate-limit:
    requests-per-minute: 120   # 每個來源 IP 每分鐘請求上限
    auth-failures: 10          # 連續驗證失敗多少次後鎖定來源 IP（0 = 不鎖定）
    lockout-seconds: 60        # 鎖定時長（秒）
  interface:
    # 查詢類
    list: true           # 啟用 GET /list
    status: true         # 啟用 GET /status
    info: true           # 啟用 GET /info
    # 生命週期
    spawn: true          # 啟用 GET /spawn
    kick: true           # 啟用 GET /kick
    kill: true           # 啟用 GET /kill
    respawn: true        # 啟用 GET /respawn
    # 行為控制
    action: true         # 啟用 GET /action
    stop: true           # 啟用 GET /stop
    say: true            # 啟用 GET /say
    teleport: true       # 啟用 GET /teleport
    look: true           # 啟用 GET /look
    hold: true           # 啟用 GET /hold
    swap: true           # 啟用 GET /swap
    # 其他
    cmd: true            # 啟用 GET /cmd
    batch: true          # 啟用 GET /kickall、/killall、/sayall
```

所有介面預設接受 `GET`，同時也接受 `POST`（參數一律走 query）。**權杖統一透過 `Authorization: Bearer <token>` 請求標頭傳遞，不支援 `?token=` 查詢參數** —— 權杖出現在 URL 裡會進入代理伺服器記錄、瀏覽器歷史與 `Referer`，而且每個介面都要重複拼一次參數。

路徑為**精確比對**：只有上表中的路徑會被處理，`/listfoo` 之類的路徑回傳 `404`，不會落到 `/list` 上。

| 介面 | 說明 | 成功回傳 | 失敗回傳 |
|---|---|---|---|
| `GET /list` | 列出所有線上假人 | `{"fakeplayer":["name1","name2"]}` | — |
| `GET /status[?name=<名字>]` | 查詢假人的詳細狀態（座標、朝向、血量、飢餓、經驗值、模式、建立者、手持物品、進行中的動作等）；省略 `name` 時回傳全部 | `{"fakeplayer":[{...}]}` | `{"status":"failure","msg":"..."}` |
| `GET /info` | 插件與伺服器端資訊（插件版本、MC 版本、伺服器端、假人數量、線上玩家數、數量上限） | `{"plugin":"...","minecraft":"...",...}` | — |
| `GET /spawn?name=<名字>` | 在主世界出生點生成一個假人 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kick?name=<名字>` | 踢出（移除）一個假人 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kill?name=<名字>` | 殺死一個假人（真實死亡，可能掉落物品） | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /respawn?name=<名字>` | 讓已死亡的假人復活 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /say?name=<名字>&message=<內容>` | 以假人身份發送聊天訊息（內容需 URL 編碼） | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /action?name=<名字>&action=<動作>` | 觸發任意動作，參數見下方 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /stop?name=<名字>` | 停止假人目前的所有動作 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /teleport?name=<名字>[&world=&x=&y=&z=&yaw=&pitch=]` | 傳送假人，未給出的參數保持目前值 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<名字>&direction=<方向>` | 讓假人轉向 `north`/`south`/`east`/`west`/`up`/`down` | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<名字>&at=<x,y,z>` | 讓假人看向指定座標 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<名字>&yaw=<角度>&pitch=<角度>` | 直接設定假人朝向 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /hold?name=<名字>&slot=<1-9>` | 切換假人主手欄位 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /swap?name=<名字>` | 交換假人主副手物品 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /cmd?name=<名字>&command=<指令>` | 以假人身份執行一條指令 | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kickall` | 移除伺服器全部假人 | `{"status":"success","count":n}` | — |
| `GET /killall` | 殺死伺服器全部假人 | `{"status":"success","count":n}` | — |
| `GET /sayall?message=<內容>` | 讓全部假人發言 | `{"status":"success","count":n}` | — |

`/action` 的參數：

| 參數 | 預設 | 說明 |
|---|---|---|
| `action` | 必填 | 動作名，不分大小寫，`-` 與 `_` 等價。可選：`ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | 執行次數；`-1` 表示持續執行 |
| `interval` | `0` | 兩次執行之間間隔的 tick 數；只給 `interval` 不給 `count` 時視為持續執行 |
| `wait` | `0` | 首次執行前等待的 tick 數 |
| `message` | — | 僅 `SAY` 動作需要 |

常見失敗提示：

- 生成 — `The dummy's name conflicts with that of an actual player.`（與真實玩家重名）/ `The dummy is already online.`（假人已在線）
- 踢出 — `Dummies do not exist.`（假人不存在）
- 殺死 — `The dummy does not exist.`（假人不存在）/ `The dummy is already dead.`（假人已死亡）
- 復活 — `The dummy does not exist.`（假人不存在）/ `The dummy is not dead.`（假人未死亡）
- 發言 — `Name is required`（缺少假人名稱）/ `Message is required`（缺少訊息內容）
- 動作 — `Action is required, available actions: ...`（缺少或未知動作）/ `Message is required for the say action`（SAY 缺少內容）
- 傳送 — `Invalid number: x`（座標不是數字）/ `Unknown world: xxx`（世界不存在）
- 驗證與開關 — `Unauthorized: pass the token via the Authorization: Bearer header`（401，權杖缺失或錯誤）/ `Interface disabled`（403，對應介面已關閉）
- 限流與鎖定 — `Too many requests`（429，超過每 IP 每分鐘上限）/ `Too many failed attempts, try again later`（429，連續驗證失敗被鎖定）
- 主機與來源 — `Host not allowed`（403，Host 或跨源 Origin 不在白名單內）
- 其他 — `Not found`（404，路徑不存在或不是精確路徑）/ `Request URI too long`（414）/ `Method not allowed`（405）

呼叫範例：

```bash
# 權杖統一走請求標頭，URL 裡只放業務參數
TOKEN="YOUR_TOKEN"
API="http://localhost:3253"

# 列出所有假人
curl -H "Authorization: Bearer $TOKEN" "$API/list"

# 檢視假人詳細狀態（省略 name 則回傳全部）
curl -G -H "Authorization: Bearer $TOKEN" "$API/status" --data-urlencode "name=klmgun"

# 伺服器端與插件資訊
curl -H "Authorization: Bearer $TOKEN" "$API/info"

# 生成假人
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"

# 踢出假人
curl -H "Authorization: Bearer $TOKEN" "$API/kick?name=klmgun"

# 殺死假人
curl -H "Authorization: Bearer $TOKEN" "$API/kill?name=klmgun"

# 讓假人持續攻擊（每 10 tick 一次）
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"

# 停止假人所有動作
curl -H "Authorization: Bearer $TOKEN" "$API/stop?name=klmgun"

# 把假人傳送到指定座標
curl -H "Authorization: Bearer $TOKEN" "$API/teleport?name=klmgun&world=world&x=0&y=64&z=0"

# 讓假人看向東方
curl -H "Authorization: Bearer $TOKEN" "$API/look?name=klmgun&direction=east"

# 讓假人執行指令
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"

# 讓假人發言（訊息內容需 URL 編碼）
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=你好 世界"
```

### 安全說明

內建的防護（無需額外設定）：

- 權杖使用定長比對，避免透過回應時間逐位元組推斷；`Authorization: Bearer` 不分大小寫。
- **驗證先於介面開關**：未攜帶正確權杖時一律回傳 `401`，無法透過 `401` / `403` 的差異列舉哪些介面被啟用。
- **精確路徑比對**：只有文件中的路徑會被處理，`/listfoo` 回傳 `404`，避免繞過按精確路徑放行的外部 ACL / WAF。
- **日誌安全**：名稱含 `token` 的參數一律脫敏；控制字元會被替換、長度會被截斷；未授權請求不記錄 query。避免日誌被偽造或塞爆。
- **限流與失敗鎖定**：預設每個 IP 每分鐘 120 次，連續 10 次驗證失敗鎖定 60 秒。
- **主機 / 同源校驗**：設定 `allowed-hosts` 後校驗 Host；瀏覽器發起的跨源請求（帶 `Origin`）主機不符會被拒絕（預設開啟），可緩解 DNS rebinding。
- 請求 URI 超過 4096 字元直接回傳 `414`；回應帶 `Cache-Control: no-store`，不會被瀏覽器或中間層快取。

仍需管理員注意：

- 介面**沒有 TLS**，權杖以明文經過網路。請務必使用足夠長的隨機權杖，並且只在可信網路（或置於反向代理之後）暴露該介面。
- `/cmd` 會讓假人執行指令，其權限等同於該假人在伺服器端的權限；不要把假人設為 OP。
- 建議把 `host` 改為 `127.0.0.1`（或只在內網位址上監聽），並在防火牆層面限制來源，避免直接暴露到公網。

## 個人化設定

每位玩家均可自訂專屬建立參數，修改後**下次生成假人時自動生效**。

使用範例：
- `/fp config list` — 檢視全部可設定項
- `/fp config set collidable false` — 修改指定設定

| 設定項 | 說明 |
|--------|------|
| `collidable`      | 是否開啟碰撞箱 |
| `invulnerable`    | 是否開啟無敵模式 |
| `wolverine`       | 是否開啟自動回血（快速再生） |
| `look_at_entity`  | 自動看向周邊可攻擊實體；搭配攻擊指令可實現自動刷怪 |
| `pickup_items`    | 是否開啟物品拾取 |
| `skin`            | 是否預設使用建立者造型 |
| `replenish`       | 自動補充消耗或使用完的物品；`/fp drop` 或 `/fp dropstack` 丟掉主手最後一個物品後也會補貨 |
| `replace_tools`   | GCA 風格的工具替換：非經驗修補工具損壞後更換，經驗修補工具在低耐久門檻附近更換 |
| `autofish`        | 是否開啟自動釣魚 |

在 `config.yml` 中透過 `tool-replacement.remaining-durability-threshold` 設定經驗修補工具的剩餘耐久門檻（預設 `10`）；使用 `/fp config set replace_tools true` 為假人開啟。

遵循 GCA 的預設模式，非經驗修補工具損壞後才會更換；經驗修補工具達到門檻時，如果背包中有同類型的非經驗修補工具，或剩餘耐久高於門檻的經驗修補工具，就會更換。比對只看物品類型，附魔差異不會阻止更換；按背包順序選擇第一個合格工具。

開啟 `replenish` 後，消耗奶桶、燉菜、蜂蜜瓶或可飲用藥水時，如果背包裡有同款補貨物且能存下回傳的空桶、碗或玻璃瓶，就會將容器放進背包並補回原物；否則容器會留在手上。`/fp dropinv` 仍會直接清空背包，不會自動補貨。

## 權限分組說明

<details>
<summary>點擊展開檢視詳情</summary>

每條指令均設有獨立權限節點，插件同時提供了便捷的權限分組：

### 權限組 `fakeplayer.spawn`

包含基礎假人管理權限：
- `fakeplayer.command.spawn` — 建立假人
- `fakeplayer.command.kick` — 踢出假人
- `fakeplayer.command.list` — 檢視假人清單
- `fakeplayer.command.distance` — 查詢距離
- `fakeplayer.command.select` — 選中假人
- `fakeplayer.command.selection` — 檢視選中的假人
- `fakeplayer.command.drop` — 丟棄物品
- `fakeplayer.command.dropstack` — 丟棄整組物品
- `fakeplayer.command.dropinv` — 清空背包
- `fakeplayer.command.skin` — 複製造型
- `fakeplayer.command.invsee` — 檢視背包
- `fakeplayer.command.enderchest` — 檢視終界箱
- `fakeplayer.command.status` — 檢視狀態
- `fakeplayer.command.respawn` — 復活假人
- `fakeplayer.command.config` — 修改預設設定
- `fakeplayer.command.set` — 修改單一假人設定

### 權限組 `fakeplayer.tp`

傳送相關權限：
- `fakeplayer.command.tp`
- `fakeplayer.command.tphere`
- `fakeplayer.command.tps`

### 權限組 `fakeplayer.action`

行為動作權限：
- `fakeplayer.command.attack` — 攻擊
- `fakeplayer.command.mine` — 挖掘
- `fakeplayer.command.use` — 互動使用
- `fakeplayer.command.jump` — 跳躍
- `fakeplayer.command.sneak` — 潛行
- `fakeplayer.command.sprint` — 疾跑
- `fakeplayer.command.look` — 看向目標
- `fakeplayer.command.turn` — 轉向
- `fakeplayer.command.move` — 移動
- `fakeplayer.command.ride` — 騎乘
- `fakeplayer.command.swap` — 主副手切換
- `fakeplayer.command.sleep` — 睡覺
- `fakeplayer.command.wakeup` — 喚醒
- `fakeplayer.command.stop` — 停止動作
- `fakeplayer.command.hold` — 切換快捷欄
- `fakeplayer.command.say` — 發送聊天訊息
- `fakeplayer.config.replenish` — 自動補物
- `fakeplayer.config.replenish.chest` — 從附近箱子補貨
- `fakeplayer.config.replace-tools` — 自動替換低耐久工具
- `fakeplayer.config.autofish` — 自動釣魚

若伺服器無需嚴格權限管控，可直接分配 `fakeplayer.basic` 權限組，該組包含除 `/fp cmd` 高危指令外的全部安全權限。

</details>

## 佔位符變數

| 佔位符 | 說明 |
|---|---|
| `%fakeplayer_total%` | 目前伺服器假人總數 |
| `%fakeplayer_creator%` | 假人建立者名稱 |
| `%fakeplayer_actions%` | 假人目前活躍動作，如 `USE\|ATTACK` |

## 自訂本地化翻譯

內建語言：`en`、`zh`、`zh_tw`、`zh_hk`，在 `config.yml` 中修改 `i18n.locale` 即可切換（之後執行 `/fp reload`）。

1. 在 `plugins/fakeplayer/` 下建立 `message` 目錄
2. 將[翻譯範本檔案](fakeplayer-core/src/main/resources/message/message.properties)複製到該目錄
3. 重新命名為 `message_<語言>_<地區>.properties`，如 `message_zh_cn.properties`
4. 修改 `config.yml` 中 `i18n.locale` 為對應後綴名，如 `zh_cn`
5. 執行 `/fp reload-translation` 重新載入翻譯；若修改了語言設定，需先執行 `/fp reload`

> **注意：** 翻譯檔案必須使用 **UTF-8** 編碼儲存。

## 上游版本差異說明

### FakePlayer 官方原版

為本專案的修改基礎。原版每個版本僅適配單一 Minecraft 版本，採用 Maven 建置體系發布。

### FakePlayer CE 修改彙總

1. **建置體系**：從 Maven 遷移至 Gradle Kotlin DSL 多模組工程
2. **跨版本適配**：NMS 程式碼按版本拆分為獨立模組，覆蓋 `1.20.1 ~ 26.2`
3. **發布形式**：統一單一通用 Jar 檔，不再分版本單獨分發
4. **長期維護**：持續跟進 Paper/Purpur 新版本相容性問題修復
5. **多版本修復**：針對性修復跨版本執行時期衝突 Bug

> 如需了解 FakePlayer 官方原版更新，請前往原作者上游倉庫查閱。

## 常見問題

### 斷開連線：PacketEvents 2.0 failed to inject

部分插件會竄改假人的網路連線物件，修改以下設定即可解決：

```yaml
# config.yml
prevent-kicking: ALWAYS
```

### 假人不被怪物攻擊

假人預設開啟無敵模式。執行 `/fp config set invulnerable false` 關閉無敵後，假人才會承受生命值與飢餓值傷害。可搭配生命恢復藥水或烽火台維持生存。

### 假人一段時間後自動掉線

AuthMe 等登入插件會判定假人長時間未登入而踢出。在設定檔的 `self-commands` 中填入註冊 / 登入指令可規避：

```yaml
# 請設定高強度密碼，避免被 AuthMe 安全策略攔截
self-commands:
  - '/register abc123! abc123!'
  - '/login abc123!'
```

## 專案建置

詳細步驟請參閱 [BUILD_zh_TW.md](./BUILD_zh_TW.md)。

> 該建置文件僅適用於 **FakePlayer CE Gradle 多模組編譯流程**，無法用於原 Maven 架構官方專案的建置。
