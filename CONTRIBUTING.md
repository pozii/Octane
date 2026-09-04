# Contributing to Octane

Thanks for helping make modpacks faster. This project is maintained by **pozii**
and licensed under **Apache-2.0** — by contributing you agree your work ships
under the same license.

## Quick start

Requirements: **JDK 17** (build toolchain), internet access for first setup.

```sh
./gradlew :fabric:build        # full beta jar -> fabric/build/libs/octane-*.jar
./gradlew :fabric:runClient    # dev client (Minecraft 1.20.1 + Fabric)
./gradlew :fabric:runServer    # dev server, see note below
```

Dev server note: accept the EULA first by creating `fabric/run/eula.txt`
containing `eula=true`, then launch. The run directory is git-ignored.

## Architecture rules (non-negotiable)

1. **`:common` stays pure Java.** No `net.minecraft.*`, no `net.fabricmc.*`
   imports — ever. Check yours before pushing:
   ```sh
   grep -rn "net\.minecraft\|net\.fabricmc" common/src || echo "common is clean"
   ```
   Loader/version specifics go behind `dev.pozii.octane.util.PlatformHelper`
   and `dev.pozii.octane.util.VersionAdapter`, implemented per module.
2. **No hardcoded user-facing strings.** Every player-visible text must be a
   translatable key defined in `fabric/.../assets/octane/lang/en_us.json`
   (English only for now; Crowdin comes later).
3. **Mixins are strict and tiny.** `defaultRequire: 1`, one concern per mixin,
   measurement preferred over behavior change. A mixin whose target you cannot
   prove from yarn `1.20.1+build.10` does not merge.
4. **No behavior changes in v1.** Boot + RAM scope: faster loading, less memory,
   identical gameplay. Anything touching game logic waits for v1.5 review.

## Performance claims need proof

Follow `docs/BENCHMARKS.md`: 3 cold boots, vanilla vs Octane, same machine and
JVM flags, plus the Sodium + Lithium + FerriteCore compatibility matrix. Attach
the run table to your PR or the numbers don't ship.

## Commits & PRs

- [Conventional Commits](https://www.conventionalcommits.org/): e.g.
  `feat: cache recipe reload`, `fix: report path on dedicated server`,
  `docs: benchmark table for beta.1`.
- Small, reviewable PRs: one concern each, build must pass
  (`./gradlew :fabric:build`), new user-facing strings need `en_us.json` keys.
- PR template in your words: what changed, why, benchmark delta, risk.

## Code of conduct

Be kind, be precise, assume good intent. Maintainer decisions on scope are
final — the roadmap protects the "mandatory in every modpack" goal.
