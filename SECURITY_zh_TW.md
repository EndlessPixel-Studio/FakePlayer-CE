# 安全性說明

[English](SECURITY.md) | [简体中文](SECURITY_zh.md) | 繁體中文 | [日本語](SECURITY_ja.md)

FakePlayer CE 會產生真實的伺服器玩家，並提供選用的 HTTP 管理介面——設定不當等於把伺服器控制權交給別人。本文說明支援範圍、如何回報問題，以及如何安全地執行本外掛。

## 支援範圍

只有最新發行的版本會收到安全性修正，舊版本不再維護。請從 [GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases) 或 [Hangar](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE) 更新到最新的 `fp.buildN`。

| 版本 | 是否支援 |
|---|---|
| 最新發行的 `fp.buildN` | ✅ |
| 更早的版本 | ❌ |

## 回報漏洞

**請勿為安全性問題開公開 issue。**

請使用 GitHub 的私密回報：開啟倉庫的 [Security 頁籤](https://github.com/EndlessPixel-Studio/FakePlayer-CE/security) → *Report a vulnerability*，產生的 advisory 僅維護者可見。

請附上：

- 外掛版本（`/version FakePlayer-CE` 或 jar 檔名）
- 伺服器端類型與版本（Paper / Purpur / Leaf / Folia，Minecraft 版本）
- 重現步驟、預期結果與實際結果
- 相關設定（記得遮蔽 token）與日誌片段

我們會儘快確認收到。這是志工專案，請給幾天時間。修正發布後，我們很樂意在 advisory 與更新日誌中註明你的貢獻。

## HTTP 管理介面

HTTP 介面**預設關閉**，且每個端點都有獨立開關。啟用後請留意以下幾點。

### 外掛已經做了什麼

| 防護 | 行為 |
|---|---|
| 權杖驗證 | 只接受 `Authorization: Bearer <token>`。**不支援 `?token=`**——查詢字串會進入代理日誌與瀏覽器歷史。`token` 留空時會於啟動時產生隨機值並輸出到主控台。 |
| 驗證先於路由 | 驗證在端點開關之前判定，`401` / `403` 無法被用來探測哪些端點已啟用。 |
| 固定時間比較 | 權杖以 `MessageDigest.isEqual` 比較，而非 `String.equals`。 |
| 日誌脫敏 | 記錄前會隱藏權杖並替換控制字元。 |
| 限流 | 依來源 IP 限流，連續驗證失敗後鎖定。 |
| Host / Origin 白名單 | `allowed-hosts` 會拒絕 Host 或跨來源 Origin 不符的請求，降低 DNS rebinding 風險。 |
| 禁止快取 | 回應帶 `Cache-Control: no-store`。 |

### 仍需你自己負責的部分

- **介面沒有 TLS。** 它走的是明文 HTTP。請綁定 `127.0.0.1`，或放在負責 TLS 的反向代理之後，並在防火牆層面限制來源。
- **`/cmd` 以假人身分執行指令**，權限等同該假人。這是最危險的端點——不需要時請關閉 `http-admin.interface.cmd`，並且**絕不要給假人 OP**：任何能操作該假人的人都會繼承這些權限。
- **保管好權杖。** 把它當 root 密碼對待。一旦出現在截圖、日誌或公開倉庫裡，立即更換。
- **只暴露需要的端點。** 每個端點都有 `http-admin.interface` 下的獨立開關，用不到的請關閉。

### 加固清單

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1          # 只監聽本機，或放在反向代理之後
  token: ""                # 留空會隨機產生；建議自行設定足夠長的隨機值
  allowed-hosts: []        # 透過代理對外暴露時填你的網域
  rate-limit:
    requests-per-minute: 120
    auth-failures: 10
    lockout-seconds: 60
  interface:
    cmd: false             # 用不到的端點都關閉
```

如果介面要暴露到 `localhost` 之外，還應：

- 在代理層終止 TLS，並只允許代理的位址存取
- 在權杖之外再加一層驗證（mTLS、Basic Auth 或 SSO 閘道）
- 留意外掛日誌中反覆出現的 `401` / `429`

## 範圍

屬於範圍：外掛本體（指令處理、HTTP 介面、`versions/` 下的 NMS 模組、登入外掛相容、更新檢查、本倉庫的 Hangar / GitHub 發布工作流程）。

不屬於範圍：需要先攻陷伺服器才能觸發的問題、主動給假人 OP 權限、在不受支援的伺服器端版本上使用 `forced-execution: true`，以及依文件正常使用功能所造成的破壞（例如 `/fp cmd`）。
