# ビルドガイド

[English](BUILD.md) | [简体中文](BUILD_zh.md) | [繁體中文](BUILD_zh_TW.md) | 日本語

本プロジェクトは **paperweight-userdev** Gradle プラグインを使って NMS 依存関係を自動処理します。BuildTools を手動で用意する必要はありません。

## 前提条件

- **JDK 25**（Paper 1.21+ の NMS をコンパイルするため）
- インターネット接続（初回ビルドで Paper サーバー Jar をダウンロードします）

## クイックビルド

```bash
./gradlew build --no-daemon
```

出力される Jar の場所：

```
fakeplayer-dist/build/libs/fakeplayer-fp.buildX.jar
```

## 仕組み

各バージョンモジュール（例：`fakeplayer-v1_21_6`）は `paperweight.paperDevBundle()` で対応する Paper dev bundle を宣言します。プラグインは以下を自動で行います。

1. 適切な Paper サーバー Jar をダウンロード
2. NMS クラスをリマップ・難読化解除
3. コンパイル専用の依存関係として公開

これにより、BuildTools を実行したりローカルの Maven リポジトリを管理したりせずに、NMS のソースへ完全にアクセスできます。
