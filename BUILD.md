# Build Guide

This project uses **paperweight-userdev** Gradle plugin to handle NMS dependencies automatically.
No manual BuildTools setup is required.

## Prerequisites

- **JDK 25** (for compiling Paper 1.21+ NMS)
- An internet connection (first build downloads Paper server jars)

## Quick Build

```bash
./gradlew build --no-daemon
```

The output jar will be at:

```
fakeplayer-dist/build/libs/fakeplayer-dist-fp.build2.jar
```

## How It Works

Each version module (e.g. `fakeplayer-v1_21_6`) declares its Paper dev bundle via `paperweight.paperDevBundle()`.
The plugin automatically:

1. Downloads the appropriate Paper server jar
2. Remaps and deobfuscates NMS classes
3. Exposes them as compile-only dependencies

This means you get full NMS source access without running BuildTools or managing local maven repos.

