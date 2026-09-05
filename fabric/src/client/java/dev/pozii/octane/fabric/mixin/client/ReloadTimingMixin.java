package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

/**
 * Times every client resource reload (boot, F3+T, pack changes) by wrapping
 * the returned future. The first completion is the initial game load; later
 * ones are manual reloads. Feeds the {@code boot} section of
 * {@code /octane report}. No behavior change — a timestamp on completion.
 */
@Mixin(MinecraftClient.class)
public abstract class ReloadTimingMixin {
    @Inject(
        method = "reloadResources()Ljava/util/concurrent/CompletableFuture;",
        at = @At("TAIL"),
        cancellable = true
    )
    private void octane$timeReload(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        long start = System.nanoTime();
        CompletableFuture<Void> original = cir.getReturnValue();
        if (original == null) {
            return;
        }
        cir.setReturnValue(original.thenRun(() -> OctaneProfiler.recordReloadComplete(
                (System.nanoTime() - start) / 1_000_000.0)));
    }
}
