# Octane benchmark protocol

How every performance claim on the Modrinth page must be produced.
No number ships without a run log attached here.

## Setup

- Vanilla profile vs Octane profile, same machine, same JVM flags.
- 3 cold boots each; report mean boot time (log timestamps) and peak heap.
- Compatibility matrix before every release:
  Sodium + Lithium + FerriteCore loaded together, client boot + world join,
  plus a dedicated-server boot (client-only mods can never be mandatory).

## Metrics

- `time-to-title`: process start -> title screen interactive (client boot mixin marker).
- `tick-p95`: 95th percentile of the last 100 server ticks from `/octane report`.
- `heap-after-boot`: `Runtime.totalMemory - freeMemory` at title screen.

## Current runs

| Build | Profile | time-to-title | heap-after-boot | Notes |
|-------|---------|---------------|-----------------|-------|
| 0.1.0 | vanilla | — | — | baseline pending |
| 0.1.0 | octane  | — | — | skeleton, profiler only |

## M1/M2 reload-cache runs (dev dedicated server, vanilla datapacks)

Method: boot server, run `/reload` twice, then `/octane report`.
`recipe-apply.lastMs` is the second (cached or re-parsed) apply;
`cacheHits` counts snapshot restores. Measured 2026-09-04.

| Config | recipe-apply lastMs | cacheHits | Notes |
|--------|---------------------|-----------|-------|
| `cacheRecipes: false` (vanilla path) | 6.68 ms | 0 | full re-deserialize |
| `cacheRecipes: true` | 1.96 ms | 2 | snapshot restore, no errors |

Vanilla content is small (hundreds of recipes), so the absolute delta is
~4.7 ms (~3.4x on the no-op reload path). Cost scales linearly with recipe
count — re-measure on a real modpack via `/octane report` after `/reload`.
M1 splash cache skips one file open + ~500-line parse per reload; below
wall-clock noise on vanilla, validated as reload-cache harness instead.

## Real-machine run (modded 1.20.1, 2026-09-04, beta.3)

Machine: i5-12450H + RTX 4060 Laptop, Java 17.0.20.1, loader 0.19.3, pack
with Sodium/Lithium/FerriteCore/ImmediatelyFast/EntityCulling. Single sample
after world open (no `/reload` or F3+T pressed yet, so both caches show the
boot-time miss — that is expected, not a bug).

| Metric | Value | Notes |
|--------|-------|-------|
| `recipe-apply` lastMs / cacheHits | 20.54 ms / 0 | boot parse on modded pack (~3x dev-vanilla: scales as predicted) |
| `splash-prepare` lastMs / cacheHits | miss / 0 | no resource reload pressed yet |
| server tick p50 / p95 / max | 6.55 / 14.34 / 18.97 ms | healthy, no Octane-induced spikes |
| FPS standing still, 12 chunks | 900–1000 | rendering untouched by Octane (baseline) |
| heap total / free | 1312 / 839 MB | — |

Next: press F3+T twice and run `/reload` twice, then `/octane report` —
both `cacheHits` counters should be > 0 with lower `lastMs`.
