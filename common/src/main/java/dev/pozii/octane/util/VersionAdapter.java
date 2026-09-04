package dev.pozii.octane.util;

/**
 * Version-specific answers, implemented once per Minecraft branch under
 * {@code versions/}. Anything that can move between game versions must be
 * reached through this interface, never called directly.
 */
public interface VersionAdapter {
    /** The Minecraft version this adapter was written against, e.g. {@code "1.20.1"}. */
    String minecraftVersion();

    /** The Octane branch name, e.g. {@code "1.20.1"}. */
    String octaneBranch();
}
