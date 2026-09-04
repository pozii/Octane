package dev.pozii.octane.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON config with safe defaults. Everything defaults to ON so Octane is
 * zero-config: dropping the jar in is the whole setup.
 */
public final class OctaneConfig {
    private static final Logger LOGGER = Logger.getLogger("Octane");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public Boot boot = new Boot();
    public Ram ram = new Ram();
    public Client client = new Client();

    public static final class Boot {
        public boolean cacheRecipes = true;
        public boolean cacheSplashes = true;
        public boolean skipRedundantBake = true;
        public boolean lazyLanguage = true;
        public boolean silenceMissingSounds = true;
    }

    public static final class Ram {
        public boolean dedupBlockStates = true;
        public boolean reduceAllocations = true;
    }

    /**
     * Client-side governors. All visual/audio only — gameplay is never
     * touched. Defaults are conservative: normal play looks and sounds
     * identical, only pathological spam (farms, boss arenas) gets trimmed.
     */
    public static final class Client {
        public boolean particleGovernor = true;
        /** Particles beyond this distance (blocks) are skipped. */
        public int particleCullDistance = 128;
        /** Max particles accepted per tick; overflow is skipped. */
        public int particleCapPerTick = 8192;
        public boolean soundGovernor = true;
        /** Non-protected sounds beyond this distance (blocks) are skipped. */
        public int soundCullDistance = 64;
    }

    private OctaneConfig() {
    }

    /** Loads the file, falling back to defaults (and back-filling them) on any error. */
    public static OctaneConfig load(Path file) {
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                OctaneConfig loaded = GSON.fromJson(reader, OctaneConfig.class);
                if (loaded != null) {
                    if (loaded.boot == null) {
                        loaded.boot = new Boot();
                    }
                    if (loaded.ram == null) {
                        loaded.ram = new Ram();
                    }
                    if (loaded.client == null) {
                        loaded.client = new Client();
                    }
                    return loaded;
                }
            } catch (IOException | JsonSyntaxException e) {
                LOGGER.log(Level.WARNING, "Could not read Octane config, using defaults", e);
            }
        }
        return new OctaneConfig();
    }

    public void save(Path file) {
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not write Octane config", e);
        }
    }
}
