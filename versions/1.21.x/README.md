# versions/1.21.x — skeleton branch (v2.0)

Reserved for the 1.21.x port. Nothing is wired to Gradle yet on purpose:
the 1.20.1 LTS branch must prove the profiler + config loop first.

Port checklist:
1. Duplicate the 1.20.1 adapter behind a `//? if` Stonecutter guard.
2. Re-validate every mixin target (method descriptors move between versions).
3. Re-run the full `docs/BENCHMARKS.md` matrix.
