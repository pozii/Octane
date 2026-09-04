package dev.pozii.octane.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Registers {@code /octane report}. All user-facing strings are translatable
 * keys (see {@code assets/octane/lang/en_us.json}); no hardcoded English here.
 */
public final class OctaneCommands {
    private OctaneCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(OctaneCommands::registerCommands);
    }

    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher,
            net.minecraft.command.CommandRegistryAccess registryAccess,
            CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("octane")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("report").executes(context -> {
                    ServerCommandSource source = context.getSource();
                    String json = OctaneProfiler.buildReport(
                            OctaneFabricMod.adapter().minecraftVersion(),
                            OctaneFabricMod.platform().loaderName(),
                            OctaneFabricMod.config());

                    Path out = OctaneFabricMod.platform().gameDir().resolve("octane-report.json");
                    try {
                        Files.writeString(out, json, StandardCharsets.UTF_8);
                        source.sendFeedback(() -> Text.translatable("octane.command.report.done"), false);
                    } catch (Exception e) {
                        OctaneFabricMod.LOGGER.warn("[Octane] could not write report", e);
                        source.sendFeedback(() -> Text.translatable("octane.command.report.failed"), false);
                    }
                    return 1;
                })));
    }
}
