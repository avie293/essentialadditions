package cc.avie.esad.mixin;

import cc.avie.esad.feature.dynamiclights.DynamicLights;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndLightGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// Version specific: before 26.2 the light lookup is in LevelRenderer
@Mixin(LightCoordsUtil.class)
public class LightCoordsMixin {
	/** Every block light lookup of the renderer (chunk meshes, smooth lighting, block entities, particles) ends here. */
	@ModifyReturnValue(
		method = "getLightCoords(Lnet/minecraft/util/LightCoordsUtil$BrightnessGetter;Lnet/minecraft/world/level/BlockAndLightGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
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
