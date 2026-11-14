package dev.pozii.octane.fabric.command;

import com.mojang.brigadier.CommandDispatcher;
import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.profile.OctaneProfiler;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

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
                        source.sendFeedback(() -> savedMessage(out), false);
                    } catch (Exception e) {
                        OctaneFabricMod.LOGGER.warn("[Octane] could not write report", e);
                        source.sendFeedback(() -> Text.translatable("octane.command.report.failed")
                                .formatted(Formatting.RED), false);
                    }
                    return 1;
                })));
    }

    private static Text savedMessage(Path file) {
        MutableText link = Text.literal(file.getFileName().toString())
                .styled(style -> style
                        .withColor(Formatting.AQUA)
                        .withUnderline(true)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.toAbsolutePath().toString()))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                Text.translatable("octane.command.report.hover").formatted(Formatting.GRAY))));
        MutableText prefix = Text.literal("[Octane] ").formatted(Formatting.GOLD, Formatting.BOLD);
        MutableText body = Text.translatable("octane.command.report.done", link).formatted(Formatting.GREEN);
        return prefix.append(body);
    }
}
