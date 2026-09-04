// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
package dev.pozii.octane.fabric;

import dev.pozii.octane.Octane;
import dev.pozii.octane.api.OctaneAPI;
import dev.pozii.octane.config.OctaneConfig;
import dev.pozii.octane.fabric.adapter.VersionAdapters1_20_1;
import dev.pozii.octane.fabric.command.OctaneCommands;
import dev.pozii.octane.fabric.platform.FabricPlatformHelper;
import dev.pozii.octane.profile.OctaneProfiler;
import dev.pozii.octane.util.PlatformHelper;
import dev.pozii.octane.util.VersionAdapter;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

/** Server + client common entrypoint. No behavior changes, only measurement. */
public final class OctaneFabricMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Octane.MOD_NAME);

    private static final FabricPlatformHelper PLATFORM = new FabricPlatformHelper();
    private static final VersionAdapter ADAPTER = new VersionAdapters1_20_1();
    private static OctaneConfig config;

    @Override
    public void onInitialize() {
        OctaneAPI.fire(OctaneAPI.Phase.PRE_INIT);
        OctaneProfiler.markGameStart();

        Path file = PLATFORM.configDir().resolve(Octane.MOD_ID + ".json");
        config = OctaneConfig.load(file);
        config.save(file);

        OctaneCommands.register();

        LOGGER.info("[Octane] initialized (mc={}, loader={})",
                ADAPTER.minecraftVersion(), PLATFORM.loaderName());
        OctaneAPI.fire(OctaneAPI.Phase.POST_INIT);
    }

    public static OctaneConfig config() {
        return config;
    }

    public static PlatformHelper platform() {
        return PLATFORM;
    }

    public static VersionAdapter adapter() {
        return ADAPTER;
    }
}
