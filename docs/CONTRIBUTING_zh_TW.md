# 貢獻指南

[English](CONTRIBUTING.md) | [简体中文](CONTRIBUTING_zh.md) | 繁體中文 | [日本語](CONTRIBUTING_ja.md)

感謝願意花時間貢獻。本文說明如何回報問題、程式碼如何組織，以及本倉庫既有的慣例——先讀一遍能少走不少冤枉路。

## 回報問題與功能需求

- 在 <https://github.com/EndlessPixel-Studio/FakePlayer-CE/issues> 提 issue，有範本就用範本。
- 寫上：外掛版本（`fp.buildN`）、伺服器端與 Minecraft 版本、精確步驟、預期與實際結果、相關日誌。
- **安全性漏洞請勿開 issue**，見 [SECURITY_zh_TW.md](SECURITY_zh_TW.md)。

## 專案結構

```
fakeplayer-api     SPI 契約（NMSBridge、NMSEntity、NMSServerPlayer、Action……）
fakeplayer-core    平台層：指令、管理器、設定、HTTP 管理介面、監聽器
versions/*         每個支援的 Minecraft 版本一個 Gradle 模組
fakeplayer-dist    把所有東西打包成單一 fakeplayer-fp.buildN.jar
```

`fakeplayer-core` 不直接碰 NMS 類別。與版本相關的東西一律放進 `versions/fakeplayer-vX`，執行時依伺服器端的 `isSupported()` 選擇模組。部分模組是**橋接模組**，直接沿用另一個模組的實作，所以改「真實」模組通常也涵蓋了它的橋接版本。

### 唯一不能動的地方

`fakeplayer-core` 與 `fakeplayer-api` 編譯時依賴的 **`paper-api` 釘在 `1.21-R0.1-SNAPSHOT`**。這個釘版就是我們支援的最低 API 面，**不要升級**——升了建置會失敗，外掛也無法再跑在 1.20.x 上。較新版本的 API 差異在版本模組裡處理。

## 建置與驗證

- 需要 **JDK 25**（版本模組面向 Paper 26.x 的 class 檔）。
- `./gradlew build` 會編譯全部模組，產物在 `fakeplayer-dist/build/libs/fakeplayer-fp.buildN.jar`。首次建置要下載 Paper dev bundle，需要網路。
- 專案沒有單元測試，驗證方式是「全模組編譯通過 + 在真實伺服器上手動測試」。CI（`.github/workflows/ci.yml`）會對每次 push 與 PR 執行編譯。
- 本機 `test-server/` 目錄（已 git 忽略）適合手動測試：把建置好的 jar 與 CommandAPI 放進 `plugins/`。

## 慣例

### 程式碼風格

- 統一使用 **google-java-format**（2 空格縮排、100 欄），CI 由 `spotlessCheck` 強制。
  每次 push（任意分支）與每個 PR 都會執行 `.github/workflows/format.yml` 檢查格式，格式問題的分支當場就會發現，不必等開 PR。
- 提交前執行一次 `./gradlew spotlessApply`；未格式化的程式碼會讓 CI 失敗。

### 提交

- 英文前綴 + 中文描述：`feat:`、`fix:`、`docs:`、`refactor:`、`ci:`、`chore:`。
- 一個提交做一件事。拆分提交時，CHANGELOG 條目也要跟著拆。
- 在 body 裡引用 issue 或上游提交，例如 `借鑑 xiplugin/FakePlayerPlus@7546fea1`。

### PR

- 小改動（文件、加一個設定項、單檔修正、版本號預置）可以直接推 `master`。
- 較大的功能或跨模組改動走分支 + PR，讓 CI 先跑完整建置。

### CHANGELOG

- 中英雙語，條目數量與順序必須一致。
- 只寫服主能感知的變化（功能、修正、破壞性變更、設定或權限變更）。建置腳本、CI、重構、死碼清理、倉庫檔案都不寫。
- 一條一兩句話；實作細節放在 Release notes 裡。
- 未發布的版本寫 `## fp.buildN - (Not published | 未发布)`，發布時把標題改成日期。

### 設定

- 改動 `fakeplayer-core/src/main/resources/config.yml` 時**必須把頂部 `version:` +1**，並在 CHANGELOG 提一句。
- 新增使用者可見文案要補齊五份 `message/*.properties`（`en`、`zh`、`zh_TW`、`zh_HK`、`ja`），並依現有格式做 ASCII 轉義（`\uXXXX`）。

### 文件

- `README.md`、`README_zh.md`、`README_zh_TW.md`、`README_ja.md` 結構保持一致——加指令、設定項或權限時四份都要改。
- `docs/BUILD*.md` 同理。
- 推 `README.md` 到 `master` 會自動同步 Hangar 專案首頁，內容要保持正確。

### 權限

op 與主控台可以依名稱操作**任意**假人；一般玩家只能操作自己召喚的。任何依名稱解析假人的新程式碼都必須遵循這條（`sender.isOp() ? manager.get(name) : manager.get(sender, name)`）。

### 程式風格

- 只註解不顯然的部分——用一兩行說明「為什麼這樣做」。列舉用法的類別開頭 javadoc、替每個欄位掛註解，都會被要求刪掉。
- 優先使用既有工具（`Schedulers`、`Reflections`、`ActionManager`）；實體操作必須在實體所屬執行緒執行（Folia）。
- 面向使用者的文字走翻譯鍵，不要硬寫字串。

## 翻譯

補充或改進譯文是很合適的第一個貢獻。內建語言為 `en`、`zh`、`zh_tw`、`zh_hk`、`ja`，檔案在 `fakeplayer-core/src/main/resources/message/`。若要新增語言：加檔案、在 `config.yml` 的 `i18n.locale` 註解裡登記、並更新四份 README 的語言列表。

## 行為準則

參與本專案即視為同意遵守 [CODE_OF_CONDUCT_zh_TW.md](CODE_OF_CONDUCT_zh_TW.md)。
