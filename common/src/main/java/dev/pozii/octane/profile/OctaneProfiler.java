// Copyright 2026 pozii. SPDX-License-Identifier: Apache-2.0
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

    private static final long[] tickNanos = new long[TICK_WINDOW];
    private static int tickCount;

    private static final ThreadLocal<Long> tickStart = new ThreadLocal<>();

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

    public static double millisSinceGameStart() {
        if (gameStartNanos < 0) {
            return -1;
        }
        return (System.nanoTime() - gameStartNanos) / 1_000_000.0;
    }

    /** Builds the report payload for {@code /octane report}. Pure data, no I/O. */
    public static String buildReport(String minecraftVersion, String loaderName) {
        long[] snapshot;
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
