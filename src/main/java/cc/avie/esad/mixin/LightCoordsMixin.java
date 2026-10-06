package cc.avie.esad.mixin;

import cc.avie.esad.feature.dynamiclights.DynamicLights;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Version specific: the light lookup moved to LightCoordsUtil in 26.2
@Mixin(LevelRenderer.class)
public class LightCoordsMixin {
	/** Every block light lookup of the renderer (chunk meshes, smooth lighting, block entities, particles) ends here. */
	@ModifyReturnValue(
		method = "getLightCoords(Lnet/minecraft/client/renderer/LevelRenderer$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
		at = @At("RETURN")
	)
	private static int esad$addDynamicLight(int packedLight, @Local(argsOnly = true) BlockAndLightGetter level, @Local(argsOnly = true) BlockPos pos) {
		// Inside opaque blocks the light is always 0, lighting them would brighten the shading of their neighbors
		if (!DynamicLights.hasSources() || level.getBlockState(pos).isSolidRender()) {
			return packedLight;
		}
		return DynamicLights.applyToBlock(packedLight, pos);
	}
}
