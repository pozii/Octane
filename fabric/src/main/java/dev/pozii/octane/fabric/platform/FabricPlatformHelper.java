// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
package dev.pozii.octane.fabric.platform;

import dev.pozii.octane.util.PlatformHelper;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/** Fabric implementation of {@link PlatformHelper}. */
public final class FabricPlatformHelper implements PlatformHelper {
    @Override
    public Path configDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public Path gameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public boolean isClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }

    @Override
    public String loaderName() {
        return FabricLoader.getInstance()
                .getModContainer("fabricloader")
                .map(container -> "fabric-loader " + container.getMetadata().getVersion().getFriendlyString())
                .orElse("fabric-loader unknown");
    }
}
