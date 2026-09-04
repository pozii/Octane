// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
package dev.pozii.octane.fabric.mixin;

import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

/** Feeds server tick durations into the profiler. Measurement only. */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerTickMixin {
    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("HEAD"))
    private void octane$onTickStart(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        OctaneProfiler.onServerTickStart();
    }

    @Inject(method = "tick(Ljava/util/function/BooleanSupplier;)V", at = @At("TAIL"))
    private void octane$onTickEnd(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        OctaneProfiler.onServerTickEnd();
    }
}
