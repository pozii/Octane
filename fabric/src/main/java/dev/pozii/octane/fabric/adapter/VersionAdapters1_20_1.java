package dev.pozii.octane.fabric.adapter;

import dev.pozii.octane.util.VersionAdapter;

/**
 * Adapter for the 1.20.1 branch (see {@code versions/1.20.1}).
 * Moves into a version source set during the Stonecutter migration.
 */
public final class VersionAdapters1_20_1 implements VersionAdapter {
    @Override
    public String minecraftVersion() {
        return "1.20.1";
    }

    @Override
    public String octaneBranch() {
        return "1.20.1";
    }
}
