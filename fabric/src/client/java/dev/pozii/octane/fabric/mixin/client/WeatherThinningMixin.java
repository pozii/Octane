package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WeatherThinningMixin {
    @Unique
    private int octane$weatherTick;

    @Inject(method = "tickRainSplashing", at = @At("HEAD"), cancellable = true)
    private void octane$thinRainSplashes(Camera camera, CallbackInfo ci) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().client.weatherParticleThinning) {
            return;
        }
        octane$weatherTick++;
        if ((octane$weatherTick & 1) == 1) {
            ci.cancel();
        }
    }
}
