// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Records the client-boot marker used by time-to-title measurements. */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientBootMixin {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void octane$onClientInit(RunArgs args, CallbackInfo ci) {
        OctaneProfiler.markClientInit();
    }
}
