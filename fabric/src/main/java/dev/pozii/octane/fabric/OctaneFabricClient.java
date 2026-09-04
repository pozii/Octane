// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
package dev.pozii.octane.fabric;

import dev.pozii.octane.profile.OctaneProfiler;
import net.fabricmc.api.ClientModInitializer;

/** Client entrypoint: currently only records the client-boot marker. */
public final class OctaneFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OctaneProfiler.markClientInit();
        OctaneFabricMod.LOGGER.info("[Octane] client initialized");
    }
}
