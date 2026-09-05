package dev.pozii.octane.fabric.client;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Shared pack-identity helper for reload caches. All Octane reload caches
 * key on the selected pack set: identical packs mean identical resources,
 * so re-reading and re-parsing is pure waste. Content edits inside the
 * same pack set are NOT detected by the key — caches built on it must
 * either tolerate that (cosmetic data) or compare content (see callers).
 */
public final class ReloadPackKeys {
    private ReloadPackKeys() {
    }

    /** Sorted enabled pack names joined, or null when no client exists yet. */
    public static String packKey() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) {
            return null;
        }
        List<String> names = new ArrayList<>(client.getResourcePackManager().getEnabledNames());
        names.sort(null);
        return String.join("|", names);
    }
}
