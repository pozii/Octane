<div align="center">
  <img src="https://i.ibb.co/WNFh76qx/Octane-Texted.png" alt="Octane" width="420">
  <h3>High-octane performance for your Client.</h3>
  <p>
    <a href="https://modrinth.com/mod/octane"><img src="https://img.shields.io/badge/get%20it-Modrinth-1bd96a.svg" alt="Modrinth"></a>
  </p>
</div>

Octane is an open-source Minecraft client optimizer under **Apache-2.0**. I built
it to run fine on its own, and it also plays nicely next to Sodium and Lithium
instead of fighting them. There's no config to touch and gameplay stays exactly
the same. Drop it in your mods folder and forget it's there.

> Reload caches for recipes, splashes, models and blockstates, client
> particle and sound governors, and a boot profiler behind `/octane report`.
> Every claim is measured. See `docs/BENCHMARKS.md`.

## Why Octane

- **Standalone:** all it asks for is Fabric API. The client gets faster even if
  no other optimization mod is installed.
- **Stacks well:** Sodium, Lithium and FerriteCore can sit beside it, and I've
  hit no conflicts so far.
- **Zero config:** everything starts ON. `config/octane.json` is there for
  players and pack authors who'd like some toggles.
- **Provable:** run `/octane report` and you get an `octane-report.json` with
  boot markers and recent server tick stats (p50/p95/max plus heap).
- **Modpack API:** companion mods can watch the boot through `OctaneAPI`
  (`dev.pozii.octane.api`), no mixins required.

## Install

1. Get [Fabric Loader](https://fabricmc.net) `>= 0.16` for Minecraft 1.20.1,
   then add [Fabric API](https://modrinth.com/mod/fabric-api).
2. Put `octane-*.jar` into `mods/`. That's the whole setup.

## Commands

`/octane report` (permission level 2) writes `octane-report.json` into the
game directory, then tells you so with a translatable chat message.

## Config

`config/octane.json` gets created on first launch with safe defaults:

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

You need JDK 17.

```sh
./gradlew :fabric:build
```

The finished jar ends up in `fabric/build/libs/`.

## AI policy

Humans build Octane, and we're against AI in this project. Code, art and text
written by AI don't get accepted, and we'd rather not see them in issues or pull
requests either. Contributing? Write it yourself.
