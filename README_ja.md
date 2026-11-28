<div align="center">

<img src=".github/assets/logo.png" alt="FakePlayer CE" width="200">

# FakePlayer CE（コミュニティ版）

[English](README.md) | [简体中文](README_zh.md) | [繁體中文](README_zh_TW.md) | 日本語

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Hangar](https://img.shields.io/badge/Hangar-FakePlayer--CE-1f7ae0)](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

---

> **FakePlayer CE** は [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer) のコミュニティメンテナンス版です。[Carpet-Mod](https://github.com/gnembon/fabric-carpet) に着想を得たサーバーサイドプラグインで、サーバーから見て実プレイヤーと区別できない偽プレイヤーを生成します。Gradle のマルチモジュール構成により **Minecraft `1.20.1` 〜 `26.3` を単一 Jar でカバー**します。

⭐ FakePlayer CE が気に入ったら、ぜひ Star をお願いします。プロジェクトの成長に大きく役立ちます。

## 目次

- [⚠️ コミュニティ版について](#️-コミュニティ版について)
- [クイックスタート](#クイックスタート)
- [機能](#機能)
- [動作要件](#動作要件)
- [ダウンロード](#ダウンロード)
- [設定ファイル](#設定ファイル)
- [データベース](#データベース)
- [AI チャット](#ai-チャット)
- [コマンド一覧](#コマンド一覧)
- [HTTP 管理 API](#http-管理-api)
- [セキュリティ](#セキュリティ)
- [個人設定](#個人設定)
- [権限](#権限)
- [プレースホルダー変数](#プレースホルダー変数)
- [翻訳のカスタマイズ](#翻訳のカスタマイズ)
- [ログインプラグイン互換とチャットイベント](#ログインプラグイン互換とチャットイベント)
- [FAQ](#faq)
  - [プレイヤーが切断される：「PacketEvents 2.0 failed to inject」](#プレイヤーが切断されるpacketevents-20-failed-to-inject)
  - [偽プレイヤーがモブに攻撃されない](#偽プレイヤーがモブに攻撃されない)
  - [しばらくすると偽プレイヤーがキックされる](#しばらくすると偽プレイヤーがキックされる)
- [スター履歴](#スター履歴)
- [コントリビューター](#コントリビューター)

## ⚠️ コミュニティ版について

- 本リポジトリは**独立したコミュニティ版**であり、原作者による配布・メンテナンス・承認を受けたものではありません。

## クイックスタート

1. [CommandAPI](https://commandapi.jorel.dev) を `plugins/` に導入する
2. `fakeplayer-fp.buildX.jar` を `plugins/` に入れる
3. サーバーを一度起動し、`config.tmpl.yml` を `config.yml` にリネームする
4. `/fp spawn` を実行する

## 機能

- サーバーから見て完全に実プレイヤーとして扱われる偽プレイヤーを生成——チャンクの読み込み維持に最適
- バニラおよびプラグインのコマンドと完全互換（テレポート、BAN、インベントリ編集など）
- 移動・ジャンプ・攻撃・採掘など動作を完全に操作でき、周期的な自動化にも対応
- プレイヤーごとに個別の既定設定プロファイルを保存
- **単一 Jar で多数のバージョンに対応**：1 つの Jar で `1.20.1 ~ 26.3` をカバーし、NMS コードはバージョンごとに分離
- **HTTP 管理 API**：状態の取得、偽プレイヤーの生成・削除、動作・向き・インベントリ・コマンドの遠隔操作
- **ログインプラグイン互換**：AuthMe / CatSeedLogin / LibreLogin は設定不要

## 動作要件

- [Paper](https://papermc.io) または [Purpur](http://purpurmc.org) サーバー
- [CommandAPI](https://commandapi.jorel.dev)（`10.0.0` **以外**のバージョン。**MC 26.3 では `12.1.0` 以上が必要**）

## ダウンロード

- **安定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推奨）
- **Hangar**：[hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
- **最新ビルド**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 成功した実行 → Artifacts → `fakeplayer-dist`

## 設定ファイル

初回起動時はテンプレートの `config.tmpl.yml` のみが生成されます。`config.yml` にリネームすると有効になります。記入例は [config.yml](fakeplayer-core/src/main/resources/config.yml) を参照してください。

**権限別の生成上限**：`spawn-limit` にたとえば `vip: 10` を追加し、`fakeplayer.spawn.limit.vip` 権限を付与してください。複数一致した場合は最大値を採用し、OP は上限の対象外です。

## データベース

データは既定で SQLite（`plugins/FakePlayer-CE/data.db`）に保存されます。MySQL を使う場合は空のデータベースを作成し、`config.yml` の `database.type` を `mysql` に、`database.mysql` を埋めてから再起動してください。

## AI チャット

`/fp aichat` を使うと、OpenAI 互換エンドポイント経由で偽プレイヤーに発言させられます。`plugins/FakePlayer-CE/ai-chat.yml` に `api` と `api-key`（または `api-key-env` で指定した環境変数）を設定し、`enabled: true` にしてから `/fp reload` を実行してください。

| コマンド | 説明 |
|---|---|
| `/fp aichat public [once\|continuous\|interval <ticks>] [偽プレイヤー]` | 公共チャットへ送信 |
| `/fp aichat private <プレイヤー> [once\|continuous\|interval <ticks>] [偽プレイヤー]` | 特定のプレイヤーにのみ送信 |
| `/fp aichat stop [偽プレイヤー]` | 連続発言を停止 |
| `/fp aichat model [名前]` | AI モデルの表示・切り替え（OP のみ） |

`ai-chat.yml` の `models:` に複数のモデルを定義できます。`auto-reply` も同ファイルで有効化できます。

## コマンド一覧

偽プレイヤー名を取るコマンドは `-a` で操作可能な全員に一括適用できます。`select`、`spawn`/`spawnuuid`、`rename` は対象外です。

| コマンド | 権限ノード |
|------|---------|
| /fp spawn | fakeplayer.command.spawn |
| /fp spawnjson | fakeplayer.command.spawn.json |
| /fp spawnuuid（非推奨） | fakeplayer.command.spawn.uuid |
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

## HTTP 管理 API

任意で有効化する HTTP API です（既定は無効）。Web パネルや自動化スクリプト向けです。

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

トークンは `Authorization: Bearer <token>` ヘッダーで送ります。パラメータはクエリ文字列で渡し、`GET` / `POST` のどちらも受け付けます。

| エンドポイント | 説明 |
|---|---|
| `/list`、`/status`、`/info` | 偽プレイヤーとサーバー状態の取得 |
| `/spawn`、`/kick`、`/kill`、`/respawn` | ライフサイクル |
| `/say`、`/aichat`、`/aichat/stop`、`/action`、`/stop` | チャットと動作 |
| `/teleport`、`/look`、`/hold`、`/swap`、`/goto`、`/setping`、`/sneak`、`/sprint` | 移動と状態 |
| `/reload`、`/cmd`、`/kickall`、`/killall`、`/sayall` | 管理 |

## セキュリティ

認証はエンドポイントのスイッチより先に評価されます。IP 単位のレート制限と任意の `allowed-hosts` チェックを内蔵しています。

- API に **TLS はありません**。信頼できるネットワークかリバースプロキシの内側にのみ公開してください。
- `/cmd` は偽プレイヤーとしてコマンドを実行します。**偽プレイヤーを OP にしないでください**。
- `host: 127.0.0.1` を推奨し、ファイアウォールでも送信元を制限してください。

## 個人設定

プレイヤーごとに既定値を保存できます。変更は**次回の生成時**から有効です。`/fp config list` と `/fp config set <項目> <値>` を使います。

| 設定項目 | 説明 |
|--------|------|
| `collidable` | 当たり判定を有効化 |
| `invulnerable` | 無敵モードを有効化 |
| `wolverine` | 高速再生を有効化 |
| `look_at_entity` | 周囲の攻撃可能なエンティティを自動で見る |
| `pickup_items` | アイテムの拾い上げを有効化 |
| `skin` | 作成者のスキンを既定で使用 |
| `replenish` | 消費したアイテムを自動補充 |
| `replace_tools` | GCA 風のツール交換 |
| `autofish` | 自動釣りを有効化 |
| `aichat_fake_inventory` | AI が偽プレイヤーのインベントリを読むことを許可 |
| `aichat_questioner_inventory` | AI が質問者のインベントリを読むことを許可 |
| `simulation_distance` | シミュレーション距離。`-1` でワールド設定を使用 |

## 権限

コマンドごとに個別の権限ノードがあり、利便性のためのグループは `plugin.yml` に定義されています。

## プレースホルダー変数

PlaceholderAPI を導入すると、`%fakeplayer_total%`、`%fakeplayer_list%`、`%fakeplayer_isfake%` や、`%fakeplayer_name%`、`%fakeplayer_world%`、`%fakeplayer_health%` などの属性を使用できます。

## 翻訳のカスタマイズ

組み込みロケール：`en` / `zh` / `zh_tw` / `zh_hk` / `ja`。`config.yml` の `i18n.locale` で切り替え、`/fp reload` を実行します。プレイヤー単位では `/fp lang <コード>` を使います。

## ログインプラグイン互換とチャットイベント

AuthMe / CatSeedLogin / LibreLogin が導入されている場合、偽プレイヤーは自動的にログイン済みとして扱われます。設定は不要です。`/fp say` は通常のチャットイベントを発火させます。

## FAQ

### プレイヤーが切断される：「PacketEvents 2.0 failed to inject」

`config.yml` に `prevent-kicking: ALWAYS` を設定してください。

### 偽プレイヤーがモブに攻撃されない

偽プレイヤーは無敵モードで生成されます。`/fp config set invulnerable false` を実行するとダメージを受けるようになります。

### しばらくすると偽プレイヤーがキックされる

多くは未ログインをキックするログインプラグインが原因です。上記 3 つは自動対応しています。それ以外では `auto-login.command` テンプレートを利用してください。

## スター履歴

[![Star History Chart](https://api.star-history.com/svg?repos=EndlessPixel-Studio/FakePlayer-CE&type=Date)](https://star-history.com/#EndlessPixel-Studio/FakePlayer-CE&Date)

## コントリビューター

[![Contributors](https://contrib.rocks/image?repo=EndlessPixel-Studio/FakePlayer-CE)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/graphs/contributors)
