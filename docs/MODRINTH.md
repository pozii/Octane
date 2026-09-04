![Octane](https://raw.githubusercontent.com/pozii/Octane/main/assets/octane-banner.png)

# High-octane performance for your modpack.

Octane is a lightweight optimization core for Minecraft. Drop it into your
mods folder — no setup, no config, no gameplay changes. It works *with*
Sodium and Lithium, not against them.

> **Beta:** 0.1.0-beta.1 ships the measurement foundation. Real boot and
> memory optimizations land step by step, each with published benchmarks.

## Why Octane

- **Gets you playing faster** — Octane cuts wasted work while the game
  loads, so you reach the title screen sooner.
- **Keeps it smooth** — leaner background work and less memory churn mean
  fewer stutters, even in heavy modpacks.
- **Loves modpacks** — made to sit next to Sodium, Lithium and FerriteCore.
  No conflicts, nothing to configure, nothing to learn.
- **Proves it** — run `/octane report` and see your own loading and
  smoothness numbers in `octane-report.json`.

## Install

Playing in under five minutes:

1. **Install Fabric Loader (0.16 or newer) for Minecraft 1.20.1.**
   Download it from [fabricmc.net](https://fabricmc.net/use/) and run the
   installer — it creates a ready-to-play Fabric profile in your launcher.
2. **Add Fabric API.** Get it from
   [Modrinth](https://modrinth.com/mod/fabric-api) (pick the 1.20.1 version)
   and keep it next to Octane. Almost every modpack already includes it.
3. **Add Octane.** Download `octane-*.jar` from the
   [versions page](https://modrinth.com/mod/octane/versions) for stable
   releases, or from [GitHub Releases](https://github.com/pozii/Octane/releases)
   if you want betas too — then put it in your `mods` folder:
   - Windows: `%appdata%\.minecraft\mods`
   - macOS: `~/Library/Application Support/minecraft/mods`
   - Linux: `~/.minecraft/mods`
4. **Launch the Fabric profile and play.** Nothing to configure.
5. **Check it's working (optional):** open any world, run `/octane report`,
   and look for `octane-report.json` next to your `mods` folder — that's
   Octane measuring your game.

**Servers:** same two jars into the server's `mods` folder, restart. Done —
players don't need to install anything extra.

## Links

- **Source code & docs:** [github.com/pozii/Octane](https://github.com/pozii/Octane)
- **Found a bug?** [Open an issue](https://github.com/pozii/Octane/issues) —
  attach `latest.log` and `octane-report.json` if you can.
- **Benchmarks & method:** [docs/BENCHMARKS.md](https://github.com/pozii/Octane/blob/main/docs/BENCHMARKS.md)
- **License:** [Apache-2.0](https://github.com/pozii/Octane/blob/main/LICENSE) —
  free to use in modpacks, no permission needed.
