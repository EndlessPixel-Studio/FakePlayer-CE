<div align="center">

<img src=".github/assets/logo.png" alt="FakePlayer CE" width="200">

# FakePlayer CE (Community Edition)

English | [简体中文](README_zh.md) | [繁體中文](README_zh_TW.md) | [日本語](README_ja.md)

[![CI](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml/badge.svg)](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/EndlessPixel-Studio/FakePlayer-CE)](LICENSE.txt)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1%20~%2026.3-5fbb47)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)
[![Hangar](https://img.shields.io/badge/Hangar-FakePlayer--CE-1f7ae0)](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE)
[![Built with JDK 25](https://img.shields.io/badge/Built%20with-JDK%2025-ED8B00?logo=openjdk&logoColor=white)](https://www.java.com)
[![Platforms](https://img.shields.io/badge/Platforms-Paper%20%7C%20Spigot%20%7C%20Purpur-2d2d2d)](https://github.com/EndlessPixel-Studio/FakePlayer-CE)

</div>

---

> **FakePlayer CE** is a community-maintained fork of [FakePlayer](https://github.com/tanyaofei/minecraft-fakeplayer): a server-side plugin inspired by [Carpet-Mod](https://github.com/gnembon/fabric-carpet) that spawns fake players indistinguishable from real ones, rebuilt with Gradle multi-module architecture for **single-jar compatibility from Minecraft `1.20.1` to `26.3`**.

⭐ If you like FakePlayer CE, please give it a star — it really helps the project grow.

## ⚠️ Community Edition Statement

- This repository is an **independent community fork** — not released, maintained or endorsed by the original author.
- Report all bugs and feature requests **only to this repository**, never upstream.

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

FakePlayer CE ships as a single `fakeplayer-fp.buildX.jar`. Drop it into your server's `plugins/` folder and restart (CommandAPI must be installed first).

- **Stable**: [GitHub Releases](https://github.com/EndlessPixel-Studio/FakePlayer-CE/releases) (recommended)
- **Hangar**: [hangar.papermc.io/EndlessPixel/FakePlayer-CE](https://hangar.papermc.io/EndlessPixel/FakePlayer-CE) — download and update from the web panel
- **Latest build**: [GitHub Actions](https://github.com/EndlessPixel-Studio/FakePlayer-CE/actions) → any successful run → Artifacts → `fakeplayer-dist`

## Config File

On first launch only a template `config.tmpl.yml` is generated; rename it to `config.yml` to activate it. The template lets you preview new options when upgrading without overwriting existing settings. See the [sample config](fakeplayer-core/src/main/resources/config.yml).

## AI Chat

`/fp aichat` lets fake players generate chat lines through an OpenAI-compatible endpoint. Fill in `api` and `api-key` (or the environment variable named by `api-key-env`) in `plugins/FakePlayer-CE/ai-chat.yml`, set `enabled: true`, then run `/fp reload`.

| Command | Description |
|---|---|
| `/fp aichat public [once\|continuous\|interval <ticks>] [fake player]` | Send to public chat; `continuous` keeps talking until `stop` |
| `/fp aichat private <player> [once\|continuous\|interval <ticks>] [fake player]` | Send to one player only |
| `/fp aichat stop [fake player]` | Stop continuous mode |

`auto-reply` is the recommended mode: the fake player answers only when a player says something worth answering (short, symbol-only and spam messages are filtered), paced by `chance` and `cooldown-seconds`. `continuous` talks on a timer, is off by default and needs `allow-continuous: true`. Persona, prompts, retained context and pacing live in `system-prompt`, `turn-prompt`, `max-context`, `recent-chat-lines` and `interval-ticks`.

## Commands

| Command | Description | Permission | Note |
|------|---------|---------|------|
| /fp spawn | Spawn a fake player | fakeplayer.command.spawn | |
| /fp spawnuuid | Spawn a fake player with a given UUID | fakeplayer.command.spawn.uuid | `<name> <uuid> [world] [location]`; a name already bound to another UUID is rejected |
| /fp kick | Kick fake players (remove from the server) | fakeplayer.command.kick | |
| /fp kickall | Kick all fake players on the server | OP | |
| /fp kill | Kill fake players (real death, may drop loot) | fakeplayer.command.kill | |
| /fp killall | Kill all fake players on the server | OP | |
| /fp select | Select a fake player as default | fakeplayer.command.select | After spawning more than one |
| /fp selection | View selected fake player | fakeplayer.command.selection | |
| /fp list | List spawned fake players | fakeplayer.command.list | |
| /fp distance | Show distance to a fake player | fakeplayer.command.distance | |
| /fp drop | Drop held item | fakeplayer.command.drop | |
| /fp dropstack | Drop entire stack of the held item | fakeplayer.command.dropstack | |
| /fp dropinv | Drop all items in the inventory | fakeplayer.command.dropinv | |
| /fp skin | Copy skin from another player | fakeplayer.command.skin | SkinsRestorer if installed; 60 s cooldown for offline names |
| /fp invsee | Open a fake player's inventory | fakeplayer.command.invsee | Right-click also works |
| /fp enderchest | Open a fake player's ender chest | fakeplayer.command.enderchest | Alias `/fp ec`; shift-right-click; same world |
| /fp sleep | Sleep | fakeplayer.command.sleep | |
| /fp wakeup | Wake up | fakeplayer.command.wakeup | |
| /fp status | Show status | fakeplayer.command.status | |
| /fp respawn | Respawn a dead fake player | fakeplayer.command.respawn | Needs `kick-on-dead: false` |
| /fp tp | Teleport to a fake player | fakeplayer.command.tp | |
| /fp tphere | Teleport a fake player to you | fakeplayer.command.tphere | |
| /fp tps | Swap positions with a fake player | fakeplayer.command.tps | |
| /fp set | Change one fake player's configuration | fakeplayer.command.set | |
| /fp setping | Set the ping displayed for a fake player | fakeplayer.command.setping | `0` uses the real value |
| /fp config | Change default configuration | fakeplayer.command.config | Console-capable; `[player]` = someone else |
| /fp expme | Transfer exp to you | fakeplayer.command.expme | |
| /fp attack | Attack | fakeplayer.command.attack | |
| /fp mine | Mine | fakeplayer.command.mine | |
| /fp use | Use / interact / place | fakeplayer.command.use | |
| /fp jump | Jump | fakeplayer.command.jump | |
| /fp stop | Stop all actions | fakeplayer.command.stop | |
| /fp turn | Turn around | fakeplayer.command.turn | |
| /fp look | Look at specified location | fakeplayer.command.look | |
| /fp move | Move | fakeplayer.command.move | |
| /fp goto | Walk to the specified coordinates | fakeplayer.command.goto | Pathfinds around obstacles; rejects unreachable targets; `/fp goto stop` cancels |
| /fp ride | Ride | fakeplayer.command.ride | |
| /fp sneak | Sneak | fakeplayer.command.sneak | |
| /fp sprint | Sprint | fakeplayer.command.sprint | |
| /fp swap | Swap main and off-hand items | fakeplayer.command.swap | |
| /fp hold | Hold the hotbar item in a slot | fakeplayer.command.hold | |
| /fp cmd | Execute a command as the fake player | fakeplayer.command.cmd | High risk |
| /fp say | Send a chat message | fakeplayer.command.say | Formatter-friendly |
| /fp aichat | Generate chat lines with AI | fakeplayer.command.aichat | Requires `ai-chat.yml` |
| /fp password | Set fake player login password (stored in the plugin database) | fakeplayer.command.password | Used with `auto-login.command` |
| /fp changepassword | Change fake player login password (old password optional) | fakeplayer.command.changepassword | |
| /fp reload | Reload config | OP | |

## HTTP Admin API

Optional lightweight HTTP API (disabled by default, no extra dependencies) for web panels and automation.

```yaml
http-admin:
  enabled: true
  host: 127.0.0.1        # Prefer loopback or an internal address
  port: 3253
  token: ""              # Empty generates a random token at startup and prints it to the console
  allow-get: true        # false accepts POST only
  allowed-hosts: []      # Host / Origin allowlist; empty disables the check
  rate-limit:
    requests-per-minute: 120   # Per source IP
    auth-failures: 10          # Consecutive auth failures before lockout (0 disables)
    lockout-seconds: 60
  interface:
    list: true           # Every endpoint has a switch of the same name; disabled returns 403
```

- The token goes in the `Authorization: Bearer <token>` header only; **`?token=` is not supported**.
- Parameters always travel in the query string; `GET` and `POST` are both accepted.

| Endpoint | Description |
|---|---|
| `GET /list` | List all online fake players |
| `GET /status[?name=<name>]` | Detailed state (position, rotation, health, food, exp, gamemode, creator, held items, active actions); omit `name` for every fake player |
| `GET /info` | Plugin and server information (versions, server, fake player count, online players, limits) |
| `GET /spawn?name=<name>` | Spawn a fake player at the main world spawn |
| `GET /kick?name=<name>` | Kick (remove) a fake player |
| `GET /kill?name=<name>` | Kill a fake player (real death, may drop loot) |
| `GET /respawn?name=<name>` | Respawn a dead fake player |
| `GET /say?name=<name>&message=<text>` | Send a chat message as the fake player (URL-encode the text) |
| `GET /aichat?name=<name>[&mode=public\|private][&target=<player>][&continuous=true][&interval=<ticks>]` | Let the fake player talk via AI; requires `ai-chat.yml`; `continuous=true` keeps going until `/aichat/stop` |
| `GET /aichat/stop?name=<name>` | Stop the fake player's continuous AI chat |
| `GET /action?name=<name>&action=<action>` | Trigger an action, see parameters below |
| `GET /stop?name=<name>` | Stop every running action |
| `GET /teleport?name=<name>[&world=&x=&y=&z=&yaw=&pitch=]` | Teleport; omitted parameters keep their current value |
| `GET /look?name=<name>&direction=<direction>` | Face `north`/`south`/`east`/`west`/`up`/`down` |
| `GET /look?name=<name>&at=<x,y,z>` | Look at coordinates |
| `GET /look?name=<name>&yaw=<yaw>&pitch=<pitch>` | Set rotation directly |
| `GET /hold?name=<name>&slot=<1-9>` | Switch hotbar slot |
| `GET /swap?name=<name>` | Swap main and off hand |
| `GET /goto?name=<name>&x=&y=&z=[&world=]` | Walk to coordinates (unreachable targets are rejected) |
| `GET /setping?name=<name>&ping=<ms>` | Set the tab list ping |
| `GET /sneak?name=<name>[&state=true\|false]` | Toggle (or set) sneaking |
| `GET /sprint?name=<name>[&state=true\|false]` | Toggle (or set) sprinting |
| `GET /reload` | Reload `config.yml` and translations |
| `GET /cmd?name=<name>&command=<command>` | Run a command as the fake player |
| `GET /kickall` / `/killall` / `/sayall?message=` | Bulk remove / kill / say |

`/action` parameters:

| Parameter | Default | Description |
|---|---|---|
| `action` | required | Case insensitive, `-` and `_` are equivalent: `ATTACK` `MINE` `USE` `JUMP` `LOOK_AT_NEAREST_ENTITY` `DROP_ITEM` `DROP_STACK` `DROP_INVENTORY` `SAY` |
| `count` | `1` | Executions; `-1` keeps running |
| `interval` | `0` | Ticks between executions; `interval` alone keeps the action running |
| `wait` | `0` | Ticks to wait before the first execution |
| `message` | — | Required by `SAY` only |

Success returns `{"status":"success"}` (bulk endpoints add `count`); failures return `{"status":"failure","msg":"..."}` describing the problem. HTTP status: `401` bad token, `403` endpoint disabled or Host not allowed, `404` unknown path, `405` wrong method, `414` URI too long, `429` rate limited or locked out.

```bash
TOKEN="YOUR_TOKEN"; API="http://localhost:3253"

curl -H "Authorization: Bearer $TOKEN" "$API/list"
curl -H "Authorization: Bearer $TOKEN" "$API/spawn?name=klmgun"
curl -H "Authorization: Bearer $TOKEN" "$API/action?name=klmgun&action=attack&interval=10"
curl -G -H "Authorization: Bearer $TOKEN" "$API/cmd" --data-urlencode "name=klmgun" --data-urlencode "command=say hello"
curl -G -H "Authorization: Bearer $TOKEN" "$API/say" --data-urlencode "name=klmgun" --data-urlencode "message=hello world"
```

## Security

Built in, no configuration needed: **authentication before the endpoint switch**, so `401`/`403` cannot be used to probe which endpoints are enabled; token redaction and control-character replacement in logs; per-IP rate limiting with lockout after repeated failures; optional `allowed-hosts` Host / `Origin` checks; `Cache-Control: no-store` on responses.

Still up to the administrator:

- The API has **no TLS** — use a long random token and expose it only on a trusted network or behind a reverse proxy.
- `/cmd` runs commands as the fake player, with that fake player's permissions; **never OP a fake player**.
- Prefer `host: 127.0.0.1` and restrict access at the firewall.

## Personal Configuration

Each player can save their own defaults; changes apply on the **next spawn**. Use `/fp config list` to view and `/fp config set <key> <value>` to change.

| Config Item | Description |
|--------|------|
| `collidable` | Enable collision box |
| `invulnerable` | Enable invincible mode |
| `wolverine` | Enable super heal (rapid regeneration) |
| `look_at_entity` | Auto-look at nearby attackable entities; combine with attack for auto-combat |
| `pickup_items` | Enable item pickup |
| `skin` | Use the creator's skin by default |
| `replenish` | Refill consumed or used items |
| `replace_tools` | GCA-style tool replacement |
| `autofish` | Enable auto-fishing |
| `simulation_distance` | Simulation distance (chunk radius) of the fake player; `-1` keeps the world setting |

- **`replace_tools`**: GCA-style — broken non-Mending tools, and Mending tools worn below `tool-replacement.remaining-durability-threshold` (default `10`), are swapped for an identical item from the inventory; enchantments are ignored.
- **`replenish`**: `/fp drop` and `/fp dropstack` refill the main hand; food and drink containers come back when they fit. `/fp dropinv` never refills.
- **`simulation_distance`**: chunk radius ticking around the fake player; set the default under `default-features`, override with `/fp config set` or `/fp set`.

## Permissions

<details>
<summary>Click to expand</summary>

Each command has an individual permission node; convenience groups are provided as well:

### Permission Group `fakeplayer.spawn`

Basic management (all `fakeplayer.command.*`): `spawn` `kick` `list` `distance` `select` `selection` `drop` `dropstack` `dropinv` `skin` `invsee` `enderchest` `status` `respawn` `config` `set` `setping`

### Permission Group `fakeplayer.tp`

`fakeplayer.command.tp`, `fakeplayer.command.tphere`, `fakeplayer.command.tps`

### Permission Group `fakeplayer.action`

Actions: `attack` `mine` `use` `jump` `sneak` `sprint` `look` `turn` `move` `ride` `swap` `sleep` `wakeup` `stop` `hold` `say` (all `fakeplayer.command.*`)

Features: `fakeplayer.config.replenish`, `fakeplayer.config.replenish.chest` (refill from nearby chests), `fakeplayer.config.replace-tools`, `fakeplayer.config.autofish`

> Servers without strict permission management can assign `fakeplayer.basic`, which includes every safe permission except `/fp cmd`.

</details>

## Placeholder Variables

| Placeholder | Scope | Description |
|---|---|---|
| `%fakeplayer_total%` | Global | Total fake players on the server |
| `%fakeplayer_list%` | Global | Names of every online fake player, joined by `fakeplayer.placeholder.separator` (default `, `) |
| `%fakeplayer_list_<index>%` | Global | Name at `index` (0-based); empty when out of range |
| `%fakeplayer_list_<index>_<attribute>%` | Global | Attribute of that fake player, e.g. `%fakeplayer_list_0_uuid%` |
| `%fakeplayer_isfake%` | Any player | `true` / `false` — whether the player is a fake player |
| `%fakeplayer_name%` | Fake player | Name |
| `%fakeplayer_uuid%` | Fake player | UUID |
| `%fakeplayer_creator%` | Fake player | Creator name (`%fakeplayer_spawner%` is an alias) |
| `%fakeplayer_spawntime%` | Fake player | Spawn time, formatted with `fakeplayer.placeholder.time-format` |
| `%fakeplayer_world%` | Fake player | World name |
| `%fakeplayer_x%` / `%fakeplayer_y%` / `%fakeplayer_z%` | Fake player | Block coordinates |
| `%fakeplayer_health%` | Fake player | Current health |
| `%fakeplayer_food%` | Fake player | Current food level |
| `%fakeplayer_level%` | Fake player | Current experience level |
| `%fakeplayer_gamemode%` | Fake player | Current game mode |
| `%fakeplayer_actions%` | Fake player | Active actions, e.g. `USE\|ATTACK` |
| `%fakeplayer_actions_translated%` | Fake player | Same, localised |

## Custom Translation

Built-in locales: `en`, `zh`, `zh_tw`, `zh_hk`, `ja`. Switch with `i18n.locale` in `config.yml` (then `/fp reload`).

Custom: drop `message_<language>_<region>.properties` (copy the [template](fakeplayer-core/src/main/resources/message/message.properties)) into `plugins/fakeplayer/message/`, set `i18n.locale` to the matching suffix (e.g. `en_us`), and run `/fp reload-translation`. Files must be **UTF-8** encoded.

## Login plugin compatibility & chat events

With a login plugin installed, fake players would be kicked or frozen for not logging in. FakePlayer CE marks them as logged in on join, and `/fp say` triggers the chat events so formatter plugins work normally.

| Login plugin | Compatibility | Notes |
|---|---|---|
| AuthMe / AuthMeReloaded | `forceLogin` / `forceRegister` | Marked logged in via the public API; unregistered names are auto-registered with a random short password |
| CatSeedLogin / ReCatSeedLogin | Login list | Written into its `loginPlayers` list, so `auto-kick` leaves them alone |
| LibreLogin (Paper) / LibreLoginNext (Paper) | API authorization | Temporary passwordless record while online, removed on disconnect |

- **Automatic**: no configuration needed — each plugin is only used when it is installed.

### Auto-login (two independent mechanisms)

| Mechanism | Configuration | Password needed |
|---|---|---|
| **Login-plugin API exemption** (recommended) | None — applies when any plugin above is installed | No |
| **Command-template login** | `auto-login.command.enabled: true` | Yes (set with `/fp password`, or generated) |

The first never shows a login screen; the second runs commands as the fake player and works with plugins that have no API compatibility here:

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
    change-password: "/changepassword %oldpassword% %newpassword% %newpassword%"
  random-password:
    charset: "abcdefghijklmnopqrstuvwxyz0123456789"   # empty disables generation
    length: 12
```

Both can be active at once; when the API exemption already works, the command templates are usually unnecessary.

### Chat event compatibility (`/fp say`)

`/fp say` fully triggers `AsyncPlayerChatEvent` and `AsyncChatEvent`, so EssentialsX Chat, Vane and similar plugins can rewrite format and content; the message is broadcast unless the event is cancelled. Works the same on every supported version (`1.20.1 ~ 26.3`).

## FAQ

### Player disconnected: "PacketEvents 2.0 failed to inject"

Some plugins modify the fake player's network connection; set this in your config:

```yaml
prevent-kicking: ALWAYS
```

### Fake players are not attacked by mobs

Fake players spawn with invincible mode enabled. Run `/fp config set invulnerable false` to let them take damage; consider regeneration beacons or potions to keep them alive.

### Fake players get kicked after a while

Usually a login plugin kicking players that never logged in. The three plugins above are handled automatically; if yours is not covered, fall back to command-template login:

```yaml
auto-login:
  command:
    enabled: true
    register: "/reg %password% %password%"
    login: "/login %password%"
```

> Use a strong password so AuthMe's security policy does not reject it.

## Build

See [BUILD.md](./BUILD.md). It applies to this project's Gradle multi-module workflow only, not to the upstream Maven build.

## Star History

[![Star History Chart](https://api.star-history.com/svg?repos=EndlessPixel-Studio/FakePlayer-CE&type=Date)](https://star-history.com/#EndlessPixel-Studio/FakePlayer-CE&Date)
