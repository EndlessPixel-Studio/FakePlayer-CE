# FakePlayer CE（社群版）

<div align="center">

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

[English](README.md) | [简体中文](README_zh.md) | 繁體中文 | [日本語](README_ja.md)

---

> **FakePlayer CE** 是 [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer) 的社群維護分支：受 [Carpet-Mod](https://github.com/gnembon/fabric-carpet) 啟發，在伺服器端生成對伺服器完全透明的假人玩家，並以 Gradle 多模組架構做到 **Minecraft `1.20.1` ~ `26.3` 單一 Jar 檔相容**。

⭐ 如果你喜歡 FakePlayer CE，給它點個 Star 吧！這對專案的成長非常有幫助。

## ⚠️ 社群版聲明

- 本倉庫是**獨立的社群維護分支**，不由原作者發布、維護或背書。
- 所有 Bug 與功能需求**請只提交到本倉庫**，不要提交給上游。

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

發布物為單一 `fakeplayer-fp.buildX.jar`，放入伺服器端 `plugins/` 目錄並重新啟動即可（需先安裝 CommandAPI）。

- **穩定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推薦）
- **最新建置**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 任一次成功的執行 → Artifacts → `fakeplayer-dist`

## 設定檔說明

首次載入只會生成範本 `config.tmpl.yml`，需手動重新命名為 `config.yml` 後才會生效。此機制便於升級時對照新增設定項，不會覆寫既有設定。範例見 [config.yml](fakeplayer-core/src/main/resources/config.yml)。

## AI 聊天

`/fp aichat` 讓假人透過 OpenAI 相容介面產生聊天內容。首次使用需在 `plugins/FakePlayer-CE/ai-chat.yml` 填入 `api` 與 `api-key`（或設定 `api-key-env` 指定的環境變數），把 `enabled` 改為 `true`，再執行 `/fp reload`。

| 指令 | 說明 |
|---|---|
| `/fp aichat public [once\|continuous\|interval <tick>] [假人]` | 發到公共聊天，`continuous` 持續發言直到 `stop` |
| `/fp aichat private <玩家> [once\|continuous\|interval <tick>] [假人]` | 只發給指定玩家 |
| `/fp aichat stop [假人]` | 停止持續發言 |

建議開啟 `auto-reply`：只在玩家說了值得回應的話時才接一句（短訊息、純符號與洗頻會先被過濾），由 `chance` 與 `cooldown-seconds` 控制頻率。`continuous` 為定時發言，預設關閉，需要打開 `allow-continuous`。人設、提示詞、上下文與節奏分別由 `system-prompt` / `turn-prompt` / `max-context` / `recent-chat-lines` / `interval-ticks` 控制。

## 指令清單

| 指令 | 功能說明 | 權限節點 | 備註 |
|------|---------|---------|------|
| /fp spawn | 建立假人 | fakeplayer.command.spawn | |
| /fp spawnuuid | 用指定 UUID 建立假人 | fakeplayer.command.spawn.uuid | 參數 `<名稱> <UUID> [世界] [座標]`；名稱已綁定其他 UUID 時拒絕 |
| /fp kick | 踢出假人（從伺服器移除） | fakeplayer.command.kick | |
| /fp kickall | 踢出伺服器所有假人 | OP | |
| /fp kill | 殺死假人（真實死亡，可能掉落物品） | fakeplayer.command.kill | |
| /fp killall | 殺死伺服器所有假人 | OP | |
| /fp select | 設定預設操作假人 | fakeplayer.command.select | 建立多個假人後可用 |
| /fp selection | 檢視目前選中的假人 | fakeplayer.command.selection | |
| /fp list | 列出所有線上假人 | fakeplayer.command.list | |
| /fp distance | 檢視與假人間距 | fakeplayer.command.distance | |
| /fp drop | 假人丟棄手中單個物品 | fakeplayer.command.drop | |
| /fp dropstack | 假人丟棄手中整組物品 | fakeplayer.command.dropstack | |
| /fp dropinv | 假人清空全部背包物品 | fakeplayer.command.dropinv | |
| /fp skin | 複製其他玩家造型 | fakeplayer.command.skin | 裝有 SkinsRestorer 時走其 API；離線名稱 60 秒冷卻 |
| /fp invsee | 開啟假人背包介面 | fakeplayer.command.invsee | 對假人按右鍵可觸發 |
| /fp enderchest | 開啟假人終界箱 | fakeplayer.command.enderchest | 別名 `/fp ec`；潛行按右鍵；需同世界 |
| /fp sleep | 假人進入睡覺狀態 | fakeplayer.command.sleep | |
| /fp wakeup | 喚醒睡覺中的假人 | fakeplayer.command.wakeup | |
| /fp status | 檢視假人目前狀態 | fakeplayer.command.status | |
| /fp respawn | 復活已死亡假人 | fakeplayer.command.respawn | 需 `kick-on-dead: false` |
| /fp tp | 傳送到假人位置 | fakeplayer.command.tp | |
| /fp tphere | 將假人傳送至自身位置 | fakeplayer.command.tphere | |
| /fp tps | 與假人互換位置 | fakeplayer.command.tps | |
| /fp set | 修改單一假人獨立設定 | fakeplayer.command.set | |
| /fp setping | 設定假人顯示的延遲 | fakeplayer.command.setping | 0 表示使用真實值 |
| /fp config | 修改自身建立假人的預設設定 | fakeplayer.command.config | 控制台可用；`[玩家]` 指定他人 |
| /fp expme | 提取假人經驗值至自身 | fakeplayer.command.expme | |
| /fp attack | 假人發起攻擊 | fakeplayer.command.attack | |
| /fp mine | 假人挖掘方塊 | fakeplayer.command.mine | |
| /fp use | 假人互動 / 放置方塊 / 使用物品 | fakeplayer.command.use | |
| /fp jump | 假人跳躍 | fakeplayer.command.jump | |
| /fp stop | 終止假人所有動作 | fakeplayer.command.stop | |
| /fp turn | 假人原地轉向 | fakeplayer.command.turn | |
| /fp look | 假人看向指定座標 | fakeplayer.command.look | |
| /fp move | 假人定向移動 | fakeplayer.command.move | |
| /fp goto | 讓假人尋路前往指定座標 | fakeplayer.command.goto | 自動尋路；無法抵達則拒絕 |
| /fp ride | 假人騎乘實體 | fakeplayer.command.ride | |
| /fp sneak | 假人進入潛行模式 | fakeplayer.command.sneak | |
| /fp sprint | 假人疾跑 | fakeplayer.command.sprint | |
| /fp swap | 切換主手副手物品 | fakeplayer.command.swap | |
| /fp hold | 切換快捷欄指定格子物品 | fakeplayer.command.hold | |
| /fp cmd | 讓假人執行控制台指令 | fakeplayer.command.cmd | 高風險 |
| /fp say | 讓假人發送聊天訊息 | fakeplayer.command.say | 相容格式化插件 |
| /fp aichat | 讓假人用 AI 產生聊天內容 | fakeplayer.command.aichat | 需設定 `ai-chat.yml` |
| /fp password | 設定假人登入密碼（存入插件資料庫） | fakeplayer.command.password | 搭配 `auto-login.command` 使用 |
| /fp changepassword | 修改假人登入密碼（舊密碼可省略） | fakeplayer.command.changepassword | |
| /fp reload | 重新載入插件設定檔 | OP | |

## HTTP 管理介面

選用的輕量 HTTP 介面（預設關閉，無額外依賴），用於 Web 面板與自動化腳本。

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1        # 建議只監聽本機或內網
  port: 3253
  token: ""              # 留空則啟動時產生隨機權杖並輸出到控制台
  allow-get: true        # false 時僅接受 POST
  allowed-hosts: []      # Host / Origin 白名單，留空不校驗
  rate-limit:
    requests-per-minute: 120   # 每個來源 IP 每分鐘上限
    auth-failures: 10          # 連續驗證失敗鎖定門檻（0 = 不鎖定）
    lockout-seconds: 60        # 鎖定時長
  interface:
    list: true           # 每個介面都有同名開關，關閉後回傳 403
```

- 權杖只走 `Authorization: Bearer <token>` 請求標頭，**不支援 `?token=`**。
- 參數一律走 query，`GET` / `POST` 均可。

| 介面 | 說明 |
|---|---|
| `GET /list` | 列出所有線上假人 |
| `GET /status[?name=<名字>]` | 假人詳細狀態（座標、朝向、血量、飢餓、經驗值、模式、建立者、手持物品、進行中的動作）；省略 `name` 回傳全部 |
| `GET /info` | 插件與伺服器端資訊（版本、MC 版本、伺服器端、假人數量、線上玩家數、數量上限） |
| `GET /spawn?name=<名字>` | 在主世界出生點生成假人 |
| `GET /kick?name=<名字>` | 踢出（移除）假人 |
| `GET /kill?name=<名字>` | 殺死假人（真實死亡，可能掉落） |
| `GET /respawn?name=<名字>` | 復活已死亡假人 |
| `GET /say?name=<名字>&message=<內容>` | 以假人身份發言（內容需 URL 編碼） |
| `GET /aichat?name=<名字>[&mode=public\|private][&target=<玩家>][&continuous=true][&interval=<tick>]` | 讓假人用 AI 發言，需啟用 `ai-chat.yml`；`continuous=true` 持續到 `/aichat/stop` |
| `GET /aichat/stop?name=<名字>` | 停止該假人的 AI 持續發言 |
| `GET /action?name=<名字>&action=<動作>` | 觸發動作，參數見下表 |
| `GET /stop?name=<名字>` | 停止假人目前所有動作 |
| `GET /teleport?name=<名字>[&world=&x=&y=&z=&yaw=&pitch=]` | 傳送假人，未給出的參數保持目前值 |
| `GET /look?name=<名字>&direction=<方向>` | 轉向 `north`/`south`/`east`/`west`/`up`/`down` |
| `GET /look?name=<名字>&at=<x,y,z>` | 看向指定座標 |
| `GET /look?name=<名字>&yaw=<角度>&pitch=<角度>` | 直接設定朝向 |
| `GET /hold?name=<名字>&slot=<1-9>` | 切換主手欄位 |
| `GET /swap?name=<名字>` | 交換主副手物品 |
| `GET /goto?name=<名字>&x=&y=&z=[&world=]` | 尋路前往座標（無法抵達則拒絕） |
| `GET /setping?name=<名字>&ping=<毫秒>` | 設定 Tab 列表顯示的延遲 |
| `GET /sneak?name=<名字>[&state=true\|false]` | 切換（或設定）潛行 |
| `GET /sprint?name=<名字>[&state=true\|false]` | 切換（或設定）疾跑 |
| `GET /reload` | 重新載入 `config.yml` 與語言檔 |
| `GET /cmd?name=<名字>&command=<指令>` | 以假人身份執行指令 |
| `GET /kickall` / `/killall` / `/sayall?message=` | 批次移除 / 殺死 / 發言 |

`/action` 參數：

| 參數 | 預設 | 說明 |
|---|---|---|
| `action` | 必填 | 動作名，不分大小寫，`-` 與 `_` 等價：`ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | 執行次數，`-1` 表示持續 |
| `interval` | `0` | 間隔 tick；只給 `interval` 不給 `count` 視為持續 |
| `wait` | `0` | 首次執行前等待的 tick |
| `message` | — | 僅 `SAY` 需要 |

成功回傳 `{"status":"success"}`（批次介面附帶 `count`），失敗回傳 `{"status":"failure","msg":"..."}` 說明原因。HTTP 層錯誤：`401` 權杖錯誤、`403` 介面已關閉或 Host 不被允許、`404` 路徑不存在、`405` 方法不對、`414` 請求過長、`429` 限流或鎖定。

```bash
TOKEN="YOUR_TOKEN"; API="http://localhost:3253"

curl -H "Authorization: Bearer $TOKEN" "$API/list"
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=你好 世界"
```

## 安全說明

內建防護（無需設定）：**驗證先於介面開關**（無法用 `401` / `403` 差異探測已啟用的介面）；日誌脫敏並替換控制字元；按 IP 限流，連續失敗後鎖定；可選 `allowed-hosts` 校驗 Host 與 `Origin`；回應帶 `Cache-Control: no-store`。

仍需注意：

- 介面**沒有 TLS**，權杖以明文傳輸——請使用足夠長的隨機權杖，只在可信網路或反向代理之後暴露。
- `/cmd` 以假人身份執行指令，權限等同該假人，**不要把假人設為 OP**。
- 建議 `host` 用 `127.0.0.1`，並在防火牆層面限制來源。

## 個人化設定

每位玩家可儲存專屬建立參數，修改後**下次生成假人時生效**：`/fp config list` 檢視，`/fp config set <項> <值>` 修改。

| 設定項 | 說明 |
|--------|------|
| `collidable` | 是否開啟碰撞箱 |
| `invulnerable` | 是否開啟無敵模式 |
| `wolverine` | 是否開啟自動回血（快速再生） |
| `look_at_entity` | 自動看向周邊可攻擊實體，搭配攻擊指令可自動刷怪 |
| `pickup_items` | 是否開啟物品拾取 |
| `skin` | 是否預設使用建立者造型 |
| `replenish` | 自動補充消耗或使用完的物品 |
| `replace_tools` | GCA 風格的工具替換 |
| `autofish` | 是否開啟自動釣魚 |
| `simulation_distance` | 假人的模擬距離（區塊半徑），`-1` 沿用世界設定 |

- **`replace_tools`**：GCA 風格——非經驗修補工具損壞後、經驗修補工具磨損到 `tool-replacement.remaining-durability-threshold`（預設 `10`）以下時，若背包中有同類物品即更換，不看附魔。
- **`replenish`**：`/fp drop` / `/fp dropstack` 丟空主手後自動補貨，食物與藥水的容器在放得下時歸還。`/fp dropinv` 不會補貨。
- **`simulation_distance`**：以假人為中心的區塊刻半徑；預設寫在 `default-features`，可用 `/fp config set` 或 `/fp set` 覆寫。

## 權限分組說明

<details>
<summary>點擊展開檢視詳情</summary>

每條指令都有獨立權限節點，插件另提供便捷分組：

### 權限組 `fakeplayer.spawn`

基礎管理權限（均為 `fakeplayer.command.*`）：`spawn` `kick` `list` `distance` `select` `selection` `drop` `dropstack` `dropinv` `skin` `invsee` `enderchest` `status` `respawn` `config` `set` `setping`

### 權限組 `fakeplayer.tp`

`fakeplayer.command.tp`、`fakeplayer.command.tphere`、`fakeplayer.command.tps`

### 權限組 `fakeplayer.action`

動作權限：`attack` `mine` `use` `jump` `sneak` `sprint` `look` `turn` `move` `ride` `swap` `sleep` `wakeup` `stop` `hold` `say`（均為 `fakeplayer.command.*`）

特性權限：`fakeplayer.config.replenish`、`fakeplayer.config.replenish.chest`（從附近箱子補貨）、`fakeplayer.config.replace-tools`、`fakeplayer.config.autofish`

> 無需嚴格管控時可直接分配 `fakeplayer.basic`，它包含除 `/fp cmd` 外的全部安全權限。

</details>

## 佔位符變數

| 佔位符 | 作用範圍 | 說明 |
|---|---|---|
| `%fakeplayer_total%` | 全域 | 目前伺服器假人總數 |
| `%fakeplayer_list%` | 全域 | 所有線上假人名稱，分隔符取自 `fakeplayer.placeholder.separator`（預設 `, `） |
| `%fakeplayer_list_<序號>%` | 全域 | 指定序號（由 0 起算）假人的名稱，超出範圍為空 |
| `%fakeplayer_list_<序號>_<屬性>%` | 全域 | 指定序號假人的屬性，如 `%fakeplayer_list_0_uuid%` |
| `%fakeplayer_isfake%` | 任意玩家 | `true` / `false` —— 是否為假人 |
| `%fakeplayer_name%` | 假人 | 假人名稱 |
| `%fakeplayer_uuid%` | 假人 | 假人 UUID |
| `%fakeplayer_creator%` | 假人 | 建立者名稱（`%fakeplayer_spawner%` 為別名） |
| `%fakeplayer_spawntime%` | 假人 | 生成時間，格式取自 `fakeplayer.placeholder.time-format` |
| `%fakeplayer_world%` | 假人 | 所在世界名稱 |
| `%fakeplayer_x%` / `%fakeplayer_y%` / `%fakeplayer_z%` | 假人 | 所在方塊座標 |
| `%fakeplayer_health%` | 假人 | 目前生命值 |
| `%fakeplayer_food%` | 假人 | 目前飢餓值 |
| `%fakeplayer_level%` | 假人 | 目前經驗等級 |
| `%fakeplayer_gamemode%` | 假人 | 目前遊戲模式 |
| `%fakeplayer_actions%` | 假人 | 目前活躍動作，如 `USE\|ATTACK` |
| `%fakeplayer_actions_translated%` | 假人 | 同上，輸出為本地化文字 |

## 自訂本地化翻譯

內建 `en` / `zh` / `zh_tw` / `zh_hk` / `ja`，改 `config.yml` 的 `i18n.locale` 切換（之後 `/fp reload`）。

自訂：在 `plugins/fakeplayer/message/` 放入 `message_<語言>_<地區>.properties`（複製[範本](fakeplayer-core/src/main/resources/message/message.properties)後修改），把 `i18n.locale` 設為對應後綴（如 `zh_cn`），執行 `/fp reload-translation` 生效。檔案必須使用 **UTF-8** 編碼。

## 登入插件相容與聊天事件

裝有登入插件時，假人會因未登入被踢出或凍結。FakePlayer CE 會在假人加入時直接標記為「已登入」；`/fp say` 也會完整觸發聊天事件，使格式化插件正常運作。

| 登入插件 | 相容方式 | 說明 |
|---|---|---|
| AuthMe / AuthMeReloaded | `forceLogin` / `forceRegister` | 經公開 API 標記已登入；未註冊的名稱用隨機短密碼自動註冊 |
| CatSeedLogin / ReCatSeedLogin | 加入登入名單 | 寫入其 `loginPlayers` 名單，不會被 `auto-kick` 踢出 |
| LibreLogin（Paper）/ LibreLoginNext（Paper） | API 授權 | 線上期間暫存無密碼帳號，斷線時清理 |

- **自動生效**：無需設定，僅在裝有對應插件時啟用。

### 自動登入（兩套獨立機制）

| 機制 | 設定 | 需要密碼 |
|---|---|---|
| **登入插件 API 免登入**（推薦） | 無需設定，裝有上面任一插件即生效 | 否 |
| **指令模板登入** | `auto-login.command.enabled: true` | 是（`/fp password` 設定或自動產生） |

第一種直接透過插件 API 放行，假人不會進入登入畫面；第二種以假人身份執行指令，適用於沒有 API 相容的插件：

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
    change-password: "/changepassword %oldpassword% %newpassword% %newpassword%"
  random-password:
    charset: "abcdefghijklmnopqrstuvwxyz0123456789"   # 留空表示不自動產生
    length: 12
```

兩套機制可同時開啟；已被 API 放行時通常不需要指令模板。

### 聊天事件相容（/fp say）

`/fp say` 完整觸發 `AsyncPlayerChatEvent` 與 `AsyncChatEvent`，EssentialsX Chat、Vane 等格式化插件可修改格式與內容；事件未被取消時按事件結果廣播。所有支援版本（`1.20.1 ~ 26.3`）行為一致。

## 常見問題

### 斷開連線：PacketEvents 2.0 failed to inject

部分插件會竄改假人的網路連線物件，修改設定即可：

```yaml
prevent-kicking: ALWAYS
```

### 假人不被怪物攻擊

假人預設開啟無敵模式。執行 `/fp config set invulnerable false` 後才會承受生命值與飢餓值傷害，可搭配生命恢復藥水或烽火台維持生存。

### 假人一段時間後自動掉線

通常是登入插件在踢未登入玩家。上面三個插件已內建相容；若所用插件暫無相容，可改用指令模板登入兜底：

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
```

> 請用足夠強的密碼，避免被 AuthMe 安全策略攔截。

## 專案建置

詳見 [BUILD_zh_TW.md](./BUILD_zh_TW.md)。該文件只適用於本專案的 Gradle 多模組流程，不能用於上游 Maven 架構。
