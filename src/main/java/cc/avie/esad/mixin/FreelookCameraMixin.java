package cc.avie.esad.mixin;

import cc.avie.esad.feature.freelook.Freelook;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Camera.class)
public class FreelookCameraMixin {
	@ModifyExpressionValue(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewYRot(F)F"),
		require = 1, allow = 2)
	private float esad$freelookYaw(float yaw) {
		return Freelook.isActive() ? Freelook.yaw() : yaw;
	}

	@ModifyExpressionValue(method = "alignWithEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getViewXRot(F)F"),
		require = 1, allow = 2)
	private float esad$freelookPitch(float pitch) {
		return Freelook.isActive() ? Freelook.pitch() : pitch;
	}
}
