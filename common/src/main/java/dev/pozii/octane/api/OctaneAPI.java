package dev.pozii.octane.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Public integration surface for other mods and modpacks.
 *
 * <p>Octane itself stays dependency-free and behavior-neutral; this API lets
 * companions observe the boot lifecycle (for example to align their own
 * caches with Octane's) without touching mixins.
 *
 * <pre>{@code
 * OctaneAPI.register(phase -> {
 *     if (phase == OctaneAPI.Phase.POST_INIT) {
 *         // Octane finished initializing.
 *     }
 * });
 * }</pre>
 */
public final class OctaneAPI {
    /** Boot lifecycle phases Octane notifies listeners about. */
    public enum Phase {
        /** Fired before Octane loads config and registers commands. */
        PRE_INIT,
        /** Fired after Octane finished initializing. */
        POST_INIT
    }

    /** Listener notified on every {@link Phase}. Must be fast and non-blocking. */
    public interface BootListener {
        void onBoot(Phase phase);
    }

    private static final List<BootListener> LISTENERS = new CopyOnWriteArrayList<>();

    private OctaneAPI() {
    }

    /**
     * Registers a boot listener. Safe to call at any time; listeners
     * registered late simply miss earlier phases.
     */
    public static void register(BootListener listener) {
        if (listener != null) {
            LISTENERS.add(listener);
        }
    }

    /**
     * Fires a phase to all listeners. Called by Octane itself; other mods
     * should only {@link #register(BootListener) register}, never fire.
     */
    public static void fire(Phase phase) {
        for (BootListener listener : LISTENERS) {
            try {
                listener.onBoot(phase);
            } catch (Throwable ignored) {
                // A companion must never break the boot it observes.
            }
        }
    }
}
