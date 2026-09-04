package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.Queue;

/**
 * Trims particle spam that the player cannot meaningfully see: beyond
 * {@code client.particleCullDistance}, or past {@code client.particleCapPerTick}
 * additions in a single tick. Skipped particles are counted for
 * {@code /octane report}.
 *
 * <p>Safety: returning {@code null} is exactly what vanilla does for its own
 * culled particles, so every caller already null-handles. No particle type
 * is ever banned — only distance and rate are limited.
 */
@Mixin(ParticleManager.class)
public abstract class ParticleGovernorMixin {
    @Shadow
    private Map<ParticleTextureSheet, Queue<Particle>> particles;

    @Unique
    private int octane$addsThisTick;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void octane$resetBudget(CallbackInfo ci) {
        octane$addsThisTick = 0;
        long alive = 0;
        for (Queue<Particle> queue : particles.values()) {
            alive += queue.size();
        }
        OctaneProfiler.recordParticlesAlive(alive);
    }

    @Inject(
        method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void octane$govern(ParticleEffect parameters, double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            CallbackInfoReturnable<Particle> cir) {
        OctaneProfiler.recordParticleSeen();
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().client.particleGovernor) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client == null ? null : client.player;
        if (player == null) {
            return;
        }
        double cull = OctaneFabricMod.config().client.particleCullDistance;
        double dx = x - player.getX();
        double dy = y - player.getY();
        double dz = z - player.getZ();
        if (dx * dx + dy * dy + dz * dz > cull * cull) {
            OctaneProfiler.recordParticleCull();
            cir.setReturnValue(null);
            return;
        }
        if (octane$addsThisTick >= OctaneFabricMod.config().client.particleCapPerTick) {
            OctaneProfiler.recordParticleCull();
            cir.setReturnValue(null);
            return;
        }
        octane$addsThisTick++;
    }
}
