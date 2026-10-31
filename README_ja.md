# FakePlayer CE（コミュニティ版）

<div align="center">

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

[English](README.md) | [简体中文](README_zh.md) | [繁體中文](README_zh_TW.md) | 日本語

---

> **FakePlayer CE** は [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer) のコミュニティメンテナンス版です。[Carpet-Mod](https://github.com/gnembon/fabric-carpet) に着想を得たサーバーサイドプラグインで、サーバーから見て実プレイヤーと区別できない偽プレイヤーを生成します。Gradle のマルチモジュール構成により **Minecraft `1.20.1` 〜 `26.3` を単一 Jar でカバー**します。

⭐ FakePlayer CE が気に入ったら、ぜひ Star をお願いします。プロジェクトの成長に大きく役立ちます。

## ⚠️ コミュニティ版について

- 本リポジトリは**独立したコミュニティ版**であり、原作者による配布・メンテナンス・承認を受けたものではありません。
- 不具合報告と機能要望は**必ず本リポジトリへ**お願いします。上流リポジトリへは送らないでください。

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

配布物は単一の `fakeplayer-fp.buildX.jar` です。サーバーの `plugins/` に置いて再起動してください（CommandAPI を先に導入する必要があります）。

- **安定版**：[GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases)（推奨）
- **最新ビルド**：[GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → 成功した実行 → Artifacts → `fakeplayer-dist`

## 設定ファイル

初回起動時はテンプレートの `config.tmpl.yml` のみが生成されます。`config.yml` にリネームすると有効になります。この仕組みにより、アップグレード時に既存の設定を上書きせず、追加された項目だけを確認できます。記入例は [config.yml](fakeplayer-core/src/main/resources/config.yml) を参照してください。

## AI チャット

`/fp aichat` を使うと、OpenAI 互換エンドポイント経由で偽プレイヤーに発言させられます。`plugins/FakePlayer-CE/ai-chat.yml` に `api` と `api-key`（または `api-key-env` で指定した環境変数）を設定し、`enabled: true` にしてから `/fp reload` を実行してください。

| コマンド | 説明 |
|---|---|
| `/fp aichat public [once\|continuous\|interval <ticks>] [偽プレイヤー]` | 公共チャットへ送信。`continuous` は `stop` まで発言を続ける |
| `/fp aichat private <プレイヤー> [once\|continuous\|interval <ticks>] [偽プレイヤー]` | 特定のプレイヤーにのみ送信 |
| `/fp aichat stop [偽プレイヤー]` | 連続発言を停止 |

推奨は `auto-reply` です。プレイヤーが反応に値する発言をしたときだけ返答し、短すぎる発言・記号だけの発言・連投は事前に除外されます。頻度は `chance` と `cooldown-seconds` で調整します。`continuous` はタイマー発言で、既定では無効、`allow-continuous: true` が必要です。人設・プロンプト・保持する文脈・発言間隔は `system-prompt` / `turn-prompt` / `max-context` / `recent-chat-lines` / `interval-ticks` で設定します。

## コマンド一覧

| コマンド | 説明 | 権限ノード | 備考 |
|------|---------|---------|------|
| /fp spawn | 偽プレイヤーを生成 | fakeplayer.command.spawn | |
| /fp spawnuuid | 指定した UUID で偽プレイヤーを生成 | fakeplayer.command.spawn.uuid | `<名前> <UUID> [ワールド] [座標]`。名前が別の UUID に紐付いている場合は拒否 |
| /fp kick | 偽プレイヤーをキック（サーバーから削除） | fakeplayer.command.kick | |
| /fp kickall | サーバー上の全偽プレイヤーをキック | OP | |
| /fp kill | 偽プレイヤーを倒す（実際に死亡し、ドロップあり） | fakeplayer.command.kill | |
| /fp killall | サーバー上の全偽プレイヤーを倒す | OP | |
| /fp select | 既定の操作対象として選択 | fakeplayer.command.select | 複数生成した場合に使用 |
| /fp selection | 選択中の偽プレイヤーを表示 | fakeplayer.command.selection | |
| /fp list | 生成中の偽プレイヤーを一覧表示 | fakeplayer.command.list | |
| /fp distance | 偽プレイヤーとの距離を表示 | fakeplayer.command.distance | |
| /fp drop | 手持ちのアイテムを 1 つ捨てる | fakeplayer.command.drop | |
| /fp dropstack | 手持ちのスタックを丸ごと捨てる | fakeplayer.command.dropstack | |
| /fp dropinv | インベントリの全アイテムを捨てる | fakeplayer.command.dropinv | |
| /fp skin | 他のプレイヤーのスキンをコピー | fakeplayer.command.skin | SkinsRestorer があれば経由。オフライン名は 60 秒のクールダウン |
| /fp invsee | 偽プレイヤーのインベントリを開く | fakeplayer.command.invsee | 右クリックでも可 |
| /fp enderchest | 偽プレイヤーのエンダーチェストを開く | fakeplayer.command.enderchest | 別名 `/fp ec`。スニーク右クリックでも可、同一ワールド必須 |
| /fp sleep | 寝る | fakeplayer.command.sleep | |
| /fp wakeup | 起こす | fakeplayer.command.wakeup | |
| /fp status | 状態を表示 | fakeplayer.command.status | |
| /fp respawn | 倒れた偽プレイヤーを復活させる | fakeplayer.command.respawn | `kick-on-dead: false` が必要 |
| /fp tp | 偽プレイヤーの位置へテレポート | fakeplayer.command.tp | |
| /fp tphere | 偽プレイヤーを自分の位置へテレポート | fakeplayer.command.tphere | |
| /fp tps | 偽プレイヤーと位置を入れ替える | fakeplayer.command.tps | |
| /fp set | 個々の偽プレイヤーの設定を変更 | fakeplayer.command.set | |
| /fp setping | 偽プレイヤーの表示 ping を設定 | fakeplayer.command.setping | `0` で実際の値を使用 |
| /fp config | 既定設定を変更 | fakeplayer.command.config | コンソール可。`[プレイヤー]` で他人を指定 |
| /fp expme | 経験値を自分に移す | fakeplayer.command.expme | |
| /fp attack | 攻撃 | fakeplayer.command.attack | |
| /fp mine | 採掘 | fakeplayer.command.mine | |
| /fp use | 使用・設置・インタラクト | fakeplayer.command.use | |
| /fp jump | ジャンプ | fakeplayer.command.jump | |
| /fp stop | すべての動作を停止 | fakeplayer.command.stop | |
| /fp turn | その場で向きを変える | fakeplayer.command.turn | |
| /fp look | 指定座標の方向を見る | fakeplayer.command.look | |
| /fp move | 指定方向へ移動 | fakeplayer.command.move | |
| /fp goto | 指定座標まで歩く | fakeplayer.command.goto | 障害物を避けて自動で経路探索。到達不能な目標は拒否 |
| /fp ride | 騎乗 | fakeplayer.command.ride | |
| /fp sneak | スニーク | fakeplayer.command.sneak | |
| /fp sprint | ダッシュ | fakeplayer.command.sprint | |
| /fp swap | メインハンドとオフハンドを入れ替え | fakeplayer.command.swap | |
| /fp hold | ホットバーの指定スロットを選択 | fakeplayer.command.hold | |
| /fp cmd | 偽プレイヤーとしてコマンドを実行 | fakeplayer.command.cmd | 高リスク |
| /fp say | チャットメッセージを送信 | fakeplayer.command.say | フォーマット系プラグインと共存可 |
| /fp aichat | AI で発言内容を生成 | fakeplayer.command.aichat | `ai-chat.yml` が必要 |
| /fp password | 偽プレイヤーのログインパスワードを設定（プラグイン DB に保存） | fakeplayer.command.password | `auto-login.command` と併用 |
| /fp changepassword | 偽プレイヤーのログインパスワードを変更（旧パスワードは任意） | fakeplayer.command.changepassword | |
| /fp reload | 設定を再読み込み | OP | |

## HTTP 管理 API

任意で有効化する軽量な HTTP API です（既定は無効、追加依存なし）。Web パネルや自動化スクリプト向けです。

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1        # ループバックまたは内部アドレスを推奨
  port: 3253
  token: ""              # 空にすると起動時にランダムなトークンを生成しコンソールへ出力
  allow-get: true        # false の場合は POST のみ受け付ける
  allowed-hosts: []      # Host / Origin の許可リスト。空ならチェックしない
  rate-limit:
    requests-per-minute: 120   # 送信元 IP ごと
    auth-failures: 10          # 認証失敗が連続したときにロックする回数（0 で無効）
    lockout-seconds: 60
  interface:
    list: true           # 各エンドポイントに同名のスイッチがあり、無効時は 403
```

- トークンは `Authorization: Bearer <token>` ヘッダーのみで送ります。**`?token=` には対応していません**。
- パラメータは常にクエリ文字列で渡し、`GET` / `POST` のどちらも受け付けます。

| エンドポイント | 説明 |
|---|---|
| `GET /list` | オンラインの偽プレイヤーを一覧表示 |
| `GET /status[?name=<名前>]` | 詳細状態（座標、向き、体力、満腹度、経験値、ゲームモード、作成者、手持ち、実行中の動作）。`name` を省略すると全員分 |
| `GET /info` | プラグインとサーバーの情報（バージョン、サーバー、偽プレイヤー数、オンラインプレイヤー数、上限） |
| `GET /spawn?name=<名前>` | メインワールドのスポーン地点に偽プレイヤーを生成 |
| `GET /kick?name=<名前>` | 偽プレイヤーをキック（削除） |
| `GET /kill?name=<名前>` | 偽プレイヤーを倒す（実際に死亡し、ドロップあり） |
| `GET /respawn?name=<名前>` | 倒れた偽プレイヤーを復活させる |
| `GET /say?name=<名前>&message=<内容>` | 偽プレイヤーとして発言（内容は URL エンコード） |
| `GET /aichat?name=<名前>[&mode=public\|private][&target=<プレイヤー>][&continuous=true][&interval=<tick>]` | AI で発言させる。`ai-chat.yml` が必要。`continuous=true` は `/aichat/stop` まで継続 |
| `GET /aichat/stop?name=<名前>` | その偽プレイヤーの AI 連続発言を停止 |
| `GET /action?name=<名前>&action=<動作>` | 動作を実行。パラメータは下表 |
| `GET /stop?name=<名前>` | 実行中の動作をすべて停止 |
| `GET /teleport?name=<名前>[&world=&x=&y=&z=&yaw=&pitch=]` | テレポート。省略したパラメータは現在値を維持 |
| `GET /look?name=<名前>&direction=<方向>` | `north`/`south`/`east`/`west`/`up`/`down` の方向を向く |
| `GET /look?name=<名前>&at=<x,y,z>` | 指定座標を見る |
| `GET /look?name=<名前>&yaw=<角度>&pitch=<角度>` | 向きを直接設定 |
| `GET /hold?name=<名前>&slot=<1-9>` | ホットバーのスロットを切り替え |
| `GET /swap?name=<名前>` | メインハンドとオフハンドを入れ替え |
| `GET /goto?name=<名前>&x=&y=&z=[&world=]` | 座標まで歩く（到達不能な目標は拒否） |
| `GET /setping?name=<名前>&ping=<ms>` | Tab リストの ping を設定 |
| `GET /sneak?name=<名前>[&state=true\|false]` | スニークの切り替え（または設定） |
| `GET /sprint?name=<名前>[&state=true\|false]` | ダッシュの切り替え（または設定） |
| `GET /reload` | `config.yml` と翻訳ファイルを再読み込み |
| `GET /cmd?name=<名前>&command=<コマンド>` | 偽プレイヤーとしてコマンドを実行 |
| `GET /kickall` / `/killall` / `/sayall?message=` | 一括削除・一括撃破・一括発言 |

`/action` のパラメータ：

| パラメータ | 既定値 | 説明 |
|---|---|---|
| `action` | 必須 | 動作名。大文字小文字を区別せず、`-` と `_` は同等：`ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | 実行回数。`-1` は継続 |
| `interval` | `0` | 実行間隔の tick。`interval` のみ指定すると継続動作 |
| `wait` | `0` | 初回実行までの待ち tick |
| `message` | — | `SAY` でのみ必須 |

成功時は `{"status":"success"}`（一括系は `count` を追加）を返し、失敗時は `{"status":"failure","msg":"..."}` で理由を返します。HTTP ステータス：`401` トークン不正、`403` エンドポイント無効または Host 不許可、`404` パス不存在、`405` メソッド不正、`414` URI 長すぎ、`429` レート制限またはロック中。

```bash
TOKEN="YOUR_TOKEN"; API="http://localhost:3253"

curl -H "Authorization: Bearer $TOKEN" "$API/list"
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=hello world"
```

## セキュリティ

設定不要で有効な保護：**認証はエンドポイントのスイッチより先に評価**されるため、`401` / `403` の差で有効なエンドポイントを探ることはできません。ログではトークンを伏せ字にし制御文字を置換。IP 単位のレート制限と連続失敗時のロック。任意の `allowed-hosts` による Host / `Origin` チェック。レスポンスには `Cache-Control: no-store` を付与。

管理者側で必要な対応：

- API に **TLS はありません**。十分に長いランダムなトークンを使い、信頼できるネットワークかリバースプロキシの内側にのみ公開してください。
- `/cmd` は偽プレイヤーとして、その偽プレイヤーの権限でコマンドを実行します。**偽プレイヤーを OP にしないでください**。
- `host: 127.0.0.1` を推奨し、ファイアウォールでも送信元を制限してください。

## 個人設定

プレイヤーごとに既定値を保存できます。変更は**次回の生成時**から有効です。確認は `/fp config list`、変更は `/fp config set <項目> <値>` を使います。

| 設定項目 | 説明 |
|--------|------|
| `collidable` | 当たり判定を有効化 |
| `invulnerable` | 無敵モードを有効化 |
| `wolverine` | 超回復（高速再生）を有効化 |
| `look_at_entity` | 周囲の攻撃可能なエンティティを自動で見る。攻撃と組み合わせると自動戦闘 |
| `pickup_items` | アイテムの拾い上げを有効化 |
| `skin` | 作成者のスキンを既定で使用 |
| `replenish` | 消費・使用したアイテムを自動補充 |
| `replace_tools` | GCA 風のツール交換 |
| `autofish` | 自動釣りを有効化 |
| `simulation_distance` | 偽プレイヤーのシミュレーション距離（チャンク半径）。`-1` でワールド設定を使用 |

- **`replace_tools`**：GCA 風。修繕なしのツールは破損後に、修繕付きのツールは `tool-replacement.remaining-durability-threshold`（既定 `10`）を下回ったときに、インベントリ内の同種アイテムと交換します。エンチャントは考慮しません。
- **`replenish`**：`/fp drop` と `/fp dropstack` でメインハンドを補充します。食料や飲み物の容器は収まる場合に返却されます。`/fp dropinv` では補充しません。
- **`simulation_distance`**：偽プレイヤー周囲で刻まれるチャンク半径。既定値は `default-features` に書き、`/fp config set` または `/fp set` で上書きできます。

## 権限

<details>
<summary>クリックで展開</summary>

コマンドごとに個別の権限ノードがあり、利便性のためにグループも用意されています。

### 権限グループ `fakeplayer.spawn`

基本管理（すべて `fakeplayer.command.*`）：`spawn` `kick` `list` `distance` `select` `selection` `drop` `dropstack` `dropinv` `skin` `invsee` `enderchest` `status` `respawn` `config` `set` `setping`

### 権限グループ `fakeplayer.tp`

`fakeplayer.command.tp`、`fakeplayer.command.tphere`、`fakeplayer.command.tps`

### 権限グループ `fakeplayer.action`

動作：`attack` `mine` `use` `jump` `sneak` `sprint` `look` `turn` `move` `ride` `swap` `sleep` `wakeup` `stop` `hold` `say`（すべて `fakeplayer.command.*`）

機能：`fakeplayer.config.replenish`、`fakeplayer.config.replenish.chest`（近くのチェストから補充）、`fakeplayer.config.replace-tools`、`fakeplayer.config.autofish`

> 厳密な権限管理が不要なサーバーでは `fakeplayer.basic` を付与すれば、`/fp cmd` を除く安全な権限がすべて含まれます。

</details>

## プレースホルダー変数

| プレースホルダー | スコープ | 説明 |
|---|---|---|
| `%fakeplayer_total%` | 全体 | サーバー上の偽プレイヤー総数 |
| `%fakeplayer_list%` | 全体 | オンラインの全偽プレイヤー名。区切りは `fakeplayer.placeholder.separator`（既定 `, `） |
| `%fakeplayer_list_<番号>%` | 全体 | 指定番号（0 始まり）の偽プレイヤー名。範囲外は空 |
| `%fakeplayer_list_<番号>_<属性>%` | 全体 | その偽プレイヤーの属性。例：`%fakeplayer_list_0_uuid%` |
| `%fakeplayer_isfake%` | 任意のプレイヤー | `true` / `false` —— 偽プレイヤーかどうか |
| `%fakeplayer_name%` | 偽プレイヤー | 名前 |
| `%fakeplayer_uuid%` | 偽プレイヤー | UUID |
| `%fakeplayer_creator%` | 偽プレイヤー | 作成者名（`%fakeplayer_spawner%` は別名） |
| `%fakeplayer_spawntime%` | 偽プレイヤー | 生成時刻。形式は `fakeplayer.placeholder.time-format` |
| `%fakeplayer_world%` | 偽プレイヤー | ワールド名 |
| `%fakeplayer_x%` / `%fakeplayer_y%` / `%fakeplayer_z%` | 偽プレイヤー | ブロック座標 |
| `%fakeplayer_health%` | 偽プレイヤー | 現在の体力 |
| `%fakeplayer_food%` | 偽プレイヤー | 現在の満腹度 |
| `%fakeplayer_level%` | 偽プレイヤー | 現在の経験値レベル |
| `%fakeplayer_gamemode%` | 偽プレイヤー | 現在のゲームモード |
| `%fakeplayer_actions%` | 偽プレイヤー | 実行中の動作。例：`USE\|ATTACK` |
| `%fakeplayer_actions_translated%` | 偽プレイヤー | 同上をローカライズしたもの |

## 翻訳のカスタマイズ

組み込みロケール：`en` / `zh` / `zh_tw` / `zh_hk` / `ja`。`config.yml` の `i18n.locale` で切り替えます（その後 `/fp reload`）。

独自翻訳：`message_<言語>_<地域>.properties`（[テンプレート](fakeplayer-core/src/main/resources/message/message.properties)をコピー）を `plugins/fakeplayer/message/` に置き、`i18n.locale` を対応するサフィックス（例：`en_us`）に設定して `/fp reload-translation` を実行します。ファイルは **UTF-8** で保存してください。

## ログインプラグイン互換とチャットイベント

ログインプラグインがあると、偽プレイヤーは未ログインとしてキックまたは凍結されます。FakePlayer CE は参加時に「ログイン済み」として扱い、`/fp say` はチャットイベントを正しく発火させるため、整形系プラグインも通常どおり動作します。

| ログインプラグイン | 互換方式 | 備考 |
|---|---|---|
| AuthMe / AuthMeReloaded | `forceLogin` / `forceRegister` | 公開 API でログイン済みに設定。未登録名はランダムな短いパスワードで自動登録 |
| CatSeedLogin / ReCatSeedLogin | ログインリスト | `loginPlayers` リストへ書き込むため `auto-kick` の対象になりません |
| LibreLogin（Paper）/ LibreLoginNext（Paper） | API 認可 | オンライン中は一時的なパスワードなしレコードを作成し、切断時に削除 |

- **自動で有効**：設定は不要で、該当プラグインが導入されている場合のみ使用されます。

### 自動ログイン（独立した 2 つの仕組み）

| 仕組み | 設定 | パスワード |
|---|---|---|
| **ログインプラグイン API による免除**（推奨） | 不要。上記いずれかが入っていれば有効 | 不要 |
| **コマンドテンプレートによるログイン** | `auto-login.command.enabled: true` | 必要（`/fp password` で設定、または自動生成） |

前者はログイン画面を経由しません。後者は偽プレイヤーとしてコマンドを実行するため、API 互換のないプラグインでも利用できます。

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
    change-password: "/changepassword %oldpassword% %newpassword% %newpassword%"
  random-password:
    charset: "abcdefghijklmnopqrstuvwxyz0123456789"   # 空にすると自動生成しない
    length: 12
```

両方を同時に有効にできます。API 免除が機能している場合は、コマンドテンプレートは通常不要です。

### チャットイベント互換（`/fp say`）

`/fp say` は `AsyncPlayerChatEvent` と `AsyncChatEvent` を完全に発火させるため、EssentialsX Chat や Vane などの整形プラグインが書式と内容を書き換えられます。イベントがキャンセルされない限り、その結果に従ってブロードキャストされます。対応するすべてのバージョン（`1.20.1 ~ 26.3`）で同じ挙動です。

## FAQ

### プレイヤーが切断される：「PacketEvents 2.0 failed to inject」

一部のプラグインが偽プレイヤーのネットワーク接続を書き換えます。設定に以下を追加してください。

```yaml
prevent-kicking: ALWAYS
```

### 偽プレイヤーがモブに攻撃されない

偽プレイヤーは無敵モードで生成されます。`/fp config set invulnerable false` を実行するとダメージを受けるようになります。回復のビーコンやポーションで生存を維持することを検討してください。

### しばらくすると偽プレイヤーがキックされる

多くは未ログインのプレイヤーをキックするログインプラグインが原因です。上記 3 つのプラグインは自動対応していますが、それ以外の場合はコマンドテンプレートによるログインに切り替えてください。

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
```

> AuthMe のセキュリティポリシーに弾かれないよう、十分に強いパスワードを使用してください。

## ビルド

[BUILD.md](./BUILD.md) を参照してください。本プロジェクトの Gradle マルチモジュール手順専用であり、上流の Maven ビルドには適用できません。
