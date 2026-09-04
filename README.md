![Octane — High-octane performance for your modpack.](assets/octane-banner.png)

[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/minecraft-1.20.1-green.svg)](https://minecraft.net)
[![Loader](https://img.shields.io/badge/loader-Fabric%20%C2%B7%200.16-yellow.svg)](https://fabricmc.net)
[![Status](https://img.shields.io/badge/status-beta-orange.svg)](docs/BENCHMARKS.md)

Open-source Minecraft optimization core, licensed under
**Apache-2.0**. Octane works *with* Sodium and Lithium, not against them:
zero config, no gameplay changes — drop it into a modpack and forget it.

> **Beta notice:** `0.1.0-beta.1` ships the measurement foundation (config,
> profiler, `/octane report`, first mixins). Real Boot/RAM optimizations land
> incrementally per `docs/BENCHMARKS.md` so every claim stays provable.

## Why Octane

- **Mandatory-material:** dependency-light, client + server safe, compatible
  with the Sodium/Lithium/FerriteCore stack.
- **Zero config:** everything defaults to ON; `config/octane.json` exists for
  pack authors who want toggles.
- **Provable:** `/octane report` writes `octane-report.json` with boot markers
  and recent server tick stats (p50/p95/max + heap).
- **Modpack API:** companions can observe boot via `OctaneAPI`
  (`dev.pozii.octane.api`) without touching mixins.

## Install (players & pack authors)

1. Install [Fabric Loader](https://fabricmc.net) `>= 0.16` for Minecraft 1.20.1
   plus [Fabric API](https://modrinth.com/mod/fabric-api).
2. Drop `octane-*.jar` into `mods/`. No setup needed.

## Commands

`/octane report` (permission level 2) — writes `octane-report.json` into the
game directory and confirms with a translatable chat message.

## Config

`config/octane.json` (auto-created with safe defaults):

```jsonc
{
  "boot": {
    "cacheRecipes": true,
    "skipRedundantBake": true,
    "lazyLanguage": true,
    "silenceMissingSounds": true
  },
  "ram": {
    "dedupBlockStates": true,
    "reduceAllocations": true
  }
}
```

## Building

Requirements: JDK 17.

```sh
./gradlew :fabric:build
```

The jar lands in `fabric/build/libs/`.

## Layout

```text
common/          pure Java: config, profiler, OctaneAPI, platform/version interfaces
fabric/          Fabric 1.20.1: entrypoints, command, mixins, en_us lang
neoforge-stub/   reserved for the v2.0 NeoForge port
forge-stub/      reserved for a possible Forge port
versions/        per-Minecraft-version adapters (1.20.1 active, 1.21.x skeleton)
docs/            benchmark protocol and run tables
```

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) first — especially the
architecture rules (`:common` stays pure Java, no hardcoded strings, strict
mixins, benchmarks required for perf claims).

## Contributors

Octane exists thanks to everyone who contributes code, testing, docs, and ideas.
New contributors are also credited automatically in every GitHub Release notes.

| | Name | Focus |
|---|---|---|
| <img src="https://github.com/pozii.png?size=100" width="100" alt="pozii"/> | **[pozii](https://github.com/pozii)** | Founder, architecture, core |

*Your name here — see [CONTRIBUTING.md](CONTRIBUTING.md) to get started.*

## License

[Apache-2.0](LICENSE).
