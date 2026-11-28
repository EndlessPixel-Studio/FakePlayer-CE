<div align="center">

<img src=".github/assets/logo.png" alt="FakePlayer CE" width="200">

# FakePlayer CE (Community Edition)

English | [简体中文](README_zh.md) | [繁體中文](README_zh_TW.md) | [日本語](README_ja.md)

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Hangar](https://img.shields.io/badge/Hangar-FakePlayer--CE-1f7ae0)](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

---

> **FakePlayer CE** is a community-maintained fork of [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer): a server-side plugin inspired by [Carpet-Mod](https://github.com/gnembon/fabric-carpet) that spawns fake players indistinguishable from real ones, rebuilt with Gradle multi-module architecture for **single-jar compatibility from Minecraft `1.20.1` to `26.3`**.

⭐ If you like FakePlayer CE, please give it a star — it really helps the project grow.

## Table of Contents

- [⚠️ Community Edition Statement](#️-community-edition-statement)
- [Quick Start](#quick-start)
- [Features](#features)
- [Requirements](#requirements)
- [Download](#download)
- [Config File](#config-file)
- [Database](#database)
- [AI Chat](#ai-chat)
- [Commands](#commands)
- [HTTP Admin API](#http-admin-api)
- [Security](#security)
- [Personal Configuration](#personal-configuration)
- [Permissions](#permissions)
- [Placeholder Variables](#placeholder-variables)
- [Custom Translation](#custom-translation)
- [Login plugin compatibility & chat events](#login-plugin-compatibility--chat-events)
- [FAQ](#faq)
  - [Player disconnected: "PacketEvents 2.0 failed to inject"](#player-disconnected-packetevents-20-failed-to-inject)
  - [Fake players are not attacked by mobs](#fake-players-are-not-attacked-by-mobs)
  - [Fake players get kicked after a while](#fake-players-get-kicked-after-a-while)
- [Star History](#star-history)
- [Contributors](#contributors)

## ⚠️ Community Edition Statement

- This repository is an **independent community fork** — not released, maintained or endorsed by the original author.

## Quick Start

1. Install [CommandAPI](https://commandapi.jorel.dev) into `plugins/`
2. Put `fakeplayer-fp.buildX.jar` into `plugins/`
3. Start the server once, then rename `config.tmpl.yml` to `config.yml`
4. Run `/fp spawn`

## Features

- Spawn fake players that appear fully real to the server — ideal for chunk loading
- Fully compatible with vanilla and plugin commands (teleport, ban, inventory editing, ...)
- Complete action control: movement, jumping, attacking, mining, with periodic automation
- Per-player personalized default configuration profiles
- **Single jar, many versions**: one jar covers `1.20.1 ~ 26.3`, with NMS code isolated per version
- **HTTP Admin API**: query state, spawn / remove fake players and drive actions, rotation, inventory and commands remotely
- **Login plugin compatibility**: AuthMe / CatSeedLogin / LibreLogin need no configuration

## Requirements

- [Paper](https://papermc.io) or [Purpur](http://purpurmc.org) server
- [CommandAPI](https://commandapi.jorel.dev) (any version **except** `10.0.0`; **MC 26.3 requires `12.1.0` or newer**)

## Download

- **Stable**: [GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases) (recommended)
- **Hangar**: [hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
- **Latest build**: [GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → any successful run → Artifacts → `fakeplayer-dist`

## Config File

On first launch only a template `config.tmpl.yml` is generated; rename it to `config.yml` to activate it. See the [sample config](fakeplayer-core/src/main/resources/config.yml).

**Per-permission spawn limits:** set entries under `spawn-limit` (for example `vip: 10`) and grant `fakeplayer.spawn.limit.vip`. OPs are not limited.

## Database

Data lives in SQLite (`plugins/FakePlayer-CE/data.db`) by default. To use MySQL, create an empty database and set `database.type: mysql` plus the `database.mysql` block in `config.yml`, then restart.

## AI Chat

`/fp aichat` lets fake players generate chat lines through an OpenAI-compatible endpoint. Fill in `api` and `api-key` (or the environment variable named by `api-key-env`) in `plugins/FakePlayer-CE/ai-chat.yml`, set `enabled: true`, then run `/fp reload`.

| Command | Description |
|---|---|
| `/fp aichat public [once\|continuous\|interval <ticks>] [fake player]` | Send to public chat |
| `/fp aichat private <player> [once\|continuous\|interval <ticks>] [fake player]` | Send to one player only |
| `/fp aichat stop [fake player]` | Stop continuous mode |
| `/fp aichat model [name]` | Show or switch the AI model (OP only) |

Multiple models can be configured under `models:`. `auto-reply` can be enabled in `ai-chat.yml`.

## Commands

Any command that takes a fake-player name also accepts `-a` to apply it to every fake player you can see. `select`, `spawn`/`spawnuuid` and `rename` are excluded.

| Command | Permission |
|------|------|
| /fp spawn | fakeplayer.command.spawn |
| /fp spawnjson | fakeplayer.command.spawn.json |
| /fp spawnuuid (deprecated) | fakeplayer.command.spawn.uuid |
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

## HTTP Admin API

Optional HTTP API (disabled by default) for web panels and automation.

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

Send the token in the `Authorization: Bearer <token>` header. Parameters go in the query string; `GET` and `POST` are both accepted.

| Endpoint | Description |
|---|---|
| `/list`, `/status`, `/info` | Query fake players and server state |
| `/spawn`, `/kick`, `/kill`, `/respawn` | Lifecycle |
| `/say`, `/aichat`, `/aichat/stop`, `/action`, `/stop` | Chat and actions |
| `/teleport`, `/look`, `/hold`, `/swap`, `/goto`, `/setping`, `/sneak`, `/sprint` | Movement and state |
| `/reload`, `/cmd`, `/kickall`, `/killall`, `/sayall` | Administration |

## Security

Authentication is checked before the endpoint switch. Rate limiting per IP and optional `allowed-hosts` checks are built in.

- The API has **no TLS** — expose it only on a trusted network or behind a reverse proxy.
- `/cmd` runs commands as the fake player; **never OP a fake player**.
- Prefer `host: 127.0.0.1` and restrict access at the firewall.

## Personal Configuration

Each player can save their own defaults; changes apply on the **next spawn**. Use `/fp config list` and `/fp config set <key> <value>`.

| Config Item | Description |
|--------|------|
| `collidable` | Enable collision box |
| `invulnerable` | Enable invincible mode |
| `wolverine` | Enable rapid regeneration |
| `look_at_entity` | Auto-look at nearby attackable entities |
| `pickup_items` | Enable item pickup |
| `skin` | Use the creator's skin by default |
| `replenish` | Refill used items |
| `replace_tools` | GCA-style tool replacement |
| `autofish` | Enable auto-fishing |
| `aichat_fake_inventory` | Allow AI to read the fake player's inventory |
| `aichat_questioner_inventory` | Allow AI to read the asking player's inventory |
| `simulation_distance` | Simulation distance; `-1` keeps the world setting |

## Permissions

Each command has its own permission node. Convenience groups are defined in `plugin.yml`.

## Placeholder Variables

With PlaceholderAPI installed, use `%fakeplayer_total%`, `%fakeplayer_list%`, `%fakeplayer_isfake%` and player properties such as `%fakeplayer_name%`, `%fakeplayer_world%`, `%fakeplayer_health%`.

## Custom Translation

Built-in locales: `en`, `zh`, `zh_tw`, `zh_hk`, `ja`. Switch with `i18n.locale` in `config.yml` and `/fp reload`, or per player with `/fp lang <code>`.

## Login plugin compatibility & chat events

Fake players are marked as logged in when AuthMe, CatSeedLogin or LibreLogin is installed — no configuration needed. `/fp say` triggers the normal chat events so formatter plugins work.

## FAQ

### Player disconnected: "PacketEvents 2.0 failed to inject"

Set `prevent-kicking: ALWAYS` in `config.yml`.

### Fake players are not attacked by mobs

They spawn invincible. Run `/fp config set invulnerable false` to let them take damage.

### Fake players get kicked after a while

Usually a login plugin kicking unauthenticated players. Built-in compatibility covers AuthMe / CatSeedLogin / LibreLogin; otherwise use the `auto-login.command` templates.

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=EndlessPixel-Studio/FakePlayer-CE&type=Date)](https://star-history.com/#EndlessPixel-Studio/FakePlayer-CE&Date)

## Contributors

[![Contributors](https://contrib.rocks/image?repo=EndlessPixel-Studio/FakePlayer-CE)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/graphs/contributors)
