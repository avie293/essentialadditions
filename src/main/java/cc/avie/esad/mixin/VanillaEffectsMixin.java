package cc.avie.esad.mixin;

import cc.avie.esad.feature.hud.EffectsHud;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Version specific: the effect icons are drawn by Gui before 26.3
@Mixin(Hud.class)
public class VanillaEffectsMixin {
	@Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
	private void esad$hideVanillaEffects(CallbackInfo ci) {
		if (EffectsHud.hidesVanillaIcons()) {
			ci.cancel();
		}
	}
}
