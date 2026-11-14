package dev.pozii.octane.fabric.sodium;

import com.google.common.collect.ImmutableList;
import dev.pozii.octane.config.OctaneConfig;
import dev.pozii.octane.fabric.OctaneFabricMod;
import me.jellysquid.mods.sodium.client.gui.options.OptionFlag;
import me.jellysquid.mods.sodium.client.gui.options.OptionGroup;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpact;
import me.jellysquid.mods.sodium.client.gui.options.OptionImpl;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlValueFormatter;
import me.jellysquid.mods.sodium.client.gui.options.control.SliderControl;
import me.jellysquid.mods.sodium.client.gui.options.control.TickBoxControl;
import me.jellysquid.mods.sodium.client.gui.options.storage.OptionStorage;
import net.minecraft.text.Text;

import java.util.function.BiConsumer;
import java.util.function.Function;

public final class OctaneSodiumPage {
    private static final OptionStorage<OctaneConfig> STORAGE = new OptionStorage<>() {
        @Override
        public OctaneConfig getData() {
            return OctaneFabricMod.config();
        }

        @Override
        public void save() {
            OctaneFabricMod.config().save(OctaneFabricMod.configFile());
        }
    };

    private OctaneSodiumPage() {
    }

    public static OptionPage create() {
        ImmutableList.Builder<OptionGroup> groups = ImmutableList.builder();

        groups.add(OptionGroup.createBuilder()
                .add(toggle("cacheRecipes", OptionImpact.MEDIUM, true,
                        c -> c.boot.cacheRecipes, (c, v) -> c.boot.cacheRecipes = v))
                .add(toggle("cacheSplashes", OptionImpact.LOW, true,
                        c -> c.boot.cacheSplashes, (c, v) -> c.boot.cacheSplashes = v))
                .add(toggle("skipRedundantBake", OptionImpact.HIGH, true,
                        c -> c.boot.skipRedundantBake, (c, v) -> c.boot.skipRedundantBake = v))
                .add(toggle("silenceMissingSounds", OptionImpact.LOW, true,
                        c -> c.boot.silenceMissingSounds, (c, v) -> c.boot.silenceMissingSounds = v))
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(toggle("particleGovernor", OptionImpact.HIGH, false,
                        c -> c.client.particleGovernor, (c, v) -> c.client.particleGovernor = v))
                .add(slider("particleCullDistance", OptionImpact.MEDIUM, 16, 128, 8,
                        c -> c.client.particleCullDistance, (c, v) -> c.client.particleCullDistance = v))
                .add(slider("particleCapPerTick", OptionImpact.MEDIUM, 1024, 16384, 512,
                        c -> c.client.particleCapPerTick, (c, v) -> c.client.particleCapPerTick = v))
                .add(slider("particleAliveThreshold", OptionImpact.MEDIUM, 4000, 16000, 500,
                        c -> c.client.particleAliveThreshold, (c, v) -> c.client.particleAliveThreshold = v))
                .add(toggle("weatherParticleThinning", OptionImpact.LOW, false,
                        c -> c.client.weatherParticleThinning, (c, v) -> c.client.weatherParticleThinning = v))
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(toggle("soundGovernor", OptionImpact.MEDIUM, false,
                        c -> c.client.soundGovernor, (c, v) -> c.client.soundGovernor = v))
                .add(slider("soundCullDistance", OptionImpact.LOW, 16, 128, 8,
                        c -> c.client.soundCullDistance, (c, v) -> c.client.soundCullDistance = v))
                .build());

        groups.add(OptionGroup.createBuilder()
                .add(toggle("entityCull", OptionImpact.HIGH, false,
                        c -> c.client.entityCull, (c, v) -> c.client.entityCull = v))
                .add(slider("entityCullDistance", OptionImpact.HIGH, 32, 256, 16,
                        c -> c.client.entityCullDistance, (c, v) -> c.client.entityCullDistance = v))
                .add(slider("nameTagCullDistance", OptionImpact.LOW, 8, 64, 4,
                        c -> c.client.nameTagCullDistance, (c, v) -> c.client.nameTagCullDistance = v))
                .build());

        return new OptionPage(Text.translatable("octane.options.page"), groups.build());
    }

    private static OptionImpl<OctaneConfig, Boolean> toggle(String id, OptionImpact impact, boolean restart,
            Function<OctaneConfig, Boolean> getter, BiConsumer<OctaneConfig, Boolean> setter) {
        OptionImpl.Builder<OctaneConfig, Boolean> builder = OptionImpl.createBuilder(Boolean.TYPE, STORAGE)
                .setName(Text.translatable("octane.options." + id))
                .setTooltip(tooltip(id, restart))
                .setControl(TickBoxControl::new)
                .setBinding(setter, getter)
                .setImpact(impact);
        if (restart) {
            builder.setFlags(OptionFlag.REQUIRES_GAME_RESTART);
        }
        return builder.build();
    }

    private static OptionImpl<OctaneConfig, Integer> slider(String id, OptionImpact impact, int min, int max,
            int step, Function<OctaneConfig, Integer> getter, BiConsumer<OctaneConfig, Integer> setter) {
        return OptionImpl.createBuilder(Integer.TYPE, STORAGE)
                .setName(Text.translatable("octane.options." + id))
                .setTooltip(tooltip(id, false))
                .setControl(option -> new SliderControl(option, min, max, step, ControlValueFormatter.number()))
                .setBinding(setter, getter)
                .setImpact(impact)
                .build();
    }

    private static Text tooltip(String id, boolean restart) {
        Text base = Text.translatable("octane.options." + id + ".tooltip");
        if (!restart) {
            return base;
        }
        return Text.empty().append(base).append(Text.literal("\n")).append(Text.translatable("octane.options.restart"));
    }
}
