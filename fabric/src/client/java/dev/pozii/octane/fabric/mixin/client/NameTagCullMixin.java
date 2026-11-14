package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class NameTagCullMixin<T extends Entity> {
    @Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
    private void octane$cullFarLabels(T entity, Text text, MatrixStack matrices,
            VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (OctaneFabricMod.config() == null) {
            return;
        }
        double limit = OctaneFabricMod.config().client.nameTagCullDistance;
        if (limit >= 64.0) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.gameRenderer == null) {
            return;
        }
        double distance = client.gameRenderer.getCamera().getPos().squaredDistanceTo(entity.getPos());
        if (distance > limit * limit) {
            ci.cancel();
        }
    }
}
