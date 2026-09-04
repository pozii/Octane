package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.SplashTextResourceSupplier;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

/**
 * Caches the parsed splash list across resource reloads while the selected
 * pack set is unchanged, skipping the file open + parse in {@code prepare}.
 *
 * <p>Output is identical: on any pack change the key mismatches and vanilla
 * re-reads. Note the cache is keyed on pack identity, not file contents —
 * editing a pack's splashes.txt on disk needs a pack toggle or restart to
 * show, same cosmetic tradeoff the flag documents. Disable via
 * {@code boot.cacheSplashes}.
 */
@Mixin(SplashTextResourceSupplier.class)
public abstract class SplashTextCacheMixin {
    @Unique
    private static List<String> octane$cachedSplashes;
    @Unique
    private static String octane$cachedKey;

    @Unique
    private static String octane$packKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }
        List<String> names = new ArrayList<>(client.getResourcePackManager().getEnabledNames());
        names.sort(null);
        return String.join("|", names);
    }

    @Inject(
        method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)Ljava/util/List;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void octane$reuseCachedSplashes(ResourceManager manager, Profiler profiler,
            CallbackInfoReturnable<List<String>> cir) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.cacheSplashes) {
            return;
        }
        long start = System.nanoTime();
        String key = octane$packKey();
        if (key != null && key.equals(octane$cachedKey) && octane$cachedSplashes != null) {
            // Vanilla apply() copies out of the returned list (clear + addAll),
            // so handing back the cached instance is allocation-free and safe.
            OctaneProfiler.recordReloadSample("splash-prepare",
                    (System.nanoTime() - start) / 1_000_000.0, true);
            cir.setReturnValue(octane$cachedSplashes);
        }
    }

    @Inject(
        method = "apply(Ljava/util/List;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At("TAIL")
    )
    private void octane$storeSplashes(List<String> prepared, ResourceManager manager, Profiler profiler,
            CallbackInfo ci) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.cacheSplashes) {
            return;
        }
        String key = octane$packKey();
        if (key != null) {
            octane$cachedSplashes = List.copyOf(prepared);
            octane$cachedKey = key;
            OctaneProfiler.recordReloadSample("splash-prepare", -1, false);
        }
    }
}
