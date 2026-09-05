package dev.pozii.octane.profile;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

/**
 * Allocation-light boot + tick profiler. Hot paths write into a fixed ring
 * buffer and never allocate; JSON is only built on explicit report requests.
 */
public final class OctaneProfiler {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int TICK_WINDOW = 100;

    private static volatile long gameStartNanos = -1;
    private static volatile long clientInitNanos = -1;
    private static volatile long titleNanos = -1;

    private static final long[] tickNanos = new long[TICK_WINDOW];
    private static int tickCount;

    private static final ThreadLocal<Long> tickStart = new ThreadLocal<>();

    private static final int RELOAD_PHASES = 8;
    private static final String[] reloadPhase = new String[RELOAD_PHASES];
    private static final double[] reloadLastMs = new double[RELOAD_PHASES];
    private static final long[] reloadHits = new long[RELOAD_PHASES];
    private static final long[] reloadRuns = new long[RELOAD_PHASES];
    private static int reloadPhaseCount;

    private static long particlesCulled;
    private static long soundsCulled;
    private static long particlesSeen;
    private static long soundsSeen;
    private static long particlesAliveMax;

    private static long reloadCount;
    private static double firstReloadMs = -1;
    private static double lastReloadMs = -1;

    private OctaneProfiler() {
    }

    public static void markGameStart() {
        if (gameStartNanos < 0) {
            gameStartNanos = System.nanoTime();
        }
    }

    public static void markClientInit() {
        if (clientInitNanos < 0) {
            clientInitNanos = System.nanoTime();
        }
    }

    /** First title screen: closest marker to interactive, used for boot timing. */
    public static void markTitleScreen() {
        if (titleNanos < 0) {
            titleNanos = System.nanoTime();
        }
    }

    public static void onServerTickStart() {
        tickStart.set(System.nanoTime());
    }

    public static void onServerTickEnd() {
        Long start = tickStart.get();
        if (start == null) {
            return;
        }
        tickStart.remove();
        long elapsed = System.nanoTime() - start;
        synchronized (tickNanos) {
            tickNanos[tickCount % TICK_WINDOW] = elapsed;
            tickCount++;
        }
    }

    public static double millisSinceGameStart() {        if (gameStartNanos < 0) {
            return -1;
        }
        return (System.nanoTime() - gameStartNanos) / 1_000_000.0;
    }

    /**
     * Records how long a reload phase (e.g. {@code "recipe-apply"}) took and
     * whether Octane served it from cache. Fixed-size, allocation-free.
     */
    public static void recordReloadSample(String phase, double millis, boolean cacheHit) {
        synchronized (reloadPhase) {
            int slot = -1;
            for (int i = 0; i < reloadPhaseCount; i++) {
                if (phase.equals(reloadPhase[i])) {
                    slot = i;
                    break;
                }
            }
            if (slot < 0) {
                if (reloadPhaseCount >= RELOAD_PHASES) {
                    return;
                }
                slot = reloadPhaseCount++;
                reloadPhase[slot] = phase;
            }
            reloadLastMs[slot] = millis;
            reloadRuns[slot]++;
            if (cacheHit) {
                reloadHits[slot]++;
            }
        }
    }

    /**
     * Records a particle or sound skipped by the client governors.
     * Counter only; call sites stay allocation-free.
     */
    public static void recordParticleCull() {
        particlesCulled++;
    }

    public static void recordSoundCull() {
        soundsCulled++;
    }

    /** Total governor sightings (culled or not) plus peak live particles. */
    public static void recordParticleSeen() {
        particlesSeen++;
    }

    public static void recordSoundSeen() {
        soundsSeen++;
    }

    public static void recordParticlesAlive(long alive) {
        if (alive > particlesAliveMax) {
            particlesAliveMax = alive;
        }
    }

    /** Records one completed client resource reload (boot or F3+T). */
    public static synchronized void recordReloadComplete(double millis) {
        reloadCount++;
        lastReloadMs = millis;
        if (firstReloadMs < 0) {
            firstReloadMs = millis;
        }
    }

    /** Builds the report payload for {@code /octane report}. Pure data, no I/O. */
    public static String buildReport(String minecraftVersion, String loaderName,
            dev.pozii.octane.config.OctaneConfig config) {        long[] snapshot;
        int count;
        synchronized (tickNanos) {
            count = Math.min(tickCount, TICK_WINDOW);
            snapshot = Arrays.copyOf(tickNanos, count);
        }
        Arrays.sort(snapshot);

        JsonObject root = new JsonObject();
        root.addProperty("mod", "octane");
        root.addProperty("minecraft", minecraftVersion);
        root.addProperty("loader", loaderName);
        root.addProperty("millisSinceGameStart", millisSinceGameStart());
        root.addProperty("clientInitReached", clientInitNanos >= 0);
        if (titleNanos >= 0 && gameStartNanos >= 0) {
            root.addProperty("timeToTitleMs", (titleNanos - gameStartNanos) / 1_000_000.0);
        }
        root.addProperty("serverTicksObserved", count);

        if (count > 0) {
            JsonObject ticks = new JsonObject();
            ticks.addProperty("p50ms", snapshot[count / 2] / 1_000_000.0);
            ticks.addProperty("p95ms", snapshot[(int) (count * 0.95)] / 1_000_000.0);
            ticks.addProperty("maxMs", snapshot[count - 1] / 1_000_000.0);
            root.add("serverTick", ticks);

            JsonArray last = new JsonArray();
            for (long nanos : snapshot) {
                last.add(nanos / 1_000_000.0);
            }
            root.add("recentTicksMs", last);
        }

        JsonObject reload = new JsonObject();
        synchronized (reloadPhase) {
            for (int i = 0; i < reloadPhaseCount; i++) {
                JsonObject sample = new JsonObject();
                sample.addProperty("lastMs", reloadLastMs[i]);
                sample.addProperty("runs", reloadRuns[i]);
                sample.addProperty("cacheHits", reloadHits[i]);
                reload.add(reloadPhase[i], sample);
            }
        }
        root.add("reload", reload);

        JsonObject effective = new JsonObject();
        if (config != null) {
            JsonObject boot = new JsonObject();
            boot.addProperty("cacheRecipes", config.boot.cacheRecipes);
            boot.addProperty("cacheSplashes", config.boot.cacheSplashes);
            effective.add("boot", boot);
        } else {
            effective.addProperty("note", "config was null when the report ran");
        }
        root.add("config", effective);

        JsonObject governors = new JsonObject();        governors.addProperty("particlesCulled", particlesCulled);
        governors.addProperty("soundsCulled", soundsCulled);
        governors.addProperty("particlesSeen", particlesSeen);
        governors.addProperty("soundsSeen", soundsSeen);
        governors.addProperty("particlesAliveMax", particlesAliveMax);
        root.add("governors", governors);

        JsonObject boot = new JsonObject();
        boot.addProperty("reloadCount", reloadCount);
        boot.addProperty("firstReloadMs", firstReloadMs);
        boot.addProperty("lastReloadMs", lastReloadMs);
        root.add("boot", boot);

        Runtime runtime = Runtime.getRuntime();
        JsonObject memory = new JsonObject();
        memory.addProperty("maxMB", runtime.maxMemory() / 1_048_576L);
        memory.addProperty("totalMB", runtime.totalMemory() / 1_048_576L);
        memory.addProperty("freeMB", runtime.freeMemory() / 1_048_576L);
        root.add("heap", memory);

        root.addProperty("java", System.getProperty("java.version", "unknown"));
        root.addProperty("os", System.getProperty("os.name", "unknown"));
        return GSON.toJson(root);
    }
}
