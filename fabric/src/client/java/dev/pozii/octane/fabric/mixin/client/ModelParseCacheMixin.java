package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.fabric.client.ReloadPackKeys;
import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Caches the parsed unbaked models ({@code reloadModels} does file IO plus
 * Gson parsing of every model JSON, often 10k+ files in big packs) keyed on
 * the selected pack set. The expensive bake still runs every reload against
 * the fresh atlas — only the identical re-parse is skipped.
 *
 * <p>Correctness: any pack change flips the key and vanilla re-parses.
 * Parsed models are data holders consumed read-only downstream, so sharing
 * the cached map is safe. The async completion records its own timing for
 * {@code /octane report}. Disable via {@code boot.skipRedundantBake}.
 */
@Mixin(BakedModelManager.class)
public abstract class ModelParseCacheMixin {
    @Unique
    private static final Object octane$LOCK = new Object();
    @Unique
    private static Map<Identifier, JsonUnbakedModel> octane$cachedModels;
    @Unique
    private static String octane$cachedKey;
    @Unique
    private static Map<Identifier, ?> octane$cachedBlockStates;
    @Unique
    private static String octane$cachedBlockStateKey;

    @Inject(
        method = "reloadModels(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void octane$reuseParsedModels(ResourceManager manager, Executor executor,
            CallbackInfoReturnable<CompletableFuture<Map<Identifier, JsonUnbakedModel>>> cir) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.skipRedundantBake) {
            return;
        }
        long start = System.nanoTime();
        String key = ReloadPackKeys.packKey();
        synchronized (octane$LOCK) {
            if (key != null && key.equals(octane$cachedKey) && octane$cachedModels != null) {
                OctaneProfiler.recordReloadSample("model-parse",
                        (System.nanoTime() - start) / 1_000_000.0, true);
                cir.setReturnValue(CompletableFuture.completedFuture(octane$cachedModels));
            }
        }
    }

    @Inject(
        method = "reloadModels(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("TAIL"),
        cancellable = true
    )
    private static void octane$storeParsedModels(ResourceManager manager, Executor executor,
            CallbackInfoReturnable<CompletableFuture<Map<Identifier, JsonUnbakedModel>>> cir) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.skipRedundantBake) {
            return;
        }
        String key = ReloadPackKeys.packKey();
        if (key == null) {
            return;
        }
        long start = System.nanoTime();
        CompletableFuture<Map<Identifier, JsonUnbakedModel>> original = cir.getReturnValue();
        cir.setReturnValue(original.thenApply(models -> {
            synchronized (octane$LOCK) {
                octane$cachedModels = models;
                octane$cachedKey = key;
            }
            OctaneProfiler.recordReloadSample("model-parse",
                    (System.nanoTime() - start) / 1_000_000.0, false);
            return models;
        }));
    }

    @Inject(
        method = "reloadBlockStates(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("HEAD"),
        cancellable = true
    )
    private static void octane$reuseBlockStates(ResourceManager manager, Executor executor,
            CallbackInfoReturnable<CompletableFuture> cir) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.skipRedundantBake) {
            return;
        }
        long start = System.nanoTime();
        String key = ReloadPackKeys.packKey();
        synchronized (octane$LOCK) {
            if (key != null && key.equals(octane$cachedBlockStateKey) && octane$cachedBlockStates != null) {
                OctaneProfiler.recordReloadSample("blockstate-parse",
                        (System.nanoTime() - start) / 1_000_000.0, true);
                cir.setReturnValue(CompletableFuture.completedFuture(octane$cachedBlockStates));
            }
        }
    }

    @Inject(
        method = "reloadBlockStates(Lnet/minecraft/resource/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
        at = @At("TAIL"),
        cancellable = true
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void octane$storeBlockStates(ResourceManager manager, Executor executor,
            CallbackInfoReturnable<CompletableFuture> cir) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.skipRedundantBake) {
            return;
        }
        String key = ReloadPackKeys.packKey();
        if (key == null) {
            return;
        }
        long start = System.nanoTime();
        CompletableFuture original = cir.getReturnValue();
        cir.setReturnValue(original.thenApply(models -> {
            synchronized (octane$LOCK) {
                octane$cachedBlockStates = (Map<Identifier, ?>) models;
                octane$cachedBlockStateKey = key;
            }
            OctaneProfiler.recordReloadSample("blockstate-parse",
                    (System.nanoTime() - start) / 1_000_000.0, false);
            return models;
        }));
    }
}
