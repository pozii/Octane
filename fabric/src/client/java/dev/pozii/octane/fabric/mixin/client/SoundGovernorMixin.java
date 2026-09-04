package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Trims positional sound spam beyond {@code client.soundCullDistance}.
 * Skipped sounds are counted for {@code /octane report}.
 *
 * <p>Safety: protected categories (master, music, records, weather, voice)
 * and relative (UI-anchored) sounds always pass; nearby gameplay cues are
 * untouched since only distant instances are culled. Delayed sounds queued
 * via {@code playNextTick} flow back through {@code play} and are covered.
 */
@Mixin(SoundManager.class)
public abstract class SoundGovernorMixin {
    @Inject(
        method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void octane$govern(SoundInstance sound, int delay, CallbackInfo ci) {
        OctaneProfiler.recordSoundSeen();
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().client.soundGovernor) {
            return;
        }
        SoundCategory category = sound.getCategory();
        if (category == SoundCategory.MASTER || category == SoundCategory.MUSIC
                || category == SoundCategory.RECORDS || category == SoundCategory.WEATHER
                || category == SoundCategory.VOICE) {
            return;
        }
        if (sound.isRelative()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client == null ? null : client.player;
        if (player == null) {
            return;
        }
        double cull = OctaneFabricMod.config().client.soundCullDistance;
        double dx = sound.getX() - player.getX();
        double dy = sound.getY() - player.getY();
        double dz = sound.getZ() - player.getZ();
        if (dx * dx + dy * dy + dz * dz > cull * cull) {
            OctaneProfiler.recordSoundCull();
            ci.cancel();
        }
    }
}
