// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
package dev.pozii.octane.util;

import java.nio.file.Path;

/**
 * Loader-specific answers, implemented once per loader module.
 * Implementations must stay tiny: no game logic belongs here.
 */
public interface PlatformHelper {
    /** The loader's config directory (the folder that holds {@code octane.json}). */
    Path configDir();

    /** The loader's game directory (report files go here). */
    Path gameDir();

    /** True on a physical client, false on a dedicated server. */
    boolean isClient();

    /** Human-readable loader name + version, e.g. {@code "fabric-loader 0.16.14"}. */
    String loaderName();
}
