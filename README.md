# FakePlayer CE (Community Edition)

<div align="center">

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.2-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

English | [简体中文](README_zh.md) | [繁體中文](README_zh_TW.md)

---

> **FakePlayer CE** is a community-maintained fork of the original FakePlayer project, rebuilt with Gradle multi-module architecture to deliver **single-jar cross-version compatibility** for Minecraft `1.20.1` through `26.2`.

## ⚠️ Community Edition Statement

This repository is **FakePlayer CE (Community Edition)** — an independent community fork, **NOT the original FakePlayer project**.

- This project is **not maintained, endorsed, or released by the original author** of FakePlayer.
- Refactored and extended to support **cross-version compatibility** from Minecraft `1.20.1` to `26.2` in a single universal jar.
- All issues, bugs, and feature requests should be submitted **exclusively to this repository** — please do not report them upstream.

---

## Overview

FakePlayer is a server-side plugin inspired by [Carpet-Mod](https://github.com/gnembon/fabric-carpet), enabling you to spawn and control realistic fake player entities on your Minecraft server. This CE edition expands the original with multi-version support and long-term maintainability improvements.

📺 [Watch Demo Video](https://youtu.be/NePaDz-P5nI)

## Features

- Spawn fake players that appear fully real to the server — ideal for chunk loading
- Fully compatible with vanilla and plugin commands (e.g., `/ban`, `/tp`, `/invsee`)
- Open and edit fake player inventories via `/fp invsee` or by right-clicking them
- Complete action control: movement, jumping, attacking, mining — with periodic automation
- Per-player personalized default configuration profiles

### FakePlayer CE Exclusive Enhancements

| Enhancement | Description |
|---|---|
| **Single-Jar Multi-Version** | One universal jar serves MC `1.20.1 ~ 26.2` — no per-version downloads |
| **Gradle Kotlin DSL Build** | Migrated from Maven to a modern Gradle multi-module project structure |
| **Isolated NMS Modules** | Version-specific NMS code encapsulated independently, reducing adaptation cost for future releases |
| **Ongoing Compatibility** | Continuous fixes for latest Paper/Purpur builds |
| **HTTP Admin API** | Built-in lightweight HTTP API for querying state, spawning / removing fake players and driving their actions, rotation, inventory and commands |

## Requirements

- [Paper](https://papermc.io) or [Purpur](http://purpurmc.org) server software
- [CommandAPI](https://commandapi.jorel.dev) plugin (any version **except** `10.0.0`)

## Download

FakePlayer CE ships as a **single universal jar** (`fakeplayer-fp.buildX.jar`) that supports Minecraft `1.20.1 ~ 26.2`.

### 1. Stable release (recommended)

Download the latest stable build from the GitHub Releases page:

- 📦 [Release Downloads](https://github.com/EndlessPixel/FakePlayer-CE/releases)

Each release includes the pre-built `fakeplayer-fp.buildX.jar`. Place it into your server's `plugins/` folder.

### 2. Automatic build (CI / nightly)

Every push and pull request triggers a Gradle build via GitHub Actions. You can grab the freshly built jar from the **Artifacts** section of any successful workflow run:

- 🔧 [GitHub Actions](https://github.com/EndlessPixel/FakePlayer-CE/actions)

Open a workflow run, scroll down to **Artifacts**, and download the `fakeplayer-dist` artifact (contains `fakeplayer-fp.buildX.jar`). These builds are useful for testing the latest fixes before a stable release.

> After downloading, put `fakeplayer-fp.buildX.jar` into your server's `plugins/` directory and restart the server. CommandAPI must be installed first.

## Config File

On first launch, FakePlayer generates a template file `config.tmpl.yml`. Rename it to `config.yml` to activate your configuration. This template approach lets you preview new options when upgrading without overwriting your existing settings.

[View sample config](fakeplayer-core/src/main/resources/config.yml)

## Commands

| Command       | Description                               | Permission                   | Note                                                            |
|---------------|-------------------------------------------|------------------------------|-----------------------------------------------------------------|
| /fp spawn     | Spawn a fake player                       | fakeplayer.command.spawn     |                                                                 |
| /fp kick      | Kick fake players (remove from the server) | fakeplayer.command.kick     |                                                                 |
| /fp kickall   | Kick all fake players on the server       | OP                           |                                                                 |
| /fp kill      | Kill fake players (real death, may drop loot) | fakeplayer.command.kill  |                                                                 |
| /fp killall   | Kill all fake players on the server       | OP                           |                                                                 |
| /fp select    | Select a fake player as default           | fakeplayer.command.select    | Available  when player spawned more then 1 fake players         |
| /fp selection | View selected fake player                 | fakeplayer.command.selection | Available  only when player spawned more then 1 fake players    |
| /fp list      | List spawned fake players                 | fakeplayer.command.list      |                                                                 |
| /fp distance  | Show distance to a fake player            | fakeplayer.command.distance  |                                                                 |
| /fp drop      | Drop held item                            | fakeplayer.command.drop      |                                                                 |
| /fp dropstack | Drop entire stack of the held item        | fakeplayer.command.dropstack |                                                                 |
| /fp dropinv   | Drop all items in the inventory           | fakeplayer.command.dropinv   |                                                                 |
| /fp skin      | Copy skin from another player             | fakeplayer.command.skin      | 60 seconds cooldown if copy from a offline player               |
| /fp invsee    | Open an inventory of a fake player        | fakeplayer.command.invsee    | Right-clicking on fake players has the same effect              |
| /fp sleep     | Sleep                                     | fakeplayer.command.sleep     |                                                                 |
| /fp wakeup    | Wake up                                   | fakeplayer.command.wakeup    |                                                                 |
| /fp status    | Show status                               | fakeplayer.command.status    |                                                                 |
| /fp respawn   | Respawn a dead fake player                | fakeplayer.command.respawn   | Available when server config does not kick on fake player death |
| /fp tp        | Teleport to a fake player                 | fakeplayer.command.tp        |                                                                 |
| /fp tphere    | Teleport a fake player to you             | fakeplayer.command.tphere    |                                                                 |
| /fp tps       | Swap positions with fake player           | fakeplayer.command.tps       |                                                                 |
| /fp set       | Change the configuration of a fake player | fakeplayer.command.set       |                                                                 |
| /fp config    | Change default configuration              | fakeplayer.command.config    |                                                                 |
| /fp expme     | Transfer exp to you                       | fakeplayer.command.expme     |                                                                 |
| /fp attack    | Attack                                    | fakeplayer.command.attack    |                                                                 |
| /fp mine      | Mine                                      | fakeplayer.command.mine      |                                                                 |
| /fp use       | Use/Interact/Place                        | fakeplayer.command.use       |                                                                 |
| /fp jump      | Jump                                      | fakeplayer.command.jump      |                                                                 |
| /fp stop      | Stop all actions                          | fakeplayer.command.stop      |                                                                 |
| /fp turn      | Turn around                               | fakeplayer.command.turn      |                                                                 |
| /fp look      | Look at specified location                | fakeplayer.command.look      |                                                                 |
| /fp move      | Move                                      | fakeplayer.command.move      | Typo fix: original typo `mvoe` corrected                        |
| /fp ride      | Ride                                      | fakeplayer.command.ride      |                                                                 |
| /fp sneak     | Sneak                                     | fakeplayer.command.sneak     |                                                                 |
| /fp sprint    | Sprinting                                 | fakeplayer.command.sprint    |                                                                 |
| /fp swap      | Swap main and off-hand items              | fakeplayer.command.swap      |                                                                 |
| /fp hold      | Hold corresponding hotbar item            | fakeplayer.command.hold      |                                                                 |
| /fp cmd       | Execute command                           | fakeplayer.command.cmd       |                                                                 |
| /fp say       | Send chat message                         | fakeplayer.command.say       |                                                                 |
| /fp password  | Set fake player login password (stored in plugin database) | fakeplayer.command.password | Used with auto-login                                           |
| /fp changepassword | Change fake player login password (old password optional) | fakeplayer.command.changepassword |                                                          |
| /fp reload    | Reload config file                        | OP                           |                                                                 |

## HTTP Admin API

FakePlayer CE ships with an optional lightweight HTTP API for remote management (web panels, automation scripts, etc.). It is **disabled by default** and built on the JDK's built-in HTTP server — no extra dependencies required.

Enable it in `config.yml`:

```yaml
http-admin:
  enabled: true          # Enable the HTTP admin API
  host: 0.0.0.0          # Listen address
  port: 3253             # Listen port
  token: ""              # Auth token; if left empty, a random token is generated at startup and printed to the console
  allow-get: true        # Allow GET; when false only POST is accepted
  allowed-hosts: []      # Host / Origin allowlist; empty disables the check (setting it mitigates DNS rebinding)
  rate-limit:
    requests-per-minute: 120   # Requests allowed per source IP per minute
    auth-failures: 10          # Lock the IP after this many consecutive auth failures (0 disables)
    lockout-seconds: 60        # Lockout duration in seconds
  interface:
    # Queries
    list: true           # Enable GET /list
    status: true         # Enable GET /status
    info: true           # Enable GET /info
    # Lifecycle
    spawn: true          # Enable GET /spawn
    kick: true           # Enable GET /kick
    kill: true           # Enable GET /kill
    respawn: true        # Enable GET /respawn
    # Behaviour
    action: true         # Enable GET /action
    stop: true           # Enable GET /stop
    say: true            # Enable GET /say
    teleport: true       # Enable GET /teleport
    look: true           # Enable GET /look
    hold: true           # Enable GET /hold
    swap: true           # Enable GET /swap
    # Misc
    cmd: true            # Enable GET /cmd
    batch: true          # Enable GET /kickall, /killall and /sayall
```

All endpoints accept `GET` by default and also accept `POST` (parameters always travel in the query string). **The token is always passed via the `Authorization: Bearer <token>` header — the `?token=` query parameter is no longer supported**, because a token in a URL ends up in proxy logs, browser history and `Referer` headers, and would have to be repeated in every endpoint's documentation.

Paths are matched **exactly**: only the paths listed below are handled, and `/listfoo` returns `404` instead of falling through to `/list`.

| Endpoint | Description | Success | Failure |
|---|---|---|---|
| `GET /list` | List all online fake players | `{"fakeplayer":["name1","name2"]}` | — |
| `GET /status[?name=<name>]` | Detailed state of a fake player (position, rotation, health, food, exp, game mode, creator, held items, active actions, ...); omit `name` to get every fake player | `{"fakeplayer":[{...}]}` | `{"status":"failure","msg":"..."}` |
| `GET /info` | Plugin and server information (plugin version, MC version, server, fake player count, online players, limits) | `{"plugin":"...","minecraft":"...",...}` | — |
| `GET /spawn?name=<name>` | Spawn a fake player at the main world's spawn point | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kick?name=<name>` | Kick (remove) a fake player | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kill?name=<name>` | Kill a fake player (real death, may drop loot) | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /respawn?name=<name>` | Respawn a dead fake player | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /say?name=<name>&message=<text>` | Send a chat message as a fake player (URL-encode the text) | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /action?name=<name>&action=<action>` | Trigger any action, see the parameters below | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /stop?name=<name>` | Stop every action currently running on the fake player | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /teleport?name=<name>[&world=&x=&y=&z=&yaw=&pitch=]` | Teleport a fake player; omitted parameters keep their current value | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<name>&direction=<direction>` | Make a fake player face `north`/`south`/`east`/`west`/`up`/`down` | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<name>&at=<x,y,z>` | Make a fake player look at the given coordinates | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /look?name=<name>&yaw=<yaw>&pitch=<pitch>` | Set the fake player's rotation directly | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /hold?name=<name>&slot=<1-9>` | Switch the fake player's hotbar slot | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /swap?name=<name>` | Swap the fake player's main hand and off hand items | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /cmd?name=<name>&command=<command>` | Run a command as the fake player | `{"status":"success"}` | `{"status":"failure","msg":"..."}` |
| `GET /kickall` | Remove every fake player on the server | `{"status":"success","count":n}` | — |
| `GET /killall` | Kill every fake player on the server | `{"status":"success","count":n}` | — |
| `GET /sayall?message=<text>` | Make every fake player send a chat message | `{"status":"success","count":n}` | — |

Parameters of `/action`:

| Parameter | Default | Description |
|---|---|---|
| `action` | required | Action name, case insensitive, `-` and `_` are equivalent. Available: `ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | Number of executions; `-1` means keep running |
| `interval` | `0` | Ticks between two executions; supplying only `interval` (without `count`) keeps the action running |
| `wait` | `0` | Ticks to wait before the first execution |
| `message` | — | Required by the `SAY` action only |

Common failure messages:

- Spawn — `The dummy's name conflicts with that of an actual player.` / `The dummy is already online.`
- Kick — `Dummies do not exist.`
- Kill — `The dummy does not exist.` / `The dummy is already dead.`
- Respawn — `The dummy does not exist.` / `The dummy is not dead.`
- Say — `Name is required` / `Message is required`
- Action — `Action is required, available actions: ...` / `Message is required for the say action`
- Teleport — `Invalid number: x` / `Unknown world: xxx`
- Auth & switches — `Unauthorized: pass the token via the Authorization: Bearer header` (401, missing or wrong token) / `Interface disabled` (403, endpoint turned off)
- Rate limiting — `Too many requests` (429, per-IP per-minute limit exceeded) / `Too many failed attempts, try again later` (429, locked out after consecutive auth failures)
- Host & origin — `Host not allowed` (403, the Host header or the cross-origin `Origin` is not allowlisted)
- Other — `Not found` (404, unknown or non-exact path) / `Request URI too long` (414) / `Method not allowed` (405)

Examples:

```bash
# The token always goes in the request header; the URL only carries endpoint parameters
TOKEN="YOUR_TOKEN"
API="http://localhost:3253"

# List fake players
curl -H "Authorization: Bearer $TOKEN" "$API/list"

# Detailed state (omitting name returns every fake player)
curl -G -H "Authorization: Bearer $TOKEN" "$API/status" --data-urlencode "name=klmgun"

# Plugin and server information
curl -H "Authorization: Bearer $TOKEN" "$API/info"

# Spawn a fake player
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"

# Kick a fake player
curl -H "Authorization: Bearer $TOKEN" "$API/kick?name=klmgun"

# Kill a fake player
curl -H "Authorization: Bearer $TOKEN" "$API/kill?name=klmgun"

# Keep attacking every 10 ticks
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"

# Stop every running action
curl -H "Authorization: Bearer $TOKEN" "$API/stop?name=klmgun"

# Teleport a fake player
curl -H "Authorization: Bearer $TOKEN" "$API/teleport?name=klmgun&world=world&x=0&y=64&z=0"

# Make a fake player face east
curl -H "Authorization: Bearer $TOKEN" "$API/look?name=klmgun&direction=east"

# Run a command as a fake player
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"

# Make a fake player say something (URL-encode the message)
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=hello world"
```

### Security

Built-in protections (no configuration required):

- The token is compared in constant time, so it can't be recovered byte by byte from response timing; `Authorization: Bearer` is accepted case-insensitively.
- **Authentication happens before the per-endpoint switch**, so an unauthenticated caller always gets `401` and cannot enumerate which endpoints are enabled.
- **Exact path matching**: only the documented paths are handled, and `/listfoo` returns `404`, so an external ACL/WAF that filters by exact path cannot be bypassed.
- **Log safety**: any parameter whose name contains `token` is redacted, control characters are replaced and length is capped, and unauthenticated requests are logged without their query string — the server log can't be forged or flooded.
- **Rate limiting and lockout**: 120 requests per IP per minute by default, and an IP is locked for 60 seconds after 10 consecutive auth failures.
- **Host / origin checks**: `allowed-hosts` validates the Host header, and cross-origin browser requests (those carrying `Origin`) are rejected unless they match — this mitigates DNS rebinding.
- Request URIs longer than 4096 characters are rejected with `414`, and responses carry `Cache-Control: no-store` so nothing is cached by browsers or proxies.

Still up to the administrator:

- The API has **no TLS**, so the token travels over the network in cleartext. Use a long random token, and only expose the API on a trusted network (or behind a reverse proxy).
- `/cmd` makes a fake player run a command, and its power equals that fake player's permissions — never OP a fake player.
- Prefer `host: 127.0.0.1` (or an internal address) and restrict access at the firewall; never expose this API to the public internet.

## Personal Configuration

Each player can configure their own default settings — changes take effect on the **next fake player spawn**.

Usage examples:
- `/fp config list` — View all configurable items
- `/fp config set collidable false` — Update a specific setting

| Config Item   | Description |
|---------------|-------------|
| `collidable`      | Enable collision box |
| `invulnerable`    | Enable invincible mode |
| `wolverine`       | Enable super heal (rapid regeneration) |
| `look_at_entity`  | Auto-look at nearby attackable entities; combine with `attack` for auto-combat |
| `pickup_items`    | Enable item pickup |
| `skin`            | Use the creator's skin by default |
| `replenish`       | Refill consumed or used items from inventory, including the final item dropped by `/fp drop` or `/fp dropstack` |
| `replace_tools`  | GCA-style replacement: replace non-Mending tools after they break and Mending tools near the durability threshold |
| `autofish`        | Enable auto-fishing |

Set `tool-replacement.remaining-durability-threshold` in `config.yml` to choose the remaining durability threshold for Mending tools (default `10`). Enable it per fake player with `/fp config set replace_tools true`.

Following GCA's default mode, non-Mending tools are replaced after they break. Mending tools are replaced at the threshold when a same-type non-Mending tool or a same-type Mending tool above the threshold is available. Matching uses item type, so enchantment differences do not block a replacement; the first eligible inventory item is selected.

With `replenish` enabled, container-returning consumables such as milk buckets, stews, honey bottles, and drinkable potions are refilled when a matching item is available and the returned bucket, bowl, or bottle fits in inventory. Otherwise, the returned container stays in hand. `/fp dropinv` continues to empty the inventory without refilling it.

## Permissions

<details>
<summary>Click to expand</summary>

Each command has an individual permission node. Convenience permission groups are also provided:

### Permission Group `fakeplayer.spawn`

Includes basic spawn management permissions:
- `fakeplayer.command.spawn` — Create fake players
- `fakeplayer.command.kick` — Kick fake players
- `fakeplayer.command.list` — List fake players
- `fakeplayer.command.distance` — View distance
- `fakeplayer.command.select` — Select fake player
- `fakeplayer.command.selection` — View selected fake player
- `fakeplayer.command.drop` — Drop item
- `fakeplayer.command.dropstack` — Drop entire stack
- `fakeplayer.command.dropinv` — Drop all inventory items
- `fakeplayer.command.skin` — Copy skin
- `fakeplayer.command.invsee` — View inventory
- `fakeplayer.command.status` — View status
- `fakeplayer.command.respawn` — Respawn fake player
- `fakeplayer.command.config` — Set default options
- `fakeplayer.command.set` — Set per-player options

### Permission Group `fakeplayer.tp`

Teleportation permissions:
- `fakeplayer.command.tp`
- `fakeplayer.command.tphere`
- `fakeplayer.command.tps`

### Permission Group `fakeplayer.action`

Action-related permissions:
- `fakeplayer.command.attack` — Attack
- `fakeplayer.command.mine` — Mine
- `fakeplayer.command.use` — Interact / Use
- `fakeplayer.command.jump` — Jump
- `fakeplayer.command.sneak` — Sneak
- `fakeplayer.command.sprint` — Sprint
- `fakeplayer.command.look` — Look
- `fakeplayer.command.turn` — Turn
- `fakeplayer.command.move` — Move
- `fakeplayer.command.ride` — Ride
- `fakeplayer.command.swap` — Swap main/off-hand
- `fakeplayer.command.sleep` — Sleep
- `fakeplayer.command.wakeup` — Wake up
- `fakeplayer.command.stop` — Stop all actions
- `fakeplayer.command.hold` — Switch hotbar
- `fakeplayer.command.say` — Send chat message
- `fakeplayer.config.replenish` — Auto-replenish
- `fakeplayer.config.replenish.chest` — Replenish from nearby chests
- `fakeplayer.config.replace-tools` — Replace worn tools
- `fakeplayer.config.autofish` — Auto-fish

For servers without strict permission management, assign `fakeplayer.basic` — it includes all safe permissions **except** `/fp cmd`.

</details>

## Placeholder Variables

| Placeholder | Description |
|---|---|
| `%fakeplayer_total%` | Total number of fake players on the server |
| `%fakeplayer_creator%` | Creator name of a fake player |
| `%fakeplayer_actions%` | Active actions, e.g., `USE\|ATTACK` |

## Custom Translation

1. Create a `message` folder inside `plugins/fakeplayer/`
2. Copy the [template translation file](fakeplayer-core/src/main/resources/message/message.properties) into the `message` folder
3. Rename it to `message_<language>_<region>.properties` (e.g., `message_en_us.properties`)
4. Edit `config.yml` and set `i18n.locale` to match the suffix (e.g., `en_us`)
5. Run `/fp reload-translation` to apply; if you changed the locale setting, run `/fp reload` first

> **Note:** Translation files must be saved with **UTF-8** encoding.

## Upstream vs. Community Edition

### Original FakePlayer (Upstream)

The original project is the foundation of this fork. It targets a **single fixed Minecraft version** per release and uses a **Maven** build structure.

### FakePlayer CE Changes

1. **Build system**: Migrated from Maven to Gradle Kotlin DSL multi-module project
2. **Cross-version support**: NMS code isolated into version-specific modules covering `1.20.1 ~ 26.2`
3. **Unified release**: Single universal jar replaces per-version artifacts
4. **Ongoing maintenance**: Continuous compatibility updates for latest Paper/Purpur builds
5. **Multi-version fixes**: Targeted bug fixes for cross-version runtime conflicts

> For official FakePlayer updates, please visit the original author's upstream repository.

## FAQ

### Player disconnected: "PacketEvents 2.0 failed to inject"

Some plugins modify the fake player's network connection. Set `prevent-kicking` to `ALWAYS` in your config:

```yaml
# config.yml
prevent-kicking: ALWAYS
```

### Fake players are not attacked by mobs

Fake players spawn with invincible mode enabled by default. Run `/fp config set invulnerable false` to allow them to take damage. Once disabled, they will receive hunger and health effects — consider using regeneration beacons or potions to sustain them.

### Fake players get kicked after a while

Plugins like AuthMe may detect fake players as idle and kick them. Add login commands to the `self-commands` config to prevent this:

```yaml
# Use a strong password to pass AuthMe security checks
self-commands:
  - '/register abc123! abc123!'
  - '/login abc123!'
```

## Build

See [BUILD.md](./BUILD.md) for detailed build instructions.

> This build guide applies to the **FakePlayer CE Gradle multi-module workflow** only — it is not compatible with the original Maven-based build.
