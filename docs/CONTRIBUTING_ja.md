# コントリビューションガイド

[English](CONTRIBUTING.md) | [简体中文](CONTRIBUTING_zh.md) | [繁體中文](CONTRIBUTING_zh_TW.md) | 日本語

時間を割いて貢献してくださりありがとうございます。この文書では、問題の報告方法、コードの構成、このリポジトリの慣例をまとめます。先に一読いただくとレビューの往復が減ります。

## 問題報告・機能要望

- <https://github.com/EndlessPixel-Studio/FakePlayer-CE/issues> で issue を作成してください。テンプレートがあれば利用してください。
- プラグインのビルド（`fp.buildN`）、サーバー実装と Minecraft のバージョン、正確な手順、期待した結果と実際の結果、関連ログを添えてください。
- **セキュリティ上の問題は issue にしないでください。** [SECURITY_ja.md](SECURITY_ja.md) を参照してください。

## プロジェクト構成

```
fakeplayer-api     SPI 契約（NMSBridge、NMSEntity、NMSServerPlayer、Action など）
fakeplayer-core    プラットフォーム層：コマンド、マネージャ、設定、HTTP 管理 API、リスナー
versions/*         対応する Minecraft バージョンごとの Gradle モジュール
fakeplayer-dist    すべてを単一の fakeplayer-fp.buildN.jar にまとめる
```

`fakeplayer-core` は NMS クラスを直接扱いません。バージョン依存の処理は必ず `versions/fakeplayer-vX` に置き、実行時にサーバーの `isSupported()` でモジュールを選びます。一部は**ブリッジモジュール**で、別モジュールの実装をそのまま再エクスポートするため、「実体」のモジュールを直せばブリッジ側も直ることがほとんどです。

### 変えてはいけない設定

`fakeplayer-core` と `fakeplayer-api` は **`paper-api` を `1.21-R0.1-SNAPSHOT` に固定**してコンパイルしています。この固定がサポートする最小 API 面であり、**上げないでください**。上げるとビルドが壊れ、1.20.x で動かなくなります。新しいバージョンの API 差分は各バージョンモジュール内で吸収します。

## ビルドと検証

- **JDK 25** が必要です（バージョンモジュールは Paper 26.x の class ファイルを対象にします）。
- `./gradlew build` で全モジュールをコンパイルし、`fakeplayer-dist/build/libs/fakeplayer-fp.buildN.jar` が生成されます。初回は Paper dev bundle をダウンロードするためネットワークが必要です。
- 単体テストはありません。検証は「全モジュールのコンパイル + 実サーバーでの手動テスト」です。CI（`.github/workflows/ci.yml`）が push と PR ごとにコンパイルを実行します。
- ローカルの `test-server/`（git 管理外）が手動確認に便利です。ビルドした jar と CommandAPI を `plugins/` に置いてください。

## 慣例

### コードスタイル

- **google-java-format**（2 スペースインデント、100 桁）で統一し、CI の `spotlessCheck` で強制します。
  すべての push（全ブランチ）と PR で `.github/workflows/format.yml` がフォーマットを検証するため、整形漏れは PR を開く前の時点で分かります。
- コミット前に `./gradlew spotlessApply` を実行してください。未整形のコードは CI が失敗します。

### コミット

- 英語のプレフィックス + 中国語の説明：`feat:`、`fix:`、`docs:`、`refactor:`、`ci:`、`chore:`。
- 1 コミット 1 変更。コミットを分割するときは CHANGELOG の項目も分けます。
- 本文で issue や上流コミットを参照してください（例：`借鉴 xiplugin/FakePlayerPlus@7546fea1`）。

### プルリクエスト

- 小さな変更（ドキュメント、設定項目の追加、単一ファイルの修正、バージョン番号の準備）は `master` へ直接 push して構いません。
- 大きめの機能や複数モジュールにまたがる変更はブランチ + PR で、CI にフルビルドを確認させてください。

### CHANGELOG

- 英語と中国語の両方を維持し、項目数と順序を一致させます。
- サーバー管理者が認識できる変更のみを書きます（機能、修正、破壊的変更、設定・権限の変更）。ビルドスクリプト、CI、リファクタリング、デッドコード削除、リポジトリのファイルは対象外です。
- 1 項目は 1〜2 文。実装の詳細はリリースノートに書きます。
- 未リリースは `## fp.buildN - (Not published | 未发布)` とし、公開時に見出しを日付へ変えます。

### 設定

- `fakeplayer-core/src/main/resources/config.yml` を変更したら、先頭の `version:` を **+1** し、CHANGELOG にも 1 行追加します。
- ユーザーに見える文言は 5 つの `message/*.properties`（`en`、`zh`、`zh_TW`、`zh_HK`、`ja`）すべてに追加し、既存と同じ ASCII エスケープ（`\uXXXX`）で書きます。

### ドキュメント

- `README.md`、`README_zh.md`、`README_zh_TW.md`、`README_ja.md` は構成を揃えます。コマンド・設定項目・権限を追加したら 4 つとも更新してください。
- `docs/BUILD*.md` も同様です。
- `README.md` を `master` へ push すると Hangar のプロジェクトページが自動更新されるため、内容は正確に保ってください。

### 権限

OP とコンソールは**任意の**偽プレイヤーを名前で操作できますが、一般プレイヤーは自分が生成したものだけを操作できます。名前から偽プレイヤーを解決する新しいコードは必ずこの規則に従ってください（`sender.isOp() ? manager.get(name) : manager.get(sender, name)`）。

### コードスタイル

- 自明でない部分だけコメントします。何をするかではなく「なぜそうするか」を 1〜2 行で。使い方を列挙するクラス javadoc や全フィールドへのコメントは削除を求めます。
- 既存のヘルパー（`Schedulers`、`Reflections`、`ActionManager`）を優先してください。エンティティ操作はそのエンティティのスレッドで行います（Folia）。
- ユーザー向けの文言は必ず翻訳キー経由にし、ハードコードしないでください。

## 翻訳

翻訳の追加・改善は最初の貢献として最適です。組み込みロケールは `en`、`zh`、`zh_tw`、`zh_hk`、`ja` で、ファイルは `fakeplayer-core/src/main/resources/message/` にあります。新しい言語を追加する場合は、ファイルを追加し、`config.yml` の `i18n.locale` コメントに登録し、4 つの README のロケール一覧も更新してください。

## 行動規範

本プロジェクトへの参加は [CODE_OF_CONDUCT_ja.md](CODE_OF_CONDUCT_ja.md) に同意したものとみなします。
