# Contributing to FakePlayer CE

English | [简体中文](CONTRIBUTING_zh.md) | [繁體中文](CONTRIBUTING_zh_TW.md) | [日本語](CONTRIBUTING_ja.md)

Thanks for taking the time to contribute. This document covers how to report problems, how the
codebase is organised, and the conventions this repository follows — reading it first will save
you a round of review comments.

## Reporting bugs and requesting features

- Open an issue at <https://github.com/EndlessPixel-Studio/FakePlayer-CE/issues>. If a template
  exists, use it.
- Include the plugin build (`fp.buildN`), server software and Minecraft version, exact steps,
  expected vs. actual behaviour, and the relevant log lines.
- For **security problems, do not open an issue** — see [SECURITY.md](SECURITY.md).

## Project layout

```
fakeplayer-api     SPI contracts (NMSBridge, NMSEntity, NMSServerPlayer, Action…)
fakeplayer-core    platform layer: commands, managers, config, HTTP admin API, listeners
versions/*         one Gradle module per supported Minecraft version
fakeplayer-dist    packages everything into a single fakeplayer-fp.buildN.jar
```

`fakeplayer-core` never touches NMS classes directly. Anything version-specific belongs in
`versions/fakeplayer-vX`; the runtime picks the module whose `isSupported()` matches the server.
Some modules are *bridges* that re-export another module's implementation, so a fix in the
"real" module usually covers its bridges too.

### The one thing you must not change

`fakeplayer-core` and `fakeplayer-api` compile against **`paper-api` pinned at
`1.21-R0.1-SNAPSHOT`**. That pin is the minimum API surface we support. Do not bump it to a newer
version — the build will break and the plugin would stop working on 1.20.x. Newer API differences
are handled inside the version modules.

## Building and testing

- **JDK 25** is required (the version modules target Paper 26.x class files).
- `./gradlew build` compiles every module and produces `fakeplayer-dist/build/libs/fakeplayer-fp.buildN.jar`.
  The first build downloads Paper dev bundles, so it needs network access.
- There is no unit-test suite; verification is a full multi-module compile plus manual testing on
  a real server. CI (`.github/workflows/ci.yml`) runs the compile for every push and pull request.
- A local `test-server/` directory (git-ignored) is handy for manual checks — drop the built jar
  and CommandAPI into `plugins/`.

## Conventions

### Commits

- English prefix + a description in Chinese: `feat:`, `fix:`, `docs:`, `refactor:`, `ci:`, `chore:`.
- One commit per change. If you split a feature, split the CHANGELOG entry along with it.
- Reference the issue or upstream commit in the body, e.g. `借鉴 xiplugin/FakePlayerPlus@7546fea1`.

### Pull requests

- Small changes (docs, a config key, a single-file fix, version bump) can go straight to `master`.
- Larger features or changes touching many modules go through a branch and a pull request, so CI
  can verify the full build first.

### CHANGELOG

- `CHANGELOG.md` is bilingual: keep the English and Chinese lists the same length and order.
- Only document changes a server owner can perceive (features, fixes, breaking changes, config or
  permission changes). Build scripts, CI, refactors, dead-code cleanup and repository files are
  not changelog material.
- One or two sentences per entry; put implementation detail in the release notes, not here.
- Unreleased work goes under `## fp.buildN - (Not published | 未发布)`; the heading becomes the
  release date when it ships.

### Configuration

- Any change to `fakeplayer-core/src/main/resources/config.yml` **bumps the `version:` number** at
  the top of the file and gets a CHANGELOG line.
- New user-visible messages must be added to all five `message/*.properties` files
  (`en`, `zh`, `zh_TW`, `zh_HK`, `ja`), ASCII-escaped (`\uXXXX`) like the existing entries.

### Documentation

- `README.md`, `README_zh.md`, `README_zh_TW.md` and `README_ja.md` are kept structurally
  identical — when you add a command, a config option or a permission, update all four.
- `docs/BUILD*.md` follows the same rule.
- Pushing `README.md` to `master` automatically republishes the Hangar project page, so keep it
  accurate.

### Permissions

Operators and the console may address **any** fake player by name; regular players may only
address fake players they spawned. Any new code path that resolves a fake player by name must
follow this rule (`sender.isOp() ? manager.get(name) : manager.get(sender, name)`).

### Code style

- Comment only what is not obvious — a short line explaining *why* something is done that way.
  Class-level javadoc listing every usage, or a comment on every field, will be asked for removal.
- Prefer the existing helpers (`Schedulers`, `Reflections`, `ActionManager`) over ad-hoc calls;
  entity work must run on the entity's own thread (Folia).
- User-facing text goes through the translation keys, never as a hard-coded string.

## Translations

Adding or improving a translation is a great first contribution. The built-in locales are `en`,
`zh`, `zh_tw`, `zh_hk` and `ja`; message files live in
`fakeplayer-core/src/main/resources/message/`. If you want to add a new locale, add the file,
register it in the `i18n.locale` comment in `config.yml`, and update the four READMEs' locale list.

## Code of conduct

Participation in this project is covered by [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
