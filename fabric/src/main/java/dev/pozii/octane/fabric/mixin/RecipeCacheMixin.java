package dev.pozii.octane.fabric.mixin;

import com.google.gson.JsonElement;
import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * No-op reload fast path for recipes. When the incoming data-pack map is
 * structurally identical to the last applied one, the previously built
 * (immutable) recipe maps are restored instead of re-deserializing every
 * recipe — no registry lookups, no object churn, no repeated error logging.
 *
 * <p>Correctness: any added, removed or changed recipe flips
 * {@code Map.equals} and vanilla re-parses everything, refreshing the
 * snapshot. {@code setRecipes} (the client sync path) is deliberately
 * <em>not</em> wired to invalidate: it carries byte-identical data, and the
 * server re-syncs recipes to connected players after every {@code /reload} —
 * invalidating there would wipe the fresh snapshot and turn every reload
 * into a miss on any server with players online (proven by testing).
 * Timings feed {@code /octane report} via {@link OctaneProfiler}.
 *
 * <p>Note: the snapshot is static because vanilla constructs a new
 * {@code RecipeManager} inside {@code DataPackContents} on every reload —
 * an instance field would never survive to the second reload. All access is
 * guarded by {@code octane$LOCK} since server apply and client sync run on
 * different threads.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeCacheMixin {
    @Shadow
    private Map recipes;
    @Shadow
    private Map recipesById;
    @Shadow
    private boolean errored;

    @Unique
    private static final Object octane$LOCK = new Object();
    @Unique
    private static Map<Identifier, JsonElement> octane$lastInput;
    @Unique
    private static Map octane$lastRecipes;
    @Unique
    private static Map octane$lastRecipesById;
    @Unique
    private static boolean octane$lastErrored;

    @Unique
    private static final ThreadLocal<Long> octane$applyStartNanos = new ThreadLocal<>();

    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void octane$cachedApply(Map<Identifier, JsonElement> map, ResourceManager manager,
            Profiler profiler, CallbackInfo ci) {
        octane$applyStartNanos.set(System.nanoTime());
        boolean enabled = OctaneFabricMod.config() != null && OctaneFabricMod.config().boot.cacheRecipes;
        OctaneFabricMod.LOGGER.info(
                "[Octane] recipe apply start (manager={}, entries={}, cacheRecipes={}, cached={})",
                System.identityHashCode(this), map.size(), enabled, octane$lastInput != null);
        if (!enabled) {
            return;
        }
        synchronized (octane$LOCK) {
            if (octane$lastInput != null && octane$lastInput.equals(map)) {
                recipes = octane$lastRecipes;
                recipesById = octane$lastRecipesById;
                errored = octane$lastErrored;
                double ms = (System.nanoTime() - octane$applyStartNanos.get()) / 1_000_000.0;
                OctaneProfiler.recordReloadSample("recipe-apply", ms, true);
                OctaneFabricMod.LOGGER.info("[Octane] recipe cache hit, skipped full re-parse ({} ms)", ms);
                ci.cancel();
            } else if (octane$lastInput != null) {
                octane$logMissDiff(octane$lastInput, map);
            }
        }
    }

    /**
     * One-line diagnostic for cache misses: sizes, added/removed ids (first
     * 5 each) and count of same-key entries whose JSON changed. Reloads are
     * rare manual actions, so one INFO line per miss is acceptable.
     */
    @Unique
    private static void octane$logMissDiff(Map<Identifier, JsonElement> oldMap,
            Map<Identifier, JsonElement> fresh) {
        try {
            StringBuilder added = new StringBuilder();
            StringBuilder removed = new StringBuilder();
            StringBuilder changed = new StringBuilder();
            int addedCount = 0;
            int removedCount = 0;
            int changedCount = 0;
            for (Map.Entry<Identifier, JsonElement> entry : oldMap.entrySet()) {
                JsonElement now = fresh.get(entry.getKey());
                if (now == null) {
                    if (removedCount < 5) {
                        if (removedCount > 0) {
                            removed.append(',');
                        }
                        removed.append(entry.getKey());
                    }
                    removedCount++;
                } else if (!now.equals(entry.getValue())) {
                    if (changedCount < 5) {
                        if (changedCount > 0) {
                            changed.append(',');
                        }
                        changed.append(entry.getKey());
                    }
                    changedCount++;
                }
            }
            for (Identifier id : fresh.keySet()) {
                if (!oldMap.containsKey(id)) {
                    if (addedCount < 5) {
                        if (addedCount > 0) {
                            added.append(',');
                        }
                        added.append(id);
                    }
                    addedCount++;
                }
            }
            OctaneFabricMod.LOGGER.info(
                    "[Octane] recipe cache miss ({} -> {} entries, +{} [{}], -{} [{}], ~{} [{}])",
                    oldMap.size(), fresh.size(), addedCount, added,
                    removedCount, removed, changedCount, changed);
        } catch (Throwable t) {
            OctaneFabricMod.LOGGER.warn("[Octane] recipe diff failed", t);
        }
    }

    @Inject(
        method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
        at = @At("TAIL")
    )
    private void octane$snapshotApply(Map<Identifier, JsonElement> map, ResourceManager manager,
            Profiler profiler, CallbackInfo ci) {
        Long start = octane$applyStartNanos.get();
        octane$applyStartNanos.remove();
        if (start != null) {
            OctaneProfiler.recordReloadSample("recipe-apply",
                    (System.nanoTime() - start) / 1_000_000.0, false);
        }
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().boot.cacheRecipes) {
            synchronized (octane$LOCK) {
                octane$lastInput = null;
            }
            return;
        }
        synchronized (octane$LOCK) {
            octane$lastInput = map;
            octane$lastRecipes = recipes;
            octane$lastRecipesById = recipesById;
            octane$lastErrored = errored;
        }
    }

}
