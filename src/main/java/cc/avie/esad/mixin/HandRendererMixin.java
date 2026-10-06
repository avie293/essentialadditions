package cc.avie.esad.mixin;

import cc.avie.esad.feature.zoom.Zoom;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Version specific: the hand renderer changed in 26.2 and 26.3
@Mixin(ItemInHandRenderer.class)
public class HandRendererMixin {
	@Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
	private void esad$hideHandWhileZooming(CallbackInfo ci) {
		if (Zoom.shouldHideHand()) {
			ci.cancel();
		}
	}
}
