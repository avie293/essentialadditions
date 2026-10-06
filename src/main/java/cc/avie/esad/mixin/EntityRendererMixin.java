package cc.avie.esad.mixin;

import cc.avie.esad.feature.dynamiclights.DynamicLights;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
	@ModifyReturnValue(method = "getPackedLightCoords", at = @At("RETURN"))
	private int esad$addDynamicLight(int packedLight, Entity entity, float partialTick) {
		if (!DynamicLights.hasSources()) {
			return packedLight;
		}
		// Uses the exact position instead of the block, so entities brighten smoothly while walking towards a light
		Vec3 pos = entity.getLightProbePosition(partialTick);
		return DynamicLights.withBlockLight(packedLight, DynamicLights.lightAt(pos.x, pos.y, pos.z));
	}
}
