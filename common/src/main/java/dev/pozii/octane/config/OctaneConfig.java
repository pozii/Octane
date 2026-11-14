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

    public static final class Client {
        public boolean particleGovernor = true;
        public int particleCullDistance = 128;
        public int particleCapPerTick = 8192;
        public int particleAliveThreshold = 16000;
        public boolean soundGovernor = true;
        public int soundCullDistance = 64;
        public int nameTagCullDistance = 64;
        public boolean entityCull = true;
        public int entityCullDistance = 128;
        public boolean weatherParticleThinning = false;
    }

    private OctaneConfig() {
    }

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
