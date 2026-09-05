package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Records the first title screen as the closest marker to an interactive
 * game, feeding {@code timeToTitleMs} in {@code /octane report}. No behavior
 * change — a single timestamp on first init.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMarkerMixin {
    @Inject(method = "init()V", at = @At("HEAD"))
    private void octane$markTitle(CallbackInfo ci) {
        OctaneProfiler.markTitleScreen();
    }
}
