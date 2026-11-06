# 贡献指南

[English](CONTRIBUTING.md) | 简体中文 | [繁體中文](CONTRIBUTING_zh_TW.md) | [日本語](CONTRIBUTING_ja.md)

感谢愿意花时间贡献。本文说明如何报问题、代码怎么组织，以及本仓库的既有约定——先读一遍能少走不少弯路。

## 报告问题与功能需求

- 在 <https://github.com/EndlessPixel-Studio/FakePlayer-CE/issues> 提 issue，有模板就用模板。
- 写上：插件版本（`fp.buildN`）、服务端与 Minecraft 版本、精确步骤、预期与实际结果、相关日志。
- **安全漏洞不要开 issue**，见 [SECURITY_zh.md](SECURITY_zh.md)。

## 项目结构

```
fakeplayer-api     SPI 契约（NMSBridge、NMSEntity、NMSServerPlayer、Action……）
fakeplayer-core    平台层：命令、管理器、配置、HTTP 管理接口、监听器
versions/*         每个受支持的 Minecraft 版本一个 Gradle 模块
fakeplayer-dist    把所有东西打包成单一 fakeplayer-fp.buildN.jar
```

`fakeplayer-core` 不直接碰 NMS 类。与版本相关的东西一律放进 `versions/fakeplayer-vX`，运行时按服务端的 `isSupported()` 选择模块。部分模块是**桥接模块**，直接复用另一个模块的实现，所以改「真实」模块通常也覆盖了它的桥接版本。

### 唯一不能动的地方

`fakeplayer-core` 与 `fakeplayer-api` 编译时依赖的 **`paper-api` 钉在 `1.21-R0.1-SNAPSHOT`**。这个钉版就是我们支持的最低 API 面，**不要升级**——升了构建会挂，插件也无法再跑在 1.20.x 上。高版本的 API 差异在版本模块里处理。

## 构建与验证

- 需要 **JDK 25**（版本模块面向 Paper 26.x 的 class 文件）。
- `./gradlew build` 会编译全部模块，产物在 `fakeplayer-dist/build/libs/fakeplayer-fp.buildN.jar`。首次构建要下载 Paper dev bundle，需要联网。
- 项目没有单元测试，验证方式是「全模块编译通过 + 在真实服务端手动测试」。CI（`.github/workflows/ci.yml`）会对每次 push 和 PR 跑编译。
- 本地 `test-server/` 目录（已 git 忽略）适合手测：把构建好的 jar 和 CommandAPI 丢进 `plugins/`。

## 约定

### 代码风格

- 统一使用 **google-java-format**（2 空格缩进、100 列），CI 里由 `spotlessCheck` 强制。
- 提交前跑一次 `./gradlew spotlessApply`；未格式化的代码会让 CI 失败。

### 提交

- 英文前缀 + 中文描述：`feat:`、`fix:`、`docs:`、`refactor:`、`ci:`、`chore:`。
- 一个提交做一件事。拆提交时，CHANGELOG 条目也要跟着拆。
- 在 body 里引用 issue 或上游提交，例如 `借鉴 xiplugin/FakePlayerPlus@7546fea1`。

### PR

- 小改动（文档、加个配置项、单文件修复、版本号预置）可以直接推 `master`。
- 较大的功能或跨模块改动走分支 + PR，让 CI 先跑完整构建。

### CHANGELOG

- 中英双语，条目数量与顺序必须一致。
- 只写服主能感知的变化（功能、修复、破坏性变更、配置或权限变更）。构建脚本、CI、重构、死代码清理、仓库文件都不写。
- 一条一两句话；实现细节放在 Release notes 里。
- 未发布的版本写 `## fp.buildN - (Not published | 未发布)`，发布时把标题改成日期。

### 配置

- 改动 `fakeplayer-core/src/main/resources/config.yml` 时**必须把顶部 `version:` +1**，并在 CHANGELOG 提一句。
- 新增用户可见文案要补全五份 `message/*.properties`（`en`、`zh`、`zh_TW`、`zh_HK`、`ja`），并按现有格式做 ASCII 转义（`\uXXXX`）。

### 文档

- `README.md`、`README_zh.md`、`README_zh_TW.md`、`README_ja.md` 结构保持一致——加命令、配置项或权限时四份都要改。
- `docs/BUILD*.md` 同理。
- 推 `README.md` 到 `master` 会自动同步 Hangar 项目主页，内容要保持准确。

### 权限

op 与控制台可以按名字操作**任意**假人；普通玩家只能操作自己召唤的。任何按名字解析假人的新代码都必须遵循这条（`sender.isOp() ? manager.get(name) : manager.get(sender, name)`）。

### 代码风格

- 只注释不显然的部分——用一两行说明「为什么这样做」。罗列用法的类头 javadoc、给每个字段挂注释，都会被要求删掉。
- 优先使用已有工具（`Schedulers`、`Reflections`、`ActionManager`）；实体操作必须在实体所属线程执行（Folia）。
- 面向用户的文本走翻译键，不要硬编码字符串。

## 翻译

补充或改进译文是很合适的第一个贡献。内置语言为 `en`、`zh`、`zh_tw`、`zh_hk`、`ja`，文件在 `fakeplayer-core/src/main/resources/message/`。若要新增语言：加文件、在 `config.yml` 的 `i18n.locale` 注释里登记、并更新四份 README 的语言列表。

## 行为准则

参与本项目即视为同意遵守 [CODE_OF_CONDUCT_zh.md](CODE_OF_CONDUCT_zh.md)。
