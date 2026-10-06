package cc.avie.esad.mixin;

import cc.avie.esad.feature.pingdisplay.PingDisplay;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class NametagPingMixin {
	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void esad$appendPingToNametag(Entity entity, EntityRenderState state, float partialTick, CallbackInfo ci) {
		Component withPing = PingDisplay.nametagWithPing(entity, state.nameTag);
		if (withPing != null) {
			state.nameTag = withPing;
		}
	}
}
