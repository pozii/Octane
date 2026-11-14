package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import dev.pozii.octane.fabric.sodium.OctaneSodiumPage;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = SodiumOptionsGUI.class, remap = false)
public abstract class SodiumOptionsMixin {
    @Shadow
    @Final
    private List<OptionPage> pages;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void octane$addPage(Screen previous, CallbackInfo ci) {
        if (OctaneFabricMod.config() != null) {
            this.pages.add(OctaneSodiumPage.create());
        }
    }
}
