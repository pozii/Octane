package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import net.minecraft.client.sound.SoundSystem;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Demotes the per-event {@code Missing sound for event} warnings emitted on
 * every resource reload to debug level. Modded registries routinely contain
 * hundreds of events with no sound file, turning each reload into a wall of
 * log spam plus wasted string formatting. The {@code UNKNOWN_SOUNDS}
 * bookkeeping is untouched — only the log level changes. Disable via
 * {@code boot.silenceMissingSounds}.
 */
@Mixin(SoundSystem.class)
public abstract class MissingSoundSilencerMixin {
    @Redirect(
        method = "reloadSounds()V",
        at = @At(
            value = "INVOKE",
            target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V",
            ordinal = 0
        )
    )
    private void octane$silenceMissingSound(Logger logger, String message, Object arg) {
        if (OctaneFabricMod.config() != null && OctaneFabricMod.config().boot.silenceMissingSounds) {
            logger.debug(message, arg);
        } else {
            logger.warn(message, arg);
        }
    }
}
