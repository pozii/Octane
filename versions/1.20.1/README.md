# versions/1.20.1 — active branch

Adapter implementation for Minecraft 1.20.1 lives in code:
`fabric/src/main/java/dev/pozii/octane/fabric/adapter/VersionAdapters1_20_1.java`.

When the Stonecutter migration (see `stonecutter.gradle`) happens, the adapter
moves into a version source set here so `1.21.x` can ship its own next to it.
