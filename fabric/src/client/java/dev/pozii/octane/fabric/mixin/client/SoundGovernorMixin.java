package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Trims positional sound spam beyond {@code client.soundCullDistance}.
 * Both {@code play} overloads are covered — the single-arg variant is the
 * one nearly every sound (including packet-driven ones) flows through.
 * Skipped sounds are counted for {@code /octane report}.
 *
 * <p>Safety: protected categories (master, music, records, weather, voice)
 * and relative (UI-anchored) sounds always pass; nearby gameplay cues are
 * untouched since only distant instances are culled. The
 * {@code playNextTick} queue (music/menu loops, protected categories
 * anyway) stays out of scope.
 */
@Mixin(SoundManager.class)
public abstract class SoundGovernorMixin {
    @Inject(
        method = "play(Lnet/minecraft/client/sound/SoundInstance;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void octane$governNow(SoundInstance sound, CallbackInfo ci) {
        OctaneProfiler.recordSoundSeen();
        if (octane$shouldCull(sound)) {
            OctaneProfiler.recordSoundCull();
            ci.cancel();
        }
    }

    @Inject(
        method = "play(Lnet/minecraft/client/sound/SoundInstance;I)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void octane$governDelayed(SoundInstance sound, int delay, CallbackInfo ci) {
        OctaneProfiler.recordSoundSeen();
        if (octane$shouldCull(sound)) {
            OctaneProfiler.recordSoundCull();
            ci.cancel();
        }
    }

    @Unique
    private static boolean octane$shouldCull(SoundInstance sound) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().client.soundGovernor) {
            return false;
        }
        SoundCategory category = sound.getCategory();
        if (category == SoundCategory.MASTER || category == SoundCategory.MUSIC
                || category == SoundCategory.RECORDS || category == SoundCategory.WEATHER
                || category == SoundCategory.VOICE) {
            return false;
        }
        if (sound.isRelative()) {
            return false;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client == null ? null : client.player;
        if (player == null) {
            return false;
        }
        double cull = OctaneFabricMod.config().client.soundCullDistance;
        double dx = sound.getX() - player.getX();
        double dy = sound.getY() - player.getY();
        double dz = sound.getZ() - player.getZ();
        return dx * dx + dy * dy + dz * dz > cull * cull;
    }
}
