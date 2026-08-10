# FakePlayer CE (Community Edition)

![BANNER_IMAGE](.github/README/BANNER.png)

English | [简体中文](README_zh.md)

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

## Requirements

- [Paper](https://papermc.io) or [Purpur](http://purpurmc.org) server software
- [CommandAPI](https://commandapi.jorel.dev) plugin (any version **except** `10.0.0`)

## Config File

On first launch, FakePlayer generates a template file `config.tmpl.yml`. Rename it to `config.yml` to activate your configuration. This template approach lets you preview new options when upgrading without overwriting your existing settings.

[View sample config](fakeplayer-core/src/main/resources/config.yml)

## Commands

| Command       | Description                               | Permission                   | Note                                                            |
|---------------|-------------------------------------------|------------------------------|-----------------------------------------------------------------|
| /fp spawn     | Spawn a fake player                       | fakeplayer.command.spawn     |                                                                 |
| /fp kill      | Kill a fake player                        | fakeplayer.command.kill      |                                                                 |
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
| /fp reload    | Reload config file                        | OP                           |                                                                 |

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
| `replenish`       | Enable auto-replenish from inventory |
| `autofish`        | Enable auto-fishing |

## Permissions

<details>
<summary>Click to expand</summary>

Each command has an individual permission node. Convenience permission groups are also provided:

### Permission Group `fakeplayer.spawn`

Includes basic spawn management permissions:
- `fakeplayer.command.spawn` — Create fake players
- `fakeplayer.command.kill` — Kill fake players
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
- `fakeplayer.config.replenish` — Auto-replenish
- `fakeplayer.config.replenish.chest` — Replenish from nearby chests
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
2. **Cross-version support**: NMS code isolated into version-specific modules covering `1.20.1 ~ 1.21.11`
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