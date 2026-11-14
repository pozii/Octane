package dev.pozii.octane.fabric.mixin.client;

import dev.pozii.octane.fabric.OctaneFabricMod;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityCullMixin {
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private <E extends Entity> void octane$cullFarEntities(E entity, Frustum frustum, double x, double y,
            double z, CallbackInfoReturnable<Boolean> cir) {
        if (OctaneFabricMod.config() == null || !OctaneFabricMod.config().client.entityCull) {
            return;
        }
        double limit = OctaneFabricMod.config().client.entityCullDistance;
        double dx = entity.getX() - x;
        double dy = entity.getY() - y;
        double dz = entity.getZ() - z;
        if (dx * dx + dy * dy + dz * dz > limit * limit) {
            cir.setReturnValue(false);
        }
    }
}
