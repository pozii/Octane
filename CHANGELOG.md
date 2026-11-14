# Changelog

Octane releases are cut from tags (`v<mod_version>-mc<mc_version>`) by CI —
see `.github/workflows/release.yml`. Release notes are auto-generated from
conventional commits in the "Subject (#PR) (@author)" style, so keep commit
subjects clean. The compatibility and benchmark matrix lives in
`docs/BENCHMARKS.md`.

## Unreleased

- CI + autonomous releases (GitHub + Modrinth beta channel).
- Mod Menu icon wiring (pending `icon.png`).
- Sodium video settings now carry an Octane page with 14 live toggles and sliders. Boot options flag a restart.
- New options: entity draw distance, name tag distance, and thinner rain splashes.
- `/octane report` prints in color, and clicking the file name opens it.
