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
