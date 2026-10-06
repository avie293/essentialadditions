package cc.avie.esad.mixin;

import cc.avie.esad.feature.fullbright.Fullbright;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LightmapRenderStateExtractor.class)
public class LightmapMixin {
	@Inject(method = "extract", at = @At("TAIL"))
	private void esad$fullbright(LightmapRenderState state, float partialTick, CallbackInfo ci) {
		float strength = Fullbright.strength();
		if (strength > state.nightVisionEffectIntensity) {
			state.nightVisionEffectIntensity = strength;
		}
	}
}
